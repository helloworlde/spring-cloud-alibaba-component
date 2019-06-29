package io.github.helloworlde.order.config;

import org.springframework.cloud.alibaba.dubbo.annotation.DubboTransported;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * @author HelloWood
 */
@Component
public class RestTemplateConfig {

    @LoadBalanced
    @Bean
    @DubboTransported
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
