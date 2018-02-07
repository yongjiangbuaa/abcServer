package com.geng.handlers;

import com.geng.core.data.ISFSObject;
import com.geng.core.data.SFSObject;
import com.geng.exceptions.GameException;
import com.geng.exceptions.GameExceptionCode;
import com.geng.gameengine.ItemManager;
import com.geng.puredb.model.UserProfile;
import com.geng.puredb.model.UserStory;
import com.geng.service.UserService;
import com.geng.utils.G;
import com.geng.utils.xml.GameConfigManager;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class UnlockStoryRequestHandler implements IRequestHandler {
    public static final String ID = "story.unlock";
    @Override
    public void handle(String deviceId, String uid, String data, StringBuilder sb) throws GameException {


    }

    @Override
    public void handle(String deviceId, UserProfile userProfile, String data, StringBuilder sb) throws GameException {
        if(StringUtils.isBlank(data))
            throw new GameException(GameExceptionCode.INVALID_OPT,"param not valid!! no data!");
        UserStory param = G.fromJson(data, UserStory.class);
        if(param == null || StringUtils.isBlank(param.getStoryid()) ){
            throw new GameException(GameExceptionCode.INVALID_OPT,"param has no storyid");
        }
        String storyid = param.getStoryid();
        UserStory story = null;
        List<UserStory> storyList = UserStory.getByUserId(userProfile.getUid());
        if (null == storyList || storyList.size() == 0) {
            story = UserStory.newInstance(userProfile.getUid(), storyid);
            story.setStoryid(storyid);
            story.insert();
        } else{
            story = storyList.get(0);
            story.setStoryid(storyid);
            story.update();
        }


        //扣星星
        userProfile.setStar(userProfile.getStar() - new GameConfigManager("quest").getItem(storyid).getInt("requireStar"));
        userProfile.update();

        //组织返回数据

        //组织返回数据
        sb.append(UserService.fillAll(userProfile).toJson());
    }
}
