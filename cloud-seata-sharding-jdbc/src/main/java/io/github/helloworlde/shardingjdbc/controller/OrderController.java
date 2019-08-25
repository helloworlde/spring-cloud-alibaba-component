package io.github.helloworlde.shardingjdbc.controller;

import io.github.helloworlde.shardingjdbc.common.OperationResponse;
import io.github.helloworlde.shardingjdbc.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author HelloWood
 */
@RestController
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/add")
    @ResponseBody
    public OperationResponse add(Boolean success) throws Exception {

        Integer record = orderService.add(success);

        return OperationResponse.builder()
                                .data(record)
                                .success(record > 0)
                                .build();
    }
}
