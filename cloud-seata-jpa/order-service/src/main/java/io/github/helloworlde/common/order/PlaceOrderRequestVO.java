package io.github.helloworlde.common.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 下单请求 VO
 *
 * @author HelloWood
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlaceOrderRequestVO {
    private Long userId;

    private Long productId;

    private Double price;
}
