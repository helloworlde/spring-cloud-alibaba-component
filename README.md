# Spring Cloud Alibaba 组件使用

## Nacos

> Nacos 是一个配置和注册中心，类似 Spring Cloud Config 和 Eureka、ZooKeeper、Consul

- [Spring Boot 使用 Nacos 作为配置中心](./boot-config/README.md)
- [Spring Cloud 使用 Nacos 作为配置中心](./cloud-config/README.md)
- [Spring Cloud 使用 Nacos 作为服务注册中心](./cloud-discovery/README.md)

## Sentinel 

> Sentinel 是一个流量控制框架，支持流量控制，熔断降级，系统负载保护，类似 Hystrix、resilience4j

- [Spring Cloud 使用 Sentinel 作为限流降级工具](./sentinel-nacos-config/README.md)

## OSS 

> spring-cloud-starter-alicloud-oss 是用于阿里云 OSS 的 SpringBoot Starter，通过封装 SDK 实现对 OSS 的操作

- [Spring Cloud 使用阿里云 OSS](./cloud-oss/README.md)

## Dubbo

> Dubbo 是一个远程调用框架，用于实现方法的远程调用

推荐使用 ZooKeeper 作为注册中心，当前使用 Nacos 会有各种问题

- [Spring Cloud 使用 Dubbo 实现远程调用，Nacos 作为注册中心](./cloud-dubbo-nacos/README.md)
- [Spring Cloud 使用 Dubbo 实现远程调用，ZooKeeper 作为注册中心](./cloud-dubbo-zk/README.md)

## Seata 

> Seata 是一个分布式事务框架，可以通过 Seata 框架的注解实现非侵入性的分布式事务

- [Spring Cloud 使用 Seata 实现分布式事务 - MyBatis](./cloud-seata-mybatis/README.md)
- [Spring Cloud 使用 Seata 实现分布式事务 - JPA](./cloud-seata-jpa/README.md)
- [Spring Cloud 使用 Seata 实现分布式事务 - Mybatis/Nacos](./cloud-seata-nacos/README.md)
- [Spring Cloud 使用 Seata 实现分布式事务 - Mybatis/Nacos/Dubbo](./cloud-seata-dubbo-nacos/README.md)

MyBatis 和 JPA 通过 Seata 实现分布式事务都需要注入 `io.seata.rm.datasource.DataSourceProxy`, 不同的是，MyBatis 还需要额外注入 `org.apache.ibatis.session.SqlSessionFactory`

<details>
<summary>MyBatis</summary>

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
</details>


<details>
<summary>JPA</summary>

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

</details>

------------------

## 版本说明 

相关的 Spring Boot, Spring Cloud 以及 Spring Cloud Alibaba 之间的对应关系可以参考 [版本说明](https://github.com/spring-cloud-incubator/spring-cloud-alibaba/wiki/%E7%89%88%E6%9C%AC%E8%AF%B4%E6%98%8E)

| 组件 | 版本 | 说明|
|:----|:-----|:----|
|Spring Boot |2.1.5.RELEASE||
|Spring Cloud |Greenwich.SR1||
|Spring Cloud Alibaba |0.9.1.BUILD-SNAPSHOT|| 
|Seata |0.6.1|`org.springframework.cloud:spring-cloud-starter-alibaba-seata:0.9.1.BUILD-SNAPSHOT` 中的 0.5.2 中有一些问题，所以单独使用Seata 0.6.1|
|Dubbo |2.7.1||