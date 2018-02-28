package com.geng.core.db;

import com.geng.core.data.ISFSArray;
import com.geng.core.data.SFSArray;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDriver;
import org.apache.commons.pool.KeyedObjectPoolFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;

import java.sql.*;

/**
 * Author: shushenglin
 * Date:   16/1/8 09:44
 */
public class SFSDBManager extends AbstractDBManager {
    private static final String JDBC_APACHE_COMMONS_DBCP = "jdbc:apache:commons:dbcp:";

    public SFSDBManager(DBConfig config) {
        super(config);
    }

    public void init(Object o) {
        super.init(o);
        if (this.config.active) {
            try {
                this.setupDriver();
                this.active = true;
                if (this.config.testSql != null && this.config.testSql.length() > 0) {
                    this.log.info(this.config.testSql);
                    this.testSQLStatement();
                }
            } catch (Exception ex) {
                this.log.error("The initialization of the DBManager has failed.\n" +
                        "if the database driver is not \'seen\' int the server classpath the setup fails.\n" +
                        "Make sure to deploy the driver .jar file in the extensions/__lib__/ folder and restart the Server.", ex);
            }

        }
    }

    public void destroy(Object o) {
        super.destroy(o);

        try {
            PoolingDriver driver = (PoolingDriver) DriverManager.getDriver(JDBC_APACHE_COMMONS_DBCP);
            driver.closePool(this.name);
        } catch (SQLException ex) {
            this.log.warn(String.format("Failed shutting down DBManager: %s, Reason: %s", this.name, ex.toString()));
        }

    }

    public Connection getConnection() throws SQLException {
        this.checkState();
        return DriverManager.getConnection(JDBC_APACHE_COMMONS_DBCP + this.name);
    }

    public ISFSArray executeQuery(String sql) throws SQLException {
        return this.executeQuery(sql, (Object[]) null);
    }

    public ISFSArray executeQuery(String sql, Object[] params) throws SQLException {
        this.checkState();
        SFSArray sfsa = null;
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement(sql);
            if (params != null) {
                int index = 1;
                for (Object o : params) {
                    stmt.setObject(index++, o);
                }
            }
            if (this.log.isDebugEnabled()) {
                this.log.debug("ExecuteQuery SQL: " + stmt.toString());
            }
            ResultSet result = stmt.executeQuery();
            if (result != null) {
                sfsa = SFSArray.newFromResultSet(result);
            }
        } finally {
            if (stmt != null) {
                stmt.close();
            }
            if (conn != null) {
                conn.close();
            }

        }

        return sfsa;
    }

    public void executeUpdate(String sql) throws SQLException {
        this.executeUpdate(sql, (Object[]) null);
    }

    public void executeUpdate(String sql, Object[] params) throws SQLException {
        this.checkState();
        Connection conn = null;
        PreparedStatement stmt = null;
        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement(sql);
            if (params != null) {
                int index = 1;
                for (Object o : params) {
                    stmt.setObject(index++, o);
                }
            }
            if (this.log.isDebugEnabled()) {
                this.log.debug("ExecuteUpdate SQL: " + stmt.toString());
            }
            stmt.executeUpdate();
        } finally {
            if (stmt != null) {
                stmt.close();
            }

            if (conn != null) {
                conn.close();
            }

        }

    }

    public Object executeInsert(String sql, Object[] params) throws SQLException {
        this.checkState();
        Connection conn = null;
        PreparedStatement stmt = null;
        Object id = null;

        try {
            conn = this.getConnection();
            stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            if (params != null) {
                int generatedKeys = 1;
                for (Object o : params) {
                    stmt.setObject(generatedKeys++, o);
                }
            }
            if (this.log.isDebugEnabled()) {
                this.log.debug("ExecuteInsert SQL: " + stmt.toString());
            }
            stmt.executeUpdate();
            ResultSet result = stmt.getGeneratedKeys();
            if (!result.next()) {
                throw new SQLException("INSERT failed: " + stmt.toString());
            }
            id = result.getObject(1);
        } finally {
            if (stmt != null) {
                stmt.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
        return id;
    }

    public int getActiveConnections() {
        if (!this.isActive()) {
            return 0;
        } else {
            int value = -1;
            try {
                PoolingDriver driver = (PoolingDriver) DriverManager.getDriver(JDBC_APACHE_COMMONS_DBCP);
                ObjectPool e = driver.getConnectionPool(this.name);
                value = e.getNumActive();
            } catch (SQLException var4) {
                this.log.info(var4.toString());
            }
            return value;
        }
    }

    public int getIdleConnections() {
        if (!this.isActive()) {
            return 0;
        } else {
            int value = -1;
            try {
                PoolingDriver driver = (PoolingDriver) DriverManager.getDriver(JDBC_APACHE_COMMONS_DBCP);
                ObjectPool e = driver.getConnectionPool(this.name);
                value = e.getNumIdle();
            } catch (SQLException ex) {
                this.log.info(ex.toString());
            }
            return value;
        }
    }

    private void checkState() throws SQLException {
        if (!this.active) {
            throw new SQLException("The DBManager is NOT active. SQL Query failed. Please activate it the DBManager");
        }
    }

    private void setupDriver() throws Exception {
        Class.forName(this.config.driverName);
        GenericObjectPool.Config cfg = new GenericObjectPool.Config();
        cfg.maxActive = this.config.maxActiveConnections;
        cfg.maxIdle = this.config.maxIdleConnections;
        cfg.testOnBorrow = true;
        if (this.config.exhaustedPoolAction.equalsIgnoreCase(DBConfig.POOL_ACTION_GROW)) {
            cfg.whenExhaustedAction = 2;
        } else if (this.config.exhaustedPoolAction.equalsIgnoreCase(DBConfig.POOL_ACTION_FAIL)) {
            cfg.whenExhaustedAction = 0;
        } else if (this.config.exhaustedPoolAction.equalsIgnoreCase(DBConfig.POOL_ACTION_BLOCK)) {
            cfg.whenExhaustedAction = 1;
            cfg.maxWait = (long) this.config.blockTime;
        }

        GenericObjectPool connectionPool = new GenericObjectPool((PoolableObjectFactory) null, cfg);
        DriverManagerConnectionFactory connectionFactory = new DriverManagerConnectionFactory(this.config.connectionString, this.config.userName, this.config.password);
        new PoolableConnectionFactory(connectionFactory, connectionPool, (KeyedObjectPoolFactory) null, this.config.testSql, false, true);
        PoolingDriver driver = new PoolingDriver();
        driver.registerPool(this.name, connectionPool);
    }

    private void testSQLStatement() {
        try {
            this.executeQuery(this.config.testSql);
        } catch (SQLException ex) {
            this.log.error("The DBManager Test SQL failed", ex);
        }
    }
}