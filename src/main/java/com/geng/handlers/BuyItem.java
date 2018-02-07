package com.geng.handlers;

import com.geng.core.data.SFSObject;
import com.geng.exceptions.GameException;
import com.geng.exceptions.COKException;
import com.geng.exceptions.GameExceptionCode;
//import com.geng.gameengine.GoodsType;
import com.geng.gameengine.ItemManager;
import com.geng.puredb.model.UserProfile;
import com.geng.utils.CommonUtils;
import com.geng.core.data.ISFSObject;

/**
 * @author Administrator
 *
 */
public class BuyItem implements IRequestHandler {
	public static final String ID = "item.buy";



    private ISFSObject handleRetObj(UserProfile userProfile, ISFSObject params) throws COKException {
        String itemId = params.getUtfString("itemId");
//        if(CommonUtils.isSameStr(GoodsType.CHANGE_BUILDING_POS.getGoodsId(),itemId)){
//            throw new COKException(GameExceptionCode.INVALID_OPT, "can not buy item "+ itemId);
//        }
        boolean hasCDTime = false;
        if (params.containsKey("hasCDTime")) {
            hasCDTime = params.getBool("hasCDTime");
        }
        if (params.containsKey("batch") && params.getInt("batch") > 0){
            return ItemManager.buyItemBatch(userProfile, itemId, params.getInt("num"), false, hasCDTime);
        }else {
            return ItemManager.buyItem(userProfile, itemId, params.getInt("num"), false, hasCDTime);
        }
    }


    @Override
    public void handle(String deviceId, String uid, String data, StringBuilder sb) throws GameException {

    }

    @Override
    public void handle(String deviceId, UserProfile u, String data, StringBuilder sb) throws GameException {
        ISFSObject retObj;
        ISFSObject params = SFSObject.newFromJsonData(data);
        retObj = handleRetObj(u, params);

        sb.append(retObj.toJson());
    }
}
