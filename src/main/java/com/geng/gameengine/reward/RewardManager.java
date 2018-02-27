/**
 * @author: lifangkai@elex-tech.com
 * @date: 2013年10月8日 上午10:24:01
 */
package com.geng.gameengine.reward;

import com.geng.core.ConcurrentLock;
import com.geng.core.GameEngine;
import com.geng.exceptions.COKException;
import com.geng.gameengine.*;
//import com.geng.gameengine.activity.RoseCrownActivity;
//import com.geng.gameengine.alliance.AllianceManager;
import com.geng.gameengine.mail.MailSrcFuncType;
import com.geng.puredb.model.*;
import com.geng.utils.LoggerUtil;
import com.geng.utils.LoggerUtil.GoldCostType;
import com.geng.utils.xml.ConfigMap;
import com.geng.utils.xml.GameConfigManager;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.geng.core.data.ISFSArray;
import com.geng.core.data.ISFSObject;
import com.geng.core.data.SFSArray;
import com.geng.core.data.SFSObject;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * 奖励统一处理
 */
public class RewardManager {
	private String rewardId;
	private ConfigMap rewardConfigMap;
	private ISFSArray awardArr;
	private UserProfile userProfile;
	private boolean userProfileUpdate = false;
	private boolean resourceUpdate = false;
	private boolean addSafeResource = false;
	private boolean safeResSwitchOpened = false;
	private boolean dungeonsReward;
	private double resRewardAdd=0;
	//后台自定义字段 用于处理统一的物品奖励格式 itemId;num|itemId;num|
	public static final String CustomItemField = "customItem";

	public static final String PushAlliancePoint = "al.point.push";

	private static ListMultimap<RewardType, String> typeConfigKeyMap;
	static {
		List<RewardType> rewardTypeList = Arrays.asList(RewardType.EXP,
				RewardType.WOOD, RewardType.FOOD, RewardType.IRON,
				RewardType.STONE, RewardType.GOLD,
				RewardType.CRYSTAL, RewardType.POWER,
				RewardType.ALLIANCE_POINT, RewardType.HONOR,
				RewardType.WB_SKILL, RewardType.ROSE_CROWN);

		typeConfigKeyMap = ArrayListMultimap.create(20, 2);

		for (RewardType rewardType : rewardTypeList) {
			typeConfigKeyMap.put(rewardType, rewardType.getRewardStr());
		}
		typeConfigKeyMap.put(RewardType.WOOD, "wood_safe");
		typeConfigKeyMap.put(RewardType.FOOD, "food_safe");
		typeConfigKeyMap.put(RewardType.IRON, "iron_safe");
		typeConfigKeyMap.put(RewardType.STONE, "stone_safe");
		typeConfigKeyMap.put(RewardType.CHIP, "casino");
		typeConfigKeyMap.put(RewardType.DIAMOND, "gold_casino");
		typeConfigKeyMap.put(RewardType.SILVER, "silver");
	}



	public RewardManager(String uid, String rewardId) {
		this.rewardId = rewardId;
		userProfile = UserProfile.getWithUid(uid);//GameEngine.getInstance().getUserProfile(uid);
		rewardConfigMap = GameConfigManager.getItem("reward", rewardId);
		addSafeResource = hasSafeResCfg();
		awardArr = SFSArray.newInstance();
	}

	public RewardManager(UserProfile userProfile, String rewardId,boolean dungeonsReward) {
		this.rewardId = rewardId;
		this.dungeonsReward = dungeonsReward;
		this.userProfile = userProfile;
		rewardConfigMap =  GameConfigManager.getItem("reward", rewardId);
		addSafeResource = hasSafeResCfg();
		awardArr = SFSArray.newInstance();
	}

	public RewardManager(UserProfile userProfile, String... rewardId) {
		awardArr = SFSArray.newInstance();
		this.userProfile = userProfile;
		this.rewardId = null;
		rewardConfigMap = mergeReward(rewardId);
		addSafeResource = hasSafeResCfg();
	}

