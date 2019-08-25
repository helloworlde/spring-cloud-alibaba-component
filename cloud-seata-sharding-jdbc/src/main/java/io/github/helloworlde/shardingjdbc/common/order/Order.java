package io.github.helloworlde.shardingjdbc.common.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author HelloWood
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Order {

    private Integer id;

    private Integer userId;

    private Integer productId;

}
