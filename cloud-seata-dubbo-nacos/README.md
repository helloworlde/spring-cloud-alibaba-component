@(SpringCloud)[SpringCloud, Seata, Nacos, Dubbo]

# Spring Cloud 使用 Dubbo 实现远程调用，Nacos 作为注册中心，使用 Seata 实现分布式事务

> 使用 Seata 作为分布式事务组件，配置中心和注册中心使用 Nacos，使用 MySQL 数据库和 MyBatis，同时使用 Nacos 作为 Seata 的配置中心，使用 Dubbo 作为 RPC 框架

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

1. 在 [Seata Release](https://github.com/seata/seata/releases) 下载最新版的 Seata Server 并解压
2. 修改 `conf/registry.conf` 配置，将 type 改为 `nacos`

```
registry {
  type = "nacos"

  nacos {
    serverAddr = "localhost"
    namespace = "public"
    cluster = "default"
  }
}

config {
  type = "nacos"

  nacos {
    serverAddr = "localhost"
    namespace = "public"
    cluster = "default"
  }
}
```

3. 修改 `conf/nacos-config.txt`配置

修改 `service.vgroup_mapping`为自己应用对应的名称；如果有多个服务，添加相应的配置

如 
```
service.vgroup_mapping.my_test_tx_group=default

//改为 

service.vgroup_mapping.order-service-fescar-service-group=default
service.vgroup_mapping.pay-service-fescar-service-group=default
service.vgroup_mapping.storage-service-fescar-service-group=default
```

也可以在 Nacos 配置页面添加，data-id 为 `service.vgroup_mapping.${YOUR_SERVICE_NAME}-fescar-service-group`, group 为 `SEATA_GROUP`， 如果不添加该配置，启动后会提示`no available server to connect` 

注意配置文件末尾有空行，需要删除，否则会提示失败，尽管实际上是成功的

4. 将 Seata 配置添加到 Nacos 中 

```bash
cd conf
sh nacos-config.sh localhost
```

成功后会提示

```bash
init nacos config finished, please start seata-server
```

在 Nacos 管理页面应该可以看到有 47 个 Group 为`SEATA_GROUP`的配置

5. 启动 Seata Server 

```bash
cd ..
sh ./bin/seata-server.sh 8091 file
```

启动后在 Nacos 的服务列表下面可以看到一个名为`serverAddr`的服务

## 应用 

有三个应用，order-service, pay-service, storage-service; order-service 的 placeOrder 接口会分别调用 pay-service 和 storage-service 的接口

应用有两种角色，一种是生产者，一种是消费者；在这个 Demo 中，order-service 是消费者，pay-service 和 storage-service 是生产者

### 添加应用

#### 添加公共模块

> Dubbo 服务接口是服务提供方与消费方的远程通讯契约，通常由普通的 Java 接口（interface）来声明
> 为了确保契约的一致性，推荐的做法是将 Dubbo 服务接口打包在第二方或者第三方的 artifact（jar）中， 对于服务提供方而言，不仅通过依赖 artifact 的形式引入 Dubbo 服务接口，而且需要将其实现。对应的服务消费端，同样地需要依赖该 artifact， 并以接口调用的方式执行远程方法。接下来进一步讨论怎样实现 Dubbo 服务提供方和消费方。

因此，添加一个公共模块用于各个应用作为依赖；将相关的接口和 Model 放在这里

- build.gradle 

```java
plugins {
    id 'java'
}


group = 'io.github.helloworlde'
archivesBaseName = 'dubbo-base'
version = '0.0.1-SNAPSHOT'
sourceCompatibility = '1.8'

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
}

repositories {
    maven { url 'https://repo.spring.io/snapshot/' }
    maven { url 'http://maven.aliyun.com/nexus/content/groups/public/' }
    mavenCentral()
}


dependencies {
    compileOnly 'org.projectlombok:lombok:1.18.8'
    annotationProcessor 'org.projectlombok:lombok:1.18.8'
}
```

#### 应用配置

以下的配置所有的项目都是类似的

##### 配置依赖 build.gradle 

```gradle

dependencies {
    compile project(':cloud-dubbo-nacos/dubbo-base')

    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:1.3.2")

    compile('org.springframework.cloud:spring-cloud-starter-dubbo')
    compile('org.springframework.cloud:spring-cloud-starter-alibaba-nacos-config')
    compile('org.springframework.cloud:spring-cloud-starter-alibaba-nacos-discovery')
 
    // Seata 0.5.2 版本有一些 bug，使用 0.6.1  
    compile('org.springframework.cloud:spring-cloud-starter-alibaba-seata') {
        exclude group: 'io.seata', module: 'seata-spring'
    }
    compile('io.seata:seata-all:0.6.1')
    
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    runtime("mysql:mysql-connector-java")

    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

需要注意的是当前Spring Boot 和 Spring Cloud 以及 Spring Cloud Alibaba 的版本号需要互相对应，否则可能会存在各种问题；具体可以参考[版本说明](https://github.com/spring-cloud-incubator/spring-cloud-alibaba/wiki/%E7%89%88%E6%9C%AC%E8%AF%B4%E6%98%8E)


##### registry.conf

```
registry {
  type = "nacos"

  nacos {
    serverAddr = "localhost"
    namespace = "public"
    cluster = "default"
  }
}

config {
  type = "nacos"

  nacos {
    serverAddr = "localhost"
    namespace = "public"
    cluster = "default"
  }
}
```

##### 配置 bootstrap.properties

```
spring.application.name=storage-service
spring.main.allow-bean-definition-overriding=true
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

- StorageService, PayService

```
server.port=8082
management.endpoints.web.exposure.exclude=*
# MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/seata?useUnicode=true&characterEncoding=utf8&allowMultiQueries=true&useSSL=false
spring.datasource.username=root
spring.datasource.password=123456
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
# Dubbo
dubbo.scan.base-packages=io.github.helloworlde.pay.service.impl
dubbo.protocols.dubbo.name=dubbo
dubbo.protocols.dubbo.port=-1
dubbo.registry.address=spring-cloud://localhost
```

`dubbo.scan.base-packages`的路径为相应接口实现类的包路径
`dubbo.protocols.dubbo.name=dubbo`用于指定协议
`dubbo.protocols.dubbo.port=-1`使用随机端口，从 20880开始递增
`dubbo.registry.address=spring-cloud://localhost`挂载到 Spring Cloud 注册中心

- OrderService 

```
# 消费者的Dubbo配置和生产者略有差别
dubbo.cloud.subscribed-services=*
dubbo.registry.address=spring-cloud://localhost
dubbo.protocols.dubbo.name=dubbo
dubbo.protocols.dubbo.port=-1
```

`dubbo.cloud.subscribed-services=*`表示订阅所有的服务

##### DataSourceProxy 配置

这里是尤其需要注意的，Seata 是通过代理数据源实现事务分支，所以需要配置 `io.seata.rm.datasource.DataSourceProxy` 的 Bean，否则事务不会回滚，无法实现分布式事务 

```java
@Configuration
public class DataSourceProxyConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource dataSource() {
        return new DruidDataSource();
    }

    @Bean
    public DataSourceProxy dataSourceProxy(DataSource dataSource) {
        return new DataSourceProxy(dataSource);
    }

    @Bean
    public SqlSessionFactory sqlSessionFactoryBean(DataSourceProxy dataSourceProxy) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSourceProxy);
        return sqlSessionFactoryBean.getObject();
    }
}
```

如果使用的是 Hikari 数据源，需要修改数据源的配置，以及注入的 Bean 的配置前缀

```
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

#### Pay-Service

- PayService 接口 

```java
public interface PayService {
    OperationResponse reduceBalance(ReduceBalanceRequestVO reduceBalanceRequestVO) throws Exception;
}
```

- PayService 实现

```java
// Dubbo 的 Service
@Service
@Slf4j
public class PayServiceImpl implements PayService {

    @Autowired
    private AccountDao accountDao;

    @Override
    public OperationResponse reduceBalance(ReduceBalanceRequestVO reduceBalanceRequestVO) throws Exception {
        checkBalance(reduceBalanceRequestVO.getUserId(), reduceBalanceRequestVO.getPrice());

        Integer record = accountDao.reduceBalance(reduceBalanceRequestVO.getPrice());
      
        return OperationResponse.builder()
                                .success(record > 0)
                                .message(record > 0 ? "操作成功" : "扣余额失败")
                                .build();
    }
}
```

Storage-Service 和 Pay-Service 类似

#### Order-Service

- OrderServiceImpl.java

```java
// Spring 的 Service
@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderDao orderDao;

    @Reference
    @Lazy
    private PayService payService;

    @Reference
    @Lazy
    private StorageService storageService;


    @Override
    public OperationResponse placeOrder(PlaceOrderRequestVO placeOrderRequestVO) throws Exception {

        Integer price = placeOrderRequestVO.getPrice();
        // ...
        OperationResponse storageOperationResponse = storageService.reduceStock(reduceStockRequestVO);
        // ...
        OperationResponse balanceOperationResponse = payService.reduceBalance(reduceBalanceRequestVO);
        // ...       
        return OperationResponse.builder()
                                .success(balanceOperationResponse.isSuccess() && storageOperationResponse.isSuccess())
                                .build();
    }

}
```

- 添加相应的 REST 接口

- 在应用入口添加 `@EnableDubbo`注解

## 测试

- 启动应用

先启动 Pay-Service 和 Storage-Service，待启动完成后再启动Order-Service

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