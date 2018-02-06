package com.geng.handlers;

import com.geng.core.data.ISFSObject;
import com.geng.core.data.SFSObject;
import com.geng.exception.GameException;
import com.geng.exception.GameExceptionCode;
import com.geng.gameengine.ItemManager;
import com.geng.puredb.model.UserProfile;
import com.geng.puredb.model.UserStory;
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
        if(userProfile.getHeart() < 1)
            throw new GameException(GameExceptionCode.LIFE_NOT_ENOUGH,"heart not enough!!");
        userProfile.setHeart(userProfile.getHeart() -  1);
        if(userProfile.getHearttime() == 0L) userProfile.setHearttime(System.currentTimeMillis() + UserService.recoverTime);
        userProfile.update();

        ISFSObject retObj = SFSObject.newInstance();
        ItemManager.getLoginInfo(userProfile.getUid(),retObj);
        UserStory.getLoginInfo(userProfile,retObj);
        userProfile.fillLoginInfo(retObj);
        sb.append(retObj.toJson());
    }
}
