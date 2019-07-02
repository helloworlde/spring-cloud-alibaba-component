package io.github.helloworlde.consumer.controller;

import io.github.helloworlde.provider.service.ProviderService;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author HelloWood
 */
@RestController
public class ConsumerController {

    @Reference
    private ProviderService providerService;

    @GetMapping("/hello")
    public String hello(String name) {
        return providerService.sayHello(name);
    }
}
