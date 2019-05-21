# Spring Cloud Alibaba 组件使用

## Nacos

> Nacos 是一个配置和注册中心，类似 Spring Cloud Config 和 Eureka、ZooKeeper、Consul

- 快速启动 Nacos 

```bash
docker run --name nacos -d -p 8848:8848 -e MODE=standalone nacos/nacos-server
```

- Clone 应用 

```bash
https://github.com/helloworlde/spring-cloud-alibaba-component.git

cd spring-cloud-alibaba-component/

./gradlew clean build -x test
```


### 模块 

|模块 | 特性|
|:---|:---|
|[Spring Boot 使用 Nacos 作为配置中心](./boot-config/README.md)|Spring Boot 中使用 Nacos 作为配置中心|
|[Spring Cloud 使用 Nacos 作为配置中心](./cloud-config/README.md)|Spring Cloud 中使用 Nacos 作为配置中心|
|[Spring Cloud 使用 Nacos 作为服务注册中心](./cloud-discovery/README.md)|Spring Cloud 中使用 Nacos 作为配置中心和服务注册中心|