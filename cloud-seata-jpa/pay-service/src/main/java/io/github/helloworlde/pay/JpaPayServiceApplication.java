package io.github.helloworlde.pay;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author HelloWood
 */
@SpringBootApplication
@EnableDiscoveryClient
public class JpaPayServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(JpaPayServiceApplication.class, args);
    }

}
