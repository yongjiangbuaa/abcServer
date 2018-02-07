package com.geng.core.db;

import com.geng.core.data.ISFSArray;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Author: shushenglin
 * Date:   16/1/8 09:38
 */
public interface IDBManager {
    void init(Object config);

    void destroy(Object param);

    String getName();

    void setName(String name);

    boolean isActive();

    DBConfig getConfig();

    Connection getConnection() throws SQLException;

    ISFSArray executeQuery(String var1, Object[] var2) throws SQLException;

    void executeUpdate(String sql, Object[] params) throws SQLException;

    Object executeInsert(String sql, Object[] params) throws SQLException;

}