	public RewardManager(UserProfile userProfile,double resAdd, String... rewardId) {
		awardArr = SFSArray.newInstance();
		this.userProfile = userProfile;
		this.rewardId = null;
		this.resRewardAdd = resAdd;
		rewardConfigMap = mergeReward(rewardId);
		addSafeResource = hasSafeResCfg();
	}
	
	public RewardManager(UserProfile userProfile, Map<String, String> rewardConfigMap) {
		awardArr = SFSArray.newInstance();
		if (rewardConfigMap instanceof ConfigMap) {
			this.rewardConfigMap = (ConfigMap) rewardConfigMap;
		} else {
			this.rewardConfigMap = new ConfigMap(rewardConfigMap);
		}
		addSafeResource = hasSafeResCfg();
		this.userProfile = userProfile;
	}

	public static void getRewardInfo(ISFSArray rewardArray,String itemId,int num){
		int item = Integer.parseInt(itemId);
		if (item < 50) {
			RewardType rewardType = RewardType.getByValue(item);
			if (rewardType != null) {
				rewardArray.addSFSObject(getRewardObj(rewardType, num));
			}
		} else if (item >= 1000000 && item <= 1999999) {
			addItemIdInfo(rewardArray, RewardType.EQUIP, itemId, num);
		} else {
			addItemIdInfo(rewardArray, RewardType.GOODS, itemId, num);
		}
	}

	public static ISFSArray getRewardInfo(String rewardId) {
		return getRewardInfo(rewardId,1);
	}

	public static ISFSArray getRewardInfo(String rewardId,int count) {
		ConfigMap rewardConfigMap = GameConfigManager.getItem("reward", rewardId);
		ISFSArray rewardArray = getRewardInfoByMap(rewardConfigMap,count);
		return rewardArray;
	}

	public static ISFSArray getRewardInfoByMap(ConfigMap rewardConfigMap,int count) {
		ISFSArray rewardArray = SFSArray.newInstance();
		for (RewardType rewardType : typeConfigKeyMap.keySet()) {
			addReward(rewardConfigMap, rewardType, typeConfigKeyMap.get(rewardType), count, rewardArray);
		}


		String goodsXmlValue = rewardConfigMap.get("item");
		if (null != goodsXmlValue) {
			Map<String, Integer> goodsReward = getRandomGoodsWithoutLimit(rewardConfigMap, count);
			if(!StringUtils.contains(goodsXmlValue, ';')){ //如果不需要随机道具奖励，则根据填写顺序排序
				for(String itemId: StringUtils.split(goodsXmlValue, "|")){
					if(goodsReward.containsKey(itemId)){
						getRewardInfo(rewardArray,itemId,goodsReward.get(itemId));
					}
				}
			}else{
				for (Map.Entry<String, Integer> goodsEntry : goodsReward.entrySet()) {
					getRewardInfo(rewardArray,goodsEntry.getKey(),goodsEntry.getValue());
				}
			}
		}
		return rewardArray;
	}

	private static void addReward(ConfigMap rewardConfigMap, RewardType rewardType, List<String> configList, int count, ISFSArray rewardArray) {
		int addValue = 0;
		for (String configKey : configList) {
			Integer xmlValue = rewardConfigMap.getInt(configKey, 0);
			if (null != xmlValue && xmlValue > 0) {
				addValue += xmlValue;
			}
		}
		if (addValue > 0) {
			rewardArray.addSFSObject(getRewardObj(rewardType, addValue * count));
		}
	}

	private static void addItemIdInfo(ISFSArray rewardArray, RewardType rewardType, String id, int num) {
			ISFSObject itemIdObj = SFSObject.newInstance();
			itemIdObj.putUtfString("id", id);
			itemIdObj.putInt("num", num);
			rewardArray.addSFSObject(getRewardObj(rewardType, itemIdObj));
 	}

	private static ISFSObject getRewardObj(RewardType type, ISFSObject object) {
		ISFSObject rewardObj = SFSObject.newInstance();
		rewardObj.putInt("type", type.ordinal());
		rewardObj.putSFSObject("value", object);
		return rewardObj;
	}

	public static ISFSObject getRewardObj(RewardType type, int value) {
		ISFSObject rewardObj = SFSObject.newInstance();
		rewardObj.putInt("type", type.ordinal());
		rewardObj.putInt("value", value);
		return rewardObj;
	}

