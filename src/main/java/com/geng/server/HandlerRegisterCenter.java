package com.geng.server;

import com.geng.handlers.LevelFailRequestHandler;
import com.geng.handlers.LevelUpRequestHandler;
import com.geng.handlers.LoginRequestHandler;
import com.geng.handlers.SaveRequestHandler;

import java.util.concurrent.ConcurrentHashMap;

public class HandlerRegisterCenter {
    ConcurrentHashMap<String,Object> handlerRegisterMap = new ConcurrentHashMap<>();

    static class LAZY_LOAD{
        public static HandlerRegisterCenter h = new HandlerRegisterCenter();
    }

    public static HandlerRegisterCenter getInstance() {
        return LAZY_LOAD.h;
    }

    private HandlerRegisterCenter(){
        handlerRegisterMap.put(LoginRequestHandler.ID,LoginRequestHandler.class);
        handlerRegisterMap.put(SaveRequestHandler.ID,SaveRequestHandler.class);
        handlerRegisterMap.put(LevelUpRequestHandler.ID,LevelUpRequestHandler.class);
        handlerRegisterMap.put(LevelFailRequestHandler.ID,LevelFailRequestHandler.class);

    }

    public Object findHandlerInstance(String cmd) throws IllegalAccessException, InstantiationException {
        Class handlerClass = (Class)handlerRegisterMap.get(cmd);
        if(null == handlerClass)  return  null;
        return handlerClass.newInstance();
    }

}
