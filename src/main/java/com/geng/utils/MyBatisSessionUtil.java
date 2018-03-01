package com.geng.utils;

import com.geng.server.GameEngine;
import com.geng.core.GameScheduler;
import com.geng.exceptions.ExceptionMonitorType;
//import com.geng.rpcdao.SqlSessionRPCProxy;
//import com.geng.utils.mybatis.AutoCloseSqlSession;
//import com.geng.utils.mybatis.DefaultAutoCloseSqlSession;
//import com.geng.utils.mybatis.SqlSessionManager;
//import com.geng.utils.mybatis.SqlSessionProxy;
import com.geng.utils.properties.PropertyFileReader;
import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MyBatisSessionUtil {
    public static final Logger logger = LoggerFactory.getLogger(MyBatisSessionUtil.class);
    private static final String LOCAL_MYBATIS_FILE = "mybatis.xml";
    private static final String CROSS_SERVER_MYBATIS_PATH = String.format("%s/mybatis-cross.xml", Constants.GAME_CONFIG_PATH);
    private SqlSessionFactory sqlSessionFactory = null;
    private SqlSessionFactory unpooledSqlSessionFactory;
    private boolean autoCloseLeakSessions = true;
    private Lock lock;
    
    private LoadingCache<Integer, SqlSessionFactory> serverSessionFactoryMap;
//    private SqlSessionManager sqlSessionManager;
	private boolean sessionAutoCommitDefault = true;
	private boolean enableLeakSessionStackCapture = true;
	private boolean useRPCSessionProxy = false;
	private int defaultStatementTimeout = 30;

    private MyBatisSessionUtil() {
		useRPCSessionProxy = PropertyFileReader.getBooleanItem("useRPCSession","false");
		if (useRPCSessionProxy){
			return;
		}
        serverSessionFactoryMap = CacheBuilder.newBuilder()
                .maximumSize(PropertyFileReader.getIntItem("cache_cross_mybatis_max_size", 500))
                .expireAfterAccess(2, TimeUnit.MINUTES)
                .recordStats()
                .build(new CacheLoader<Integer, SqlSessionFactory>() {
                    @Override
                    public SqlSessionFactory load(Integer serverId) {
                        try {
                            ClassLoader origLoader = Thread.currentThread().getContextClassLoader();
                            ClassLoader extensionLoader = getClass().getClassLoader();
                            Thread.currentThread().setContextClassLoader(extensionLoader);
                            Reader reader = getMybatisCrossConfigReader();
                            Properties properties = GameEngine.getInstance().getConfigProperties();
                            SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader, Integer.toString(serverId), properties);
                            setStatementTimeout(sqlSessionFactory, defaultStatementTimeout);
							Thread.currentThread().setContextClassLoader(origLoader);
                            return sqlSessionFactory;
                        } catch (Exception e) {
                            String msg = String.format("init server %d mybatis", Constants.SERVER_ID);
                            COKLoggerFactory.monitorException(msg, ExceptionMonitorType.CROSS_DB, COKLoggerFactory.ExceptionOwner.COMMON, e);
                            return null;
                        }
                    }


                });
        lock = new ReentrantLock();
    }
    private Reader getMybatisCrossConfigReader() throws IOException {
        ClassLoader extensionLoader = getClass().getClassLoader();
        return Resources.getResourceAsReader(extensionLoader, CROSS_SERVER_MYBATIS_PATH);
    }
    public void init() {
		if(useRPCSessionProxy){
			return;
		}
        if (sqlSessionFactory != null) return;
        try {
            ClassLoader origLoader = Thread.currentThread().getContextClassLoader();
            ClassLoader extensionLoader = getClass().getClassLoader();
            Thread.currentThread().setContextClassLoader(extensionLoader);

            Properties properties = GameEngine.getInstance().getConfigProperties();
            properties.setProperty("local.jdbc.url", PropertyFileReader.getItem("local.jdbc.url"));
            if (!properties.containsKey("bonecp.connectionTimeout")) {
                // 设置获取连接的timeout时间
                properties.setProperty("bonecp.connectionTimeout", "3000");
            }

            InputStream in = getClass().getResourceAsStream("/" + LOCAL_MYBATIS_FILE);
            if (properties.containsKey("mybatis_env")) {
                String env = properties.getProperty("mybatis_env");
                logger.info("mybatis init with env {}", env);
                sqlSessionFactory = new SqlSessionFactoryBuilder().build(in, env, properties);
            } else {
                sqlSessionFactory = new SqlSessionFactoryBuilder().build(in, properties);
            }
            // 设置不使用pool的factory,需要重新读取配置文件
            in = getClass().getResourceAsStream("/" + LOCAL_MYBATIS_FILE);
            unpooledSqlSessionFactory  = new SqlSessionFactoryBuilder().build(in, "unpooled", properties);
            autoCloseLeakSessions = PropertyFileReader.getBooleanItem("mybatis.close_leak_session","true");
			enableLeakSessionStackCapture = PropertyFileReader.getBooleanItem("mybatis.leak_session_stack", "false");

			defaultStatementTimeout = PropertyFileReader.getIntItem("mybatis.stmt.timeout", "30");
			setStatementTimeout(sqlSessionFactory, defaultStatementTimeout);
			setStatementTimeout(unpooledSqlSessionFactory, defaultStatementTimeout);

//			if(autoCloseLeakSessions) {
//                sqlSessionManager = new SqlSessionManager();
//                sqlSessionManager.setLeakDetectTime(PropertyFileReader.getIntItem("mybatis.leak_detect_time", "60"));
//
//                // 启动一个定时器任务, 定期关闭泄露的sql session
//                GameScheduler.getInstance().scheduleAtFixedRate(new Runnable() {
//                    @Override
//                    public void run() {
//                        closeLeakSessions();
//                    }
//                }, 60 * 5, 15, TimeUnit.SECONDS);
//				// 用来刷新是否开启stack标志的task
//				GameScheduler.getInstance().scheduleAtFixedRate(new Runnable() {
//					@Override
//					public void run() {
//						refreshFlag();
//					}
//				}, 60 * 5, 60, TimeUnit.SECONDS);
//            }

            Thread.currentThread().setContextClassLoader(origLoader);
//            updateMybatisCrossConfig();
            logger.info("mybatis is ready");
        } catch (Exception e) {
            COKLoggerFactory.monitorException("init mybatis, please check mapper", ExceptionMonitorType.STARTUP, COKLoggerFactory.ExceptionOwner.COMMON, e);
        }
    }

    private void setStatementTimeout(SqlSessionFactory factory, int timeout){
		if (factory != null) {
			factory.getConfiguration().setDefaultStatementTimeout(timeout);
		}
	}

	/**
	 * 更新默认的SQL执行超时时间
	 */
	public int updateStatementTimeout() {
		defaultStatementTimeout = PropertyFileReader.getIntItem("mybatis.stmt.timeout", "30");
		setStatementTimeout(sqlSessionFactory, defaultStatementTimeout);
		setStatementTimeout(unpooledSqlSessionFactory, defaultStatementTimeout);
		return defaultStatementTimeout;
	}

	public boolean isUseRPCSessionProxy() {
		return useRPCSessionProxy;
	}

	public void setUseRPCSessionProxy(boolean useRPCSessionProxy) {
		this.useRPCSessionProxy = useRPCSessionProxy;
	}

	public boolean isEnableLeakSessionStackCapture() {
		return enableLeakSessionStackCapture;
	}

	public void setEnableLeakSessionStackCapture(boolean enableLeakSessionStackCapture) {
		this.enableLeakSessionStackCapture = enableLeakSessionStackCapture;
	}

	public boolean isSessionAutoCommitDefault() {
		return sessionAutoCommitDefault;
	}

	public void setSessionAutoCommitDefault(boolean sessionAutoCommitDefault) {
		this.sessionAutoCommitDefault = sessionAutoCommitDefault;
	}

	public boolean isAutoCloseLeakSessions() {
        return autoCloseLeakSessions;
    }

    public void disableCloseLeakSessions() {
        logger.info("disable mybatis session leak monitor, current state: {}", this.isAutoCloseLeakSessions());
        this.autoCloseLeakSessions = false;
    }
    private void refreshFlag(){
		enableLeakSessionStackCapture = PropertyFileReader.getBooleanItem("mybatis.leak_session_stack","false");
		if (enableLeakSessionStackCapture){
			logger.warn("Leak session stack capture enabled. Use this feature in product environment with caution.");
		}
	}

    private void closeLeakSessions(){
//        if(sqlSessionManager == null){
//            return;
//        }
//        Collection<SqlSessionProxy> leakSessions = sqlSessionManager.getLeakSessions();
//        if(leakSessions == null || leakSessions.size() < 1){
//            return;
//        }
//        logger.info("found leak session count: {}" , leakSessions.size());
//        for(SqlSessionProxy sessionProxy : leakSessions){
//            sessionProxy.close();
//        }
    }