	private final static String[] REWARD_KEYS = {"wood", "stone", "iron", "food", "silver", "gold", "exp", "power", "rate",
				"honor", "alliance_point", "casino", "gold_casino", "wbskill", "crystal", "wood_safe", "food_safe", "iron_safe", "stone_safe"};

	public static ConfigMap mergeReward(String... rewardIds) {

		GameConfigManager configManager = new GameConfigManager("reward");
		if (rewardIds.length == 1) {
			return configManager.getItem(rewardIds[0], false);
		}
		List<ConfigMap> configList = new ArrayList<>();
		for (String rewardId : rewardIds) {
			ConfigMap xml = configManager.getItem(rewardId);
			if (xml == null || xml.isEmpty()) {
				continue;
			}
			configList.add(xml);
		}
		Map<String, Integer> rewardMap = new HashMap<>();
		StringBuilder rate = new StringBuilder(), item = new StringBuilder(), num = new StringBuilder(), circle = new StringBuilder();
		for (Map<String, String> configMap : configList) {
			for (String key : REWARD_KEYS) {
				if (configMap.containsKey(key)) {
					if (key.equals("rate")) {
						rate.append(configMap.get("rate")).append('|');
						circle.append(configMap.get("circle")).append('|');
						item.append(configMap.get("item")).append('|');
						num.append(configMap.get("num")).append('|');
					} else {
						int add = Integer.parseInt(configMap.get(key));
						rewardMap.put(key, rewardMap.containsKey(key) ? add + rewardMap.get(key) : add);
					}
				}
			}
		}
		ConfigMap retConfigMap = new ConfigMap();
		for (Map.Entry<String, Integer> entry : rewardMap.entrySet()) {
			retConfigMap.put(entry.getKey(), Integer.toString(entry.getValue()));
		}
		if (rate.length() > 0) {
			retConfigMap.put("rate", rate.toString());
			retConfigMap.put("circle", circle.toString());
			retConfigMap.put("item", item.toString());
			retConfigMap.put("num", num.toString());
		}
		return retConfigMap;
	}
    // type 标记奖励来源
	public ISFSArray award(MailSrcFuncType srctype){
		if (rewardConfigMap == null) {
			return awardArr;
		}
		return award(1,srctype);
	}

	public ISFSArray awardHasRes(int multiple,MailSrcFuncType srcType){
		//查询下开关
		safeResSwitchOpened = SwitchConstant.SafeResourceGetSwitch.isSwitchOpen();
		addSafeResource = addSafeResource || safeResSwitchOpened;
		return award(multiple,srcType);
	}

	/**
	 * 判断reward.xml的配置项是否有安全资源的配置并且大于0
	 * @return
	 */
	private boolean hasSafeResCfg() {
		boolean hasSafeRes = false;
		if (rewardConfigMap != null) {
			hasSafeRes = (rewardConfigMap.containsKey("wood_safe") && Long.valueOf(rewardConfigMap.get("wood_safe")) > 0) ||
					(rewardConfigMap.containsKey("food_safe") && Long.valueOf(rewardConfigMap.get("food_safe")) > 0) ||
					(rewardConfigMap.containsKey("iron_safe") && Long.valueOf(rewardConfigMap.get("iron_safe")) > 0) ||
					(rewardConfigMap.containsKey("stone_safe") && Long.valueOf(rewardConfigMap.get("stone_safe")) > 0);
		}
		return hasSafeRes;
	}


	/**
	 * 发奖
	 * @return
     * type 奖励类型 用于标记从哪奖励的
	 */
	public ISFSArray award(int multiple,MailSrcFuncType srcType){
		if (multiple < 1) {
			return awardArr;
		}
		handleResourceReward(multiple,srcType);//有金币
		handleExpReward(multiple);
//		handleGeneralReward();
		handleRandomReward(multiple);
		handlePowerReward(multiple);
		handleCustomField(multiple);
		handleHonorReward(multiple);
		handleAlliancePoint(multiple);
		handleResourceRate(multiple,srcType); //有金币
		handleWbskill(multiple);
		if (userProfileUpdate) {
			userProfile.update();
		}
//		if (resourceUpdate) {
//			userProfile.getUserResource().update();
//			if(addSafeResource){
//				userProfile.getUserResource().synPushSafeResource(false,null);
//			}
//		}
		return awardArr;
	}

