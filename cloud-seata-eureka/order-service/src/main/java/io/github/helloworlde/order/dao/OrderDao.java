package io.github.helloworlde.order.dao;

import io.github.helloworlde.order.model.Order;
import io.github.helloworlde.order.model.OrderStatus;
import org.apache.ibatis.annotations.*;

/**
 * @author HelloWood
 */
@Mapper
public interface OrderDao {

    /**
     * 保存订单
     *
     * @param order 订单
     * @return 影响行数
     */
    @Insert("INSERT INTO orders (user_id, product_id, pay_amount, status) VALUES (#{userId}, #{productId}, #{payAmount}, #{status})")
    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    Integer saveOrder(Order order);

    /**
     * 更新订单状态
     *
     * @param id     订单 ID
     * @param status 状态
     * @return 影响行数
     */
    @Update("UPDATE orders SET status = #{status} WHERE id = #{id}")
    Integer updateOrder(@Param("id") Integer id, @Param("status") OrderStatus status);


}
