package com.geng.handlers;

import com.geng.exception.GameException;
import com.geng.puredb.model.UserItem;
import com.geng.utils.G;
import com.geng.utils.MyHttpParam;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class DelItemRequestHandler implements IRequestHandler{
    public static final String ID = "item.del";

    @Override
    public void handle(String deviceId, String uid, String data, StringBuilder sb) throws GameException {
        //TODO 解析多参数
        MyHttpParam param  = G.fromJson(data,MyHttpParam.class);
        String item = param.getItem();
        if(StringUtils.isBlank(item) || StringUtils.isBlank(param.getNum()))
            throw new GameException(GameException.GameExceptionCode.INVALID_OPTION,"param not valid no item or no num!!");

        String[] items = StringUtils.split(param.getItem(),"|");
        String[] nums = StringUtils.split(param.getNum(),"|");
        if(items.length != nums.length)
            throw new GameException(GameException.GameExceptionCode.INVALID_OPTION,"item num length not equal!!");
        Map<String,Integer> iMap  = new HashMap<>();
        for(int i = 0;i < items.length;i++){
            iMap.put(items[i],Integer.parseInt(nums[i]));
        }
        if(iMap.size() < items.length)
            throw new GameException(GameException.GameExceptionCode.INVALID_OPTION,"item error or repeat!!");


        List itemList = new ArrayList();
        Collections.addAll(itemList,items);

        synchronized (this) {
            List<UserItem> userItemList = UserItem.getMutiItemByItemIds(uid, itemList);
            if( null == userItemList || userItemList.size() == 0 || userItemList.size() != items.length)
                throw new GameException(GameException.GameExceptionCode.ITEM_NOT_ENOUGH,"cheating item use!!");
            for(UserItem u : userItemList){
                    if(u.getCount() < iMap.get(u.getItemid()) )
                        throw new GameException(GameException.GameExceptionCode.INVALID_OPTION,"cheating item use!!");
                    u.setCount(u.getCount() - iMap.get(u.getItemid()));

            }
            //update
            for(UserItem userItem : userItemList){
                userItem.update();
            }

            List<UserItem> res = UserItem.getItemByOwnerid(uid);
            sb.append("{\"items\":").append(G.toJson(res)).append("}");
        }
    }
}
