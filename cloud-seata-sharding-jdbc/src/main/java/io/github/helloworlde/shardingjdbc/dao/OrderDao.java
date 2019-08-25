package io.github.helloworlde.shardingjdbc.dao;

import io.github.helloworlde.shardingjdbc.common.order.Order;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

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
    @Insert("INSERT INTO order (user_id, product_id) VALUES (#{userId}, #{productId})")
    @Options(useGeneratedKeys = true, keyColumn = "id", keyProperty = "id")
    Integer saveOrder(Order order);

}
