package io.github.helloworlde.order.dao;

import io.github.helloworlde.order.model.Order;
import io.github.helloworlde.order.model.OrderStatus;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author HelloWood
 */
public interface OrderDao extends CrudRepository<Order, Long> {

    /**
     * 更新订单状态
     *
     * @param id     订单 ID
     * @param status 状态
     * @return 影响行数
     */
    @Modifying
    @Query("UPDATE Order SET status = :status WHERE id = :id")
    @Transactional
    Integer updateOrder(@Param("id") Long id, @Param("status") OrderStatus status);


}
