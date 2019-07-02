# Spring Cloud 使用 Dubbo 实现远程调用，ZooKeeper 作为注册中心

> Dubbo 是阿里巴巴开源的远程 RPC 框架，可以通过本地方法调用的方式实现远程调用

## 环境准备

### 启动 ZooKeeper

```bash
docker run --name zookeeper -p 2181:2181 -p 2888:2888 -p 3888:3888 -d zookeeper
```

## 应用 

两个应用，一个是生产者，一种是消费者；生产者提供接口，消费者调用后返回相应的数据

### 添加应用

#### 添加公共模块

> Dubbo 服务接口是服务提供方与消费方的远程通讯契约，通常由普通的 Java 接口（interface）来声明
> 为了确保契约的一致性，推荐的做法是将 Dubbo 服务接口打包在第二方或者第三方的 artifact（jar）中， 对于服务提供方而言，不仅通过依赖 artifact 的形式引入 Dubbo 服务接口，而且需要将其实现。对应的服务消费端，同样地需要依赖该 artifact， 并以接口调用的方式执行远程方法。接下来进一步讨论怎样实现 Dubbo 服务提供方和消费方。

因此，添加一个公共模块用于各个应用作为依赖；将相关的接口放在这里

- build.gradle 

```groovy
plugins {
    id 'java'
}

archivesBaseName = 'dubbo-base'

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

##### 配置依赖 build.gradle 

```groovy
dependencies {
    compile project(':cloud-dubbo-nacos/dubbo-base')

    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'org.springframework.boot:spring-boot-starter-web'

    compile('org.springframework.cloud:spring-cloud-starter-dubbo')
    compile('org.springframework.cloud:spring-cloud-starter-zookeeper-discovery')

    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'

    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}
```

需要注意的是当前Spring Boot 和 Spring Cloud 以及 Spring Cloud Alibaba 的版本号需要互相对应，否则可能会存在各种问题；具体可以参考[版本说明](https://github.com/spring-cloud-incubator/spring-cloud-alibaba/wiki/%E7%89%88%E6%9C%AC%E8%AF%B4%E6%98%8E)

##### 配置 bootstrap.properties

```properties
spring.application.name=
# Discovery
spring.cloud.zookeeper.enabled=true
spring.cloud.zookeeper.connect-string=localhost:2181
```

##### application.properties

- 生产者

```properties
server.port=8082
management.endpoints.web.exposure.include=*
# Dubbo
dubbo.scan.base-packages=io.github.helloworlde.provider.service.impl
dubbo.registry.address=spring-cloud://localhost
dubbo.protocol.port=-1
dubbo.protocol.name=dubbo
```

`dubbo.scan.base-packages`暴露的服务的扫描路径
`dubbo.protocol.name=dubbo`用于指定协议
`dubbo.protocol.port=-1`使用随机端口，从 20880开始递增
`dubbo.registry.address=spring-cloud://localhost`挂载到 Spring Cloud 注册中心

- 消费者

```properties
server.port=8081
management.endpoints.web.exposure.exclude=*
# Dubbo
dubbo.cloud.subscribed-services=*
dubbo.registry.address=spring-cloud://localhost
dubbo.protocol.port=-1
dubbo.protocol.name=dubbo
```

`dubbo.cloud.subscribed-services=*`表示订阅所有的服务


#### 生产者添加接口

```java
@Service
public class ProviderServiceImpl implements ProviderService {

    @Override
    public String sayHello(String name) {
        return "Provider: Hello " + name;
    }
}
```

#### 消费者添加相应的 REST 接口

```java
@RestController
public class ConsumerController {

    @Reference
    private ProviderService providerService;

    @GetMapping("/hello")
    public String hello(String name) {
        return providerService.sayHello(name);
    }
}
```


## 测试

- 启动应用

先启动 Provider，待启动完成后再启动 Consumer


- 调用接口

```bash
curl 'localhost:8081/hello?name=dubbo'
```

此时返回结果为：

```bash
Provider: Hello dubbo%
```

----------

## 注意 

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