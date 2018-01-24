package com.geng.handlers;

import com.geng.puredb.model.UserProfile;
import com.google.gson.Gson;

public class SaveRequestHandler implements IRequestHandler{
    public static final String ID="user.save";
    @Override
    public void handle(String deviceId, String uid, String data, StringBuilder sb) {
            UserProfile userProfile = new Gson().fromJson(data, UserProfile.class);
            UserProfile.update(userProfile);//TODO 倒计时
    }
}
