package io.github.helloworlde.provider.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

/**
 * @author HelloWood
 */
@RestController
@RequestMapping("/provider")
@RefreshScope
public class ProviderController {

    @Value("${message.prefix:empty\t}")
    private String messagePrefix;

    @GetMapping("/echo")
    @ResponseBody
    public String echo(@RequestParam String message) {
        return messagePrefix + message;
    }
}
