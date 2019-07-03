# Spring Cloud 使用 Seata 实现分布式事务 - JPA

> 使用 Seata 作为分布式事务组件，配置中心和注册中心使用 Nacos，使用 MySQL 数据库，使用 JPA 作为数据访问层

## 环境准备

### 创建数据库及表

- 业务表

```sql
DROP DATABASE IF EXISTS seata;
CREATE DATABASE seata;
USE seata;

CREATE TABLE account
(
    id               INT AUTO_INCREMENT PRIMARY KEY,
    balance          INT,
    last_update_time DATETIME DEFAULT now() ON UPDATE now()
);

CREATE TABLE product
(
    id               INT AUTO_INCREMENT PRIMARY KEY,
    price            INT,
    stock            INT,
    last_update_time DATETIME DEFAULT now() ON UPDATE now()
);

CREATE TABLE orders
(
    id               INT AUTO_INCREMENT PRIMARY KEY,
    user_id          INT,
    product_id       INT,
    pay_amount       INT,
    status           VARCHAR(100),
    add_time         DATETIME DEFAULT now(),
    last_update_time DATETIME DEFAULT now() ON UPDATE now()
);

INSERT INTO account (id, balance)
VALUES (1, 100);
INSERT INTO product (id, price, stock)
VALUES (1, 5, 10);
```

- Seata Undo 表

```sql
CREATE TABLE undo_log
(
    id            BIGINT(20)   NOT NULL AUTO_INCREMENT,
    branch_id     BIGINT(20)   NOT NULL,
    xid           VARCHAR(100) NOT NULL,
    rollback_info LONGBLOB     NOT NULL,
    log_status    INT(11)      NOT NULL,
    log_created   DATETIME     NOT NULL,
    log_modified  DATETIME     NOT NULL,
    ext           VARCHAR(100) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY ux_undo_log (xid, branch_id)
) ENGINE = InnoDB
  AUTO_INCREMENT = 1
  DEFAULT CHARSET = utf8;
```

### 启动 Nacos

```bash
docker run --name nacos -p 8848:8848 -e MODE=standalone nacos/nacos-server
```

### 启动 Seata Server 

