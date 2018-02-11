package com.geng.handlers;

import com.geng.core.data.ISFSObject;
import com.geng.core.data.SFSObject;
import com.geng.exceptions.GameException;
import com.geng.exceptions.GameExceptionCode;
import com.geng.gameengine.ItemManager;
import com.geng.gameengine.login.LoginInfo;
import com.geng.puredb.model.UidBind;
import com.geng.puredb.model.UserProfile;
import com.geng.puredb.model.UserStory;
import com.geng.service.UserService;
import com.geng.utils.G;
import org.apache.commons.lang.StringUtils;

public class LoginRequestHandler implements IRequestHandler{
    public static final String ID="user.login";
    @Override
    public void handle(String deviceId, String uid, String data, StringBuilder sb) throws GameException {
        UserProfile userProfile = null;
        String ipAddress = "";
        if(!StringUtils.isEmpty(deviceId) && StringUtils.isEmpty(uid)){
            UidBind bind = UidBind.getWithbindId(deviceId);
            if(null == bind) {
                LoginInfo loginInfo = null;
                if(StringUtils.isNotBlank(data)){
                    loginInfo = G.fromJson(data,LoginInfo.class);
                }
                userProfile = com.geng.gameengine.UserService.handleLogin(loginInfo,ipAddress);
            }else{
                userProfile = UserProfile.getWithUid(bind.getUid());
            }

        }else if(!StringUtils.isEmpty(uid)){
             userProfile = UserProfile.getWithUid(uid);
             if (null == userProfile) throw new GameException(GameExceptionCode.UID_NOT_EXIST,"uid not exist!");
        }
        UserService.checkHeartTime(userProfile);

        //组织返回数据
        sb.append(UserService.fillAll(userProfile).toJson());

    }

    @Override
    public void handle(String deviceId, UserProfile u, String data, StringBuilder sb) throws GameException {

    }
}
