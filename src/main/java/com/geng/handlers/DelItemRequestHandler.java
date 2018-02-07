package com.geng.handlers;

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

public class DelItemRequestHandler implements IRequestHandler{
    public static final String ID = "item.del";

    @Override
    public void handle(String deviceId, String uid, String data, StringBuilder sb) throws GameException {

    }

    @Override
    public void handle(String deviceId, UserProfile userProfile, String data, StringBuilder sb) throws GameException {
        if(StringUtils.isBlank(data))
            throw new GameException(GameExceptionCode.INVALID_OPT,"param not valid!! no data!");

        //TODO 解析多参数
        MyHttpParam param  = G.fromJson(data,MyHttpParam.class);
        String item = param.getItem();
        if(StringUtils.isBlank(item) || StringUtils.isBlank(param.getNum()))
            throw new GameException(GameExceptionCode.INVALID_OPT,"param not valid no item or no num!!");

        String[] items = StringUtils.split(param.getItem(),"|");
        String[] nums = StringUtils.split(param.getNum(),"|");
        if(items.length != nums.length)
            throw new GameException(GameExceptionCode.INVALID_OPT,"item num length not equal!!");
        Map<String,Integer> iMap  = new HashMap<>();
        for(int i = 0;i < items.length;i++){
            iMap.put(items[i],Integer.parseInt(nums[i]));
        }
        if(iMap.size() < items.length)
            throw new GameException(GameExceptionCode.INVALID_OPT,"item error or repeat!!");


        List itemList = new ArrayList();
        Collections.addAll(itemList,items);

        synchronized (this) {
            List<UserItem> userItemList = UserItem.getMutiItemByItemIds(userProfile.getUid(), itemList);
            if( null == userItemList || userItemList.size() == 0 || userItemList.size() != items.length)
                throw new GameException(GameExceptionCode.ITEM_NOT_ENOUGH,"cheating item use!!");
            for(UserItem u : userItemList){
                    if(u.getCount() < iMap.get(u.getItemId()) )
                        throw new GameException(GameExceptionCode.INVALID_OPT,"cheating item use!!");
                    u.setCount(u.getCount() - iMap.get(u.getItemId()));

            }
            //update
            for(UserItem userItem : userItemList){
                userItem.update();
            }

            List<UserItem> res = UserItem.getItems(userProfile.getUid());
//            sb.append("{\"items\":").append(G.toJson(res)).append("}");
        }

        sb.append(UserService.fillAll(userProfile).toJson());
    }
}
