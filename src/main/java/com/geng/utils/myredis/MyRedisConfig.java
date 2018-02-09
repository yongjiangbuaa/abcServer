package com.geng.utils.myredis;

import com.geng.utils.properties.PropertyFileReader;

/**
 * Created by XuZiHui on 2017/3/20.
 */
public class MyRedisConfig {

    public static final String GLOBAL_REDIS_IP = PropertyFileReader.getItem("global.redis.ip");
    public static final String GLOBAL_SLAVE_REDIS_IP = PropertyFileReader.getItem("global.slave.redis.ip");
    public static final int GLOBAL_REDIS_PORT = PropertyFileReader.getIntItem("global.redis.port", 6379);

    public static final String GLOBAL_SECOND_REDIS_IP = PropertyFileReader.getItem("global.second.redis.ip");
    public static final int GLOBAL_SECOND_REDIS_PORT = PropertyFileReader.getIntItem("global.second.redis.port", 6380);
    public static final int CROSS_REDIS_TIMEOUT = PropertyFileReader.getIntItem("cross.redis.timeout", 5000);
    public static final int LOCAL_REDIS_TIMEOUT = PropertyFileReader.getIntItem("local.redis.timeout", 5000);
    public static final int REDIS_PORT = PropertyFileReader.getIntItem("redis.port", "6379");
}
