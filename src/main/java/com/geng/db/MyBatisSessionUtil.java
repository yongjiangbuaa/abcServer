package com.geng.db;

import com.geng.server.GameEngine;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.InputStream;
import java.util.Properties;

public class MyBatisSessionUtil {
    private static SqlSessionFactory sqlSessionFactory ;

    private MyBatisSessionUtil(){
        init();
    }

    private void init(){
        if(null == sqlSessionFactory ){
            InputStream in = getClass().getResourceAsStream("/mybatis.xml");
            Properties p = GameEngine.getInstance().getConfigProperties();
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(in,p);
        }
    }

    private  SqlSessionFactory getSqlSessionFactory() {
        if(null == sqlSessionFactory) init();

        return sqlSessionFactory;
    }

    static class LAZY_LOAD{
        public static MyBatisSessionUtil instance = new MyBatisSessionUtil();
    }

    public static MyBatisSessionUtil getInstance(){
        return LAZY_LOAD.instance;
    }

    public static SqlSession getSession(){
        return       MyBatisSessionUtil.getInstance().getSqlSessionFactory().openSession(ExecutorType.SIMPLE,true);
    }


}
