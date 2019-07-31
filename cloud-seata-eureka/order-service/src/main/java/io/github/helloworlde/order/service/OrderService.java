package io.github.helloworlde.order.service;

import io.github.helloworlde.common.OperationResponse;
import io.github.helloworlde.order.model.PlaceOrderRequestVO;

/**
 * @author HelloWood
 */
public interface OrderService {

    /**
     * 下单
     *
     * @param placeOrderRequestVO 请求参数
     * @return 下单结果
     */
    OperationResponse placeOrder(PlaceOrderRequestVO placeOrderRequestVO);
}
