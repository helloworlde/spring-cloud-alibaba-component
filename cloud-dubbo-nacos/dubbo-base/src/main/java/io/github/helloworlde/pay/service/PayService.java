package io.github.helloworlde.pay.service;

import io.github.helloworlde.common.OperationResponse;
import io.github.helloworlde.pay.model.ReduceBalanceRequestVO;

/**
 * @author HelloWood
 */
public interface PayService {
    /**
     * @param reduceBalanceRequestVO
     * @return
     * @throws Exception
     */
    OperationResponse reduceBalance(ReduceBalanceRequestVO reduceBalanceRequestVO) throws Exception;
}
