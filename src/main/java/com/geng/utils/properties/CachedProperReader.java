package com.geng.utils.properties;


import com.geng.server.GameEngine;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Set;

/**
 * Created by lifangkai on 15/1/15.
 */
public class CachedProperReader extends IPropertyReader {

    @Override
    public String getItem(String key, String defaultValue) {
        String configValue = GameEngine.getInstance().getConfigValue(key);
        if (StringUtils.isBlank(configValue) || "null".equals(configValue)) {
            configValue = defaultValue;
        }
        return configValue;
    }

    @Override
    public Map<String, String> getBatchItems(Set<String> keys) {
        throw new UnsupportedOperationException("not supported to get batch items for cache property reader");
    }
}
