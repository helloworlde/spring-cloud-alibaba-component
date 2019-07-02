package io.github.helloworlde.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 操作返回结果
 *
 * @author HelloWood
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OperationResponse implements Serializable {

    private static final long serialVersionUID = -7978181653942166351L;

    private boolean success;

    private String message;

    private Object data;
}
