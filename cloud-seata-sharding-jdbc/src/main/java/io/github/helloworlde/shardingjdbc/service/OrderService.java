package io.github.helloworlde.shardingjdbc.service;

import io.github.helloworlde.shardingjdbc.common.order.Order;

/**
 * @author HelloWood
 */
public interface OrderService {

    Integer add(Boolean success) throws Exception;

    Integer saveEvenOrder(Order order);

    Integer saveOddOrder(Order order);
}