在 [Seata Release](https://github.com/seata/seata/releases) 下载最新版的 Seata Server，解压后执行以下命令启动，这里使用 file 配置

```bash
sh ./bin/seata-server.sh 8091 file
```

## 应用 

有三个应用，order-service, pay-service, storage-service; order-service 的 placeOrder 接口会分别调用 pay-service 和 storage-service 的接口

### 添加应用

#### 应用配置

以下的配置所有的项目都是类似的

##### 配置依赖 build.gradle 

```gradle
plugins {
    id 'org.springframework.boot' version '2.1.5.RELEASE'
    id 'java'
}

apply plugin: 'io.spring.dependency-management'

group = 'io.github.helloworlde'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '1.8'

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
}

repositories {
    maven { url 'http://maven.aliyun.com/nexus/content/groups/public/' }
    maven { url 'https://repo.spring.io/snapshot/' }
    mavenCentral()
}


ext {
    springCloudVersion = 'Greenwich.SR1'
    springCloudAlibabaVersion = '0.9.1.BUILD-SNAPSHOT'
}

dependencyManagement {
    imports {
        mavenBom "org.springframework.cloud:spring-cloud-dependencies:${springCloudVersion}"
        mavenBom "org.springframework.cloud:spring-cloud-alibaba-dependencies:${springCloudAlibabaVersion}"
    }
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'

    // Seata 0.5.2 版本有一些 bug，使用最新的 0.6.1  
    compile('org.springframework.cloud:spring-cloud-starter-alibaba-seata') {
        exclude group: 'io.seata', module: 'seata-spring'
    }
    compile('io.seata:seata-all:0.6.1')

    compile('org.springframework.cloud:spring-cloud-starter-alibaba-nacos-config')
    compile('org.springframework.cloud:spring-cloud-starter-alibaba-nacos-discovery')

    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    runtime("mysql:mysql-connector-java")

    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

需要注意的是当前Spring Boot 和 Spring Cloud 以及 Spring Cloud Alibaba 的版本号需要互相对应，否则可能会存在各种问题；具体可以参考[版本说明](https://github.com/spring-cloud-incubator/spring-cloud-alibaba/wiki/%E7%89%88%E6%9C%AC%E8%AF%B4%E6%98%8E)

- file.conf 

因为当前使用Seata 文件配置的形式，所以需要这个 `resources/file.conf`

```
transport {
  # tcp udt unix-domain-socket
  type = "TCP"
  #NIO NATIVE
  server = "NIO"
  #enable heartbeat
  heartbeat = true
  #thread factory for netty
  thread-factory {
    boss-thread-prefix = "NettyBoss"
    worker-thread-prefix = "NettyServerNIOWorker"
    server-executor-thread-prefix = "NettyServerBizHandler"
    share-boss-worker = false
    client-selector-thread-prefix = "NettyClientSelector"
    client-selector-thread-size = 1
    client-worker-thread-prefix = "NettyClientWorkerThread"
    # netty boss thread size,will not be used for UDT
    boss-thread-size = 1
    #auto default pin or 8
    worker-thread-size = 8
  }
}
## transaction log store
store {
  ## store mode: file、db
  mode = "file"

  ## file store
  file {
    dir = "sessionStore"

    # branch session size , if exceeded first try compress lockkey, still exceeded throws exceptions
    max-branch-session-size = 16384
    # globe session size , if exceeded throws exceptions
    max-global-session-size = 512
    # file buffer size , if exceeded allocate new buffer
    file-write-buffer-cache-size = 16384
    # when recover batch read size
    session.reload.read_size = 100
    # async, sync
    flush-disk-mode = async
  }

  ## database store
  db {
    driver_class = ""
    url = ""
    user = ""
    password = ""
  }

}
service {
  #vgroup->rgroup
  vgroup_mapping.order-service-fescar-service-group = "default"
  #only support single node
  default.grouplist = "127.0.0.1:8091"
  #degrade current not support
  enableDegrade = false
  #disable
  disable = false
}
client {
  async.commit.buffer.limit = 10000
  lock {
    retry.internal = 10
    retry.times = 30
  }
}
```

需要注意的是 service{}下面的 `vgroup_mapping.order-service-fescar-service-group = "default"`这个配置的 order-service 必须要和 `spring.application.name`相同，不同的项目需要手动修改成对应的，否则启动后会提`config operation timeout,cost:5005 ms,op:GET,dataId:service.vgroup_mapping.order-service-fescar-service-group`错误，可以参考 [Lack service.vgroup_mapping.user-{appid}-fescar-service-group](https://github.com/seata/seata/issues/681)

- registry.conf

```
registry {
  # file 、nacos 、eureka、redis、zk、consul、etcd3、sofa
  type = "file"

  nacos {
    serverAddr = "localhost"
    namespace = "public"
    cluster = "default"
  }
  eureka {
    serviceUrl = "http://localhost:1001/eureka"
    application = "default"
    weight = "1"
  }
  redis {
    serverAddr = "localhost:6379"
    db = "0"
  }
  zk {
    cluster = "default"
    serverAddr = "127.0.0.1:2181"
    session.timeout = 6000
    connect.timeout = 2000
  }
  consul {
    cluster = "default"
    serverAddr = "127.0.0.1:8500"
  }
  etcd3 {
    cluster = "default"
    serverAddr = "http://localhost:2379"
  }
  sofa {
    serverAddr = "127.0.0.1:9603"
    application = "default"
    region = "DEFAULT_ZONE"
    datacenter = "DefaultDataCenter"
    cluster = "default"
    group = "SEATA_GROUP"
    addressWaitTime = "3000"
  }
  file {
    name = "file.conf"
  }
}

config {
  # file、nacos 、apollo、zk
  type = "file"

  nacos {
    serverAddr = "localhost"
    namespace = "public"
    cluster = "default"
  }
  apollo {
    app.id = "seata-server"
    apollo.meta = "http://192.168.1.204:8801"
  }
  zk {
    serverAddr = "127.0.0.1:2181"
    session.timeout = 6000
    connect.timeout = 2000
  }
  file {
    name = "file.conf"
  }
}
```

##### 配置 bootstrap.properties

```properties
spring.application.name=order-service
# Config
spring.cloud.nacos.config.server-addr=127.0.0.1:8848
spring.cloud.nacos.config.namespace=public
spring.cloud.nacos.config.group=DEFAULT_GROUP
spring.cloud.nacos.config.prefix=${spring.application.name}
spring.cloud.nacos.config.file-extension=properties
# Discovery
spring.cloud.nacos.discovery.server-addr=127.0.0.1:8848
spring.cloud.nacos.discovery.namespace=public
```

这里的 Nacos 配置使用的是本地启动的，nacos.config 的 Group, namespace 都是默认的，如果需要可以修改成自己对应的，具体可以参考[Spring Cloud 使用 Nacos 作为服务注册中心](https://github.com/helloworlde/spring-cloud-alibaba-component/blob/master/cloud-discovery/README.md)

##### application.properties

```properties
server.port=8081
management.endpoints.web.exposure.exclude=*
# MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/seata?useUnicode=true&characterEncoding=utf8&allowMultiQueries=true&useSSL=false
spring.datasource.username=root
spring.datasource.password=123456
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

##### DataSourceProxy 配置

这里是尤其需要注意的，Seata 是通过代理数据源实现事务分支，所以需要配置 `io.seata.rm.datasource.DataSourceProxy` 的 Bean，且是 `@Primary`默认的数据源，否则事务不会回滚，无法实现分布式事务 

```java
@Configuration
public class DataSourceProxyConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DruidDataSource druidDataSource() {
        return new DruidDataSource();
    }

    @Primary
    @Bean
    public DataSourceProxy dataSource(DruidDataSource druidDataSource) {
        return new DataSourceProxy(druidDataSource);
    }
}
```


如果使用的是 Hikari 数据源，需要修改数据源的配置，以及注入的 Bean 的配置前缀

```properties
spring.datasource.hikari.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.hikari.jdbc-url=jdbc:mysql://localhost:3306/seata?useUnicode=true&characterEncoding=utf8&allowMultiQueries=true&useSSL=false
spring.datasource.hikari.username=root
spring.datasource.hikari.password=123456
```

```java
    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public DataSource dataSource() {
        return new HikariDataSource();
    }
```

#### Order-Service

- 配置 RestTemplate

```java
@Component
public class RestTemplateConfig {

    @LoadBalanced
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

- OrderController

```java
@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/placeOrder")
    @ResponseBody
    public OperationResponse placeOrder(@RequestBody PlaceOrderRequestVO placeOrderRequestVO) {
        log.info("收到下单请求,用户:{}, 商品:{}", placeOrderRequestVO.getUserId(), placeOrderRequestVO.getProductId());
        return orderService.placeOrder(placeOrderRequestVO);
    }
}
```

- OrderService 

```java
@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private OrderDao orderDao;

    private final String STORAGE_SERVICE_HOST = "http://storage-service/storage";
    private final String PAY_SERVICE_HOST = "http://pay-service/pay";

    @GlobalTransactional
    @Override
    public OperationResponse placeOrder(PlaceOrderRequestVO placeOrderRequestVO) {
        Integer amount = 1;
        Integer price = placeOrderRequestVO.getPrice();

        Order order = Order.builder()
                           .userId(placeOrderRequestVO.getUserId())
                           .productId(placeOrderRequestVO.getProductId())
                           .status(OrderStatus.INIT)
                           .payAmount(price)
                           .build();

        Integer saveOrderRecord = orderDao.saveOrder(order);
        log.info("保存订单{}", saveOrderRecord > 0 ? "成功" : "失败");
        log.info("当前 XID: {}", RootContext.getXID());
        // 扣减库存
        log.info("开始扣减库存");
        ReduceStockRequestVO reduceStockRequestVO = ReduceStockRequestVO.builder()
                                                                        .productId(placeOrderRequestVO.getProductId())
                                                                        .amount(amount)
                                                                        .build();
        String storageReduceUrl = String.format("%s/reduceStock", STORAGE_SERVICE_HOST);
        OperationResponse storageOperationResponse = restTemplate.postForObject(storageReduceUrl, reduceStockRequestVO, OperationResponse.class);
        log.info("扣减库存结果:{}", storageOperationResponse);

        // 扣减余额
        log.info("开始扣减余额");
        ReduceBalanceRequestVO reduceBalanceRequestVO = ReduceBalanceRequestVO.builder()
                                                                              .userId(placeOrderRequestVO.getUserId())
                                                                              .price(price)
                                                                              .build();

        String reduceBalanceUrl = String.format("%s/reduceBalance", PAY_SERVICE_HOST);
        OperationResponse balanceOperationResponse = restTemplate.postForObject(reduceBalanceUrl, reduceBalanceRequestVO, OperationResponse.class);
        log.info("扣减余额结果:{}", balanceOperationResponse);

        Integer updateOrderRecord = orderDao.updateOrder(order.getId(), OrderStatus.SUCCESS);
        log.info("更新订单:{} {}", order.getId(), updateOrderRecord > 0 ? "成功" : "失败");

        return OperationResponse.builder()
                                .success(balanceOperationResponse.isSuccess() && storageOperationResponse.isSuccess())
                                .build();
    }

}
```

需要注意的是在方法上添加了使用分布式事务的注解 ` @GlobalTransactional`

#### Pay-Service

- PayController
```java
@RestController
@RequestMapping("/pay")
@Slf4j
public class PayController {

