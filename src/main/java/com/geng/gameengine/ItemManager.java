/**
 * @author: lifangkai@elex-tech.com
 * @date: 2013年10月24日 下午12:00:38
 */
package com.geng.gameengine;

import com.geng.core.data.ISFSArray;
import com.geng.core.data.ISFSObject;
import com.geng.core.data.SFSArray;
import com.geng.core.data.SFSObject;
import com.geng.exceptions.COKException;
import com.geng.exceptions.GameExceptionCode;
import com.geng.puredb.model.UserItem;
import com.geng.puredb.model.UserProfile;
import com.geng.server.GameEngine;
import com.geng.utils.CommonUtils;
import com.geng.utils.LoggerUtil;
import com.geng.utils.xml.GameConfigManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 负责玩家背包管理
 */
public class ItemManager {
    private static Logger logger = LoggerFactory.getLogger(ItemManager.class);

    private static final String PUSH_ADD_ITEM = "push.item.add";
    private static final String PUSH_DEL_ITEM = "push.item.del";


    private static final String BUG_FOR_SEND_FRIEND_GIFT_MAIL = "11900";

    /**
     * 0  杂物道具
     *1  队列道具
     *2  加速道具
     *3  作用效果道具
     *4  状态效果道具
     *5  礼包
     *6  自动生成道具
     *7  装备材料
     *8  加兵道具
     *9  龙晶道具
     *10  鲜花道具
     *11  资源点道具
     *12  怪物道具
     *
     * ************13-33是合并COK的道具类型，不一定使用
     *13  物品兑换道具
     *14  龙晶道具
     *15  金币红包道具
     *16  放置怪物
     *17  填充道具
     *18  万能装备道具
     *19  骑士徽章
     *20  互送道具
     *21  特效道具
     *22  使用弹出界面
     *23  双重效果道具
     *24  礼包增益道具
     *25  赠送
     *26  英雄升级
     *27  这个暂时不知道是什么东东
     *28  鲜花道具
     *29  增加技能点
     *30  英雄皮肤
     *31  增加英雄道具
     *32  宠物龙礼物
     *33  获得礼包
     *85  怪物扫荡道具
     *86  炼金房解锁道具
     *87  英雄经验池经验增加
     *88  荣誉值增加道具
     *89  英雄招募
     *90  换肤  赠送,自己用,都是同样道具
     *92  兵种强化
     *93  臭鸡蛋
     *94  使用金币获得更多奖励道具
     *
     *96 只能赠送
     *97 自己用reward1 他人用reward2
     */
    public enum ItemType {
        SUNDRIES(0), OPEN_QUEUE(1), SPD(2), USE(3), STATE(4), REWARD(5), AUTO(6), MATERIAL(7), ADD_SOLDIER(8), UNKNOWN1(9), UNKNOWN2(10), RESOURCE(11), MONSTER(12),
        SYN_USE(13), UNKNOWN3(14), RED_PACKETS(15), WORLD_BOSS(16), SUPPLIES(17), EQUIPMENT(18), TALISMAN(19),HEART(20), JUST_USE(21), SUGGEST_ITEM(22),
        DOUBLE_EFFECT(23),PACKAGE_GAIN(24), GIVE(25),HERO_LEVELUP(26), UNKOWN_KEY(27), FLOWER_ITEM(28), PLUS_SKILL(29), HERO_SKIN(30), HERO_GOODS(31), PET_DRAGON_GIFT(32),GIFT_BAG(33),
        SKILL(72), DRAGON_POWER(74), DRAGON_EXP(75), BUILDING_ITEM(76),NEWWORLD_ITEM(77),STAR_EQUIP_EXP_ITEM(78), QUICK_DNF(80), EQUIP_RANDOM(81), GLAMOUR_GIFT(82), BUFF_ADD(83), ALCHEMY_FIELD(84), MOP_UP_PROPS(85), ALCHEMY_UNLOCK(86) ,HERO_EXP(87),
        GLORY(88), HERO_EMPLOY(89), AVATAR_GIFT(90), ARENA_BATTLE_COUNT(91), ARMY_ENHANCE(92), EGG_GIFT(93),COIN_MORE_REWARD(94),MONSTER_ITEM(95),
        GIFT_ITEM1(96),GIFT_ITEM2(97),BUSSINESS_RES_SCOUT(99),GOD_EXP(100),GOD_ITEM(101),LOVER_GIFT(103), APRIL_FOOL_DAY(104),CLOWN_GIFT(105),NATION_ARMY_AVATAR(107),EXCHANGE_MULTI_GOLD_CARD(109), USER_STAR_JUMP(110);


