package com.geng.handlers;

import com.geng.core.data.ISFSObject;
import com.geng.core.data.SFSObject;
import com.geng.exceptions.GameException;
import com.geng.exceptions.GameExceptionCode;
import com.geng.puredb.model.UidBind;
import com.geng.puredb.model.UserProfile;
//import com.geng.service.UserService;
import com.geng.gameengine.UserService;
import org.apache.commons.lang.StringUtils;

public class Login implements IRequestHandler{
    public static final String ID="user.login";
    @Override
    public void handle(String deviceId, String uid, String data, StringBuilder sb) throws GameException {
        UserProfile userProfile = null;
        ISFSObject initObj = SFSObject.newInstance();
        if(!StringUtils.isEmpty(deviceId) && StringUtils.isEmpty(uid)){
            UidBind bind = UidBind.getWithbindId(deviceId);
            if(null == bind) {
                //TODO find deviceMapping uid
//                if(StringUtils.isBlank(data))
//                    throw new GameException(GameExceptionCode.INVALID_OPT,"param not valid!! no data!");
                userProfile = com.geng.service.UserService.Register(deviceId, data);//注册
            }else{
                userProfile = UserProfile.getWithUid(bind.getUid());
            }

        }else if(!StringUtils.isEmpty(uid)){
             userProfile = UserProfile.getWithUid(uid);
             if (null == userProfile) throw new GameException(GameExceptionCode.UID_NOT_EXIST,"uid not exist!");
        }

//        userProfile = UserService.handleLogin(deviceId, data);//TODO 新协议支持登录时带入的信息存储及相关分析

        com.geng.service.UserService.checkHeartTime(userProfile);

        //组织返回数据
        sb.append(com.geng.service.UserService.fillAll(userProfile).toJson());

    }

    @Override
    public void handle(String deviceId, UserProfile u, String data, StringBuilder sb) throws GameException {

    }
}
