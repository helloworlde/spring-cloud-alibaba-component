package io.github.helloworlde.storage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * @author HelloWood
 */
@SpringBootApplication
@EnableDiscoveryClient
public class JpaStorageServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(JpaStorageServiceApplication.class, args);
    }

}