        private final int index;

        private ItemType(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }

        public static ItemType getByValue(int value) {
            ItemType[] ges = values();
            for (ItemType ge : ges) {
                if (ge.getIndex() == value) {
                    return ge;
                }
            }
            throw new IllegalArgumentException("ResourceType is not exists:" + value);
        }
    }

    public enum ItemEffectType {
        WORLD_SHIELD, REWARD
    }

    public static UserItem addItem(UserProfile userProfile, String itemId, int num, int value, LoggerUtil.GoodsGetType goodsGetType, int param2) {
        return addItem(userProfile, itemId, num, value, false, goodsGetType,param2);
    }
    public static UserItem addItem(UserProfile userProfile, String itemId, int num, int value, LoggerUtil.GoodsGetType goodsGetType) {
        return addItem(userProfile, itemId, num, value, false, goodsGetType,0);
    }

    public static UserItem addItem(UserProfile userProfile, String itemId, int num, int value, boolean isPush, LoggerUtil.GoodsGetType goodsGetType) {
        return  addItem(userProfile, itemId, num, value, isPush, goodsGetType,0);
    }
    public static UserItem addItem(UserProfile userProfile, String itemId, int num, int value, boolean isPush, LoggerUtil.GoodsGetType goodsGetType,int param2) {
        UserItem item;
        int originalCount = 0;
        Object itemLock = userProfile.getItemLock();
        if (itemLock == null) {
            itemLock = new Object();
            logger.warn("can't find item lock {}", userProfile.getUid());
        }
        synchronized (itemLock) {
            item = UserItem.selectItem(userProfile.getUid(), itemId, value);
            if (item != null) {
                if (item.checkOverLap()) { //可以叠放
                    originalCount = item.getCount();
                    item.incrCount(num);
                    item.update();
                } else { //不可以叠放
                    item = UserItem.newInstance(userProfile.getUid(), itemId, num, value);
                }
            } else {
                item = UserItem.newInstance(userProfile.getUid(), itemId, num, value);
            }
        }
        if(num>0)
        {//成就 注掉
//            if(userProfile.getDailyActiveManager() != null){
//                userProfile.getDailyActiveManager().triggerDailyiItemActive(userProfile, DailyActiveManager.ActiveType.COLLECT_ITEM, num, itemId);
//            }
//            if(userProfile.getAchievementManager() != null){
//                userProfile.getAchievementManager().triggerTask(AchievementManager.AchievementTriggerType.ITEM_ADD, itemId, num, true);
//            }
        }
        if(isPush) {
//            GameEngine.getInstance().pushMsg(PUSH_ADD_ITEM, item.toSFSObject(), userProfile);
        }
//        LoggerUtil.getInstance().recordGoodsCost(userProfile.getUid(), item.getItemId(), 0, goodsGetType.ordinal(), param2, originalCount, num, item.getCount());
//        GoldExchangeInfo.updateExchangeData(userProfile, item.getItemId(), 0, goodsGetType.ordinal(), 0, originalCount, num, item.getCount());
        return item;
    }




