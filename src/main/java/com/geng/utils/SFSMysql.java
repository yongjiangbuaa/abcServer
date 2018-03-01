/**
 * @author: lifangkai@elex-tech.com
 * @date: 2013年9月4日 下午3:00:22
 */
package com.geng.utils;

import com.geng.exceptions.ExceptionMonitorType;
import com.geng.utils.distributed.SqlParamsObj;
import com.geng.utils.properties.PropertyFileReader;
import com.geng.utils.xml.GameConfigManager;
import com.google.common.base.Optional;
import com.geng.core.db.DBConfig;
import com.geng.core.db.IDBManager;
import com.geng.core.db.SFSDBManager;
import com.geng.core.Zone;
import com.geng.core.data.ISFSArray;
import com.geng.core.data.SFSArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mysql数据库操作接口
 */
public class SFSMysql {
    private static final Logger logger = LoggerFactory.getLogger(SFSMysql.class);
    private static final String JDBC_URL = "jdbc:mysql://%s:3306/%s";
    private static SFSMysql instance = new SFSMysql();
    private IDBManager dbManager;
    private IDBManager globalDBManager;
    private IDBManager payDBManager;
    private ConcurrentHashMap<Integer, JdbcTemplate> serverJdbcTemplateMap;

    private SFSMysql() {
        serverJdbcTemplateMap = new ConcurrentHashMap<>();
    }

    public void init(Zone zone) {
        initLocalDBManager();
        initGlobalDBManager(zone);
        initPayDBManager(zone);
        logger.info("database is ready");
    }

    public static SFSMysql getInstance() {
        return instance;
    }

    /**
     * 获取数据库连接，方便灵活使用
     *
     * @return
     */
    public Optional<Connection> getConnection() {
        try {
            Connection connection = dbManager.getConnection();
            return Optional.of(connection);
        } catch (SQLException e) {
            COKLoggerFactory.monitorException("can't get connection from local db", ExceptionMonitorType.LOCAL_DB, COKLoggerFactory.ExceptionOwner.COMMON, e);
            return Optional.absent();
        }
    }

	public Optional<Connection> getGlobalConnection() {
		try {
			Connection connection = globalDBManager.getConnection();
			return Optional.of(connection);
		} catch (SQLException e) {
            COKLoggerFactory.monitorException("can't get connection from global db", ExceptionMonitorType.GLOBAL_DB, COKLoggerFactory.ExceptionOwner.COMMON, e);
			return Optional.absent();
		}
	}

