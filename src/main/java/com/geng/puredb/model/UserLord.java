package com.elex.cok.puredb.model;

import com.elex.cok.core.GameEngine;
import com.elex.cok.core.GameScheduler;
import com.elex.cok.exceptions.COKException;
import com.elex.cok.exceptions.GameExceptionCode;
import com.elex.cok.gameengine.FreshRechargeService;
import com.elex.cok.gameengine.GoldExchangeInfo;
import com.elex.cok.gameengine.SevenDayExchangeService;
import com.elex.cok.gameengine.mail.MailServicePlus;
import com.elex.cok.gameengine.mail.MailSrcFuncType;
import com.elex.cok.gameengine.mail.MailType;
import com.elex.cok.puredb.dao.UserLordMapper;
import com.elex.cok.utils.*;
import com.elex.cok.utils.xml.GameConfigManager;
import com.smartfoxserver.v2.entities.User;
import com.smartfoxserver.v2.entities.data.ISFSObject;
import com.smartfoxserver.v2.entities.data.SFSObject;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class UserLord  implements Serializable {

    private String uid;
    private int firstJoinAllianceFlag;	//第一次加入联盟标志(1为第一次，0为不是第一次)
    private String userSign;			//玩家签名
    private String medal;			//勋章
	private int levelUp;				//升级奖励的领取进度
    private int newPlayerAwardSoldierFlag; //新手送兵
	private long lastPayTime;			//上一次支付的时间
	private int pveLevel;				//打野怪等级
    private int firstBindAccountRewardFlag; //第一次绑定领取过奖励了吗(1 true; 0 false)
	private String firstBindShareRewardStatus; //第一次分享领取奖励了吗(type;1|type;0  1 true; 0 false)
	private long dailyTaskFlushTime;	//每日任务刷新时间
	private int dailyTaskComplete;		//每日任务完成ID
	private int isHDLogin;		//每日任务完成ID
	private int freshRecharge;				//新手充值标记
	private int freshRechargeTotal;			//新手充值数量
	private String loginLink;		//deep link 登录标记
	private long dailyActiveTime; //每日活跃度刷新时间
	private long dailyFirstEnterMapTime;  // 每日第一次进入大地时间
	private int npcGarrison;      //npc驻守
	private long payTotal;					//充值金币总额(知己在内存里) 每次登录记录做为礼包判断条件 不做动态更新
	private int daily7GiftCount; //7日礼包编号
	private long daily7GiftTime;//7日礼包购买时间
	private int daily7Gift2Count; //7日礼包编号
	private long daily7Gift2Time;//7日礼包购买时间
	private int daily7Gift3Count; //7日礼包编号
	private long daily7Gift3Time;//7日礼包购买时间
	private int daily7Gift4Count; 	//新手第一阶段7日礼包次数
	private long daily7Gift4Time;	//新手第一阶段的7日礼包下次可以购买时间
	private int daily7Gift5Count; 	//新手第二阶段7日礼包次数
	private long daily7Gift5Time;	//新手第二阶段的7日礼包下次可以购买时间
	private int daily7Gift6Count; 	//新手第三阶段7日礼包次数
	private long daily7Gift6Time;	//新手第三阶段的7日礼包下次可以购买时间


    private transient UserProfile userProfile;

	private transient ScheduledFuture<?> scheduledFuture;

	public static final String DAILY7_GIFT_FIRST_STAGE_TYPE = "80";
	public static final String DAILY7_GIFT_SECOND_STAGE_TYPE = "81";
	public static final String DAILY7_GIFT_THIRD_STAGE_TYPE = "82";

    public static UserLord init(SqlSession session, UserProfile userProfile) {
	    UserLord userLord = new UserLord();
        userLord.setUserProfile(userProfile);
		userLord.setUid(userProfile.getUid());
        userLord.setUserSign("");
        userLord.setFirstJoinAllianceFlag(1);
		userLord.setLevelUp(1);
		userLord.setLastPayTime(userProfile.getRegTime());
		userLord.setPveLevel(0);
        userLord.setFirstBindAccountRewardFlag(0);
		userLord.setFirstBindShareRewardStatus("");
        userLord.setDailyTaskFlushTime(System.currentTimeMillis());
		userLord.setDailyTaskComplete(0);
		userLord.setLoginLink("");
		userLord.setDailyActiveTime(0);
		userLord.setNpcGarrison(0);
		userLord.setFreshRecharge(0);
		userLord.setFreshRechargeTotal(0);
		userLord.setDaily7GiftCount(0);
		userLord.setDaily7GiftTime(0);
		userLord.setDaily7Gift2Count(0);
		userLord.setDaily7Gift2Time(0);
		userLord.setDaily7Gift3Count(0);
		userLord.setDaily7Gift3Time(0);
		userLord.setDaily7Gift4Count(0);
		userLord.setDaily7Gift4Time(0);
		userLord.setDaily7Gift5Count(0);
		userLord.setDaily7Gift5Time(0);
		userLord.setDaily7Gift6Count(0);
		userLord.setDaily7Gift6Time(0);
		session.getMapper(UserLordMapper.class).insert(userLord);
		return userLord;
    }
    
    public void getLoginInfo(UserProfile userProfile, ISFSObject outData) {
    	ISFSObject lordObj = SFSObject.newInstance();
		checkLevelUp();
		checkDaily7Gift();
		lordObj.putInt("levelUp", levelUp);
		lordObj.putLong("nextDay", DateUtils.getNewDay());
		lordObj.putInt("pveLevel", pveLevel);
		lordObj.putUtfString("medal", medal == null ? "" : medal);
		lordObj.putInt("freshRechargeTotal", freshRechargeTotal);
		SevenDayExchangeService.updateSevenDayInfo(this, lordObj);
		//查询该用户是否还处于新手阶段
		boolean isRegAfter = GoldExchangeInfo.isRegAfterDaily7GiftForNewPlayerOpen(userProfile.getRegTime());
		daily7GiftSFS(lordObj, isBoughtAllNewDaily7GiftsByType(DAILY7_GIFT_FIRST_STAGE_TYPE, isRegAfter));
		daily7Gift2SFS(lordObj, isBoughtAllNewDaily7GiftsByType(DAILY7_GIFT_SECOND_STAGE_TYPE, isRegAfter));
		daily7Gift3SFS(lordObj, isBoughtAllNewDaily7GiftsByType(DAILY7_GIFT_THIRD_STAGE_TYPE, isRegAfter));
    	outData.putSFSObject("lord", lordObj);
    }

	private void checkDaily7Gift(){
		Map<String,String> configMap = new GameConfigManager("item").getItem("daily7Gift");
		long begin = CommonUtils.parseTimeStamp("yyyy-MM-dd", configMap.get("k3"));
		long end = CommonUtils.parseTimeStamp("yyyy-MM-dd", configMap.get("k4"));
		long now = System.currentTimeMillis();
		if(now<begin || now>end){//活动时间外，数据清0
			if(daily7GiftTime !=0 && (daily7GiftTime<begin || daily7GiftTime>end)){
				daily7GiftTime = 0;
				daily7GiftCount = 0;
				updateDaily7Gift();
			}
			if(daily7Gift2Time !=0 && (daily7Gift2Time<begin || daily7Gift2Time>end)){
				daily7Gift2Time = 0;
				daily7Gift2Count = 0;
				updateDaily7Gift2();
			}
			if(daily7Gift3Time !=0 && (daily7Gift3Time<begin || daily7Gift3Time>end)){
				daily7Gift3Time = 0;
				daily7Gift3Count = 0;
				updateDaily7Gift3();
			}
			if(daily7Gift4Time !=0 && (daily7Gift4Time<begin || daily7Gift4Time>end)){
				daily7Gift4Time = 0;
				daily7Gift4Count = 0;
				updateDaily7Gift4();
			}
			if(daily7Gift5Time !=0 && (daily7Gift5Time<begin || daily7Gift5Time>end)){
				daily7Gift5Time = 0;
				daily7Gift5Count = 0;
				updateDaily7Gift5();
			}
			if(daily7Gift6Time !=0 && (daily7Gift6Time<begin || daily7Gift6Time>end)){
				daily7Gift6Time = 0;
				daily7Gift6Count = 0;
				updateDaily7Gift6();
			}
		} else {
			//因为支持了老手7日礼包的循环购买，所以需要将那些之前已经购买完老手7日礼包的用户重置，否则他们将无法循环购买
			if (daily7GiftCount >= 7) {
				daily7GiftTime = 0;
				daily7GiftCount = 0;
				updateDaily7Gift();
			}
			if (daily7Gift2Count >= 7) {
				daily7Gift2Time = 0;
				daily7Gift2Count = 0;
				updateDaily7Gift2();
			}
			if (daily7Gift3Count >= 7) {
				daily7Gift3Time = 0;
				daily7Gift3Count = 0;
				updateDaily7Gift3();
			}
		}
	}

	public void disConnect(){
		if(scheduledFuture != null) {
			scheduledFuture.cancel(true);
		}
	}

	private void sendMail(boolean isForNew){
		int firstStageCount, secondStageCount, thirdStageCount;
		long firstStageTime, secondStageTime, thirdStageTime;
		if (isForNew) {
			firstStageCount = daily7Gift4Count;
			secondStageCount = daily7Gift5Count;
			thirdStageCount = daily7Gift6Count;
			firstStageTime = daily7Gift4Time;
			secondStageTime = daily7Gift5Time;
			thirdStageTime = daily7Gift6Time;
		} else {
			firstStageCount = daily7GiftCount;
			secondStageCount = daily7Gift2Count;
			thirdStageCount = daily7Gift3Count;
			firstStageTime = daily7GiftTime;
			secondStageTime = daily7Gift2Time;
			thirdStageTime = daily7Gift3Time;
		}
		disConnect();
		long now = System.currentTimeMillis();
		String exchangeId = null;
		long time = 0;
		if(firstStageTime>now){
			String daily7GiftId = GoldExchangeInfo.getDaily7GiftId(uid, firstStageCount, DAILY7_GIFT_FIRST_STAGE_TYPE, isForNew);
			if(StringUtils.isNotEmpty(daily7GiftId)) {
				exchangeId = daily7GiftId;
				time = firstStageTime;
			}
		}
		if(secondStageTime>now && (time==0 || secondStageTime<time)){
			String daily7GiftId = GoldExchangeInfo.getDaily7GiftId(uid, secondStageCount, DAILY7_GIFT_SECOND_STAGE_TYPE, isForNew);
			if(StringUtils.isNotEmpty(daily7GiftId)) {
				exchangeId = daily7GiftId;
				time = secondStageTime;
			}
		}
		if(thirdStageTime>now && (time==0 || thirdStageTime<time)){
			String daily7GiftId = GoldExchangeInfo.getDaily7GiftId(uid, thirdStageCount, DAILY7_GIFT_THIRD_STAGE_TYPE, isForNew);
			if(StringUtils.isNotEmpty(daily7GiftId)) {
				exchangeId = daily7GiftId;
				time = thirdStageTime;
			}
		}
		if(StringUtils.isNotEmpty(exchangeId) && time>0) {
			final String id = exchangeId;
			scheduledFuture = GameScheduler.getInstance().schedule(new Runnable() {
				@Override
				public void run() {
					List<String> list = new ArrayList<>();
					String percent = GoldExchangeInfo.getExchangeConfig(uid).getItem(id).get("percent");
					list.add(percent);
					MailServicePlus.sendMailByMailXmlWithParam(uid, "111670", null, MailType.System, list, MailSrcFuncType.USER_LORD);
				}
			},(int)(time-now-60*1000),TimeUnit.MILLISECONDS);
		}
	}

	public void daily7GiftSFS(ISFSObject obj, boolean isForNew){
		int giftCount;
		long giftTime;
		if (isForNew) {
			giftCount = daily7Gift4Count;
			giftTime = daily7Gift4Time;
		} else {
			giftCount = daily7GiftCount;
			giftTime = daily7GiftTime;
		}
		String daily7GiftId = GoldExchangeInfo.getDaily7GiftId(uid, giftCount, DAILY7_GIFT_FIRST_STAGE_TYPE, isForNew);
		boolean has = false;
		if(StringUtils.isNotEmpty(daily7GiftId)) {
			obj.putUtfString("daily7GiftId", daily7GiftId);
			obj.putLong("daily7GiftTime", giftTime / 1000);
			has = true;
			sendMail(isForNew);
		}else{
			obj.putUtfString("daily7GiftId", "0000");
		}
		obj.putBool("hasDaily7Gift", has);
	}

	public void daily7Gift2SFS(ISFSObject obj, boolean isForNew){
		int giftCount;
		long giftTime;
		if (isForNew) {
			giftCount = daily7Gift5Count;
			giftTime = daily7Gift5Time;
		} else {
			giftCount = daily7Gift2Count;
			giftTime = daily7Gift2Time;
		}
		String daily7GiftId = GoldExchangeInfo.getDaily7GiftId(uid, giftCount, DAILY7_GIFT_SECOND_STAGE_TYPE, isForNew);
		boolean has = false;
		if(StringUtils.isNotEmpty(daily7GiftId)) {
			obj.putUtfString("daily7Gift2Id", daily7GiftId);
			obj.putLong("daily7Gift2Time", giftTime / 1000);
			has = true;
			sendMail(isForNew);
		}else{
			obj.putUtfString("daily7Gift2Id", "0000");
		}
		obj.putBool("hasDaily7Gift2",has);
	}

	public void daily7Gift3SFS(ISFSObject obj, boolean isForNew){
		int giftCount;
		long giftTime;
		if (isForNew) {
			giftCount = daily7Gift6Count;
			giftTime = daily7Gift6Time;
		} else {
			giftCount = daily7Gift3Count;
			giftTime = daily7Gift3Time;
		}
		String daily7GiftId = GoldExchangeInfo.getDaily7GiftId(uid, giftCount, DAILY7_GIFT_THIRD_STAGE_TYPE, isForNew);
		boolean has = false;
		if(StringUtils.isNotEmpty(daily7GiftId)) {
			obj.putUtfString("daily7Gift3Id", daily7GiftId);
			obj.putLong("daily7Gift3Time", giftTime / 1000);
			has = true;
			sendMail(isForNew);
		}else{
			obj.putUtfString("daily7Gift3Id", "0000");
		}
		obj.putBool("hasDaily7Gift3",has);
	}

	private void checkLevelUp() {
		int notReceive = userProfile.getLevel() - levelUp;
		if (notReceive > 0) {
			GameConfigManager roleConfig = new GameConfigManager("role");
			for (int index = 1; index <= notReceive; index++) {
				if (!StringUtils.isBlank(roleConfig.getItem(Integer.toString(Constants.ROLE_BASE_VALUE + (levelUp + index - 1))).get("level_reward"))) {
					break;
				} else {
					levelUp++;
				}
			}
		}
	}

	public static void onLogin(SqlSession session, UserProfile userProfile) {
		UserLord userLord = UserLord.getWithUid(session, userProfile.getUid());
		if (userLord != null) {
			userLord.payTotal = userProfile.getPayTotal();
			userProfile.setUserLord(userLord);
			userLord.setUserProfile(userProfile);
		}
	}

	public static UserLord getWithUid(SqlSession session, String uid) {
		UserLord lord = null;
		if(session == null) {
			session = MyBatisSessionUtil.getInstance().getSession();
			try {
				UserLordMapper userLordMapper = session.getMapper(UserLordMapper.class);
				lord = userLordMapper.select(uid);
			} finally {
				session.close();
			}
		} else {
			UserLordMapper userLordMapper = session.getMapper(UserLordMapper.class);
			lord = userLordMapper.select(uid);
		}
		return lord;
	}

    private void updateSign(String userSign) {
        SqlSession session = MyBatisSessionUtil.getInstance().getSession();
        try {
            UserLordMapper userLordMapper = session.getMapper(UserLordMapper.class);
            userLordMapper.updateSign(this.getUid(), userSign);
            session.commit();
        } finally {
            session.close();
        }
    }

	public void updateMedal() {
		SqlSession session = MyBatisSessionUtil.getInstance().getSession();
		try {
			UserLordMapper userLordMapper = session.getMapper(UserLordMapper.class);
			userLordMapper.updateMedal(uid, medal);
			session.commit();
		} finally {
			session.close();
		}
	}

    public void updateFirstJoinAllianceFlag(int firstJoinAllianceFlag) {
        this.setFirstJoinAllianceFlag(firstJoinAllianceFlag);
        SqlSession session = MyBatisSessionUtil.getInstance().getSession();
        try {
            UserLordMapper userLordMapper = session.getMapper(UserLordMapper.class);
			userLordMapper.updateFirstJoinAllianceFlag(this.getUid(), firstJoinAllianceFlag);
            session.commit();
        } finally {
            session.close();
        }
    }

    public void updateBindAccountFlag() {
        SqlSession session = MyBatisSessionUtil.getInstance().getSession();
        try {
            UserLordMapper userLordMapper = session.getMapper(UserLordMapper.class);
			userLordMapper.updateBindAccountFlag(uid, firstBindAccountRewardFlag);
            session.commit();
        } finally {
            session.close();
        }
    }

	public synchronized int updateLevelUp(int newLevel) throws COKException {
		if (levelUp >= newLevel) {
			throw new COKException(GameExceptionCode.INVALID_OPT, "cann't receive repeatly");
		}
		levelUp = newLevel;
		SFSMysql.getInstance().execute("UPDATE user_lord SET levelUp = ? WHERE uid = ?", new Object[]{levelUp, uid});
		return levelUp;
	}

	public void updateLastPayTime() {
		SFSMysql.getInstance().execute("UPDATE user_lord SET lastPayTime = ? WHERE uid = ?", new Object[]{lastPayTime, uid});
	}

	public void updateFreshRecharge(int rechargeValue) {
		freshRecharge = 1;
		freshRechargeTotal += rechargeValue;
		SqlSession session = MyBatisSessionUtil.getInstance().getSession();
		try {
			session.getMapper(UserLordMapper.class).updateFreshRecharge(uid, lastPayTime, freshRecharge, freshRechargeTotal);
			session.commit();
		} finally {
			session.close();
		}
		if (userProfile != null) {
			User user = userProfile.getSfsUser();
			if (user != null) {
				ISFSObject pushObj = SFSObject.newInstance();
				pushObj.putInt("freshRechargeTotal", freshRechargeTotal);
				GameEngine.getInstance().pushMsg(FreshRechargeService.PUSH_FRESH_RECHARGE, pushObj, user);
			}
		}
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid == null ? null : uid.trim();
	}

	public void setUserSign(String userSign) {
		this.userSign = userSign;
	}

	public String getUserSign() {
		return userSign;
	}

	public void setUserProfile(UserProfile userProfile) {
		this.userProfile = userProfile;
		if (userProfile.getLevel() < levelUp) {
			this.levelUp = userProfile.getLevel();
		}
	}

	public int getLevelUp() {
		return levelUp;
	}

	public void setLevelUp(int levelUp) {
		this.levelUp = levelUp;
	}

	public int getFirstJoinAllianceFlag() {
		return firstJoinAllianceFlag;
	}

	public void setFirstJoinAllianceFlag(int firstJoinAllianceFlag) {
		this.firstJoinAllianceFlag = firstJoinAllianceFlag;
	}

	public void setSign(String userSign) {
		this.setUserSign(userSign);
		this.updateSign(userSign);
	}

	public long getDailyFirstEnterMapTime() {
		return dailyFirstEnterMapTime;
	}

	public void setDailyFirstEnterMapTime(long dailyFirstEnterMapTime) {
		this.dailyFirstEnterMapTime = dailyFirstEnterMapTime;
	}

	public long getLastPayTime() {
		return lastPayTime;
	}

	public String getMedal() {
		return medal;
	}

	public void setMedal(String medal) {
		this.medal = medal;
	}

	public void setLastPayTime(long lastPayTime) {
		this.lastPayTime = lastPayTime;
	}

    public int getNewPlayerAwardSoldierFlag() {
        return newPlayerAwardSoldierFlag;
    }

    public void setNewPlayerAwardSoldierFlag(int newPlayerAwardSoldierFlag) {
        this.newPlayerAwardSoldierFlag = newPlayerAwardSoldierFlag;
    }

	public int getPveLevel() {
		return pveLevel;
	}

	public void setPveLevel(int pveLevel) {
		this.pveLevel = pveLevel;
	}

    public int getFirstBindAccountRewardFlag() {
        return firstBindAccountRewardFlag;
    }

    public void setFirstBindAccountRewardFlag(int firstBindAccountRewardFlag) {
        this.firstBindAccountRewardFlag = firstBindAccountRewardFlag;
    }

    public void updateNewPlayerAwardSoldiersFlag(int i) {
        newPlayerAwardSoldierFlag = i;
        SqlSession session = MyBatisSessionUtil.getInstance().getSession();
        try {
            UserLordMapper userLordMapper = session.getMapper(UserLordMapper.class);
            userLordMapper.updateAwardSoldierFlag(uid, newPlayerAwardSoldierFlag);
            session.commit();
        } finally {
            session.close();
        }
    }

	public void updatePveLevel(int i) {
		pveLevel = i;
		SqlSession session = MyBatisSessionUtil.getInstance().getSession();
		try {
			UserLordMapper userLordMapper = session.getMapper(UserLordMapper.class);
			userLordMapper.updatePveLevel(uid, pveLevel);
			session.commit();
		} finally {
			session.close();
		}
	}

	public void updateTaskFlushTime() {
		SqlSession session = MyBatisSessionUtil.getInstance().getSession();
		try {
			UserLordMapper userLordMapper = session.getMapper(UserLordMapper.class);
			userLordMapper.updateTaskFlushTime(uid, dailyTaskFlushTime);
			session.commit();
		} finally {
			session.close();
		}
	}

	public void updateTaskComplete() {
		SqlSession session = MyBatisSessionUtil.getInstance().getSession();
		try {
			UserLordMapper userLordMapper = session.getMapper(UserLordMapper.class);
			userLordMapper.updateTaskComplete(uid, dailyTaskComplete);
			session.commit();
		} finally {
			session.close();
		}
	}

	public void flushDailyTask() {
		SqlSession session = MyBatisSessionUtil.getInstance().getSession();
		try {
			flushDailyTask(session);
			session.commit();
		} finally {
			session.close();
		}
	}

	public void updateHDLogin() {
		SqlSession session = MyBatisSessionUtil.getInstance().getSession();
		try {
			session.getMapper(UserLordMapper.class).updateHDLogin(uid, isHDLogin);
			session.commit();
		} finally {
			session.close();
		}
	}

	public void flushDailyTask(SqlSession session) {
		dailyTaskFlushTime = System.currentTimeMillis();
		dailyTaskComplete = 0;
		UserLordMapper userLordMapper = session.getMapper(UserLordMapper.class);
		userLordMapper.flushDailyTask(uid, dailyTaskFlushTime);
	}

	public void flushDailyActive(SqlSession session) {
		dailyActiveTime = System.currentTimeMillis();
		UserLordMapper userLordMapper = session.getMapper(UserLordMapper.class);
		userLordMapper.flushDailyActive(uid, dailyActiveTime);
	}

	public void updateDailyFirstEnterMapTime(SqlSession session) {
		boolean isAutoClose = false;
		if(session == null){
			isAutoClose = true;
			session = MyBatisSessionUtil.getInstance().getSession();
		}
		try{
			dailyFirstEnterMapTime = System.currentTimeMillis();
			UserLordMapper userLordMapper = session.getMapper(UserLordMapper.class);
			userLordMapper.updateDailyFirstEnterMapTime(uid, dailyFirstEnterMapTime);
			if(isAutoClose){
				session.commit();
			}
		}finally {
			if(isAutoClose){
				session.close();
			}
		}

	}

	public void updateNpcGarrison(){
		npcGarrison = 1;
		SqlSession session = MyBatisSessionUtil.getInstance().getSession();
		try {
			UserLordMapper userLordMapper = session.getMapper(UserLordMapper.class);
			userLordMapper.updateNpcGarrison(uid, npcGarrison);
			session.commit();
		}finally {
			session.close();
		}
	}

	public void updateDaily7Gift(){
		SqlSession session = MyBatisSessionUtil.getInstance().getSession();
		try {
			UserLordMapper userLordMapper = session.getMapper(UserLordMapper.class);
			userLordMapper.updateDaily7Gift(uid, daily7GiftCount, daily7GiftTime);
			session.commit();
		}finally {
			session.close();
		}
	}

	public void updateDaily7Gift2(){
		SqlSession session = MyBatisSessionUtil.getInstance().getSession();
		try {
			UserLordMapper userLordMapper = session.getMapper(UserLordMapper.class);
			userLordMapper.updateDaily7Gift2(uid, daily7Gift2Count, daily7Gift2Time);
			session.commit();
		}finally {
			session.close();
		}
	}

	public void updateDaily7Gift3(){
		SqlSession session = MyBatisSessionUtil.getInstance().getSession();
		try {
			UserLordMapper userLordMapper = session.getMapper(UserLordMapper.class);
			userLordMapper.updateDaily7Gift3(uid, daily7Gift3Count, daily7Gift3Time);
			session.commit();
		}finally {
			session.close();
		}
	}

	public void updateDaily7Gift4(){
		SqlSession session = MyBatisSessionUtil.getInstance().getSession();
		try {
			UserLordMapper userLordMapper = session.getMapper(UserLordMapper.class);
			userLordMapper.updateDaily7Gift4(uid, daily7Gift4Count, daily7Gift4Time);
			session.commit();
		}finally {
			session.close();
		}
	}

	public void updateDaily7Gift5(){
		SqlSession session = MyBatisSessionUtil.getInstance().getSession();
		try {
			UserLordMapper userLordMapper = session.getMapper(UserLordMapper.class);
			userLordMapper.updateDaily7Gift5(uid, daily7Gift5Count, daily7Gift5Time);
			session.commit();
		}finally {
			session.close();
		}
	}

	public void updateDaily7Gift6(){
		SqlSession session = MyBatisSessionUtil.getInstance().getSession();
		try {
			UserLordMapper userLordMapper = session.getMapper(UserLordMapper.class);
			userLordMapper.updateDaily7Gift6(uid, daily7Gift6Count, daily7Gift6Time);
			session.commit();
		}finally {
			session.close();
		}
	}

	public void updateSevenDayInfo(SqlSession session){
		boolean isCommit = false;
		if(session == null){
			session = MyBatisSessionUtil.getInstance().getSession();
			isCommit = true;
		}

		try {
			UserLordMapper userLordMapper = session.getMapper(UserLordMapper.class);
			userLordMapper.updateSevenDayInfo(this);
			if(isCommit){
				session.commit();
			}
		}finally {
			if(isCommit){
				session.close();
			}
		}
	}

    public boolean isNpcGarrison() {
        return npcGarrison == 1;
    }

    public boolean isAcceptFirstBindAccountReward() {
        return firstBindAccountRewardFlag == 1;
    }

	public long getDailyTaskFlushTime() {
		return dailyTaskFlushTime;
	}

	public void setDailyTaskFlushTime(long dailyTaskFlushTime) {
		this.dailyTaskFlushTime = dailyTaskFlushTime;
	}

	public int getDailyTaskComplete() {
		return dailyTaskComplete;
	}

	public void setDailyTaskComplete(int dailyTaskComplete) {
		this.dailyTaskComplete = dailyTaskComplete;
	}

	public int getIsHDLogin() {
		return isHDLogin;
	}

	public void setIsHDLogin(int isHDLogin) {
		this.isHDLogin = isHDLogin;
	}

	public String getLoginLink() {
		return loginLink;
	}

	public void setLoginLink(String loginLink) {
		this.loginLink = loginLink;
	}

	public long getDailyActiveTime() {
		return dailyActiveTime;
	}

	public void setDailyActiveTime(long dailyActiveTime) {
		this.dailyActiveTime = dailyActiveTime;
	}

	public int getNpcGarrison() {
		return npcGarrison;
	}

	public void setNpcGarrison(int npcGarrison) {
		this.npcGarrison = npcGarrison;
	}

	public int getFreshRecharge() {
		return freshRecharge;
	}

	public void setFreshRecharge(int freshRecharge) {
		this.freshRecharge = freshRecharge;
	}

	public int getFreshRechargeTotal() {
		return freshRechargeTotal;
	}

	public void setFreshRechargeTotal(int freshRechargeTotal) {
		this.freshRechargeTotal = freshRechargeTotal;
	}

	public long getPayTotal() {
		return payTotal;
	}

	public void setPayTotal(long payTotal){
		this.payTotal = payTotal;
	}

	public int getDaily7GiftCount() {
		return daily7GiftCount;
	}

	public void setDaily7GiftCount(int daily7GiftCount) {
		this.daily7GiftCount = daily7GiftCount;
	}

	public long getDaily7GiftTime() {
		return daily7GiftTime;
	}

	public void setDaily7GiftTime(long daily7GiftTime) {
		this.daily7GiftTime = daily7GiftTime;
	}

	public int getDaily7Gift2Count() {
		return daily7Gift2Count;
	}

	public void setDaily7Gift2Count(int daily7Gift2Count) {
		this.daily7Gift2Count = daily7Gift2Count;
	}

	public long getDaily7Gift2Time() {
		return daily7Gift2Time;
	}

	public void setDaily7Gift2Time(long daily7Gift2Time) {
		this.daily7Gift2Time = daily7Gift2Time;
	}

	public int getDaily7Gift3Count() {
		return daily7Gift3Count;
	}

	public void setDaily7Gift3Count(int daily7Gift3Count) {
		this.daily7Gift3Count = daily7Gift3Count;
	}

	public long getDaily7Gift3Time() {
		return daily7Gift3Time;
	}

	public void setDaily7Gift3Time(long daily7Gift3Time) {
		this.daily7Gift3Time = daily7Gift3Time;
	}

	public int getDaily7Gift4Count() {
		return daily7Gift4Count;
	}

	public long getDaily7Gift4Time() {
		return daily7Gift4Time;
	}

	public int getDaily7Gift5Count() {
		return daily7Gift5Count;
	}

	public long getDaily7Gift5Time() {
		return daily7Gift5Time;
	}

	public int getDaily7Gift6Count() {
		return daily7Gift6Count;
	}

	public long getDaily7Gift6Time() {
		return daily7Gift6Time;
	}

	public void setDaily7Gift4Count(int daily7Gift4Count) {
		this.daily7Gift4Count = daily7Gift4Count;
	}

	public void setDaily7Gift4Time(long daily7Gift4Time) {
		this.daily7Gift4Time = daily7Gift4Time;
	}

	public void setDaily7Gift5Count(int daily7Gift5Count) {
		this.daily7Gift5Count = daily7Gift5Count;
	}

	public void setDaily7Gift5Time(long daily7Gift5Time) {
		this.daily7Gift5Time = daily7Gift5Time;
	}

	public void setDaily7Gift6Count(int daily7Gift6Count) {
		this.daily7Gift6Count = daily7Gift6Count;
	}

	public void setDaily7Gift6Time(long daily7Gift6Time) {
		this.daily7Gift6Time = daily7Gift6Time;
	}

	public String getFirstBindShareRewardStatusInfo() {
		String status = getFirstBindShareRewardStatus();
		if(StringUtils.isBlank(status)) {
			status = "facebook;0|weibo;0|weixin;0";
		}
		StringBuilder shares = new StringBuilder();
		String[] shareInfos = StringUtils.split(status, "|");
		Map<String, String> shareMap = new HashMap<>();

		for(String shareInfo: shareInfos) {
			String[] info = StringUtils.split(shareInfo, ";");
			shareMap.put(info[0], info[1]);
		}
		Map<String, String> bindMap = GameConfigManager.getItem("sys_config", "BindSwitch");
		String[] sharePF = new String[]{"facebook", "weibo", "weixin"};
		String[] types = new String[]{"FB", "WB", "WX"};
		for( int i = 0; i<sharePF.length; i++) {
			String info;
			boolean open = false;
			if(bindMap.get(types[i] + "_BindSwitch").equals("1")) {
				open = true;
			}
			if(shareMap.containsKey(sharePF[i])) {
				info = sharePF[i] + ";" + (open ? shareMap.get(sharePF[i]) : "-1");
			} else {
				info = sharePF[i] + ";" + (open ? 0 : -1);
			}

			String mailId = bindMap.get(types[i] + "_mail_share");

			String rewardId = GameConfigManager.getString("mail", mailId, "reward");
			Integer gold = GameConfigManager.getInt("reward", rewardId, "gold");

			shares.append(info).append(";").append(gold == null ? 0 : gold).append("|");
		}
		if(shares.length() < 1) {
			return "";
		}
		String shareInfoWithGold = shares.toString();
		return shareInfoWithGold.substring(0, shareInfoWithGold.length() - 1);
	}

	public String getFirstBindShareRewardStatus() {
		return firstBindShareRewardStatus;
	}

	public void setFirstBindShareRewardStatus(String firstBindShareRewardStatus) {
		this.firstBindShareRewardStatus = firstBindShareRewardStatus;
	}

	public boolean isBoughtAllNewDaily7GiftsByType(String giftType, boolean isRegAfter) {
		//如果注册日期在新手7日礼包配置开始时间之前，则显示老礼包
		if (!isRegAfter) {
			return false;
		}
		if (DAILY7_GIFT_FIRST_STAGE_TYPE.equals(giftType)) {
			//第一档礼包
			return daily7Gift4Count < 7 && daily7Gift4Count > 0;
		} else if (DAILY7_GIFT_SECOND_STAGE_TYPE.equals(giftType)) {
			//第二档礼包
			return daily7Gift5Count < 7 && daily7Gift5Count > 0;
		} else if (DAILY7_GIFT_THIRD_STAGE_TYPE.equals(giftType)) {
			//第三档礼包
			return daily7Gift6Count < 7 && daily7Gift6Count > 0;
		}
		//礼包的type值传的不对，返回false
		return false;
	}

	public boolean isAcceptFirstBindShareRewardStatus(String type) {
		String[] shares = StringUtils.split(firstBindShareRewardStatus, "|");
		if(shares == null || shares.length == 0) {
			return false;
		}

		for(String shareStatus : shares) {
			String[] shareRewardStatus = StringUtils.split(shareStatus, ";");
			if(type.equals(shareRewardStatus[0]) && shareRewardStatus[1].equals("1")) {
				return true;
			}
		}

		return false;
	}

	public void updateFirstBindShareRewardStatus(String type) {
		if(StringUtils.isBlank(firstBindShareRewardStatus)) {
			firstBindShareRewardStatus = type + ";1";
		} else {
			firstBindShareRewardStatus = firstBindShareRewardStatus + "|" + type + ";1";
		}
		SqlSession session = MyBatisSessionUtil.getInstance().getSession();
		try {
			UserLordMapper userLordMapper = session.getMapper(UserLordMapper.class);
			userLordMapper.updateBindShareStatus(uid, firstBindShareRewardStatus);
			session.commit();
		} finally {
			session.close();
		}
	}
}