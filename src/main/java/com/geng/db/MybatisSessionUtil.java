package com.geng.db;

import com.geng.server.GameEngine;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.InputStream;
import java.util.Properties;

public class MybatisSessionUtil {
    private static SqlSessionFactory sqlSessionFactory ;

    private MybatisSessionUtil(){
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
        public static MybatisSessionUtil instance = new MybatisSessionUtil();
    }

    public static  MybatisSessionUtil getInstance(){
        return LAZY_LOAD.instance;
    }

    public static SqlSession getSession(){
        return       MybatisSessionUtil.getInstance().getSqlSessionFactory().openSession();
    }


}