    @Autowired
    private PayService payService;

    @PostMapping("/reduceBalance")
    @ResponseBody
    public OperationResponse reduceBalance(@RequestBody ReduceBalanceRequestVO reduceBalanceRequestVO) throws Exception {
        return payService.reduceBalance(reduceBalanceRequestVO);
    }
}
```

- PayService 

```java
@Service
@Slf4j
public class PayServiceImpl implements PayService {

    @Autowired
    private AccountDao accountDao;

    @Override
    public OperationResponse reduceBalance(ReduceBalanceRequestVO reduceBalanceRequestVO) throws Exception {

        log.info("当前 XID: {}", RootContext.getXID());

        checkBalance(reduceBalanceRequestVO.getUserId(), reduceBalanceRequestVO.getPrice());

        log.info("开始扣减用户 {} 余额", reduceBalanceRequestVO.getUserId());
        Integer record = accountDao.reduceBalance(reduceBalanceRequestVO.getPrice());
        log.info("扣减用户 {} 余额结果:{}", reduceBalanceRequestVO.getUserId(), record > 0 ? "操作成功" : "扣减余额失败");

        return OperationResponse.builder()
                                .success(record > 0)
                                .message(record > 0 ? "操作成功" : "扣余额失败")
                                .build();

    }

    private void checkBalance(Long userId, Integer price) throws Exception {
        log.info("检查用户 {} 余额", userId);
        Integer balance = accountDao.getBalance(userId);

        if (balance.compareTo(price) < 0) {
            log.warn("用户 {} 余额不足，当前余额:{}", userId, balance);
            throw new Exception("余额不足");
        }

    }
}
```

这里，当余额不足时可以抛出异常

#### Storage-Service

- StorageController

```java
@RestController
@RequestMapping("/storage")
@Slf4j
public class StorageController {

