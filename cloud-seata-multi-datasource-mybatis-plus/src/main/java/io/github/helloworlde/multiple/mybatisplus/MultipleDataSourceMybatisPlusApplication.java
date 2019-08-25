package io.github.helloworlde.multiple.mybatisplus;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author HelloWood
 */
@SpringBootApplication
@MapperScan("io.github.helloworlde.multiple.mybatisplus.dao")
public class MultipleDataSourceMybatisPlusApplication {

    public static void main(String[] args) {
        SpringApplication.run(MultipleDataSourceMybatisPlusApplication.class, args);
    }

}
