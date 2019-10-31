# Spring Cloud 使用 Seata 实现分布式事务，Nacos 作为 Seata 配置中心

> 使用 Seata 作为分布式事务组件，配置中心和注册中心使用 Nacos，使用 MySQL 数据库和 MyBatis，同时使用 Nacos 作为 Seata 的配置中心

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
VALUES (1, 10);
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
    context       VARCHAR(128) NOT NULL,
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

1. 在 [Seata Release](https://github.com/seata/seata/releases) 下载相应版本的 Seata Server 并解压
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
sh ./bin/seata-server.sh
```

启动后在 Nacos 的服务列表下面可以看到一个名为`serverAddr`的服务

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

## 注意 

### DataSourceProxy 配置

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
    
    @Bean
    public SqlSessionFactory sqlSessionFactoryBean(DataSourceProxy dataSourceProxy) throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSourceProxy);
        return sqlSessionFactoryBean.getObject();
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

### 版本

需要注意的是当前Spring Boot 和 Spring Cloud 以及 Spring Cloud Alibaba 的版本号需要互相对应，否则可能会存在各种问题；具体可以参考[版本说明](https://github.com/spring-cloud-incubator/spring-cloud-alibaba/wiki/%E7%89%88%E6%9C%AC%E8%AF%B4%E6%98%8E)

