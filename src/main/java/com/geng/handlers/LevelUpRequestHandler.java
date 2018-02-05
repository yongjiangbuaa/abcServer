package com.geng.handlers;

import com.geng.exception.GameException;
import com.geng.puredb.model.UserProfile;
import com.geng.service.UserService;
import com.geng.utils.G;
import com.geng.utils.xml.GameConfigManager;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LevelUpRequestHandler implements IRequestHandler{
    public static final int ADD_GOLD = 200;
    private final Logger logger = LoggerFactory.getLogger(LevelUpRequestHandler.class);
    public static final String ID = "level.up";//传所加star

    @Override
    public void handle(String deviceId, String uid, String data, StringBuilder sb) throws GameException {

    }

    @Override
    public void handle(String deviceId, UserProfile userProfile, String data, StringBuilder sb) throws GameException {
        UserService.checkHeartTime(userProfile);
        UserProfile param = G.fromJson(data,UserProfile.class);
        if(null == param || param.getStar() == null)
            throw new GameException(GameException.GameExceptionCode.INVALID_OPTION,"params invalid");
        userProfile.setGold(userProfile.getGold() + new GameConfigManager("matchlevel").getItem(String.valueOf(1000000+userProfile.getLevel())).getInt("coin",ADD_GOLD));
        userProfile.setStar(userProfile.getStar() + new GameConfigManager("matchlevel").getItem(String.valueOf(1000000+userProfile.getLevel())).getInt("star",1));
        userProfile.setLevel(userProfile.getLevel() + 1);
        userProfile.update();
        sb.append(G.toJson(userProfile));

    }
}
