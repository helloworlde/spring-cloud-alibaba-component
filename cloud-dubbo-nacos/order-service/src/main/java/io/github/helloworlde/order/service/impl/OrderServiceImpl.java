package io.github.helloworlde.order.service.impl;

import io.github.helloworlde.common.OperationResponse;
import io.github.helloworlde.order.dao.OrderDao;
import io.github.helloworlde.order.model.Order;
import io.github.helloworlde.order.model.OrderStatus;
import io.github.helloworlde.order.model.PlaceOrderRequestVO;
import io.github.helloworlde.order.service.OrderService;
import io.github.helloworlde.pay.model.ReduceBalanceRequestVO;
import io.github.helloworlde.pay.service.PayService;
import io.github.helloworlde.storage.model.ReduceStockRequestVO;
import io.github.helloworlde.storage.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.math.BigDecimal;

/**
 * @author HelloWood
 */
@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderDao orderDao;

    @Reference
    @Lazy
    private PayService payService;

    @Reference
    @Lazy
    private StorageService storageService;


    @Override
    public OperationResponse placeOrder(PlaceOrderRequestVO placeOrderRequestVO) throws Exception {
        Integer amount = 1;
        BigDecimal price = placeOrderRequestVO.getPrice();

        Order order = Order.builder()
                           .userId(placeOrderRequestVO.getUserId())
                           .productId(placeOrderRequestVO.getProductId())
                           .status(OrderStatus.INIT)
                           .payAmount(price)
                           .build();

        Integer saveOrderRecord = orderDao.saveOrder(order);
        log.info("保存订单{}", saveOrderRecord > 0 ? "成功" : "失败");
        // 扣减库存
        log.info("开始扣减库存");
        ReduceStockRequestVO reduceStockRequestVO = ReduceStockRequestVO.builder()
                                                                        .productId(placeOrderRequestVO.getProductId())
                                                                        .amount(amount)
                                                                        .build();


        OperationResponse storageOperationResponse = storageService.reduceStock(reduceStockRequestVO);
        log.info("扣减库存结果:{}", storageOperationResponse);

        // 扣减余额
        log.info("开始扣减余额");
        ReduceBalanceRequestVO reduceBalanceRequestVO = ReduceBalanceRequestVO.builder()
                                                                              .userId(placeOrderRequestVO.getUserId())
                                                                              .price(price)
                                                                              .build();

        OperationResponse balanceOperationResponse = payService.reduceBalance(reduceBalanceRequestVO);
        log.info("扣减余额结果:{}", balanceOperationResponse);

        Integer updateOrderRecord = orderDao.updateOrder(order.getId(), OrderStatus.SUCCESS);
        log.info("更新订单:{} {}", order.getId(), updateOrderRecord > 0 ? "成功" : "失败");

        return OperationResponse.builder()
                                .success(balanceOperationResponse.isSuccess() && storageOperationResponse.isSuccess())
                                .build();
    }

}
