package io.github.helloworlde.multiple.mybatisplus.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.helloworlde.multiple.mybatisplus.common.order.Order;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author HelloWood
 */
@Mapper
public interface OrderDao extends BaseMapper<Order> {

}
