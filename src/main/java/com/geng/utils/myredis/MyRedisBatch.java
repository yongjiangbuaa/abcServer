package com.geng.utils.myredis;

import redis.clients.jedis.Jedis;

/**
 * Created by XuZiHui on 2017/3/20.
 */
public interface MyRedisBatch {

    public Object run(Jedis jedis);
}
