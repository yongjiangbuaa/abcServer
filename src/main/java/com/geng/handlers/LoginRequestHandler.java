package com.geng.handlers;

import com.geng.puredb.model.UserProfile;
import com.geng.service.UserService;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;

public class LoginRequestHandler implements IRequestHandler{
    public static final String ID="user.login";
    @Override
    public void handle(String deviceId, String uid, String data, StringBuilder sb) {
        UserProfile userProfile = null;
        if(!StringUtils.isEmpty(deviceId) && StringUtils.isEmpty(uid)){
            userProfile = UserService.Register(deviceId,data);//注册

        }else if(!StringUtils.isEmpty(uid)){
             userProfile = UserProfile.getWithUid(uid);
        }
        sb.append(new Gson().toJson(userProfile));

    }
}
