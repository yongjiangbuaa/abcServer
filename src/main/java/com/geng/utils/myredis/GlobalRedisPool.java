package com.geng.utils.myredis;

import com.elex.cok.utils.RedisSessionUtil;
import com.elex.cok.utils.properties.PropertyFileReader;
import redis.clients.jedis.Jedis;

/**
 * Created by XuZiHui on 2017/3/20.
 */
public class GlobalRedisPool extends MyRedisPool {
    @Override
    public Jedis getJedisClient() {
        Jedis jedis;
        if (PropertyFileReader.getBooleanItem("global.redis.pool", "true")) {
            jedis = RedisSessionUtil.getInstance().getGlobalResource();
        } else {
            jedis = new Jedis(MyRedisConfig.GLOBAL_REDIS_IP, MyRedisConfig.GLOBAL_REDIS_PORT, MyRedisConfig.CROSS_REDIS_TIMEOUT);
        }
        return jedis;
    }
}