	public void handlePowerReward(int num) {

	}

	/**
	 * 资源 及 金币 奖励处理
	 */
	private void handleResourceReward(int num,MailSrcFuncType srcType) {

	}

	/**
	 * 经验奖励处理
	 */
	private void handleExpReward(int num) {

	}

	/**
	 * 处理自定义的物品字段
	 */
	private void handleCustomField(int num) {

	}

	/**
	 * 处理跨服战中的技能点
	 * */
	private void handleWbskill(int num) {

	}

	public static String getCustomRewardString(Map<String, String> exchangeConfigMap, StringBuilder sbReward) {
		if (StringUtils.isNotBlank(exchangeConfigMap.get("item"))){
			String itemArray[] = StringUtils.split(exchangeConfigMap.get("item"), '|');
			for (String item :itemArray){
				String itemInfo[] = StringUtils.split(item, ';');
//				if (itemInfo[0].equals(GoodsType.OPEN_TRAIN_POS_1.getGoodsId()) || itemInfo[0].equals(GoodsType.OPEN_TRAIN_POS_2.getGoodsId()) || itemInfo[0].equals(GoodsType.OPEN_TRAIN_POS_3.getGoodsId())) {
//					continue;
//				}
				sbReward.append("goods,").append(itemInfo[0]).append(',').append(itemInfo[1]).append('|');
			}
		}
		if (StringUtils.isNotBlank(exchangeConfigMap.get("equipment"))){
			String equipArray[] = StringUtils.split(exchangeConfigMap.get("equipment"), '|');
			for (String equip :equipArray){
				String equipInfo[] = StringUtils.split(equip, ';');
				sbReward.append("equip,").append(equipInfo[0]).append(',').append(equipInfo[1]).append('|');
			}
		}
		if(sbReward.length() > 0) {
			return sbReward.substring(0, sbReward.length() - 1);
		} else {
			return "";
		}
	}

	/**
	 * 保底物品奖励处理
	 */
	private static void controlItemNum(String uid, Map<String, String> rewardConfigMap, Map<String, Integer> sendGoodsMap) {

	}

	private void handleHonorReward(int num) {

	}

	private void handleAlliancePoint(int num) {

	}

	private static int addAlliancePoint(String allianceId, int pointAdd, UserProfile userProfile) {

		return 0;
	}

	/**
	 * 随机奖励处理
	 */
    private void handleRandomReward(int multiple) {
		Map<String, Integer> goodsReward = getRandomGoodsWithLimit(rewardConfigMap, multiple, userProfile.getUid());
		for (Map.Entry<String, Integer> goodsEntry : goodsReward.entrySet()) {
			randomRewardInfo(Integer.parseInt(goodsEntry.getKey()), goodsEntry.getValue());
		}
	}

	public static Map<String, Integer> getRandomGoodsWithoutLimit(Map<String, String> rewardConfigMap, int multiple) {
		return getRandomGoodsWithLimit(rewardConfigMap, multiple, null);
	}

