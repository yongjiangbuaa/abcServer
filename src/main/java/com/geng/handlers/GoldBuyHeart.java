package com.geng.handlers;

import com.geng.core.data.ISFSObject;
import com.geng.core.data.SFSObject;
import com.geng.exceptions.COKException;
import com.geng.exceptions.GameException;
import com.geng.exceptions.GameExceptionCode;
import com.geng.puredb.model.UserProfile;
import com.geng.service.UserService;
import com.geng.utils.LoggerUtil;
import com.geng.utils.xml.GameConfigManager;

/**
 * 仅仅在heart为0时金币购买heart
 */
public class GoldBuyHeart implements  IRequestHandler {
    public static final String ID = "heart.buy";

    @Override
    public void handle(String deviceId, String uid, String data, StringBuilder sb) throws GameException {

    }

    @Override
    public void handle(String deviceId, UserProfile u, String data, StringBuilder sb) throws GameException {

        if(u.getHeart() > 0)
            throw new COKException(GameExceptionCode.INVALID_OPT,"heart is not zero.but request goldBuyHeart!");
        int price = new GameConfigManager("setting").getItem("life").getInt("livesPrice");
        if(u.getGold() == 0 || u.getGold() < price)
            throw new COKException(GameExceptionCode.USERGOLD_IS_NOT_ENOUGH,"gold not enough");
        ISFSObject errObj = SFSObject.newInstance();
        u.decrAllGold(LoggerUtil.GoldCostType.HEART,price,0,0,errObj);
        u.setHeart(new GameConfigManager("setting").getItem("life").getInt("maxLives"));
        u.setHearttime(0L);
        u.update();
        sb.append(UserService.fillAll(u).toJson());

    }
}