    public static ISFSObject buyItem(UserProfile userProfile, String itemId, int count) throws  COKException{
        return buyItem(userProfile, itemId, count, false, false);
    }
    public static ISFSObject buyItem(UserProfile userProfile, String itemId, int count, boolean isBuyForUse,boolean hasCDTime) throws  COKException {

        ISFSObject retObj = new SFSObject();
        Map<String, String> goodsMap = new GameConfigManager("goods").getItem(itemId);
        if (goodsMap == null){
            throw new COKException(GameExceptionCode.INVALID_OPT, "item error"); //TODO:
        }

        int price = 0;
        if ("7".equals(goodsMap.get("type"))) {
            if (StringUtils.isBlank(goodsMap.get("make"))) {
                throw new COKException(GameExceptionCode.INVALID_OPT, "item error");
            }
            String make[] = StringUtils.split(goodsMap.get("make"), '|');
            if (make != null && make.length > 2) {
                price = Integer.parseInt(make[2]);
            }
        } else {
            if (StringUtils.isBlank(goodsMap.get("price"))){
                throw new COKException(GameExceptionCode.INVALID_OPT, "item error");
            }
            price = Integer.parseInt(goodsMap.get("price"));
        }
        if (price <=0 || count <= 0) {
            throw new COKException(GameExceptionCode.INVALID_OPT, "param error"); //TODO:参数不合法
        }
        if(goodsMap.containsKey("sales")) {
            String[] salesArr = StringUtils.split(goodsMap.get("sales"), "|");
            boolean isOk = false;
            for(String salesItem : salesArr) {
                String[] numPriceArr = StringUtils.split(salesItem, ";");
                if(count == Integer.parseInt(numPriceArr[0])) {
                    price = Integer.parseInt(numPriceArr[1]);
                    isOk = true;
                    break;
                }
            }
            if (!isOk){
                count = 1;
            }
        }else{
            if(count > 1){
                int cost = price * count;
                count = cost / price;
                price = cost;
            }
        }
        if (price <=0 || count <= 0) {
            throw new COKException(GameExceptionCode.INVALID_OPT, "param error"); //TODO: 参数不合法
        }
        long remainGold = userProfile.decrAllGold(LoggerUtil.GoldCostType.ITEM, price, Integer.parseInt(itemId), count, null);
		userProfile.update();
        if (!isBuyForUse) {
            UserItem item = addItem(userProfile, itemId, count, 0, LoggerUtil.GoodsGetType.BUY);
            writeBuyItemLog(userProfile, item, price, count);
            ISFSObject itemObj = item.toSFSObject();
            retObj.putSFSObject("item", itemObj);
        }
        retObj.putLong("remainGold", remainGold);
        retObj.putInt("costGold", price);
        return retObj;
    }


    private static void writeBuyItemLog(UserProfile userProfile, UserItem item, int price, int count) {
        int originalCount = item.getCount() - count;
        if (!item.checkOverLap()) {
			originalCount = 0;
		}
        try {
			logger.info("ITEM | {} | {} | {} | {} | {} | {} | {} | {} | {} | {} | {}", new Object[]{userProfile.getUid(), item.getItemId(), originalCount, count, item.getCount(), userProfile.getPayTotal(), userProfile.getPf(), userProfile.getCountry(), GameEngine.ZONE_ID, price, 0});
		} catch (Exception ex) {
			logger.error("item log error:{}", ex.getMessage());
		}
    }

    /**
     * 根据传递的售卖价格，进行消费交易，不走goods表
     * @param userProfile
     * @param propsId   购买的道具ID
     * @param itemId    物品ID
     * @param price     消费的总金额
     * @param unitCount 单倍数量
     * @param multiple  倍率
     * @return
     * @throws COKException
     */
    /**
    public static ISFSObject buyItemBySalePrice(UserProfile userProfile, String propsId, String itemId, int price, int unitCount, int multiple) throws  COKException {
        ISFSObject retObj = new SFSObject();
        long remainGold = userProfile.decrAllGold(LoggerUtil.GoldCostType.ITEM, price, Integer.parseInt(propsId), unitCount, null);//购买道具时记录道具ID
        userProfile.update(false);
        UserItem item = addItem(userProfile, itemId, unitCount * multiple, 0, LoggerUtil.GoodsGetType.BUY);
        ISFSObject itemObj = item.toSFSObject();
        retObj.putSFSObject("item", itemObj);
        retObj.putLong("remainGold", remainGold);
        retObj.putInt("costGold", price);
        return retObj;
    }**/



    public static ISFSObject buyItemBatch(UserProfile userProfile, String itemId, int count) throws  COKException{
        return buyItemBatch(userProfile, itemId, count, false, false);
    }
    /**
     * 批量购买
     * @param userProfile
     * @param itemId
     * @param //count
     * @return
     * @throws COKException
     */
    public static ISFSObject buyItemBatch(UserProfile userProfile, String itemId, int count, boolean isBuyForUse,boolean hasCDTime) throws  COKException {
        throw new COKException(GameExceptionCode.INVALID_OPT,"");
    }

