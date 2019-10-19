package io.github.helloworlde.pay.dao;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * @author HelloWood
 */
@Mapper
public interface AccountDao {

    /**
     * 获取账户余额
     *
     * @param id 用户 ID
     * @return 账户余额
     */
    @Select("SELECT balance FROM account WHERE id = #{id}")
    Integer getBalance(@Param("id") Long id);

    /**
     * 扣减余额
     *
     * @param price 需要扣减的数目
     * @return 影响记录行数
     */
    @Update("UPDATE account SET balance = balance - #{price} WHERE id = 1")
    Integer reduceBalance(@Param("price") Integer price);
}
