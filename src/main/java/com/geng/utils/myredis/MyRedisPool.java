package com.geng.utils.myredis;

import redis.clients.jedis.Jedis;

/**
 * Created by XuZiHui on 2017/3/20.
 */
public abstract class MyRedisPool {

    abstract public Jedis getJedisClient();
}
