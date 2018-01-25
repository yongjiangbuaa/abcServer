package com.geng.handlers;

import com.geng.exception.GameException;
import com.geng.puredb.model.UserProfile;
import com.geng.service.UserService;
import com.geng.utils.G;
import org.apache.commons.lang3.StringUtils;

public class LoginRequestHandler implements IRequestHandler{
    public static final String ID="user.login";
    @Override
    public void handle(String deviceId, String uid, String data, StringBuilder sb) throws GameException {
        UserProfile userProfile = null;
        if(!StringUtils.isEmpty(deviceId) && StringUtils.isEmpty(uid)){
            //TODO find deviceMapping uid
            userProfile = UserService.Register(deviceId,data);//注册

        }else if(!StringUtils.isEmpty(uid)){
             userProfile = UserProfile.getWithUid(uid);
             if (null == userProfile) throw new GameException(GameException.GameExceptionCode.UID_NOT_EXIST,"uid not exist!");
        }
        sb.append(G.toJson(userProfile));

    }
}
