package com.geng.service;

import com.geng.core.data.ISFSObject;
import com.geng.core.data.SFSObject;
import com.geng.gameengine.ItemManager;
import com.geng.puredb.model.UidBind;
import com.geng.puredb.model.UserProfile;
import com.geng.puredb.model.UserStory;
import com.geng.utils.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

public class UserService {
    public static final int maxHeart = 5;
    public static final long recoverTime = 30 * 60 * 1000L;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
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
        logger.info("uid={} heartTime={}",userProfile.getUid(), userProfile.getHearttime());
        long now = System.currentTimeMillis();
        int oldHeart = userProfile.getHeart();
        if(userProfile.getHearttime() <= now ){
            int pastSince = Math.toIntExact (now - userProfile.getHearttime() - recoverTime);
            int heartNeedAdd = pastSince/Math.toIntExact(recoverTime) ;
            int timeRemain  = pastSince%Math.toIntExact(recoverTime);

            if(userProfile.getHeart() + heartNeedAdd >= maxHeart) {//满，时间数据清0
                userProfile.setHearttime(0L);
                logger.error("uid={} heart will be max.set heartTime=0",userProfile.getUid());
            } else {//未满，下一恢复周期。需要根据流逝时间计算。
                userProfile.setHearttime(System.currentTimeMillis() + recoverTime  - timeRemain);
                logger.error("uid={} heart is not  max yet.set heartTime in next 30 mins.that is {} ",userProfile.getUid(),userProfile.getHearttime());
            }

            if(userProfile.getHeart() < maxHeart) {
                userProfile.setHeart(userProfile.getHeart() + heartNeedAdd);
                userProfile.setHeart(userProfile.getHeart() > maxHeart ? 5 : userProfile.getHeart());
                logger.info("uid={}  heart= added {} now heart={}",userProfile.getUid(),userProfile.getHeart() - oldHeart,userProfile.getHeart());
            }
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
