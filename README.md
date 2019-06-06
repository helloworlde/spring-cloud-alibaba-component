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

## Seata 

> Seata 是一个分布式事务框架，可以通过 Seata 框架的注解实现非侵入性的分布式事务

- [Spring Cloud 使用 Seata 实现分布式事务 - MyBatis](./cloud-seata/README.md)
- [Spring Cloud 使用 Seata 实现分布式事务 - JPA](./cloud-seata-jpa/README.md)

MyBatis 和 JPA 通过 Seata 实现分布式事务都需要注入 `io.seata.rm.datasource.DataSourceProxy`, 不同的是，MyBatis 还需要额外注入 `org.apache.ibatis.session.SqlSessionFactory`

MyBatis

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

JPA 

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