package com.geng.server;

import com.geng.handlers.*;

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
        handlerRegisterMap.put(DelItemRequestHandler.ID,DelItemRequestHandler.class);
        handlerRegisterMap.put(UnlockStoryRequestHandler.ID,UnlockStoryRequestHandler.class);
    }

    public Object findHandlerInstance(String cmd) throws IllegalAccessException, InstantiationException {
        Class handlerClass = (Class)handlerRegisterMap.get(cmd);
        if(null == handlerClass)  return  null;
        return handlerClass.newInstance();
    }

}