    @Autowired
    private StorageService storageService;

    @PostMapping("/reduceStock")
    @ResponseBody
    public OperationResponse reduceStock(@RequestBody ReduceStockRequestVO reduceStockRequestVO) throws Exception {
        return storageService.reduceStock(reduceStockRequestVO);
    }
}
```

- StorageService

```java
@Service
@Slf4j
public class StorageServiceImpl implements StorageService {

    @Autowired
    private ProductDao productDao;

    @Override
    public OperationResponse reduceStock(ReduceStockRequestVO reduceStockRequestVO) throws Exception {
        log.info("当前 XID: {}", RootContext.getXID());

        // 检查库存
        checkStock(reduceStockRequestVO.getProductId(), reduceStockRequestVO.getAmount());

        log.info("开始扣减 {} 库存", reduceStockRequestVO.getProductId());
        // 扣减库存
        Integer record = productDao.reduceStock(reduceStockRequestVO.getProductId(), reduceStockRequestVO.getAmount());
        log.info("扣减 {} 库存结果:{}", reduceStockRequestVO.getProductId(), record > 0 ? "操作成功" : "扣减库存失败");

        return OperationResponse.builder()
                                .success(record > 0)
                                .message(record > 0 ? "操作成功" : "扣减库存失败")
                                .build();

    }

    public void checkStock(Long productId, Integer requiredAmount) throws Exception {
        log.info("检查 {} 库存", productId);
        Integer stock = productDao.getStock(productId);

        if (stock < requiredAmount) {
            log.warn("{} 库存不足，当前库存:{}", productId, stock);
            throw new Exception("库存不足");
        }
    }
}
```

## 测试

- 启动应用

分别启动 Order-Service，Pay-Service 和 Storage-Service

- 测试成功场景

调用 placeOrder 接口，将 price 设置为 1，此时余额为 10，可以下单成功

```bash
curl -X POST \
  http://localhost:8081/order/placeOrder \
  -H 'Content-Type: application/json' \
  -d '{
    "userId": 1,
    "productId": 1,
    "price": 1
}'
```

此时返回结果为：

```json
{"success":true,"message":null,"data":null}
```

- 测试失败场景

设置 price 为 100，此时余额不足，会下单失败，pay-service会抛出异常，事务会回滚

```bash
curl -X POST \
  http://localhost:8081/order/placeOrder \
  -H 'Content-Type: application/json' \
  -d '{
    "userId": 1,
    "productId": 1,
    "price": 100
}'
```

查看 undo_log 的日志或者主键，可以看到在执行过程中有保存数据

如查看主键自增的值，在执行前后的值会发生变化，在执行前是 1，执行后是 5

```sql
SELECT
    auto_increment
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'seata'
  AND TABLE_NAME = 'undo_log'
```