    public static int getItemCount(UserProfile userProfile,String itemId,int value) {
        try {
            UserItem item = UserItem.selectItem(userProfile.getUid(), itemId, value);
            if (item != null) {
                if (item.checkOverLap()) { //可以叠放
                    return item.getCount();
                }
            }
            return 0;
        } catch (Exception ex) {
            logger.error("get item count error:{}", ex.getMessage());
            return 0;
        }
    }
//    public static ISFSObject useItem(UserProfile userProfile, String uuid, ItemType purpose, int num) throws COKException {
//        return useItem(userProfile, uuid, purpose, num, null);
//    }

//    public static ISFSObject useItem(UserProfile userProfile, String uuid, ItemType purpose, int num, boolean replaceStat) throws COKException {
//        return useItem(userProfile, uuid, purpose, num, null, replaceStat);
//    }
/**
    public static ISFSObject  useItem(UserProfile userProfile, String uuid, ItemType purpose, int num, String extraInfo, boolean isBuyForUse, String itemId) throws COKException {
        return useItem(userProfile, uuid, purpose, num, null, extraInfo, isBuyForUse, itemId, false,0);
    }

    public static ISFSObject  useItem(UserProfile userProfile, String uuid, int num, boolean isBuyForUse, boolean replaceStat,int coinNum) throws COKException {
        return useItem(userProfile, uuid, null, num, null, null, isBuyForUse, null, replaceStat,coinNum);
    }

    public static ISFSObject useItem(UserProfile userProfile, String uuid, ItemType purpose, int num , UserItem userItem, String extraInfo, boolean isBuyForUse, String goodsId) throws COKException{
        return useItem(userProfile, uuid, purpose, num, userItem, extraInfo, isBuyForUse, goodsId, false,0);
    }
//    public static ISFSObject useItem(UserProfile userProfile, String uuid, ItemType purpose, int num , UserItem userItem) throws COKException {
//        return useItem(userProfile, uuid, purpose, num, null, false);
//    }

    public static ISFSObject useItem(UserProfile userProfile, String uuid, ItemType purpose, int num , UserItem userItem, String extraInfo, boolean isBuyForUse, String goodsId, boolean replaceStat,int coinNum) throws COKException {

    }
 **/






    private static ISFSObject itemUseLogic(UserProfile userProfile, UserItem item, int num,boolean isPushNow) throws COKException{
        return itemUseLogic(userProfile,item.getItemId(), item.getItemMap(),num, item.getValue(),"", isPushNow, false,0);
    }

    private static ISFSObject itemUseLogic(UserProfile userProfile, String itemId, Map<String, String> itemMap, int num, int itemValue,  String extraInfo, boolean isPushNow, boolean replaceState,int coinNum) throws COKException{
            return null;
    }



    /**
     * This method is not threadsafe
     */
    public static ISFSObject decreaseItem(UserProfile userProfile, UserItem userItem, ItemType purpose, int num) throws COKException {
        if(num < 0) {
            throw new COKException(GameExceptionCode.INVALID_OPT, "decrease num < 0");
        }

        if(userItem.getCount() < num) {
            throw new COKException(GameExceptionCode.ITEM_NOT_ENOUGH, "item is not enough"); //TODO: 道具不足
        }
        Map<String, String> itemMap = userItem.getItemMap();
        ItemType itemType = ItemType.getByValue(Integer.parseInt(itemMap.get("type")));
        if(purpose != null && purpose.getIndex() != itemType.getIndex()) {
            throw new COKException(GameExceptionCode.INVALID_OPT, "item type incorrect"); //TODO: 道具类型错误
        }
        ISFSObject itemEffectObj = new SFSObject();
        for(int i=1; i<10; i++) {
            String key = "para" + i;
            if(itemMap.containsKey(key)) {
                if(!"104".equals(itemMap.get("type"))) {
                    itemEffectObj.putInt(key, Integer.parseInt(itemMap.get(key)));
                } else {
                    itemEffectObj.putUtfString(key, itemMap.get(key));
                }
            }
        }
        int originalCount = userItem.getCount();
        userItem.decrCount(num);
        userItem.update();
//        LoggerUtil.getInstance().recordGoodsCost(userItem.getOwnerId(), userItem.getItemId(), 1, LoggerUtil.GoodsUseType.USE.ordinal(), 0, originalCount, num, userItem.getCount());
//        GoldExchangeInfo.updateExchangeData(userProfile, userItem.getItemId(), 1, LoggerUtil.GoodsUseType.USE.ordinal(), 0, originalCount, num, userItem.getCount());

        ISFSObject retObj = new SFSObject();
        retObj.putUtfString("itemId", userItem.getItemId());
        retObj.putInt("count", userItem.getCount());
        retObj.putSFSObject("itemEffectObj", itemEffectObj);
//        userProfile.getAchievementManager().triggerTask(AchievementManager.AchievementTriggerType.ITEM_USE_NUM, userItem.getItemId(), 1, true);
        return retObj;
    }




