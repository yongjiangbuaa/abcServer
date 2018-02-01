package com.geng.handlers;

import com.geng.exception.GameException;
import com.geng.puredb.model.UserProfile;
import com.geng.service.UserService;
import com.geng.utils.G;

public class LevelFailRequestHandler implements IRequestHandler{
    public static final String ID = "level.fail";

    @Override
    public void handle(String deviceId, String uid, String data, StringBuilder sb) throws GameException {

    }

    @Override
    public void handle(String deviceId, UserProfile userProfile, String data, StringBuilder sb) throws GameException {
        UserService.checkHeartTime(userProfile);
        userProfile.setHeart(userProfile.getHeart() -  1);
        if(userProfile.getHearttime() == 0L) userProfile.setHearttime(System.currentTimeMillis() + UserService.recoverTime);
        userProfile.update();
        sb.append(G.toJson(userProfile));
    }
}
