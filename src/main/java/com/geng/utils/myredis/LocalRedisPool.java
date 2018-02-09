package com.geng.utils.myredis;

import com.elex.cok.utils.RedisSessionUtil;
import redis.clients.jedis.Jedis;

/**
 * Created by XuZiHui on 2017/3/20.
 */
public class LocalRedisPool extends MyRedisPool {

    @Override
    public Jedis getJedisClient() {
        return RedisSessionUtil.getInstance().getResource();
    }
}
