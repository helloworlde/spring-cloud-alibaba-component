package io.github.helloworlde.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author HelloWood
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableJpaRepositories
@EnableTransactionManagement
public class JpaOrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(JpaOrderServiceApplication.class, args);
    }

}
