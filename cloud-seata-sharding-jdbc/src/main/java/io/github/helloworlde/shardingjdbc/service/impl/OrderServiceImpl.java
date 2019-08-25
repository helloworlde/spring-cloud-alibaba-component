package io.github.helloworlde.shardingjdbc.service.impl;

import io.github.helloworlde.shardingjdbc.common.order.Order;
import io.github.helloworlde.shardingjdbc.dao.OrderDao;
import io.github.helloworlde.shardingjdbc.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author HelloWood
 */
@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private OrderService orderService;

    private AtomicInteger id = new AtomicInteger();

    // @GlobalTransactional
    @Override
    public Integer add(Boolean success) throws Exception {
        Integer record = 0;
        for (int i = 0; i < 12; i++) {

            Order order = Order.builder()
                               .userId(id.get())
                               .productId(id.get())
                               .build();

            log.info("保存第{}条数据", id.getAndIncrement());
            if (i % 2 == 0) {
                record += orderService.saveEvenOrder(order);
            } else {
                record += orderService.saveOddOrder(order);
            }
        }

        if (!success) {
            throw new Exception("事务回滚");
        }

        return record;
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public Integer saveEvenOrder(Order order) {
        return orderDao.saveOrder(order);
    }

    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    @Override
    public Integer saveOddOrder(Order order) {
        return orderDao.saveOrder(order);
    }
}
