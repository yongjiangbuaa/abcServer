package com.geng.utils.myredis;

import com.geng.utils.xml.GameConfigManager;
import org.apache.commons.lang.StringUtils;
import redis.clients.jedis.Jedis;

import java.util.Map;

/**
 * Created by XuZiHui on 2017/3/20.
 */
public class RemoteRedisPool extends MyRedisPool {
    int serverId = -1;

    public RemoteRedisPool(int serverId) {
        this.serverId = serverId;
    }

    @Override
    public Jedis getJedisClient() {

        Map<String, String> map = new GameConfigManager("servers").getItem(Integer.toString(serverId));

        String ip = map.get("redis_ip");
        if (StringUtils.isBlank(ip)) {
            ip = map.get("inner_ip");
            if (StringUtils.isBlank(ip)) {
                ip = map.get("ip");
            }
        }

        String redisPort = map.get("redis_port");
        int port = MyRedisConfig.REDIS_PORT;
        if (StringUtils.isNotBlank(redisPort)) {
            port = Integer.parseInt(redisPort);
        }
        int db = 0;
        String redisDb = map.get("redis_db");
        if (StringUtils.isNotBlank(redisDb)) {
            db = Integer.parseInt(redisDb);
        }
        Jedis jedis = new Jedis(ip, port, MyRedisConfig.CROSS_REDIS_TIMEOUT);
        if (jedis.getDB() != db) {
            jedis.select(db);
        }
        return jedis;
    }
}
