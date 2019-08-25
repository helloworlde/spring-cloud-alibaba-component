package io.github.helloworlde.multiple.mybatisplus.service;

import io.github.helloworlde.multiple.mybatisplus.common.OperationResponse;
import io.github.helloworlde.multiple.mybatisplus.common.order.PlaceOrderRequestVO;

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
    OperationResponse placeOrder(PlaceOrderRequestVO placeOrderRequestVO) throws Exception;
}
