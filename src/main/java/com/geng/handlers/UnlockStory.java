package com.geng.handlers;

import com.geng.core.data.ISFSObject;
import com.geng.core.data.SFSObject;
import com.geng.exceptions.COKException;
import com.geng.exceptions.GameException;
import com.geng.exceptions.GameExceptionCode;
import com.geng.gameengine.ItemManager;
import com.geng.puredb.model.UserProfile;
import com.geng.puredb.model.UserStory;
import com.geng.service.UserService;
import com.geng.utils.G;
import com.geng.utils.LoggerUtil;
import com.geng.utils.xml.GameConfigManager;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class UnlockStory implements IRequestHandler {
    public static final String ID = "story.unlock";
    public static final Logger logger = LoggerFactory.getLogger(UnlockStory.class);
    @Override
    public void handle(String deviceId, String uid, String data, StringBuilder sb) throws GameException {


    }

    @Override
    public void handle(String deviceId, UserProfile userProfile, String data, StringBuilder sb) throws GameException {
        UserService.checkHeartTime(userProfile);
        if(StringUtils.isBlank(data))
            throw new GameException(GameExceptionCode.INVALID_OPT,"param not valid!! no data!");
        if(userProfile.getStar() <= 0)
            throw new COKException(GameExceptionCode.STAR_NOT_ENOUGH,"star not enough!");
        UserStory param = G.fromJson(data, UserStory.class);
        if(param == null || StringUtils.isBlank(param.getStoryid()) ){
            throw new GameException(GameExceptionCode.INVALID_OPT,"param has no storyid");
        }


        String storyid = param.getStoryid();
        int need = new GameConfigManager("quest").getItem(storyid).getInt("requireStar");
        if(userProfile.getStar() < need)
            throw new COKException(GameExceptionCode.STAR_NOT_ENOUGH,"star not enough!");

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
        userProfile.setStar(userProfile.getStar() - need);
        userProfile.update();

        //扣物品
        String requireItem = new GameConfigManager("quest").getItem(storyid).get("requireItem");
        logger.info("uid {} storyid {} requireItem {}", userProfile.getUid(),storyid,requireItem);
        if(!StringUtils.isBlank(requireItem) && !StringUtils.equals(requireItem,"0"))
            ItemManager.decItems(userProfile,requireItem);


        //组织返回数据
        sb.append(UserService.fillAll(userProfile).toJson());
    }
}
