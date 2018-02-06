package com.geng.handlers;

import com.geng.exception.GameException;
import com.geng.exception.GameExceptionCode;
import com.geng.puredb.model.UidBind;
import com.geng.puredb.model.UserProfile;
import com.geng.puredb.model.UserStory;
import com.geng.service.UserService;
import com.geng.utils.G;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

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
//                    throw new GameException(GameExceptionCode.INVALID_OPT,"param not valid!! no data!");
                userProfile = UserService.Register(deviceId, data);//注册
            }else{
                userProfile = UserProfile.getWithUid(bind.getUid());
            }

        }else if(!StringUtils.isEmpty(uid)){
             userProfile = UserProfile.getWithUid(uid);
             if (null == userProfile) throw new GameException(GameExceptionCode.UID_NOT_EXIST,"uid not exist!");
        }
        UserService.checkHeartTime(userProfile);
        List<UserStory> list = UserStory.getByUserId(uid);
        if(list != null || list.size() > 0){
            userProfile.setStoryId(list.get(0).getStoryid());
        }
        sb.append(G.toJson(userProfile));

    }

    @Override
    public void handle(String deviceId, UserProfile u, String data, StringBuilder sb) throws GameException {

    }
}