    /**
     * This method is not threadsafe
     * 检查物品是否满足
     * @param uuid
     * @param purpose
     * @param num
     * @param itemIdList itemId检查
     * @return
     */
    public static UserItem checkItem(String uuid, String uid, ItemType purpose, int num, List<String> itemIdList) {
        UserItem userItem = UserItem.selectItem(uuid);
        if(userItem == null) {
            return null;
        }
        if(userItem.getOwnerId() == null || !userItem.getOwnerId().equals(uid)) {
            return null;
        }
        if(userItem.getCount() < num) {
            return null;
        }
        Map<String, String> itemMap = userItem.getItemMap();
        if(itemMap == null || !itemMap.containsKey("type") || !itemMap.containsKey("id")) {
            return null;
        }
        ItemType itemType = ItemType.getByValue(Integer.parseInt(itemMap.get("type")));
        if(purpose != null && purpose.getIndex() != itemType.getIndex()) {
            return null;
        }
        if(itemIdList != null && !itemIdList.isEmpty()) {
            String itemId = itemMap.get("id");
            if (itemId == null || !itemIdList.contains(itemId)) {
                return null;
            }
        }
        return userItem;
    }

    /**
     * 删除多个道具
     * itemId;num|itemId;num|...
     * @param materialCost
     */
    public static void decMultiItem(UserProfile userProfile, String materialCost, LoggerUtil.GoodsUseType goodsUseType) throws COKException {
        Object itemLock = userProfile.getItemLock();
        if (itemLock == null) {
            itemLock = new Object();
            logger.warn("can't find item lock {}", userProfile.getUid());
        }
        synchronized (itemLock) {
            Map<String, Integer> currItemMap = ItemManager.checkItem(userProfile.getUid(), materialCost);
            if (!ItemManager.isItemEnough(materialCost, currItemMap)) {
                throw new COKException(GameExceptionCode.ITEM_NOT_ENOUGH);
            } else {
                ItemManager.removeItem(userProfile.getUid(), materialCost, currItemMap, goodsUseType);
            }
        }
    }


    /**
     * This method is not threadsafe
     * 获取多个道具数量
     * itemId;num|itemId;num|...
     */
    public static Map<String, Integer> checkItem(String uid, String itemStr) {
        String[] items = StringUtils.split(itemStr, "|");
        int len = items.length;
        String[] conditions = new String[len + 1];
        conditions[0] = uid;
        Map<String, Integer> itemNumMap = new HashMap<>();
        int index = 1;
        for(String itemNum : items) {
            String[] itemNumArr = StringUtils.split(itemNum, ";");
            int count = Integer.parseInt(itemNumArr[1]);
            String itemId = itemNumArr[0];
            itemNumMap.put(itemId, count);
            conditions[index++] = itemId;
        }
        Map<String, Integer> currItemMap = queryItems(conditions);
        return currItemMap;
    }

    public static String afterEffectStr(String itemStr, int effect) {
        if(effect == 0 ) {
            return itemStr;
        }
        StringBuilder ret = new StringBuilder();
        String[] items = StringUtils.split(itemStr, "|");
        for(String itemNum : items) {
            String[] itemNumArr = StringUtils.split(itemNum, ";");
            int count = Integer.parseInt(itemNumArr[1]);
            count *= (1 - effect/100.0);
            ret.append(itemNumArr[0]).append(";").append(count).append("|");
        }
        itemStr = ret.toString();
        return itemStr.substring(0,itemStr.length() -1 );
    }

