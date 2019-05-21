package io.github.hellworlde.config.controller;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author HelloWood
 */
@RestController
@RequestMapping("/app-info")
@NacosPropertySource(dataId = "app", groupId = "APP_INFO", autoRefreshed = true)
public class AppInfoController {

    @NacosValue(value = "${spring.application.version}", autoRefreshed = true)
    private String version;

    @GetMapping
    @ResponseBody
    public String appInfo() {
        return version;
    }
}
