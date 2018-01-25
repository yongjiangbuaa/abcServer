package com.geng.server;

import com.geng.exception.GameException;
import com.geng.handlers.IRequestHandler;
import com.geng.handlers.LoginRequestHandler;
import com.geng.handlers.SaveRequestHandler;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Handler;

public class GameEngine {
    Properties config ;
    private Logger logger = LoggerFactory.getLogger(GameEngine.class);
    ConcurrentHashMap<String,Object> handlerRegisterMap = new ConcurrentHashMap<>();

    private GameEngine(){
        handlerRegisterMap.put(LoginRequestHandler.ID,LoginRequestHandler.class);
        handlerRegisterMap.put(SaveRequestHandler.ID,SaveRequestHandler.class);

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


    private void dispatchOp(String cmd, String data, String uid, String deviceId,StringBuilder sb) {
        logger.info("uid={} cmd={} device={} data={}",uid,cmd,deviceId,data);
        //操作派发到相应类
        try {
            IRequestHandler handler = (IRequestHandler) findHandlerInstance(cmd);
            if(null != handler)
                handler.handle(deviceId, uid, data, sb);
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

    private Object findHandlerInstance(String cmd) throws IllegalAccessException, InstantiationException {
        Class handlerClass = (Class)handlerRegisterMap.get(cmd);
        if(null == handlerClass)  return  null;
        return handlerClass.newInstance();
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
