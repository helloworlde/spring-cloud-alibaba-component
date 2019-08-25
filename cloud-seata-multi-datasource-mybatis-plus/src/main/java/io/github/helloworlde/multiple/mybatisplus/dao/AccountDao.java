package io.github.helloworlde.multiple.mybatisplus.dao;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.helloworlde.multiple.mybatisplus.common.pay.Account;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author HelloWood
 */
@Mapper
public interface AccountDao extends BaseMapper<Account> {

}