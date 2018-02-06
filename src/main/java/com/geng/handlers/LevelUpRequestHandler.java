package com.geng.handlers;

import com.geng.core.data.ISFSObject;
import com.geng.core.data.SFSObject;
import com.geng.exception.GameException;
import com.geng.exception.GameExceptionCode;
import com.geng.gameengine.ItemManager;
import com.geng.puredb.model.UserProfile;
import com.geng.service.UserService;
import com.geng.utils.G;
import com.geng.utils.LoggerUtil;
import com.geng.utils.xml.GameConfigManager;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class LevelUpRequestHandler implements IRequestHandler{
    public static final int ADD_GOLD = 200;
    private final Logger logger = LoggerFactory.getLogger(LevelUpRequestHandler.class);
    public static final String ID = "level.up";//传所加star

    @Override
    public void handle(String deviceId, String uid, String data, StringBuilder sb) throws GameException {

    }

    @Override
    public void handle(String deviceId, UserProfile userProfile, String data, StringBuilder sb) throws GameException {
        UserService.checkHeartTime(userProfile);
        userProfile.setGold(userProfile.getGold() + new GameConfigManager("matchlevel").getItem(String.valueOf(1000000+userProfile.getLevel())).getInt("coin",ADD_GOLD));
        userProfile.setStar(userProfile.getStar() + new GameConfigManager("matchlevel").getItem(String.valueOf(1000000+userProfile.getLevel())).getInt("star",1));
        userProfile.setLevel(userProfile.getLevel() + 1);
        userProfile.update();


        String itemReward = new GameConfigManager("matchlevel").getItem(String.valueOf(1000000+userProfile.getLevel())).get("itemReward");
        logger.info("itemReward {}",itemReward);
        synchronized (this) {
            String[] itemsProp = StringUtils.split(itemReward,",");
            for(String desc :itemsProp) {
                String[] item_num_rate = StringUtils.split(desc,":");
                boolean add  = false;
                int got = RandomUtils.nextInt(1,100);
                if( got <= Integer.parseInt(item_num_rate[2])) add = true;
                else
                    logger.info("rate {} but got {}",item_num_rate[2],got);

                //add items
                if(add) {
                    ItemManager.addItem(userProfile, item_num_rate[0], Integer.parseInt(item_num_rate[1]), 0, LoggerUtil.GoodsGetType.LEVEL_UP);
                }
            }
        }

        ISFSObject retObj = SFSObject.newInstance();
        ItemManager.getLoginInfo(userProfile.getUid(),retObj);
        userProfile.fillLoginInfo(retObj);
        sb.append(retObj.toJson());

    }
}
