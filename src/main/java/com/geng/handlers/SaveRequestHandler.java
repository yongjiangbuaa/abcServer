package com.geng.handlers;

import com.geng.puredb.model.UserProfile;
import com.geng.utils.G;
import com.google.gson.Gson;

public abstract class SaveRequestHandler implements IRequestHandler{
    public static final String ID="user.save";
    @Override
    public void handle(String deviceId, String uid, String data, StringBuilder sb) {
            UserProfile userProfile = G.fromJson(data, UserProfile.class);
            userProfile.update();//TODO 倒计时
        sb.append(G.toJson(userProfile));
    }
}
