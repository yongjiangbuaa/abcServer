package com.geng.core.db;

/**
 * Author: shushenglin
 * Date:   16/1/8 09:37
 */
public class DBConfig {
    public static final String POOL_ACTION_FAIL = "FAIL";
    public static final String POOL_ACTION_BLOCK = "BLOCK";
    public static final String POOL_ACTION_GROW = "GROW";
    public boolean active = false;
    public String driverName = "";
    public String connectionString = "";
    public String userName = "";
    public String password = "";
    public String testSql = "";
    public int maxActiveConnections = 10;
    public int maxIdleConnections = 10;
    public String exhaustedPoolAction = POOL_ACTION_FAIL;
    public int blockTime = 3000;

    public DBConfig() {
    }
}
