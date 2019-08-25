package io.github.helloworlde.multiple.mybatisplus.common.pay;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    private Integer price;
}
