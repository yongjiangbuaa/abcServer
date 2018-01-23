package com.geng.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

public class GameEngine {
    Properties config ;
    private Logger logger = LoggerFactory.getLogger(GameEngine.class);

    private GameEngine(){

    }

    static class LAZY_LOAD{
        public static GameEngine instance = new GameEngine();
    }

    public static GameEngine getInstance() {
        return LAZY_LOAD.instance;
    }

    public Properties getConfigProperties() {
        if(null != config) return config;
        config = new Properties();
        try {
            config.load(getClass().getResourceAsStream("config.properties"));
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("error when load config.properties");
        };
        return config;
    }
}
