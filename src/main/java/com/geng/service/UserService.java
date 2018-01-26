package com.geng.service;

import com.geng.puredb.model.UidBind;
import com.geng.puredb.model.UserProfile;

import java.util.concurrent.atomic.AtomicLong;

public class UserService {
    private static AtomicLong defaultNameIndex = new AtomicLong();
    public static UserProfile Register(String deviceId, String data) {
        defaultNameIndex = new AtomicLong(UserProfile.getMaxNameIndex());//"select count(uid) from user_profile;"
        long id = defaultNameIndex.incrementAndGet();
        String uid = String .valueOf(id);
        UidBind bind = UidBind.newInstance(uid,deviceId,1,System.currentTimeMillis());
        UidBind.insert(bind);

        int heart = 5;//TODO config.get("h");
        int gold = 1000;//config.get("g");
        int star = 0;//config.get("s");
        UserProfile userProfile = UserProfile.newInstance(uid,gold,heart,star,0L,1);
        userProfile.insert();
        return userProfile;

    }

    public static void checkHeartTime(UserProfile userProfile) {
        if(userProfile.getHearttime() <= System.currentTimeMillis() ){
            userProfile.setHearttime(0L);
            if(userProfile.getHeart() < 5) userProfile.setHeart(userProfile.getHeart() + 1);
            userProfile.update();
        }

    }
}
