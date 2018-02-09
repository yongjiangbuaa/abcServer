/**
 * @author: lifangkai@elex-tech.com
 * @date: 2013年9月2日 下午8:17:50
 */
package com.geng.utils;

import com.geng.utils.myredis.MyRedisConfig;
import com.geng.utils.properties.PropertyFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import static com.geng.utils.properties.PropertyFileReader.*;

/**
 * Jedis session pool
 */
public class RedisSessionUtil {
    private static final Logger logger = LoggerFactory.getLogger(RedisSessionUtil.class);

    private static final String CODIS_ZK_PATH = "/zk/codis/db_%s/proxy";

    private JedisPool jedisPool = null;
//    private JedisResourcePool roundRobinGlobalPool = null;
    private JedisPool globalJedisPool = null;
	/** 持久化的全局 Redis */
    private JedisPool globalPersistPool = null;
    private JedisPool globalSecondJedisPool = null;

    private RedisSessionUtil() {
        initPool();
        initGlobalPool();
        initGlobalSecondPool();
//        initRoundRobinGlobalPool();
		initGlobalPersistPool();
    }

    private void initGlobalPool() {
        if (PropertyFileReader.getBooleanItem("global.redis.pool", "true") == false) {
            return;
        }
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(getIntItem("codis_maxActive"));
        poolConfig.setMaxIdle(getIntItem("codis_maxIdle"));
        poolConfig.setMinIdle(getIntItem("codis_minIdle",4));
        poolConfig.setMaxWaitMillis(getLongItem("codis_maxWait"));
        poolConfig.setTestOnBorrow(getBooleanItem("redis.pool.testOnBorrow"));
        poolConfig.setTestOnReturn(getBooleanItem("redis.pool.testOnReturn"));
        globalJedisPool = new JedisPool(poolConfig, getItem("global.redis.ip"), getIntItem("global.redis.port"));
    }

    private void initGlobalSecondPool(){
        if (PropertyFileReader.getBooleanItem("global.redis.pool", "true") == false) {
            return;
        }

        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(getIntItem("second_global_codis_maxActive"));
        poolConfig.setMaxIdle(getIntItem("second_global_codis_maxIdle"));
        poolConfig.setMinIdle(getIntItem("second_global_codis_minIdle",4));
        poolConfig.setMaxWaitMillis(getLongItem("codis_maxWait"));
        poolConfig.setTestOnBorrow(getBooleanItem("redis.pool.testOnBorrow"));
        poolConfig.setTestOnReturn(getBooleanItem("redis.pool.testOnReturn"));
        globalSecondJedisPool = new JedisPool(poolConfig, getItem("global.second.redis.ip"), getIntItem("global.second.redis.port"));
    }

//    private void initRoundRobinGlobalPool() {
//        try {
//            String zkConnect = getItem("zk_connect");
//            if(StringUtils.isEmpty(zkConnect)){
//                // 没有配置zookeeper的时候，禁用全局的redis pool
//                return;
//            }
//            int zkSessionTimeout = getIntItem("zk_session_timeout", 30000);
//            String zkPath = String.format(CODIS_ZK_PATH, getItem("codis_product", "cok-global"));
//
//            JedisPoolConfig poolConfig = new JedisPoolConfig();
//            poolConfig.setMaxTotal(getIntItem("codis_maxActive"));
//            poolConfig.setMaxIdle(getIntItem("codis_maxIdle"));
//            poolConfig.setMaxWaitMillis(getLongItem("codis_maxWait"));
//
//            roundRobinGlobalPool = new RoundRobinJedisPool(zkConnect, zkSessionTimeout, zkPath, poolConfig);
//
//            logger.info("init codis successfully");
//        } catch (Exception e) {
//            logger.error("init codis", e);
//        }
//    }

    public Jedis getResource() {
        Jedis jedis=jedisPool.getResource();

        return jedis;
        //return jedisPool.getResource();
    }

    public Jedis getGlobalResource() {
//        if(roundRobinGlobalPool == null){
            return globalJedisPool.getResource();
//        }
//        return roundRobinGlobalPool.getResource();
    }
	
	public JedisPool getPersistPool() {
    	return globalPersistPool;
    }

    public Jedis getGlobalSecondJedisPool(){
        return globalSecondJedisPool.getResource();
    }
    private void initPool() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(getIntItem("redis.pool.maxActive", 8));
        poolConfig.setMaxIdle(getIntItem("redis.pool.maxIdle", 16));
        poolConfig.setMinIdle(getIntItem("redis.pool.minIdle",4));
        poolConfig.setMaxWaitMillis(getLongItem("redis.pool.maxWait", "0"));
        poolConfig.setTestOnBorrow(getBooleanItem("redis.pool.testOnBorrow","false"));
        poolConfig.setTestOnReturn(getBooleanItem("redis.pool.testOnReturn", "false"));
        jedisPool = new JedisPool(poolConfig,
                getItem("redis.ip","127.0.0.1"),
                getIntItem("redis.port","6379"),
                MyRedisConfig.LOCAL_REDIS_TIMEOUT,
                null,
                getIntItem("redis.db","0"));
    }
	private void initGlobalPersistPool() {
    	if(getItem("persist.redis.ip").equals("")){
    		logger.warn("no [persist.redis] config in extension/COK{}/config.properties. use global redis", Constants.SERVER_ID);
            globalPersistPool = globalJedisPool;
    		return;
    	}
    	JedisPoolConfig poolConfig = new JedisPoolConfig();
    	poolConfig.setMaxTotal(getIntItem("persist.redis.pool.maxActive", 10));
    	poolConfig.setMaxIdle(getIntItem("persist.redis.pool.maxIdle", 5));
    	poolConfig.setMaxWaitMillis(getLongItem("persist.redis.pool.maxWait", "5000"));
    	poolConfig.setTestOnBorrow(getBooleanItem("persist.redis.pool.testOnBorrow", "false"));
    	poolConfig.setTestOnBorrow(getBooleanItem("persist.redis.pool.testOnReturn", "false"));
    	globalPersistPool = new JedisPool(poolConfig, getItem("persist.redis.ip"), getIntItem("persist.redis.port"));
    }

    private static class LazyHolder {
        private static final RedisSessionUtil instance = new RedisSessionUtil();
    }

    public static RedisSessionUtil getInstance() {
        return LazyHolder.instance;
    }
}