/*
    private String getMybatisMappers() throws IOException {
        String mybatisContent = FileUtils.readFile(getClass().getResourceAsStream("/" + LOCAL_MYBATIS_FILE));
        logger.debug("mybatis content length {}", mybatisContent.length());
        int start = mybatisContent.indexOf("<mappers>");
        int end = mybatisContent.lastIndexOf("</mappers>");
        return mybatisContent.substring(start, end + "</mappers>".length());
    }

    public void updateMybatisCrossConfig() {
        String mybatisCrossUrl = GameEngine.getInstance().getConfigProperties().getProperty("mybatis_cross_config_url");
        if (StringUtils.isNotBlank(mybatisCrossUrl)) {
            logger.info("update mybatis cross config file use url {}", mybatisCrossUrl);
            String crossConfigContent = FileUtils.readUrl(mybatisCrossUrl);
            try {
                String mappers = getMybatisMappers();
                if (StringUtils.isNotBlank(crossConfigContent) && StringUtils.isNotBlank(mappers)) {
                    String mybatisCrossConfig = StringUtils.replace(crossConfigContent, "<!--{MAPPERS}-->", mappers);
                    FileUtils.atomicWriteFile(CROSS_SERVER_MYBATIS_PATH, mybatisCrossConfig);
                }
            }catch (Exception e){
                logger.error("update mybatis cross config error",e);
            }
        }
    }

    public void initTest() {
        if (sqlSessionFactory != null) return;
        try {
            InputStream in = getClass().getResourceAsStream("/" + LOCAL_MYBATIS_FILE);
            Properties properties = GameEngine.getInstance().getConfigProperties();
            properties.setProperty("local.jdbc.url", properties.getProperty("realtime_local.jdbc.url"));
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(in, properties);
            logger.info("mybatis is ready");
        } catch (Exception e) {
            logger.error("init mybatis error", e);
        }
    }*/

    private SqlSessionFactory getSqlSessionFactory() {
        if (sqlSessionFactory == null) {
            lock.lock();
            try {
                if (sqlSessionFactory == null) {
                    init();
                }
            } finally {
                lock.unlock();
            }
        }
        return sqlSessionFactory;
    }

    /**
     * 本服务db
     *
     * @return
     */
    public SqlSession getSession() {
		return getSessionInternal(ExecutorType.SIMPLE, sessionAutoCommitDefault);
    }

	public SqlSession getSession(boolean autoCommit) {
		return getSessionInternal(ExecutorType.SIMPLE, autoCommit);
	}

	/**
     * 本服db，批量模式
     *
     * @return
     */
    public SqlSession getBatchSession() {
		return getSessionInternal(ExecutorType.BATCH, sessionAutoCommitDefault);
    }

	public SqlSession getBatchSession(boolean autoCommit) {
		return getSessionInternal(ExecutorType.BATCH, autoCommit);
	}
 

	//public Map<String,Long> leak=new ConcurrentHashMap<>();

	private SqlSession getSessionInternal(ExecutorType executorType, boolean autoCommit) {
		/*if(useRPCSessionProxy){
			return new SqlSessionRPCProxy(Constants.SERVER_ID);
		}*/
		SqlSession sqlSession;
		try {
			sqlSession = getSqlSessionFactory().openSession(executorType, autoCommit);
		}catch (PersistenceException ex){

			COKLoggerFactory.monitorException(
					String.format("Error for getSession type %s with autoCommit %s.", executorType, autoCommit),
					ExceptionMonitorType.LOCAL_DB, COKLoggerFactory.ExceptionOwner.COMMON, ex);
			sqlSession = unpooledSqlSessionFactory.openSession(executorType, autoCommit);
		}



//		if(autoCloseLeakSessions) {
			/*SqlSessionProxy session = new SqlSessionProxy(sqlSession, sqlSessionManager);
			if (enableLeakSessionStackCapture) {
				session.setStackTraceException(new Exception("LEAK session detected"));
			}*/

//			logger.warn("AAAAOOOOXXXXX_X:"+System.identityHashCode(session));
//
//			Exception e=new RuntimeException();
//			StringBuilder buff=new StringBuilder();
//			for(StackTraceElement t:e.getStackTrace()){
//				buff.append(t.toString());
//			}
//			logger.warn(buff.toString());

//			return session;
//		}else{
			return sqlSession;
//		}
	}

	/**
	 * 其他服db，不走连接池
	 *
	 * @param serverId
	 * @return
	 */
	public Optional<SqlSession> getSession(int serverId) {
		SqlSession session = getSession(serverId, isSessionAutoCommitDefault());
		return Optional.fromNullable(session);
	}

	/**
	 * 其他服db，不走连接池
	 *
	 * @param serverId
	 * @return
	 */
	public Optional<SqlSession> getBatchSession(int serverId) {
		SqlSession session = getBatchSession(serverId, isSessionAutoCommitDefault());
		return Optional.fromNullable(session);
	}

	/**
	 * 获取可以自动关闭的Session，必须用于try-with-resources块，才能自动关闭。
	 * 具体可以参考https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html
	 * @return
	 */
