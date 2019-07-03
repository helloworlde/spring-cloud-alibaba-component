package io.github.helloworlde.pay.service.impl;

import io.github.helloworlde.common.OperationResponse;
import io.github.helloworlde.common.pay.ReduceBalanceRequestVO;
import io.github.helloworlde.pay.dao.AccountDao;
import io.github.helloworlde.pay.service.PayService;
import io.seata.core.context.RootContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author HelloWood
 */
@Service
@Slf4j
public class PayServiceImpl implements PayService {

    @Autowired
    private AccountDao accountDao;

    @Override
    public OperationResponse reduceBalance(ReduceBalanceRequestVO reduceBalanceRequestVO) throws Exception {

        log.info("当前 XID: {}", RootContext.getXID());

        checkBalance(reduceBalanceRequestVO.getUserId(), reduceBalanceRequestVO.getPrice());

        log.info("开始扣减用户 {} 余额", reduceBalanceRequestVO.getUserId());
        Integer record = accountDao.reduceBalance(reduceBalanceRequestVO.getPrice());
        log.info("扣减用户 {} 余额结果:{}", reduceBalanceRequestVO.getUserId(), record > 0 ? "操作成功" : "扣减余额失败");

        return OperationResponse.builder()
                                .success(record > 0)
                                .message(record > 0 ? "操作成功" : "扣余额失败")
                                .build();

    }

    private void checkBalance(Long userId, Integer price) throws Exception {
        log.info("检查用户 {} 余额", userId);
        Integer balance = accountDao.getBalance(userId);

        if (balance < price) {
            log.warn("用户 {} 余额不足，当前余额:{}", userId, balance);
            throw new Exception("余额不足");
        }

    }

}
