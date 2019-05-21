package io.github.hellworlde.config.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author HelloWood
 */
@RestController
@RequestMapping("/app-info")
@RefreshScope
public class AppInfoController {

    @Value("${spring.application.version:empty}")
    private String version;

    @GetMapping
    @ResponseBody
    public String appInfo() {
        return version;
    }
}