	public static Map<String, Integer> getRandomGoodsWithLimit(Map<String, String> rewardConfigMap, int multiple, String uid) {
		String randomXmlValue = rewardConfigMap.get("rate");
		Map<String, Integer> retMap = new HashMap<>();
		if (null != randomXmlValue) {
			String[] rateCircle = StringUtils.split(rewardConfigMap.get("circle"), '|');
			String[] rateArray = StringUtils.split(randomXmlValue, '|');
			String[] itemArray = StringUtils.split(rewardConfigMap.get("item"), '|');
			String[] numArray = StringUtils.split(rewardConfigMap.get("num"), '|');
			int itemIndex = 0;
			for (String rateStr : rateArray) {
				String item[] = StringUtils.split(itemArray[itemIndex], ';');
				String num[] = StringUtils.split(numArray[itemIndex], ';');
				int loopTimes = Integer.parseInt(rateCircle[itemIndex]);
				loopTimes *= multiple;
				for (int index = 0; index < loopTimes; index++) {
					int randomKey = randomKey(rateStr);
					if (randomKey > item.length - 1 || StringUtils.isBlank(item[randomKey])) {
						continue;
					}
					if (retMap.containsKey(item[randomKey])) {
						retMap.put(item[randomKey], retMap.get(item[randomKey]) + Integer.parseInt(num[randomKey]));
					} else {
						retMap.put(item[randomKey], Integer.parseInt(num[randomKey]));
					}
				}
				itemIndex++;
			}
		}
		if (StringUtils.isNotBlank(uid)) {
			controlItemNum(uid, rewardConfigMap, retMap);
		}
		return retMap;
	}

	private void randomRewardInfo(int item, int num) {
		if (item < 50) {
			RewardType rewardType = RewardType.getByValue(item);
			if (rewardType != null) {
				toRewardInfo(rewardType, num);
			}
		} else if (item >= 1000000 && item <= 1999999) {
			/*List<UserEquip> equipList = EquipService.awardEquip(userProfile.getUid(), item, num);
			if (equipList != null && equipList.size() > 0) {
				for (UserEquip equip : equipList) {
					toRewardInfo(RewardType.EQUIP, equip.toSFSObject());
				}
			}*/
		} else {
			//有时间 加一个 批量添加道具的接口 todo
			ISFSObject goodsObj ;
			//if(rewardLong>=1003000 && rewardLong <=1004000){//地下城
//			if(dungeonsReward){
//				UserDungeonsItem goodItem =  DungeonsManager.addItem(userProfile, Integer.toString(item), num);
//				goodsObj = goodItem.toSFSObject();
//			}else {
				UserItem goodItem = ItemManager.addItem(userProfile, Integer.toString(item), num, 0, LoggerUtil.GoodsGetType.REWARD_RANDOM);
				goodsObj = goodItem.toSFSObject();
//			}

			goodsObj.putInt("rewardAdd", num);
			toRewardInfo(RewardType.GOODS, goodsObj);
		}
	}

	public static int randomKey(String rateStr) {
		String[] rate = StringUtils.split(rateStr, ';');
		int randomSum = 0;
		int[] rateInt = new int[rate.length];
		int index = 0;
		for (String oneRate : rate) {
			int value = Integer.parseInt(oneRate);
			randomSum += value;
			rateInt[index] = value;
			index++;
		}
		int randValue = new Random().nextInt(randomSum);
		int randKey = 0;
		index = 0;
		for (int oneRate : rateInt) {
			if (randValue < oneRate) {
				randKey = index;
				break;
			}
			randValue -= oneRate;
			index++;
		}
		return randKey;
	}

/*	public UserGeneral handleGeneralReward() {
		String generalKey = RewardType.GENERAL.toString().toLowerCase();
		UserGeneral ug = null;
		if(rewardConfigMap.containsKey(generalKey)) {
			Map<String, String> generalMap = getGeneralMap(rewardConfigMap.get(generalKey), null);
			if (generalMap.size() > 0) {
				ug = userProfile.getUgManager().addGeneral(generalMap.get("generalId"), Integer.parseInt(generalMap.get("generalLevel")));
				if (ug != null) {
					ISFSObject genObj = ug.toSFSObject();
					toRewardInfo(RewardType.GENERAL, genObj);
				}
			}
		}
		return ug;
	}*/

	private static Map<String, String> getGeneralMap(String genRewardStr, Set<String> generalMove) {
		String genRandom[] = StringUtils.split(genRewardStr, '|');
		int PERCENT = 1000;
		Set<Integer> removeIndex = new HashSet<>();
		if (generalMove != null) {
			for (int i = 0; i < genRandom.length; i++) {
				String general[] = StringUtils.split(genRandom[i], ';'); //array(id, level, rate)
				if (generalMove.contains(general[0])) {
					PERCENT -= Integer.parseInt(general[2]);
					removeIndex.add(i);
				}
			}
		}
		int random = new Random().nextInt(PERCENT);
		Map<String,String> resultMap = new HashMap<>();
		for (int i = 0; i < genRandom.length; i++) {
			if (removeIndex.contains(i)) {
				continue;
			}
			String general[] = StringUtils.split(genRandom[i], ';'); //array(id, level, rate)
			int rate = Integer.parseInt(general[2]);
			if (random < rate) {
				resultMap.put("generalId", general[0]);
				resultMap.put("generalLevel", general[1]);
				break;
			}
			random -= rate;
		}
		return resultMap;
	}