    /**
     * This method is not threadsafe
     */
	public static boolean isItemEnough(String itemStr, Map<String, Integer> currItemMap) {
		boolean ret;
		if (StringUtils.isBlank(itemStr)) {
			ret = true;
		} else {
			if (currItemMap == null || currItemMap.isEmpty()) {
				ret = false;
			} else {
				ret = true;
				String[] items = StringUtils.split(itemStr, "|");
				for(String itemNum : items) {
					String[] itemNumArr = StringUtils.split(itemNum, ";");
					int count = Integer.parseInt(itemNumArr[1]);
					String itemId = itemNumArr[0];
					int curCount = currItemMap.get(itemId) == null ? 0 : currItemMap.get(itemId);
					if (curCount < count) {
						ret = false;
						break;
					}
				}
			}
		}
		return ret;
	}

	public static int getNotEnoughGoodsCost(String itemStr, Map<String, Integer> currItemMap) {
		int ret = 0;
		if (StringUtils.isNotBlank(itemStr)) {
            GameConfigManager goodsConfig = new GameConfigManager("goods");
			String[] items = StringUtils.split(itemStr, "|");
			for(String itemNum : items) {
				String[] itemNumArr = StringUtils.split(itemNum, ";");
				int count = Integer.parseInt(itemNumArr[1]);
				String itemId = itemNumArr[0];
				int curCount = currItemMap == null ? 0 : (currItemMap.get(itemId) == null ? 0 : currItemMap.get(itemId));
				if (curCount < count) {
					Map<String, String> goodsXml = goodsConfig.getItem(itemId);
					int price = goodsXml.get("price") == null ? 0 : Integer.parseInt(goodsXml.get("price"));
					ret += price * (count - curCount);
				}
			}
		}
		return ret;
	}

    /**
     * This method is not threadsafe
     * @param conditions {ownerId, itemId1, itemId2, ...}
     */
    private static Map<String, Integer> queryItems(String conditions[]) {
        return null;
    }

    /**
     * This method is not threadsafe
     */
    private static ISFSArray removeItem(String uid, String itemStr, Map<String, Integer> originalItemMap, LoggerUtil.GoodsUseType useType) {

        return null;
    }

    //TODO make a thread safe using above method  "; |"
    public static ISFSArray decItems(String a){
        return null;
    }



//    public static ISFSObject combineItem(UserProfile userProfile, String subId, int multiple, String targetId) throws COKException {
//
//    }



    public static void onLogin(String uid, SqlSession session) {
        List<UserItem> itemList = UserItem.getItems(uid);
        if (itemList != null) {
            Map<String, Integer> itemCountMap = new HashMap<>();
            for (UserItem oneItem : itemList) {
                int oldCount = itemCountMap.containsKey(oneItem.getItemId()) ? itemCountMap.get(oneItem.getItemId()) : 0;
                int count = oneItem.getCount() < 0 ? 0: oneItem.getCount();
                itemCountMap.put(oneItem.getItemId(), oldCount + count);
            }
            Set<String> insertIdSet = new HashSet<>();
            List<UserItem> insertList = new ArrayList<>();
            for (UserItem oneItem : itemList) {
                String itemId = oneItem.getItemId();
                if (oneItem.getCount() < 0) {
                    oneItem.delete(session);
                } else if (oneItem.getCount() != itemCountMap.get(itemId)) {
                    oneItem.delete(session);
                    if (!insertIdSet.contains(itemId)) {
                        UserItem insertItem = UserItem.copyItem(uid, itemId, itemCountMap.get(itemId), oneItem.getValue(), oneItem.getVanishTime());
                        insertList.add(insertItem);
                        insertIdSet.add(itemId);
                    }
                }
            }
            if (insertList != null) {
                UserItem.insertBatch(insertList, session);
            }
        }
    }