    /**
     * 有事务的批量执行
     *
     * @param isAutoCommit
     * @param sqlList
     */
    public boolean executeBatch(boolean isAutoCommit, List<String> sqlList) {
        boolean flag = false;
        Optional<Connection> connectionOptional = getConnection();
        if (connectionOptional.isPresent()) {
            Connection connection = connectionOptional.get();
            Statement stmt = null;
            try {
                connection.setAutoCommit(isAutoCommit);
                stmt = connection.createStatement();
                for (String sql : sqlList) {
                    stmt.addBatch(sql);
                }
                stmt.executeBatch();
                if (!isAutoCommit) {
                    connection.commit();
                    connection.setAutoCommit(true);
                }
                flag = true;
            } catch (Exception e) {
                try {
                    connection.rollback();
                    COKLoggerFactory.monitorException(String.format("execute batch %s", sqlList.toString()), ExceptionMonitorType.LOCAL_DB, COKLoggerFactory.ExceptionOwner.COMMON, e);
                } catch (SQLException e1) {
                    COKLoggerFactory.monitorException(String.format("rollback batch %s", sqlList.toString()), ExceptionMonitorType.LOCAL_DB, COKLoggerFactory.ExceptionOwner.COMMON, e);
                }
            } finally {
                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (SQLException e) {
                        COKLoggerFactory.monitorException(String.format("close stmt %s", sqlList.toString()), ExceptionMonitorType.LOCAL_DB, COKLoggerFactory.ExceptionOwner.COMMON, e);
                    }
                }
                try {
                    connection.close();
                } catch (SQLException e) {
                    COKLoggerFactory.monitorException(String.format("close connection %s", sqlList.toString()), ExceptionMonitorType.LOCAL_DB, COKLoggerFactory.ExceptionOwner.COMMON, e);
                }
            }
        }
        return flag;
    }

    /**
     * 有事务的批量执行
     */
    public boolean executeUpdateWithTransaction(List<SqlParamsObj> sqlParamsObjList) {
        boolean flag = false;
        Optional<Connection> connectionOptional;
        List<PreparedStatement> preparedStatements = new ArrayList<>(sqlParamsObjList.size());
        Connection connection = null;
        try {
            connectionOptional = getConnection();
            if (connectionOptional.isPresent()) {
                connection = connectionOptional.get();
                connection.setAutoCommit(false);
                for (SqlParamsObj sqlParamsObj : sqlParamsObjList) {
                    PreparedStatement stmt = sqlParamsObj.createStatement(connection);
                    preparedStatements.add(stmt);
                    stmt.executeUpdate();
                }
                connection.commit();
                connection.setAutoCommit(true);
                flag = true;
            }
        } catch (Exception e) {
            try {
                if(connection != null) {
                    connection.rollback();
                }
                COKLoggerFactory.monitorException(String.format("transaction execute batch %s", sqlParamsObjList.toString()), ExceptionMonitorType.GLOBAL_DB, COKLoggerFactory.ExceptionOwner.COMMON, e);
            } catch (SQLException e1) {
                COKLoggerFactory.monitorException(String.format("transaction rollback batch %s", sqlParamsObjList.toString()), ExceptionMonitorType.GLOBAL_DB, COKLoggerFactory.ExceptionOwner.COMMON, e);
            }
        } finally {
            for (PreparedStatement stmt : preparedStatements) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    COKLoggerFactory.monitorException(String.format("close stmt %s", stmt.toString()), ExceptionMonitorType.GLOBAL_DB, COKLoggerFactory.ExceptionOwner.COMMON, e);
                }
            }
            try {
                if(connection != null) {
                    connection.close();
                }
            } catch (SQLException e) {
                COKLoggerFactory.monitorException(String.format("close connection %s", sqlParamsObjList.toString()), ExceptionMonitorType.GLOBAL_DB, COKLoggerFactory.ExceptionOwner.COMMON, e);
            }
        }
        return flag;
    }

    /**
     * 有事务的批量执行
     *
     * @param isAutoCommit
     * @param sqlList
     */
    public boolean executeGlobalBatch(boolean isAutoCommit, List<String> sqlList) {
        boolean flag = false;
        Optional<Connection> connectionOptional = getGlobalConnection();
        if (connectionOptional.isPresent()) {
            Connection connection = connectionOptional.get();
            Statement stmt = null;
            try {
                connection.setAutoCommit(isAutoCommit);
                stmt = connection.createStatement();
                for (String sql : sqlList) {
                    logger.info(sql);
                    stmt.addBatch(sql);
                }
                stmt.executeBatch();
                if (!isAutoCommit) {
                    connection.commit();
                    connection.setAutoCommit(true);
                }
                flag = true;
            } catch (Exception e) {
                try {
                    connection.rollback();
                    COKLoggerFactory.monitorException(String.format("execute batch %s", sqlList.toString()), ExceptionMonitorType.GLOBAL_DB, COKLoggerFactory.ExceptionOwner.COMMON, e);
                } catch (SQLException e1) {
                    COKLoggerFactory.monitorException(String.format("rollback batch %s", sqlList.toString()), ExceptionMonitorType.GLOBAL_DB, COKLoggerFactory.ExceptionOwner.COMMON, e);
                }
            } finally {
                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (SQLException e) {
                        COKLoggerFactory.monitorException(String.format("close stmt %s", sqlList.toString()), ExceptionMonitorType.GLOBAL_DB, COKLoggerFactory.ExceptionOwner.COMMON, e);
                    }
                }
                try {
                    connection.close();
                } catch (SQLException e) {
                    COKLoggerFactory.monitorException(String.format("close connection %s", sqlList.toString()), ExceptionMonitorType.GLOBAL_DB, COKLoggerFactory.ExceptionOwner.COMMON, e);
                }
            }
        }
        return flag;
    }

    /**
     * 有事务的批量执行
     */
    public boolean executeUpdateGlobalWithTransaction(List<SqlParamsObj> sqlParamsObjList) {
        boolean flag = false;
        Optional<Connection> connectionOptional = getGlobalConnection();
        List<PreparedStatement> preparedStatements;
        if (connectionOptional.isPresent()) {
            Connection connection = connectionOptional.get();
            preparedStatements = new ArrayList<>();
            try {
                connection.setAutoCommit(false);
                for (SqlParamsObj sqlParamsObj : sqlParamsObjList) {
                    PreparedStatement stmt = sqlParamsObj.createStatement(connection);
                    preparedStatements.add(stmt);
                    stmt.executeUpdate();
                }
                connection.commit();
                connection.setAutoCommit(true);
                flag = true;
            } catch (Exception e) {
                try {
                    connection.rollback();
                    COKLoggerFactory.monitorException(String.format("transaction execute batch %s", sqlParamsObjList.toString()), ExceptionMonitorType.GLOBAL_DB, COKLoggerFactory.ExceptionOwner.COMMON, e);
                } catch (SQLException e1) {
                    COKLoggerFactory.monitorException(String.format("transaction rollback batch %s", sqlParamsObjList.toString()), ExceptionMonitorType.GLOBAL_DB, COKLoggerFactory.ExceptionOwner.COMMON, e);
                }
            } finally {
                if (preparedStatements != null) {
                    for (PreparedStatement stmt : preparedStatements) {
                        try {
                            stmt.close();
                        } catch (SQLException e) {
                            COKLoggerFactory.monitorException(String.format("close stmt %s", stmt.toString()), ExceptionMonitorType.GLOBAL_DB, COKLoggerFactory.ExceptionOwner.COMMON, e);
                        }
                    }
                }
                try {
                    connection.close();
                } catch (SQLException e) {
                    COKLoggerFactory.monitorException(String.format("close connection %s", sqlParamsObjList.toString()), ExceptionMonitorType.GLOBAL_DB, COKLoggerFactory.ExceptionOwner.COMMON, e);
                }
            }
        }
        return flag;
    }

    /**
     * 本服查询
     *
     * @param sql
     * @param params
     * @return
     */
    public ISFSArray query(String sql, Object[] params) {
        ISFSArray retArray = new SFSArray();
        try {
            retArray = dbManager.executeQuery(sql, params);
        } catch (SQLException e) {
            String msg = String.format("query \"%s\" with %s", sql, CommonUtils.toString(params));
            COKLoggerFactory.monitorException(msg, ExceptionMonitorType.LOCAL_DB, COKLoggerFactory.ExceptionOwner.COMMON, e);
        }
        return retArray;
    }

    public ISFSArray query(String sql) {
        return query(sql, new Object[]{});
    }

    /**
     * 本服插入更新
     *
     * @param sql
     * @param params
     */
    public void execute(String sql, Object[] params) {
        try {
            dbManager.executeUpdate(sql, params);
        } catch (SQLException e) {
            String msg = String.format("execute \"%s\" with %s", sql, CommonUtils.toString(params));
            COKLoggerFactory.monitorException(msg, ExceptionMonitorType.LOCAL_DB, COKLoggerFactory.ExceptionOwner.COMMON, e);
        }
    }

    public void executeWithException(String sql, Object[] params) throws SQLException {
        dbManager.executeUpdate(sql, params);
    }

    public void execute(String sql) {
        execute(sql, new Object[]{});
    }

	/**
	 * 使用MySQL的insert ... values ()... ON DUPLICATE KEY UPDATE ... 来进行批量插入更新
	 * @param tableName
	 * @param fieldList
	 * @param values
	 * @param updateFields
	 * @param batchCount
	 */
    public static void insertBatchWithUpdate(String tableName, List<String> fieldList, List<String> values, List<String> updateFields, int batchCount) {
		StringBuilder sqlBuilder = new StringBuilder(512);
		sqlBuilder.append("insert into ").append(tableName).append("(").append(fieldList.get(0));
		for (int i = 1; i < fieldList.size(); i++) {
			sqlBuilder.append(",").append(fieldList.get(i));
		}
		sqlBuilder.append(") values ");
		String preSql = sqlBuilder.toString();
		sqlBuilder.setLength(0);
		sqlBuilder.append(" ON DUPLICATE KEY UPDATE ");
		for (String f : updateFields){
			sqlBuilder.append(f).append("=VALUES(").append(f).append("),");
		}
		sqlBuilder.setLength(sqlBuilder.length() - 1);
		String updateSQL = sqlBuilder.toString();

		if (!values.isEmpty()) {
			StringBuilder sb = new StringBuilder(1024);
			sb.append(preSql);
			for (int i = 0; i < values.size(); i++) {
				sb.append(values.get(i));
				if ( i >= batchCount && i % batchCount == 0) {
					sb.append(updateSQL);
					SFSMysql.getInstance().execute(sb.toString());
					sb.setLength(0);
					sb.append(preSql);
				} else {
					sb.append(",");
				}
			}
			if (sb.length() > preSql.length()) {
				sb.setLength(sb.length() - 1);
				sb.append(updateSQL);
				String remainSql = sb.toString();
				SFSMysql.getInstance().execute(remainSql);
			}
		}
	}

    /**
     * 批量插入
     *
     * @param values
     * @param batchCount
     */
    public static void insertBatch(String tableName, List<String> fieldList, List<String> values, int batchCount) {
        StringBuilder sqlBuilder = new StringBuilder(512);
        sqlBuilder.append("insert into ").append(tableName).append("(").append(fieldList.get(0));
        for (int i = 1; i < fieldList.size(); i++) {
            sqlBuilder.append(",").append(fieldList.get(i));
        }
        sqlBuilder.append(") values ");
        String preSql = sqlBuilder.toString();
        if (!values.isEmpty()) {
            StringBuilder sb = new StringBuilder(preSql);
            for (int i = 0; i < values.size(); i++) {
                sb.append(values.get(i));
                if ((i + 1) % batchCount == 0) {
                    SFSMysql.getInstance().execute(sb.toString());
                    sb.delete(0, sb.length());
                    sb.append(preSql);
                } else {
                    sb.append(",");
                }
            }
            if (sb.length() > preSql.length()) {
                String remainSql = sb.toString();
                SFSMysql.getInstance().execute(remainSql.substring(0, remainSql.length() - 1));
            }
        }
    }

    /**
     * 返回批量插入的sql列表
     *
     * @param values
     * @param batchCount
     */
    public static List<String> generateInsertBatchSqlList(String tableName, List<String> fieldList, List<String> values, int batchCount) {
        List<String> sqlList = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder("insert into ");
        sqlBuilder.append(tableName).append("(").append(fieldList.get(0));
        for (int i = 1; i < fieldList.size(); i++) {
            sqlBuilder.append(",").append(fieldList.get(i));
        }
        sqlBuilder.append(") values ");
        String preSql = sqlBuilder.toString();
        if (!values.isEmpty()) {
            StringBuilder sb = new StringBuilder(preSql);
            for (int i = 0; i < values.size(); i++) {
                sb.append(values.get(i));
                if ((i + 1) % batchCount == 0) {
                    sqlList.add(sb.toString());
                    sb.delete(0, sb.length());
                    sb.append(preSql);
                } else {
                    sb.append(",");
                }
            }
            if (sb.length() > preSql.length()) {
                String remainSql = sb.toString();
                sqlList.add(remainSql.substring(0, remainSql.length() - 1));
            }
        }
        return sqlList;
    }

    /**
     * 全局查询
     *
     * @param sql
     * @param params
     * @return
     */
    public ISFSArray queryGlobal(String sql, Object[] params) {
        ISFSArray retArray = new SFSArray();
        try {
            retArray = globalDBManager.executeQuery(sql, params);
        } catch (SQLException e) {
            String msg = String.format("global db, query \"%s\" with %s", sql, CommonUtils.toString(params));
            COKLoggerFactory.monitorException(msg, ExceptionMonitorType.GLOBAL_DB, COKLoggerFactory.ExceptionOwner.COMMON, e);
        }
        return retArray;
    }

    /**
     * 全局执行
     *
     * @param sql
     * @param params
     */
    public boolean executeGlobal(String sql, Object[] params) {
        try {
            globalDBManager.executeUpdate(sql, params);
            return true;
        } catch (SQLException e) {
            String msg = String.format("global db, execute \"%s\" with %s", sql, CommonUtils.toString(params));
            COKLoggerFactory.monitorException(msg, ExceptionMonitorType.GLOBAL_DB, COKLoggerFactory.ExceptionOwner.COMMON, e);
            return false;
        }
    }

    /**
     * 全局插入
     * @param sql
     * @param params
     * @return
     */
    public Object executeGlobalSave(String sql, Object[] params){
        try {
            Object ret = globalDBManager.executeInsert(sql, params);
            return ret;
        } catch (SQLException e) {
            String msg = String.format("global db, execute \"%s\" with %s", sql, CommonUtils.toString(params));
            COKLoggerFactory.monitorException(msg, ExceptionMonitorType.GLOBAL_DB, COKLoggerFactory.ExceptionOwner.COMMON, e);
            return null;
        }
    }

    public void executeGlobalWithException(String sql, Object[] params) throws SQLException {
        globalDBManager.executeUpdate(sql, params);
    }

    public ISFSArray queryPay(String sql, Object[] params) {
        ISFSArray retArray = new SFSArray();
        try {
            retArray = payDBManager.executeQuery(sql, params);
        } catch (SQLException e) {
            String msg = String.format("pay db, query \"%s\" with %s", sql, CommonUtils.toString(params));
            COKLoggerFactory.monitorException(msg, ExceptionMonitorType.PAY, COKLoggerFactory.ExceptionOwner.COMMON, e);
        }
        return retArray;
    }

    public boolean executePay(String sql, Object[] params) {
        try {
            payDBManager.executeUpdate(sql, params);
            return true;
        } catch (SQLException e) {
            String msg = String.format("pay db, execute \"%s\" with %s", sql, CommonUtils.toString(params));
            COKLoggerFactory.monitorException(msg, ExceptionMonitorType.PAY, COKLoggerFactory.ExceptionOwner.COMMON, e);
            return false;
        }
    }

    public void executePayWithException(String sql, Object[] params) throws SQLException {
        payDBManager.executeUpdate(sql, params);
    }

    /**
     * 跨服数据库连接
     *
     * @param serverId
     * @return
     */
    private JdbcTemplate getJdbcTemplate(int serverId) {
        JdbcTemplate jdbcTemplate;
        if (serverJdbcTemplateMap.containsKey(serverId)) {
            jdbcTemplate = serverJdbcTemplateMap.get(serverId);
        } else {
            Map<String, String> serverMap = new GameConfigManager("servers").getItem(Integer.toString(serverId));
            SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
            dataSource.setDriverClass(com.mysql.jdbc.Driver.class);
            dataSource.setUsername(PropertyFileReader.getItem("local.jdbc.user"));
            dataSource.setPassword(PropertyFileReader.getItem("local.jdbc.password"));
            String url = JDBC_URL;
            url = String.format(url, serverMap.get("db_ip"), serverMap.get("db_name"));
            dataSource.setUrl(url);
            jdbcTemplate = new JdbcTemplate(dataSource);
            JdbcTemplate tmp = serverJdbcTemplateMap.putIfAbsent(serverId, jdbcTemplate);
            if (tmp != null) {
                jdbcTemplate = tmp;
            }
        }
        return jdbcTemplate;
    }

    public Map<String, Object> queryCrossServer(int serverId, String sql, Object ... args ) {
        Map<String, Object> map = null;
        try {
            JdbcTemplate jdbcTemplate = getJdbcTemplate(serverId);
            map = jdbcTemplate.queryForMap(sql, args);
            return map;
        } catch (EmptyResultDataAccessException e) {
            return map;
        } catch (Exception e) {
            String msg = String.format("server %d, query \"%s\"", serverId, sql);
            COKLoggerFactory.monitorException(msg, ExceptionMonitorType.CROSS_DB, COKLoggerFactory.ExceptionOwner.COMMON, e);
            return map;
        }
    }

    public Map<String, Object> queryCrossServer(int serverId, String sql) {
        Map<String, Object> map = null;
        try {
            JdbcTemplate jdbcTemplate = getJdbcTemplate(serverId);
            map = jdbcTemplate.queryForMap(sql);
            return map;
        } catch (EmptyResultDataAccessException e) {
            return map;
        } catch (Exception e) {
            String msg = String.format("server %d, query \"%s\"", serverId, sql);
            COKLoggerFactory.monitorException(msg, ExceptionMonitorType.CROSS_DB, COKLoggerFactory.ExceptionOwner.COMMON, e);
            return map;
        }
    }


	/**
     *
     * @deprecated  Please use queryCrossServer(int serverId, String sql) or
     *                  queryCrossServer(int serverId, String sql, Object ... args )
     * @param sql
     * @param serverId
     * @return
     */
    @Deprecated
    public Map<String, Object> queryCrossServer(String sql, int serverId) {
        Map<String, Object> map = new HashMap<>();
        try {
            JdbcTemplate jdbcTemplate = getJdbcTemplate(serverId);
            map = jdbcTemplate.queryForMap(sql);
            return map;
        } catch (EmptyResultDataAccessException e) {
            return map;
        } catch (Exception e) {
            String msg = String.format("server %d, query \"%s\"", serverId, sql);
            COKLoggerFactory.monitorException(msg, ExceptionMonitorType.CROSS_DB, COKLoggerFactory.ExceptionOwner.COMMON, e);
            return map;
        }
    }
    public List<Map<String, Object>> queryListMapCrossServer(int serverId, String sql) {
        JdbcTemplate jdbcTemplate = getJdbcTemplate(serverId);
        return jdbcTemplate.queryForList(sql);
    }

    public List<Map<String, Object>> queryListMapCrossServer(int serverId, String sql, Object... args) {
        JdbcTemplate jdbcTemplate = getJdbcTemplate(serverId);
        return jdbcTemplate.queryForList(sql, args);
    }

    @Deprecated
    public List<Map<String, Object>> queryListMapCrossServer(String sql, int serverId) {
        JdbcTemplate jdbcTemplate = getJdbcTemplate(serverId);
        return jdbcTemplate.queryForList(sql);
    }

    public void updateCrossServer(int serverId, String sql, Object... args) {
        JdbcTemplate jdbcTemplate = getJdbcTemplate(serverId);
        jdbcTemplate.update(sql, args);
    }

    public void executeCrossServer(String sql, int serverId) {
        JdbcTemplate jdbcTemplate = getJdbcTemplate(serverId);
        jdbcTemplate.execute(sql);
    }

    private void initLocalDBManager(){
        DBConfig dbConfig = new DBConfig();
        dbConfig.active = true;
        dbConfig.driverName = "com.mysql.jdbc.Driver";
        logger.debug("local db manager url:" + PropertyFileReader.getItem("local.jdbc.url"));
        dbConfig.connectionString = PropertyFileReader.getItem("local.jdbc.url");
        dbConfig.userName = PropertyFileReader.getItem("local.jdbc.user");
        dbConfig.password = PropertyFileReader.getItem("local.jdbc.password");

        dbConfig.maxActiveConnections = PropertyFileReader.getIntItem("local.poolMaximumActiveConnections");
        dbConfig.maxIdleConnections = PropertyFileReader.getIntItem("local.poolMaximumIdleConnections");
        dbConfig.blockTime = PropertyFileReader.getIntItem("global_db_blockTime");
        dbConfig.exhaustedPoolAction = PropertyFileReader.getItem("global_db_exhaustedPoolAction");
        dbConfig.testSql = "select 1";
        this.dbManager = new SFSDBManager(dbConfig);
        this.dbManager.init(new Zone());
    }

    /**
     * 初始化全局数据库连接
     *
     * @param zone
     */
    private void initGlobalDBManager(Zone zone) {
        DBConfig dbConfig = new DBConfig();
        dbConfig.active = true;
        dbConfig.driverName = "com.mysql.jdbc.Driver";
        dbConfig.connectionString = PropertyFileReader.getItem("global_db_url");
        dbConfig.userName = PropertyFileReader.getItem("global_db_username");
        dbConfig.password = PropertyFileReader.getItem("global_db_password");

        dbConfig.maxActiveConnections = PropertyFileReader.getIntItem("global_db_maxActiveConnections");
        dbConfig.maxIdleConnections = PropertyFileReader.getIntItem("global_db_maxIdleConnections");
        dbConfig.blockTime = PropertyFileReader.getIntItem("global_db_blockTime");
        dbConfig.exhaustedPoolAction = PropertyFileReader.getItem("global_db_exhaustedPoolAction");
        dbConfig.testSql = "select unix_timestamp()";
        globalDBManager = new SFSDBManager(dbConfig);
        globalDBManager.init(zone);
    }

    /**
     * 初始化支付数据库连接
     *
     * @param zone
     */
    private void initPayDBManager(Zone zone) {
        DBConfig dbConfig = new DBConfig();
        dbConfig.active = true;
        dbConfig.driverName = "com.mysql.jdbc.Driver";
        dbConfig.connectionString = PropertyFileReader.getItem("pay_db_url");
        dbConfig.userName = PropertyFileReader.getItem("pay_db_username");
        dbConfig.password = PropertyFileReader.getItem("pay_db_password");

        dbConfig.maxActiveConnections = PropertyFileReader.getIntItem("pay_db_maxActiveConnections");
        dbConfig.maxIdleConnections = PropertyFileReader.getIntItem("pay_db_maxIdleConnections");
        dbConfig.blockTime = PropertyFileReader.getIntItem("pay_db_blockTime");
        dbConfig.exhaustedPoolAction = PropertyFileReader.getItem("pay_db_exhaustedPoolAction");
        dbConfig.testSql = "select unix_timestamp()";
        payDBManager = new SFSDBManager(dbConfig);
        payDBManager.init(zone);
    }

    /**
     * 单元测试方法，线上不可用
     */
    public void initTestDbManager() {
        DBConfig dbConfig = new DBConfig();
        dbConfig.active = true;
        dbConfig.driverName = "com.mysql.jdbc.Driver";
        dbConfig.connectionString = "jdbc:mysql://10.1.6.72:3306/cokdb1?characterEncoding=utf-8&autoReconnect=true";
        dbConfig.userName = PropertyFileReader.getItem("global_db_username");
        dbConfig.password = PropertyFileReader.getItem("global_db_password");

        dbConfig.maxActiveConnections = PropertyFileReader.getIntItem("global_db_maxActiveConnections");
        dbConfig.maxIdleConnections = PropertyFileReader.getIntItem("global_db_maxIdleConnections");
        dbConfig.blockTime = PropertyFileReader.getIntItem("global_db_blockTime");
        dbConfig.exhaustedPoolAction = PropertyFileReader.getItem("global_db_exhaustedPoolAction");
        dbConfig.testSql = "select unix_timestamp()";
        dbManager = new SFSDBManager(dbConfig);
        dbManager.init(null);
        initGlobalDBManager(null);
    }

    public void removeCrossCache(Integer serverId) {
        if(serverId != null && serverJdbcTemplateMap != null) {
            serverJdbcTemplateMap.remove(serverId);
        }
    }
}
