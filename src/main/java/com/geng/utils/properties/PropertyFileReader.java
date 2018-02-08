package com.geng.utils.properties;

public class PropertyFileReader {
    private static final IPropertyReader cachedPropertyReader = new CachedProperReader();

    public static String getItem(String key) {
        return getItem(key, "");
    }

    public static String getItem(String key, String defaultValue) {
        return cachedPropertyReader.getItem(key, defaultValue);
    }


    public static int getIntItem(String key) {
        return cachedPropertyReader.getIntItem(key);
    }

    public static long getLongItem(String key) {
        return cachedPropertyReader.getLongItem(key);
    }

    public static boolean getBooleanItem(String key) {
        return cachedPropertyReader.getBooleanItem(key);
    }

    public static long getLongItem(String key, String defaultValue) {
        return cachedPropertyReader.getLongItem(key, defaultValue);
    }

    public static int getIntItem(String key, String defaultValue) {
        return cachedPropertyReader.getIntItem(key, defaultValue);
    }

    public static int getIntItem(String key, int defaultValue) {
        return cachedPropertyReader.getIntItem(key, defaultValue);
    }

    public static boolean getBooleanItem(String key, String defaultValue) {
        return cachedPropertyReader.getBooleanItem(key, defaultValue);
    }


    public static String getRealTimeItem(String s) {
        return null;
    }

    public static boolean getRealBooleanItem(String mail_translation, String aTrue) {
        return false;
    }
}
