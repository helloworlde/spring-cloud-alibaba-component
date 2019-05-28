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


- [Spring Boot 使用 Nacos 作为配置中心](./boot-config/README.md)
- [Spring Cloud 使用 Nacos 作为配置中心](./cloud-config/README.md)
- [Spring Cloud 使用 Nacos 作为服务注册中心](./cloud-discovery/README.md)

## Sentinel 

> Sentinel 是一个流量控制框架，支持流量控制，熔断降级，系统负载保护，类似 Hystrix、resilience4j

- [Spring Cloud 使用 Sentinel 作为限流降级工具](./sentinel-nacos-config/README.md)

## OSS 

> spring-cloud-starter-alicloud-oss 是用于阿里云 OSS 的 SpringBoot Starter，通过封装 SDK 实现对 OSS 的操作

- [Spring Cloud 使用阿里云 OSS](./cloud-oss/README.md)

## Seata 

> Seata 是一个分布式事务框架，可以通过 Seata 框架的注解实现非侵入性的分布式事务

- [Spring Cloud 使用 Seata 实现分布式事务](./cloud-seata/README.md)