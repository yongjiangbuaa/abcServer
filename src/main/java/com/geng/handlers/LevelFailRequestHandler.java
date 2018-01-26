package com.geng.handlers;

import com.geng.exception.GameException;
import com.geng.puredb.model.UserProfile;
import com.geng.utils.G;

public class LevelFailRequestHandler implements IRequestHandler{
    public static final String ID = "level.fail";
    @Override
    public void handle(String deviceId, String uid, String data, StringBuilder sb) throws GameException {
        UserProfile userProfile = UserProfile.getWithUid(uid);
        if(null == userProfile) throw new GameException(GameException.GameExceptionCode.UID_NOT_EXIST,"");
        long now = System.currentTimeMillis();
        if(userProfile.getHearttime() <= now ){
            userProfile.setHearttime(0L);
            if(userProfile.getHeart() < 5) userProfile.setHeart(userProfile.getHeart() + 1);
            userProfile.update();
        }
        userProfile.setHeart(userProfile.getHeart() -  1);
        if(userProfile.getHearttime() == 0L) userProfile.setHearttime(System.currentTimeMillis() + 30 * 60 *1000L);
        userProfile.update();
        sb.append(G.toJson(userProfile));
    }
}
