package com.geng.handlers;

import com.geng.exception.GameException;
import com.geng.puredb.model.UidBind;
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
            UidBind bind = UidBind.getWithbindId(deviceId);
            if(null == bind) {
                //TODO find deviceMapping uid
//                if(StringUtils.isBlank(data))
//                    throw new GameException(GameException.GameExceptionCode.INVALID_OPTION,"param not valid!! no data!");
                userProfile = UserService.Register(deviceId, data);//注册
            }else{
                userProfile = UserProfile.getWithUid(bind.getUid());
            }

        }else if(!StringUtils.isEmpty(uid)){
             userProfile = UserProfile.getWithUid(uid);
             if (null == userProfile) throw new GameException(GameException.GameExceptionCode.UID_NOT_EXIST,"uid not exist!");
        }
        UserService.checkHeartTime(userProfile);
        sb.append(G.toJson(userProfile));

    }

    @Override
    public void handle(String deviceId, UserProfile u, String data, StringBuilder sb) throws GameException {

    }
}
