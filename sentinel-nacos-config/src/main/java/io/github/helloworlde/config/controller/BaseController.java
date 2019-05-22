package io.github.helloworlde.config.controller;

import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author HelloWood
 */
@RestController
@RequestMapping("/")
@RefreshScope
public class BaseController {

    @GetMapping("/hello")
    @ResponseBody
    public String hello() throws Exception {
        // String s = null;
        // s.toString();

        return "Hello World";
    }
}
