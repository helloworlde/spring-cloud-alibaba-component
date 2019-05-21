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
@RequestMapping("/datasource")
@NacosPropertySource(dataId = "datasource", autoRefreshed = true)
public class DataSourceController {

    @NacosValue(value = "${spring.datasource.url}", autoRefreshed = true)
    private String datasource;

    @GetMapping
    @ResponseBody
    public String datasource() {
        return datasource;
    }
}
