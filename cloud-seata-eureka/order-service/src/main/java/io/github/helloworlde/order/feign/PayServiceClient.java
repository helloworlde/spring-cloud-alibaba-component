package io.github.helloworlde.order.feign;

import io.github.helloworlde.common.OperationResponse;
import io.github.helloworlde.pay.model.ReduceBalanceRequestVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author HelloWood
 */
@FeignClient(value = "pay-service", fallback = PayServiceFallback.class)
@RequestMapping("/pay")
public interface PayServiceClient {

    @PostMapping("/reduceBalance")
    OperationResponse reduceBalance(ReduceBalanceRequestVO reduceBalanceRequestVO);
}


class PayServiceFallback implements PayServiceClient {

    @Override
    public OperationResponse reduceBalance(ReduceBalanceRequestVO reduceBalanceRequestVO) {
        return OperationResponse.builder()
                                .success(false)
                                .build();
    }
}