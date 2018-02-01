package com.geng.server;

import com.geng.exception.GameException;
import com.geng.handlers.*;
import com.geng.puredb.model.UserProfile;
import com.geng.utils.xml.GameConfigManager;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Handler;

public class GameEngine {
    Properties config ;
    private Logger logger = LoggerFactory.getLogger(GameEngine.class);

    private GameEngine(){

    }

    //确定的初始化逻辑 在服务初始化时调用
    public void init(){
        HandlerRegisterCenter.getInstance();
        GameConfigManager.init();

    }

    public Properties getConfigProperties() {
        if(null != config) return config;
        config = new Properties();
        try {
            Map<String,String> env = System.getenv();
            logger.debug("env={}",env);
            Properties properties = System.getProperties();
            logger.debug("properties={}",properties.toString());
            config.load(new FileInputStream("config/config.properties"));
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("error when load config.properties");
        };
        return config;
    }

    public String getConfigValue(String key) {
        String configValue = getConfigProperties().getProperty(key);
        return configValue;
    }

    static class LAZY_LOAD{
        public static GameEngine instance = new GameEngine();
    }

    public static GameEngine getInstance() {
        return LAZY_LOAD.instance;
    }




    private void dispatchOp(String cmd, String data, String uid, String deviceId,StringBuilder sb) {
        logger.info("uid={} cmd={} device={} data={}",uid,cmd,deviceId,data);
        logger.debug("uid={} cmd={} device={} data={}",uid,cmd,deviceId,data);
        //操作派发到相应类
        try {
            IRequestHandler handler = (IRequestHandler) HandlerRegisterCenter.getInstance().findHandlerInstance(cmd);
            if(null != handler) {
                if( handler instanceof  LoginRequestHandler){
                    handler.handle(deviceId, uid, data, sb);
                }else {
                    UserProfile userProfile = UserProfile.getWithUid(uid);
                    if(null == userProfile )
                        throw new GameException(GameException.GameExceptionCode.UID_NOT_EXIST,"uid not exist!");

                    handler.handle(deviceId, userProfile, data, sb);
                }
            }
        } catch (GameException e) {
            logger.error(e.getMessage());
            sb.append(e.toJson());
            logger.info(sb.toString());
        } catch (IllegalAccessException|InstantiationException e) {
            logger.error(e.getMessage());
            sb.append(new GameException(GameException.GameExceptionCode.ACCESS_CONFIG_FILE_ERROR,"error in access config file").toJson());
        }  finally {

        }
        //组织返回
    }


    public void protocal(Map<String,List<String>> params,StringBuilder sb){
            protocal(params.get("cmd").get(0),
                    !params.containsKey("device")? "":params.get("device").get(0),
                    !params.containsKey("uid") ? "": params.get("uid").get(0),
                    params.get("data").get(0),
                    sb);
    }

    /**
     * return {"err":"errorCode","errMsg":"","heart":1,"gold":0,"star":23,"heartTime":"1234324234"}
     * @param cmd
     * @param deviceId
     * @param uid
     * @param data
     * @param sb
     */
    public void protocal(String cmd,String deviceId,String uid,String data,StringBuilder sb){
            dispatchOp(cmd,data,uid,deviceId,sb);
    }

}
