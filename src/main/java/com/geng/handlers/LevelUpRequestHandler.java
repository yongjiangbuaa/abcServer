package com.geng.handlers;

import com.geng.exception.GameException;
import com.geng.puredb.model.UserProfile;
import com.geng.service.UserService;
import com.geng.utils.G;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LevelUpRequestHandler implements IRequestHandler{
    private final Logger logger = LoggerFactory.getLogger(LevelUpRequestHandler.class);
    public static final String ID = "level.up";//传所加star
    @Override
    public void handle(String deviceId, String uid, String data, StringBuilder sb) throws GameException {
        UserProfile userProfile = UserProfile.getWithUid(uid);
        if(null == userProfile)
            throw new GameException(GameException.GameExceptionCode.UID_NOT_EXIST,"");
        UserService.checkHeartTime(userProfile);
        UserProfile param = G.fromJson(data,UserProfile.class);
        if(null == param || param.getStar() == null)
            throw new GameException(GameException.GameExceptionCode.INVALID_OPTION,"params invalid");
        userProfile.setGold(userProfile.getGold() + 200);
        userProfile.setStar(userProfile.getStar() + param.getStar());
        userProfile.setLevel(userProfile.getLevel() + 1);
        userProfile.update();
        sb.append(G.toJson(userProfile));

    }
}
