package io.github.helloworlde.multiple.datasource.service.impl;

import io.github.helloworlde.multiple.datasource.common.OperationResponse;
import io.github.helloworlde.multiple.datasource.common.order.Order;
import io.github.helloworlde.multiple.datasource.common.order.OrderStatus;
import io.github.helloworlde.multiple.datasource.common.order.PlaceOrderRequestVO;
import io.github.helloworlde.multiple.datasource.config.DataSourceKey;
import io.github.helloworlde.multiple.datasource.config.DynamicDataSourceContextHolder;
import io.github.helloworlde.multiple.datasource.dao.OrderDao;
import io.github.helloworlde.multiple.datasource.service.OrderService;
import io.github.helloworlde.multiple.datasource.service.PayService;
import io.github.helloworlde.multiple.datasource.service.StorageService;
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
    private PayService payService;

    @Autowired
    private StorageService storageService;

    @GlobalTransactional
    @Override
    public OperationResponse placeOrder(PlaceOrderRequestVO placeOrderRequestVO) throws Exception {
        log.info("=============ORDER=================");
        DynamicDataSourceContextHolder.setDataSourceKey(DataSourceKey.ORDER);
        log.info("当前 XID: {}", RootContext.getXID());

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

        // 扣减库存
        boolean operationStorageResult = storageService.reduceStock(placeOrderRequestVO.getProductId(), amount);

        // 扣减余额
        boolean operationBalanceResult = payService.reduceBalance(placeOrderRequestVO.getUserId(), price);

        log.info("=============ORDER=================");
        DynamicDataSourceContextHolder.setDataSourceKey(DataSourceKey.ORDER);

        Integer updateOrderRecord = orderDao.updateOrder(order.getId(), OrderStatus.SUCCESS);
        log.info("更新订单:{} {}", order.getId(), updateOrderRecord > 0 ? "成功" : "失败");

        return OperationResponse.builder()
                                .success(operationStorageResult && operationBalanceResult)
                                .build();
    }
}
