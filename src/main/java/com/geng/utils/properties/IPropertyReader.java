package com.geng.utils.properties;

import java.util.Map;
import java.util.Set;

/**
 * Created by lifangkai on 15/1/15.
 */
public abstract class IPropertyReader {

    public abstract String getItem(String key, String defaultValue);

    public abstract Map<String, String> getBatchItems(Set<String> keys);

    public String getItem(String key) {
        return getItem(key, "");
    }

    public int getIntItem(String key, int defaultValue) {
        return Integer.parseInt(getItem(key, String.valueOf(defaultValue)));
    }

    public int getIntItem(String key, String defaultValue) {
        return Integer.parseInt(getItem(key, defaultValue));
    }

    public int getIntItem(String key) {
        return getIntItem(key, 0);
    }

    public long getLongItem(String key, long defaultValue) {
        return Long.parseLong(getItem(key, String.valueOf(defaultValue)));
    }

    public long getLongItem(String key, String defaultValue) {
        return Long.parseLong(getItem(key, defaultValue));
    }

    public long getLongItem(String key) {
        return getLongItem(key, 0);
    }

    public boolean getBooleanItem(String key, String defaultValue) {
        return Boolean.parseBoolean(getItem(key, defaultValue));
    }

    public boolean getBooleanItem(String key) {
        return getBooleanItem(key, "true");
    }
}
