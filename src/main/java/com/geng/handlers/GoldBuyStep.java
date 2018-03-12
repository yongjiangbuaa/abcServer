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
import org.apache.commons.lang.StringUtils;

/**
 * 仅仅在step为0时金币购买5步
 * 服务端根据 level.fiveMore这个协议的请求次数扣金币。重新发level.start时次数清零。
 服务端负责正确扣除金币。
 扣成功客户端就给5步。
 * 根据请求次数记录当前关卡
 */
public class GoldBuyStep implements  IRequestHandler {
    public static final String ID = "level.fivemore";

    @Override
    public void handle(String deviceId, String uid, String data, StringBuilder sb) throws GameException {

    }

    @Override
    public void handle(String deviceId, UserProfile u, String data, StringBuilder sb) throws GameException {
        //TODO 次数到最大了 按最大金额扣
        String priceStr = new GameConfigManager("setting").getItem("stepsBuy").get("price");
        String[] priceArr = StringUtils.split(priceStr,"|");
        int price = Integer.parseInt(priceArr[u.getWorldPoint() > priceArr.length ? priceArr.length - 1 : u.getWorldPoint() ]);

        if(u.getGold() == 0 || u.getGold() < price)
            throw new COKException(GameExceptionCode.USERGOLD_IS_NOT_ENOUGH,"gold not enough");
        ISFSObject errObj = SFSObject.newInstance();
        u.decrAllGold(LoggerUtil.GoldCostType.STEP,price,0,0,errObj);
        u.setWorldPoint(u.getWorldPoint() + 1);//增加重复挑战次数
        u.update();
        sb.append(UserService.fillAll(u).toJson());

    }
}
