package io.github.helloworlde.multiple.mybatisplus.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.helloworlde.multiple.mybatisplus.common.storage.Product;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author HelloWood
 */
@Mapper
public interface ProductDao extends BaseMapper<Product> {

}