	/**
	 * @param generalMove 不想刷出来的将 没有填null
	 */
	public static Map<String, String> randomGeneral(String rewardId, Set<String> generalMove) {
		return getGeneralMap(new GameConfigManager("reward").getItem(rewardId).get("general"), generalMove);
	}

	/**
	 * @param generalMove 不想刷出来的将 没有填null
	 */
	public static Map<String, String> randomGeneral(Map<String, String> rewardMap, Set<String> generalMove) {
		return getGeneralMap(rewardMap.get("general"), generalMove);
	}

	private void toRewardInfo(RewardType rewardType, int value) {
		toRewardInfo(rewardType, value, 0);
	}

	/**
	 *
	 */
	private void toRewardInfo(RewardType rewardType, int value, int total) {
		ISFSObject rewardObj = SFSObject.newInstance();
		rewardObj.putInt("type", rewardType.ordinal());
		rewardObj.putInt("value", value);

		awardArr.addSFSObject(rewardObj);
	}

	/**
	 * 将军 道具奖励
	 */
	private void toRewardInfo(RewardType rewardType, ISFSObject value) {
		ISFSObject expObj = SFSObject.newInstance();
		expObj.putInt("type", rewardType.ordinal());
		expObj.putSFSObject("value", value);
		awardArr.addSFSObject(expObj);
	}

	private void handleResourceRate(int num,MailSrcFuncType srcType) {

	}

	public enum RewardType {
		WOOD("wood"), STONE("stone"), IRON("iron"), FOOD("food"),
		SILVER("money"), GOLD("gold"), EXP("exp"), GOODS("goods"),
		GENERAL("general"), POWER("power"), HONOR("honor"),
		ALLIANCE_POINT("alliance_point"), CHIP("chip"), DIAMOND("diamond"),
		EQUIP("equip"),DRAGON_FOOD(""),DRAGON_GOLD(""), EFFECT(""),
		WB_SKILL("wbskill"), //跨服技能点
		CRYSTAL("crystal"),
		ROSE_CROWN("rose_crown"),
		SOLDIER("soldier"),
		QUICKDNF_ENERGY("energy"),
		;

		private String rewardStr;//奖励描述信息类型字段

		RewardType(String rewardStr) {
			this.rewardStr = rewardStr;
		}

        public static RewardType getByValue(int value) {
            RewardType[] ges = values();
            for (RewardType ge : ges) {
                if (ge.ordinal() == value) {
                    return ge;
                }
            }
            return null;
        }

		public String getRewardStr() {
			return rewardStr;
		}
    }

	public enum SafeResourceRewardType {
		WOOD_SAFE("wood_safe"),
		STONE_SAFE("stone_safe"),
		IRON_SAFE("iron_safe"),
		FOOD_SAFE("food_safe");

		private String rewardStr;//奖励描述信息类型字段

		SafeResourceRewardType(String rewardStr) {
			this.rewardStr = rewardStr;
		}

		public static SafeResourceRewardType getByValue(int value) {
			SafeResourceRewardType[] ges = values();
			for (SafeResourceRewardType ge : ges) {
				if (ge.ordinal() == value) {
					return ge;
				}
			}
			return null;
		}

		public String getRewardStr() {
			return rewardStr;
		}
	}

	/**
	 * 艾莎公主小累充封装奖励信息（只能包涵item）
	 */
	public static String getElsaRewardString(String rewardId, String uid) {
		if (StringUtils.isBlank(rewardId)) {
			return null;
		}
		Map<String, String> rewardConfigMap = new GameConfigManager("reward").getItem(rewardId);
		return getElsaReward(rewardConfigMap, uid);
	}

