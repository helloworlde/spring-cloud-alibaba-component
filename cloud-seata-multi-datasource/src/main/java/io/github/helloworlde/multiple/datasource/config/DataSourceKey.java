package io.github.helloworlde.multiple.datasource.config;

import lombok.Getter;

/**
 * @author HelloWood
 */

@Getter
public enum DataSourceKey {
    /**
     * Order data source key.
     */
    ORDER,
    /**
     * Storage data source key.
     */
    STORAGE,
    /**
     * Pay data source key.
     */
    PAY,
}