package io.github.helloworlde.storage.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 扣减库存请求 VO
 *
 * @author HelloWood
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReduceStockRequestVO implements Serializable {

    private static final long serialVersionUID = 3522006293385539909L;

    private Long productId;

    private Integer amount;
}
