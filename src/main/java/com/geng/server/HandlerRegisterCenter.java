package com.geng.server;

import com.geng.handlers.*;
import com.geng.handlers.requesthandlers.account.ModifyUserNickNameHandler;

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
        handlerRegisterMap.put(Login.ID,Login.class);
        handlerRegisterMap.put(SaveRequestHandler.ID,SaveRequestHandler.class);
        handlerRegisterMap.put(LevelUp.ID,LevelUp.class);
        handlerRegisterMap.put(LevelFail.ID,LevelFail.class);
        handlerRegisterMap.put(LevelStart.ID,LevelStart.class);
        handlerRegisterMap.put(LevelEnd.ID,LevelEnd.class);
        handlerRegisterMap.put(DelItem.ID,DelItem.class);
        handlerRegisterMap.put(UnlockStory.ID,UnlockStory.class);
        handlerRegisterMap.put(BuyItem.ID,BuyItem.class);
        handlerRegisterMap.put(GoldBuyHeart.ID,GoldBuyHeart.class);
        handlerRegisterMap.put(GoldBuyStep.ID,GoldBuyStep.class);
        handlerRegisterMap.put(ModifyUserNickNameHandler.ID,ModifyUserNickNameHandler.class);
    }

    public Object findHandlerInstance(String cmd) throws IllegalAccessException, InstantiationException {
        Class handlerClass = (Class)handlerRegisterMap.get(cmd);
        if(null == handlerClass)  return  null;
        return handlerClass.newInstance();
    }

}
