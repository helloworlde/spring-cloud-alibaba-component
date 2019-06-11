package io.github.helloworlde.order.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 订单
 *
 * @author HelloWood
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Order {

    private Integer id;

    private Long userId;

    private Long productId;

    private OrderStatus status;

    private BigDecimal payAmount;
}
