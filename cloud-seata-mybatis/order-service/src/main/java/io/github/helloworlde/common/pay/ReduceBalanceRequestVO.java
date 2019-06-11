package io.github.helloworlde.common.pay;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 扣减余额请求 VO
 *
 * @author HelloWood
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReduceBalanceRequestVO {

    private Long userId;

    private BigDecimal price;
}