//	public AutoCloseSqlSession getAutoCloseSession(){
//		return new DefaultAutoCloseSqlSession(getSession(sessionAutoCommitDefault));
//	}

	/**
	 * 获取可以自动关闭的batch session，必须用于try-with-resources块，才能自动关闭。
	 * @return
	 */
//	public AutoCloseSqlSession getAutoCloseBatchSession(){
//		return new DefaultAutoCloseSqlSession(getBatchSession(sessionAutoCommitDefault));
//	}

	/**
	 * 获取跨服的可自动关闭session。必须用于try-with-resources块，才能自动关闭。
	 * @param serverId
	 * @return
	 */
//	public Optional<AutoCloseSqlSession> getAutoCloseSession(int serverId){
//		SqlSession session = getSession(serverId, isSessionAutoCommitDefault());
//		if(session == null){
//			return Optional.absent();
//		}else{
//			return Optional.of(new DefaultAutoCloseSqlSession(session));
//		}
//	}

	/**
	 * 获取跨服的可自动关闭batch session。必须用于try-with-resources块，才能自动关闭。
	 * @param serverId
	 * @return
	 */
//	public Optional<AutoCloseSqlSession> getAutoCloseBatchSession(int serverId){
//		SqlSession session = getBatchSession(serverId, isSessionAutoCommitDefault());
//		if (session == null) {
//			return Optional.absent();
//		} else {
//			return Optional.of(new DefaultAutoCloseSqlSession(session));
//		}
//	}

	public SqlSession getSession(int serverId, boolean autoCommit) {
		if (serverId == Constants.SERVER_ID) {
			return getSession(autoCommit);
		}
		return getSessionInternal(serverId, ExecutorType.SIMPLE, autoCommit);
	}

	public SqlSession getBatchSession(int serverId, boolean autoCommit) {
		if(serverId == Constants.SERVER_ID){
			return getBatchSession(autoCommit);
		}
		return getSessionInternal(serverId, ExecutorType.BATCH, autoCommit);
	}

	private SqlSession getSessionInternal(int serverId, ExecutorType type, boolean autoCommit) {
//		if(isUseRPCSessionProxy()){
//			return new SqlSessionRPCProxy(serverId);
//		}
		SqlSession session = null;
		SqlSessionFactory sqlSessionFactory = null;
		try {
			sqlSessionFactory = serverSessionFactoryMap.get(serverId);
		} catch (ExecutionException e) {
			String msg = String.format("get %d server session factory", Constants.SERVER_ID);
			COKLoggerFactory.monitorException(msg, ExceptionMonitorType.CROSS_DB, COKLoggerFactory.ExceptionOwner.COMMON, e);
		}
		if (sqlSessionFactory != null) {
			session = sqlSessionFactory.openSession(type, autoCommit);
		}
		return session;
	}

	public boolean clearCrossSessionCache(int serverId) {
        SqlSessionFactory sqlSessionFactory = serverSessionFactoryMap.getIfPresent(serverId);
        if (sqlSessionFactory == null) {
            return true;
        }
        SFSMysql.getInstance().removeCrossCache(serverId);
        serverSessionFactoryMap.invalidate(serverId);
        return true;
    }

    private static class LazyHolder {
        private static final MyBatisSessionUtil INSTANCE = new MyBatisSessionUtil();
    }

    public static MyBatisSessionUtil getInstance() {
        return LazyHolder.INSTANCE;
    }


}
