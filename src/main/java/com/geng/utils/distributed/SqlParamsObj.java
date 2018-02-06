package com.geng.utils.distributed;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by lifangkai on 15/3/30.
 */
public class SqlParamsObj {
    private String sql;
    private Object[] params;

    public SqlParamsObj(String sql, Object[] objects) {
        this.sql = sql;
        params = objects;
    }

    public PreparedStatement createStatement(Connection connection) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(sql);
        if (params != null) {
            int index = 1;
            for (Object o : params) {
                stmt.setObject(index++, o);
            }
        }
        return stmt;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\"").append(sql).append("\"");
        sb.append(" with ");
        for(Object obj : params) {
            sb.append(obj).append("#");
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }
}