    /**
     * 登录时返回
     *
     * @param initObj
     */
    public static void getLoginInfo(String uid, ISFSObject initObj) {
        List<UserItem> items = UserItem.getItems(uid);
        ISFSArray qArr = new SFSArray();
        for(UserItem item : items) {
            if (!item.checkValid()){
                continue;
            }
            qArr.addSFSObject(item.toSFSObject());
        }
        initObj.putSFSArray("items", qArr);
    }


	
//	public static ISFSObject  useSuggestItem(UserProfile userProfile, String uuid, String suggestion) throws COKException {
//        return useItem(userProfile, uuid, ItemType.SUGGEST_ITEM, 1, null, suggestion, false, null, false,0);
//    }
	



/**
    public static ISFSArray getMutiItemInfo(String uid, List<String> itemIdList){
        ISFSArray retArr = SFSArray.newInstance();
        List<UserItem> itemList = UserItem.getMutiItemByItemIds(uid, itemIdList);
        if(itemIdList != null && itemIdList.size() != 0){
            for(UserItem item: itemList){
                ISFSObject obj = SFSObject.newInstance();
                obj.putUtfString("uuid", item.getUuid());
                obj.putUtfString("itemId", item.getItemId());
                obj.putInt("count", item.getCount());
                retArr.addSFSObject(obj);
            }
        }

        return retArr;
    }**/
/**
    private static ISFSObject checkItemCD(UserProfile userProfile, String statusId) throws COKException {
        Map<Integer, UserState> userState = userProfile.getStateMap();
        Integer sid = Integer.valueOf(statusId);
        if (userState.containsKey(sid)) {
            //检查当前的时间是否大于结束时间
            UserState us = userState.get(sid);
            if (us != null) {
                long now = System.currentTimeMillis();
                long endTime = us.getEndTime();
                //物品冷却时间未到
                if (now < endTime) {
                    if (Versions.Compare(userProfile.getAppVersion(), Versions.VERSION_1_8_32) < 0) {
                        //老版本的客户端，用异常的格式返回，errorCode=E100160
                        throw new COKException(GameExceptionCode.QUEUE_CD_NOT_FINISHED, "CD of the item is not finished.");
                    }
                    //如果是新版本的客户端，则通过对象返回，带返回时间
                    ISFSObject stateObj = new SFSObject();
                    stateObj.putLong(Integer.toString(us.getStateId()), endTime);
                    stateObj.putLong("startTime", us.getStartTime());
                    ISFSObject retObj = SFSObject.newInstance();
                    retObj.putSFSObject("effectState", stateObj);
                    return retObj;
                }
            }
        }
        return null;
    }
 **/


    /**
     * 买礼物送给好友
     * @param userProfile 自己
     * @param friendProfile 好友
     * @param id 物品
     * @return obj
     * @throws COKException
     *//**
    public static ISFSObject buyForGiveFriendGift(UserProfile userProfile, UserProfile friendProfile, String id) throws COKException{
        Map<String, String> shopConfig = new GameConfigManager("shop_new").getItem(id);
        if(!shopConfig.containsKey("sale_type") || !shopConfig.containsKey("price")) {
            throw new COKException(GameExceptionCode.INVALID_OPT, "[shop_new.xml] error. item id=" + id);
        }
        String type = shopConfig.get("sale_type");
        int goldCost = Integer.valueOf(shopConfig.get("price"));

        if(StringUtils.isBlank(type) || !type.equals("16")) {
            throw new COKException(GameExceptionCode.INVALID_OPT, "[shop_new.xml] error. sale_type is blank or error, item id=" + id);
        }

        if(goldCost <= 0) {
            throw new COKException(GameExceptionCode.INVALID_OPT, "[shop_new.xml] error. price<=0 , item id=" + id);
        }

        //decrease gold
        long remainGold = userProfile.decrAllGold(LoggerUtil.GoldCostType.LOVER_KEEPSAKE, goldCost, 0, 0, null);
        userProfile.update(false);
        
        ISFSObject retObj = SFSObject.newInstance();

        ItemManager.addItem(friendProfile, shopConfig.get("item_id"), 1, 0, true, LoggerUtil.GoodsGetType.BUY_FOR_SEND_GIFT);
        MailServicePlus.sendMailByMailXml(userProfile.getUid(), BUG_FOR_SEND_FRIEND_GIFT_MAIL, null, null, MailSrcFuncType.BUY_FOR_SEND_GIFT);
        retObj.putLong("goldCost", goldCost);
        retObj.putLong("remainGold", remainGold);

        List<String> paramList = new ArrayList<>();
        paramList.add(userProfile.getName());
        paramList.add(id);

        MailServicePlus.sendMailByMailXmlWithParam(friendProfile.getUid(), LoverKeepsakeManager.NOTICE_LOVER_MAIL, null, null,paramList,  MailSrcFuncType.LOVER_KEEPSAKE);

        return retObj;
    }**/

}
