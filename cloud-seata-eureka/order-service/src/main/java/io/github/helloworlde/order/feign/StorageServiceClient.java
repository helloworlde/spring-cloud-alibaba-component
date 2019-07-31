package io.github.helloworlde.order.feign;

import io.github.helloworlde.common.OperationResponse;
import io.github.helloworlde.storage.model.ReduceStockRequestVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author HelloWood
 */
@FeignClient(value = "storage-service", fallback = StorageServiceFallback.class)
@RequestMapping("/storage")
public interface StorageServiceClient {

    @PostMapping("/reduceStock")
    OperationResponse reduceStock(ReduceStockRequestVO reduceStockRequestVO);
}

class StorageServiceFallback implements StorageServiceClient {

    @Override
    public OperationResponse reduceStock(ReduceStockRequestVO reduceStockRequestVO) {
        return OperationResponse.builder()
                                .success(false)
                                .build();
    }
}
