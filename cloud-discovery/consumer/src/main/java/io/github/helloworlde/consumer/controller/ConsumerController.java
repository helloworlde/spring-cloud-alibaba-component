package io.github.helloworlde.consumer.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

/**
 * @author HelloWood
 */
@RestController
@RequestMapping("/consumer")
@RefreshScope
public class ConsumerController {

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/echo")
    @ResponseBody
    public String echo(@RequestParam String message) {
        return restTemplate.getForObject("http://nacos-provider/provider/echo?message=" + message, String.class);
    }
}
