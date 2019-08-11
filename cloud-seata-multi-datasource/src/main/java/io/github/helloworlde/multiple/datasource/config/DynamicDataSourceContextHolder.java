package io.github.helloworlde.multiple.datasource.config;

import java.util.ArrayList;
import java.util.List;

/**
 * @author HelloWood
 */
public class DynamicDataSourceContextHolder {

    private static final ThreadLocal<String> CONTEXT_HOLDER = ThreadLocal.withInitial(DataSourceKey.ORDER::name);

    private static List<Object> dataSourceKeys = new ArrayList<>();

    public static void setDataSourceKey(DataSourceKey key) {
        CONTEXT_HOLDER.set(key.name());
    }

    public static String getDataSourceKey() {
        return CONTEXT_HOLDER.get();
    }

    public static void clearDataSourceKey() {
        CONTEXT_HOLDER.remove();
    }

    public static List<Object> getDataSourceKeys() {
        return dataSourceKeys;
    }
}