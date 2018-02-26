package com.geng.utils.properties;

import com.geng.server.GameEngine;
import com.geng.utils.Constants;
import com.geng.utils.myredis.R;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by lifangkai on 15/1/16.
 */
public class RealTimePropertyReader extends IPropertyReader {
    private static final String PREFIX = "property" + Constants.SERVER_ID + "_";
    private static final String REAL_TIME_PREFIX = PREFIX +  Constants.PROPERTY_REAL_TIME_FLAG + "_";
    private static final Logger logger = LoggerFactory.getLogger(RealTimePropertyReader.class);

    @Override
    public String getItem(String key, String defaultValue) {
//		new RedisSession()
        String value = R.Local().get(REAL_TIME_PREFIX + key);
        if(value == null) {
            value = defaultValue;
        }
        return value;
    }

    @Override
    public Map<String, String> getBatchItems(Set<String> keys) {
//		new RedisSession()
		return R.Local().mget(keys, true, REAL_TIME_PREFIX, false);
    }

    public static void flushRealTimeProperties() {
		Properties properties = GameEngine.getInstance().getConfigProperties();
		Map<String, String> realProperties = new HashMap<>();
		for (Enumeration<?> e = properties.propertyNames(); e.hasMoreElements(); ) {
			String key = (String) e.nextElement();
			if (key.startsWith(Constants.PROPERTY_REAL_TIME_FLAG)) {
				String value = properties.getProperty(key);
				if (StringUtils.isBlank(value)) {
					logger.error("Real time {} is null", key);
				} else {
					realProperties.put(PREFIX + key, value);
					logger.info("Real time {} flushed into Redis values {}", key, value);
				}
			}
		}
		if (realProperties.size() > 0) {
//			new RedisSession()
			R.Local().mset(realProperties);
		}
	}

}
