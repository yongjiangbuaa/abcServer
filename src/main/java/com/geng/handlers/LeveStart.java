package com.geng.handlers;

import com.geng.exceptions.GameException;
import com.geng.exceptions.GameExceptionCode;
import com.geng.puredb.model.UserProfile;
import com.geng.service.UserService;

public class LeveStart implements IRequestHandler {
    public static final String ID = "level.start";
//扣命 扣金币
    @Override
    public void handle(String deviceId, String uid, String data, StringBuilder sb) throws GameException {

    }

    @Override
    public void handle(String deviceId, UserProfile userProfile, String data, StringBuilder sb) throws GameException {
        UserService.checkHeartTime(userProfile);
        if(userProfile.getHeart() < 1)
            throw new GameException(GameExceptionCode.LIFE_NOT_ENOUGH,"heart not enough!!");
        userProfile.setHeart(userProfile.getHeart() -  1);
        if(userProfile.getHearttime() == 0L) userProfile.setHearttime(System.currentTimeMillis() + UserService.recoverTime);
        userProfile.update();

        sb.append(UserService.fillAll(userProfile).toJson());

    }
}
