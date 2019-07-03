package io.github.helloworlde.order.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 下单请求 VO
 *
 * @author HelloWood
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlaceOrderRequestVO implements Serializable {

    private static final long serialVersionUID = 1105728290408740757L;

    private Long userId;

    private Long productId;

    private Integer price;
}
