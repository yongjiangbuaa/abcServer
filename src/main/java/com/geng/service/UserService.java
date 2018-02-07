package com.geng.service;

import com.geng.core.data.ISFSObject;
import com.geng.core.data.SFSObject;
import com.geng.gameengine.ItemManager;
import com.geng.puredb.model.UidBind;
import com.geng.puredb.model.UserProfile;
import com.geng.puredb.model.UserStory;

import java.util.concurrent.atomic.AtomicLong;

public class UserService {
    public static final int maxHeart = 5;
    public static final long recoverTime = 30 * 60 * 1000L;
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
            if(userProfile.getHeart() + 1 >= maxHeart)
                userProfile.setHearttime(0L);
            else
                userProfile.setHearttime(System.currentTimeMillis() + recoverTime);
            if(userProfile.getHeart() < maxHeart)
                userProfile.setHeart(userProfile.getHeart() + 1);
            userProfile.update();
        }

    }

    public static ISFSObject fillAll(UserProfile userProfile) {
        ISFSObject retObj = SFSObject.newInstance();
        ItemManager.getLoginInfo(userProfile.getUid(),retObj);
        UserStory.getLoginInfo(userProfile,retObj);
        userProfile.fillLoginInfo(retObj);
        return retObj;
    }
}
