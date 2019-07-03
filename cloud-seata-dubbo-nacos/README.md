# Spring Cloud 使用 Dubbo 实现远程调用，Nacos 作为注册中心

> Dubbo 是阿里巴巴开源的远程 RPC 框架，可以通过本地方法调用的方式实现远程调用

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

### 启动 Nacos

```bash
docker run --name nacos -p 8848:8848 -e MODE=standalone nacos/nacos-server
```

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

    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    runtime("mysql:mysql-connector-java")

    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

需要注意的是当前Spring Boot 和 Spring Cloud 以及 Spring Cloud Alibaba 的版本号需要互相对应，否则可能会存在各种问题；具体可以参考[版本说明](https://github.com/spring-cloud-incubator/spring-cloud-alibaba/wiki/%E7%89%88%E6%9C%AC%E8%AF%B4%E6%98%8E)

##### 配置 bootstrap.properties

- 生产者

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

- 生产者

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

#### Pay-Service

- PayService 接口 

```java
public interface PayService {
    OperationResponse reduceBalance(ReduceBalanceRequestVO reduceBalanceRequestVO) throws Exception;
}
```

- PaySerivce 实现

```java
// Dubbo 的 Service
@Service(protocol = "dubbo")
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

- bootstrap.properties

```
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
spring.main.allow-bean-definition-overriding=true
```

- application.properties

```
server.port=8081
management.endpoints.web.exposure.exclude=*
# MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/seata?useUnicode=true&characterEncoding=utf8&allowMultiQueries=true&useSSL=false
spring.datasource.username=root
spring.datasource.password=123456
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
# Dubbo
dubbo.cloud.subscribed-services=*
dubbo.registry.address=spring-cloud://localhost
dubbo.protocols.dubbo.name=dubbo
dubbo.protocols.dubbo.port=-1
```

`dubbo.cloud.subscribed-services=*`表示订阅所有的服务

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

        BigDecimal price = placeOrderRequestVO.getPrice();
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


- 调用 placeOrder 接口进行下单

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

可以在控制台查看相关的日志


----------

## 注意 

- RestTemplate

如果项目中有用到 `RestTemplate`，需要在 Bean 上添加`@DubboTransported`注解，不然会NPE 

```java
java.lang.NullPointerException: null
	at org.springframework.cloud.alibaba.dubbo.metadata.resolver.DubboTransportedAttributesResolver.resolve(DubboTransportedAttributesResolver.java:47) ~[spring-cloud-alibaba-dubbo-0.9.1.BUILD-SNAPSHOT.jar:0.9.1.BUILD-SNAPSHOT]
	at org.springframework.cloud.alibaba.dubbo.autoconfigure.DubboLoadBalancedRestTemplateAutoConfiguration.getDubboTranslatedAttributes(DubboLoadBalancedRestTemplateAutoConfiguration.java:143) ~[spring-cloud-alibaba-dubbo-0.9.1.BUILD-SNAPSHOT.jar:0.9.1.BUILD-SNAPSHOT]
	at org.springframework.cloud.alibaba.dubbo.autoconfigure.DubboLoadBalancedRestTemplateAutoConfiguration.adaptRestTemplates(DubboLoadBalancedRestTemplateAutoConfiguration.java:118) ~[spring-cloud-alibaba-dubbo-0.9.1.BUILD-SNAPSHOT.jar:0.9.1.BUILD-SNAPSHOT]
	...
```

- A component required a bean named 'dubbo' that could not be found.

配置无效：
```
dubbo.protocol.name=dubbo
dubbo.protocol.port=-1
```
需要改成以下才生效 

```
dubbo.protocols.dubbo.name=dubbo
dubbo.protocols.dubbo.port=-1
```

-  Fail to start qos server: , dubbo version: 2.7.1, current host: 172.16.81.91

启动的过程中抛出`java.net.BindException: Address already in use`，提示`Fail to start qos server: , dubbo version: 2.7.1, current host: 172.16.81.91`

这是因为已经启动的应用占用了 qos 服务的 22222 端口，虽然中有 qos 相关的配置，但是并不会起作用，需要在JVM的启动参数中添加 `-Ddubbo.application.qos.enable=false -Ddubbo.application.qos.accept.foreign.ip=false`

```
2019-07-02 15:30:33.122  WARN 10613 --- [           main] o.a.d.qos.protocol.QosProtocolWrapper    :  [DUBBO] Fail to start qos server: , dubbo version: 2.7.1, current host: 172.16.81.91

java.net.BindException: Address already in use
	at sun.nio.ch.Net.bind0(Native Method) ~[na:1.8.0_172]
	at sun.nio.ch.Net.bind(Net.java:433) ~[na:1.8.0_172]
	at sun.nio.ch.Net.bind(Net.java:425) ~[na:1.8.0_172]
	...
```

关于 qos 的配置可以参考 [新版本 telnet 命令使用说明](http://dubbo.apache.org/zh-cn/docs/user/references/qos.html)