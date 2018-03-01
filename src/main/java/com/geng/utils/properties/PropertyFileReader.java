package com.geng.utils.properties;

import com.geng.exceptions.ExceptionMonitorType;
import com.geng.utils.COKLoggerFactory;
import com.geng.core.data.ISFSObject;

import java.util.Map;

public class PropertyFileReader {
    private static final IPropertyReader cachedPropertyReader = new CachedProperReader();
    private static final IPropertyReader realTimePropertyReader = new RealTimePropertyReader();

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

/*    public static String getRealTimeItem(String key, String defaultValue) {
        return realTimePropertyReader.getItem(key, defaultValue);
    }

    public static String getRealTimeItem(String key) {
        return realTimePropertyReader.getItem(key);
    }

    public static void setRealTimeItems(ISFSObject sfsObj, Map<String, String> keyRetMap) {
        Map<String, String> map = realTimePropertyReader.getBatchItems(keyRetMap.keySet());
        for(Map.Entry<String, String> entry : map.entrySet()) {
            if(entry.getValue() == null) {
                continue;
            }
            sfsObj.putUtfString(keyRetMap.get(entry.getKey()), entry.getValue());
        }
    }

    public static void setRealTimeIntItems(ISFSObject sfsObj, Map<String, String> keyRetMap) {
        Map<String, String> map = realTimePropertyReader.getBatchItems(keyRetMap.keySet());
        for(Map.Entry<String, String> entry : map.entrySet()) {
            if(entry.getValue() == null) {
                continue;
            }
            try {
                sfsObj.putInt(keyRetMap.get(entry.getKey()), Integer.parseInt(entry.getValue()));
            } catch (NumberFormatException e) {
                String msg = String.format("config properties key %s to int", entry.getKey());
                COKLoggerFactory.monitorException(msg, ExceptionMonitorType.FAULT, COKLoggerFactory.ExceptionOwner.COMMON);
            }
        }
    }

    public static int getRealTimeIntItem(String key, String defaultValue) {
        return realTimePropertyReader.getIntItem(key, defaultValue);
    }

    public static boolean getRealBooleanItem(String key, String defaultValue) {
        return realTimePropertyReader.getBooleanItem(key, defaultValue);
    }*/
}
