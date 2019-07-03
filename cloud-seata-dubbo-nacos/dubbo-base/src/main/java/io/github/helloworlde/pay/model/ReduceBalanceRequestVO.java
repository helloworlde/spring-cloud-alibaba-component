package io.github.helloworlde.pay.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 扣减余额请求 VO
 *
 * @author HelloWood
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReduceBalanceRequestVO implements Serializable {

    private static final long serialVersionUID = -4174566386226685995L;

    private Long userId;

    private Integer price;
}
