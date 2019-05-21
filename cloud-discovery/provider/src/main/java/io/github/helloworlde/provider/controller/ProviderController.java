package io.github.helloworlde.provider.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

/**
 * @author HelloWood
 */
@RestController
@RequestMapping("/provider")
@RefreshScope
@Slf4j
public class ProviderController {

    @Value("${message.prefix:empty\t}")
    private String messagePrefix;

    @GetMapping("/echo")
    @ResponseBody
    public String echo(@RequestParam String message) {
        log.info("The request param is {}", message);
        return messagePrefix + message;
    }
}