	public static String getElsaReward(Map<String, String> rewardConfigMap, String uid) {
		StringBuilder reward = new StringBuilder();
		if(rewardConfigMap.containsKey("item") && rewardConfigMap.containsKey("num")){
			String[] itemArray = StringUtils.split(rewardConfigMap.get("item"), '|');
			String[] numArray = StringUtils.split(rewardConfigMap.get("num"), '|');
			for(int i = 0; i < itemArray.length; i++){
				reward.append("goods,").append(itemArray[i]).append(',').append(numArray[i]).append('|');
			}
		}
		return reward.toString();
	}

	/**
	 * 封装奖励信息
	 */
	public static String getRewardString(String rewardId, String uid) {
		if (StringUtils.isBlank(rewardId)) {
			return null;
		}
		Map<String, String> rewardConfigMap = new GameConfigManager("reward").getItem(rewardId);
		return getRewardString(rewardConfigMap, uid);
	}

	/**
	 * 封装奖励信息
	 */
	public static String getRewardString(Map<String, String> rewardConfigMap, String uid) {
		if (rewardConfigMap == null || rewardConfigMap.isEmpty()) {
			return null;
		}
		StringBuilder reward = new StringBuilder();
		for (RewardType rewardType : RewardType.values()) {
			if (rewardType == RewardType.GOODS || rewardType == RewardType.GENERAL) {
				continue;
			}
			String xmlRewardKey = rewardType.getRewardStr();
			if (rewardType == RewardType.SILVER) {
				xmlRewardKey = "silver";
			} else if (rewardType == RewardType.CHIP) {
				xmlRewardKey = "casino";
			} else if (rewardType == RewardType.DIAMOND) {
				xmlRewardKey = "gold_casino";
			}
			if (rewardConfigMap.containsKey(xmlRewardKey)) {
				reward.append(rewardType.getRewardStr()).append(",0,").append(rewardConfigMap.get(xmlRewardKey)).append('|');
			}
		}
		//考虑非安全资源类型
		for (SafeResourceRewardType rewardType : SafeResourceRewardType.values()) {
			String xmlRewardKey = rewardType.getRewardStr();
			if (rewardConfigMap.containsKey(xmlRewardKey)) {
				reward.append(rewardType.getRewardStr()).append(",0,").append(rewardConfigMap.get(xmlRewardKey)).append('|');
			}
		}
		String xmlResourceRate = rewardConfigMap.get("resource_rate");
		if (StringUtils.isNotBlank(xmlResourceRate)) {
			String resourceRates[] = StringUtils.split(xmlResourceRate, '|');
			for (String resourceRate: resourceRates) {
				String resource[] = StringUtils.split(resourceRate, ';');
				int type = Integer.parseInt(resource[0]);
				int min = Integer.parseInt(resource[1]);
				int max = Integer.parseInt(resource[2]);
				int k = Integer.parseInt(resource[3]);
				float r = new Random().nextFloat();
				int count = (int)(min+(max-min)*(k/100f*(1+k/100f)/(1+k/100f-r)-k/100f));
				if (count > 0) {
					String rewardKey = "";
					switch (type) {
						case 1://木头
							rewardKey = RewardType.WOOD.getRewardStr();
							break;
						case 2://粮食
							rewardKey = RewardType.FOOD.getRewardStr();
							break;
						case 3://铁矿
							rewardKey = RewardType.IRON.getRewardStr();
							break;
						case 4://秘银
							rewardKey = RewardType.STONE.getRewardStr();
							break;
						case 5://金币
							rewardKey = RewardType.GOLD.getRewardStr();
							break;
						case 6://经验
							rewardKey = RewardType.EXP.getRewardStr();
							break;
						case 7://联盟荣誉 积分
							reward.append("honor,0,").append(count).append('|');
							rewardKey = RewardType.ALLIANCE_POINT.getRewardStr();
							break;
						case 8://铜币
							rewardKey = RewardType.CHIP.getRewardStr();
							break;
						case 9://龙币
							rewardKey = RewardType.DIAMOND.getRewardStr();
							break;
						case 10://钢
							rewardKey = RewardType.SILVER.getRewardStr();
							break;
						case 11://水晶
							rewardKey = RewardType.CRYSTAL.getRewardStr();
							break;
						case 12://玫瑰花冠
							rewardKey = RewardType.ROSE_CROWN.getRewardStr();
							break;
					}
					if (StringUtils.isNotBlank(rewardKey)) {
						reward.append(rewardKey).append(",0,").append(count).append('|');
					}
				}
			}
		}
		if(rewardConfigMap.containsKey("item")) {
			Map<String, Integer> goodsReward = getRandomGoodsWithLimit(rewardConfigMap, 1, uid);
			for (Map.Entry<String, Integer> goodsEntry : goodsReward.entrySet()) {
				int id = Integer.parseInt(goodsEntry.getKey());
				if (id < 50) {
					RewardType rewardType = RewardType.getByValue(id);
					if (rewardType != null) {
						reward.append(rewardType.getRewardStr()).append(",0,").append(goodsEntry.getValue()).append('|');
					}
				} else if (id >= 1000000 && id <= 1999999) {
					reward.append("equip,").append(goodsEntry.getKey()).append(',').append(goodsEntry.getValue()).append('|');
				} else {
					reward.append("goods,").append(goodsEntry.getKey()).append(',').append(goodsEntry.getValue()).append('|');
				}
			}
		}
		String customFieldValue = rewardConfigMap.get(CustomItemField);
		if (null != customFieldValue) {
			String itemArray[] = StringUtils.split(customFieldValue, '|');
			for (String item :itemArray) {
				String itemInfo[] = StringUtils.split(item, ';');
				if (itemInfo == null || itemInfo.length < 2) {
					continue;
				}
//				if (itemInfo[0].equals(GoodsType.OPEN_TRAIN_POS_1.getGoodsId()) || itemInfo[0].equals(GoodsType.OPEN_TRAIN_POS_2.getGoodsId()) || itemInfo[0].equals(GoodsType.OPEN_TRAIN_POS_3.getGoodsId())) {
//					continue;
//				}
				int id = Integer.parseInt(itemInfo[0]);
				int value = Integer.parseInt(itemInfo[1]);
				if (id < 50) {
					RewardType rewardType = RewardType.getByValue(id);
					if (rewardType != null) {
						reward.append(rewardType.getRewardStr()).append(",0,").append(value).append('|');
					}
				} else if (id >= 1000000 && id <= 1999999) {
					reward.append("equip,").append(id).append(',').append(value).append('|');
				} else {
					reward.append("goods,").append(id).append(',').append(value).append('|');
				}
			}
		}
		if(rewardConfigMap.containsKey("general")) {
			String gens[] = StringUtils.split(rewardConfigMap.get("general"), '|');
			String general[] = StringUtils.split(gens[0], ';');
			reward.append("general,").append(general[0]).append(',').append(general[1]).append("|");
		}
		if(reward.length() > 0) {
			reward.setLength(reward.length() - 1);
			return reward.toString();
		} else {
			return "";
		}
	}

	public static String appendGoodsStr(String src, String goodsId, int num) {
		if (StringUtils.isBlank(src)) {
			return new StringBuilder("goods,").append(goodsId).append(',').append(num).toString();
		} else {
			return new StringBuilder(src).append('|').append("goods,").append(goodsId).append(',').append(num).toString();
		}
	}
    //这个函数,虽然mailSrcType 为0, 但是costType 细分了
    public static ISFSObject sendActivityReward(UserProfile userProfile, long actStartTime, String reward, boolean isMail,GoldCostType costType) {
        return sendActivityReward(userProfile,actStartTime,reward,isMail,costType,0);
    }
    //isMail true 领取的是安全资源
        public static ISFSObject sendActivityReward(UserProfile userProfile, long actStartTime, String reward, boolean isMail,GoldCostType costType,int mailSrcType) {
		ISFSObject retObj = new SFSObject();

		return retObj;
	}

	public void setResRewardAdd(double resRewardAdd) {
		this.resRewardAdd = resRewardAdd;
	}
}
