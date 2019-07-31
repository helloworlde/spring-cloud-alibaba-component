package io.github.helloworlde.order.service.impl;

import io.github.helloworlde.common.OperationResponse;
import io.github.helloworlde.order.dao.OrderDao;
import io.github.helloworlde.order.feign.PayServiceClient;
import io.github.helloworlde.order.feign.StorageServiceClient;
import io.github.helloworlde.order.model.Order;
import io.github.helloworlde.order.model.OrderStatus;
import io.github.helloworlde.order.model.PlaceOrderRequestVO;
import io.github.helloworlde.order.service.OrderService;
import io.github.helloworlde.pay.model.ReduceBalanceRequestVO;
import io.github.helloworlde.storage.model.ReduceStockRequestVO;
import io.seata.core.context.RootContext;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author HelloWood
 */
@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private PayServiceClient payServiceClient;

    @Autowired
    private StorageServiceClient storageServiceClient;

    @GlobalTransactional
    @Override
    public OperationResponse placeOrder(PlaceOrderRequestVO placeOrderRequestVO) {
        Integer amount = 1;
        Integer price = placeOrderRequestVO.getPrice();

        Order order = Order.builder()
                           .userId(placeOrderRequestVO.getUserId())
                           .productId(placeOrderRequestVO.getProductId())
                           .status(OrderStatus.INIT)
                           .payAmount(price)
                           .build();

        Integer saveOrderRecord = orderDao.saveOrder(order);
        log.info("保存订单{}", saveOrderRecord > 0 ? "成功" : "失败");
        log.info("当前 XID: {}", RootContext.getXID());
        // 扣减库存
        log.info("开始扣减库存");
        ReduceStockRequestVO reduceStockRequestVO = ReduceStockRequestVO.builder()
                                                                        .productId(placeOrderRequestVO.getProductId())
                                                                        .amount(amount)
                                                                        .build();
        OperationResponse storageOperationResponse = storageServiceClient.reduceStock(reduceStockRequestVO);
        log.info("扣减库存结果:{}", storageOperationResponse);

        // 扣减余额
        log.info("开始扣减余额");
        ReduceBalanceRequestVO reduceBalanceRequestVO = ReduceBalanceRequestVO.builder()
                                                                              .userId(placeOrderRequestVO.getUserId())
                                                                              .price(price)
                                                                              .build();

        OperationResponse balanceOperationResponse = payServiceClient.reduceBalance(reduceBalanceRequestVO);
        log.info("扣减余额结果:{}", balanceOperationResponse);

        Integer updateOrderRecord = orderDao.updateOrder(order.getId(), OrderStatus.SUCCESS);
        log.info("更新订单:{} {}", order.getId(), updateOrderRecord > 0 ? "成功" : "失败");

        return OperationResponse.builder()
                                .success(balanceOperationResponse.isSuccess() && storageOperationResponse.isSuccess())
                                .build();
    }

}
