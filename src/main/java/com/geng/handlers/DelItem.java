package com.geng.handlers;

import com.geng.core.data.ISFSArray;
import com.geng.core.data.ISFSObject;
import com.geng.core.data.SFSObject;
import com.geng.exceptions.GameException;
import com.geng.exceptions.GameExceptionCode;
import com.geng.gameengine.ItemManager;
import com.geng.puredb.model.UserItem;
import com.geng.puredb.model.UserProfile;
import com.geng.puredb.model.UserStory;
import com.geng.service.UserService;
import com.geng.utils.G;
import com.geng.utils.MyHttpParam;
import org.apache.commons.lang.StringUtils;

import java.util.*;

public class DelItem implements IRequestHandler{
    public static final String ID = "item.del";

    @Override
    public void handle(String deviceId, String uid, String data, StringBuilder sb) throws GameException {

    }

    @Override
    public void handle(String deviceId, UserProfile userProfile, String data, StringBuilder sb) throws GameException {
        UserService.checkHeartTime(userProfile);
        if(StringUtils.isBlank(data))
            throw new GameException(GameExceptionCode.INVALID_OPT,"param not valid!! no data!");

        //TODO 解析多参数
        MyHttpParam param  = G.fromJson(data,MyHttpParam.class);
        String item = param.getItem();
        if(StringUtils.isBlank(item) || StringUtils.isBlank(param.getNum()))
            throw new GameException(GameExceptionCode.INVALID_OPT,"param not valid no item or no num!!");

        String[] items = StringUtils.split(param.getItem(),"|");
        String[] nums = StringUtils.split(param.getNum(),"|");
         ItemManager.decItems(userProfile,items,nums);


        sb.append(UserService.fillAll(userProfile).toJson());
    }
}
