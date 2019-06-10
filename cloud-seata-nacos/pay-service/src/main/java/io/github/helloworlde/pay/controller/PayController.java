package io.github.helloworlde.pay.controller;

import io.github.helloworlde.common.OperationResponse;
import io.github.helloworlde.common.pay.ReduceBalanceRequestVO;
import io.github.helloworlde.pay.service.PayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author HelloWood
 */
@RestController
@RequestMapping("/pay")
@Slf4j
public class PayController {

    @Autowired
    private PayService payService;

    @PostMapping("/reduceBalance")
    @ResponseBody
    public OperationResponse reduceBalance(@RequestBody ReduceBalanceRequestVO reduceBalanceRequestVO) throws Exception {
        return payService.reduceBalance(reduceBalanceRequestVO);
    }

}
