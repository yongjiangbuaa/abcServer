package com.geng.puredb.model;

import com.geng.core.ConcurrentLock;
import com.geng.core.data.ISFSObject;
import com.geng.core.data.SFSObject;
import com.geng.exceptions.COKException;
import com.geng.exceptions.GameExceptionCode;
import com.geng.gameengine.UserService;
import com.geng.gameengine.login.LoginInfo;
import com.geng.puredb.dao.UserProfileMapper;
import com.geng.utils.Constants;
import com.geng.utils.LoggerUtil;
import com.geng.utils.MyBatisSessionUtil;
import com.geng.utils.xml.GameConfigManager;
import com.google.common.base.Optional;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;

import java.util.Stack;

public class UserProfile {
    private String uid;

    private Integer heart;

    private long gold;
    private long paidGold;
    private Integer star;
    private Long hearttime;
    private String storyId;

    private String name;
    private int level;
    private long exp;
    private long crystal;
    private long payTotal;
    private String gcmRegisterId;
    private String parseRegisterId;
    private String banGMName;
    private String pic;
    private int picVer;
    private String country;

    private String nationalFlag;
    private int chNameCount;
    private int worldPoint;//三消中用于记录重复挑战次数
    private String deviceId;
    private String gaid;
    private String platform;
    private String pf;
    private String lang;
    private String appVersion;
    private String lastAppVersion;
    private int gmFlag;
    private long regTime;
    private String openedPos;
    private long offLineTime;
    private long banTime;
    private long chatBanTime;
    private long noticeBanTime;
    private long lastOnlineTime;
    private String gmail;
    private int serverId;
    private int crossFightSrcServerId;
    private String phoneDevice;
    private LoginInfo loginInfo;
    private int dragonEggType;
    private long dragonEggEndTime;  //龙蛋孵化结束时间
    private long dragonEggDurationTime; //龙蛋孵化需要的持续时间
    private long dragonEndTime;//龙蛋孵化结束时间，害怕以前的字段有其他用处所以没有复用上面那个字段
    private String bustPic;
    //上次登录时间只许在登录时修改其他地方不能进行任何修改
    private long lastLoginTime;
    private String curGaid;
    private String promotionId; //deeplink的promotion id
    private int isBusinessman;
    private String allianceId;


    private Object itemLock = new Object();


    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid == null ? null : uid.trim();
    }

    public Integer getHeart() {
        return heart;
    }

    public void setHeart(Integer heart) {
        this.heart = heart;
    }

    public long getGold() {
        return gold;
    }

    public void setGold(long gold) {
        this.gold = gold;
    }

    public Integer getStar() {
        return star;
    }

    public void setStar(Integer star) {
        this.star = star;
    }

    public Long getHearttime() {
        return hearttime;
    }

    public void setHearttime(Long hearttime) {
        this.hearttime = hearttime;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getStoryId() {
        return storyId;
    }

    public void setStoryId(String storyId) {
        this.storyId = storyId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getGaid() {
        return gaid;
    }

    public void setGaid(String gaid) {
        this.gaid = gaid;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public void setPf(String pf) {
        this.pf = pf;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public long getCrystal() {
        return crystal;
    }

    public void setCrystal(long crystal) {
        this.crystal = crystal;
    }

    public long getPaidGold() {
        return paidGold;
    }

    public void setPaidGold(long paidGold) {
        this.paidGold = paidGold;
    }

    public long getPayTotal() {
        return payTotal;
    }

    public void setPayTotal(long payTotal) {
        this.payTotal = payTotal;
    }

    public String getGcmRegisterId() {
        return gcmRegisterId;
    }

    public void setGcmRegisterId(String gcmRegisterId) {
        this.gcmRegisterId = gcmRegisterId;
    }

    public String getParseRegisterId() {
        return parseRegisterId;
    }

    public void setParseRegisterId(String parseRegisterId) {
        this.parseRegisterId = parseRegisterId;
    }

    public String getBanGMName() {
        return banGMName;
    }

    public void setBanGMName(String banGMName) {
        this.banGMName = banGMName;
    }

    public String getPic() {
        return pic;
    }

    public void setPic(String pic) {
        this.pic = pic;
    }

    public int getPicVer() {
        return picVer;
    }

    public void setPicVer(int picVer) {
        this.picVer = picVer;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPf() {
        return pf;
    }
    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getLastAppVersion() {
        return lastAppVersion;
    }

    public void setLastAppVersion(String lastAppVersion) {
        this.lastAppVersion = lastAppVersion;
    }

    public int getGmFlag() {
        return gmFlag;
    }

    public void setGmFlag(int gmFlag) {
        this.gmFlag = gmFlag;
    }

    public long getRegTime() {
        return regTime;
    }

    public void setRegTime(long regTime) {
        this.regTime = regTime;
    }

    public String getOpenedPos() {
        return openedPos;
    }

    public void setOpenedPos(String openedPos) {
        this.openedPos = openedPos;
    }

    public long getOffLineTime() {
        return offLineTime;
    }

    public void setOffLineTime(long offLineTime) {
        this.offLineTime = offLineTime;
    }

    public long getBanTime() {
        return banTime;
    }

    public void setBanTime(long banTime) {
        this.banTime = banTime;
    }

    public long getChatBanTime() {
        return chatBanTime;
    }

    public void setChatBanTime(long chatBanTime) {
        this.chatBanTime = chatBanTime;
    }

    public long getNoticeBanTime() {
        return noticeBanTime;
    }

    public void setNoticeBanTime(long noticeBanTime) {
        this.noticeBanTime = noticeBanTime;
    }

    public long getLastOnlineTime() {
        return lastOnlineTime;
    }

    public void setLastOnlineTime(long lastOnlineTime) {
        this.lastOnlineTime = lastOnlineTime;
    }

    public String getGmail() {
        return gmail;
    }

    public void setGmail(String gmail) {
        this.gmail = gmail;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public int getCrossFightSrcServerId() {
        return crossFightSrcServerId;
    }

    public void setCrossFightSrcServerId(int crossFightSrcServerId) {
        this.crossFightSrcServerId = crossFightSrcServerId;
    }
    public void setLevel(int level) {
        this.level = level;
    }

    public long getExp() {
        return exp;
    }

    public void setExp(long exp) {
        this.exp = exp;
    }

    public String getNationalFlag() {
        return nationalFlag;
    }

    public void setNationalFlag(String nationalFlag) {
        this.nationalFlag = nationalFlag;
    }

    public int getChNameCount() {
        return chNameCount;
    }

    public void setChNameCount(int chNameCount) {
        this.chNameCount = chNameCount;
    }

    public int getWorldPoint() {
        return worldPoint;
    }

    public void setWorldPoint(int worldPoint) {
        this.worldPoint = worldPoint;
    }

    public int getDragonEggType() {
        return dragonEggType;
    }

    public void setDragonEggType(int dragonEggType) {
        this.dragonEggType = dragonEggType;
    }

    public long getDragonEggEndTime() {
        return dragonEggEndTime;
    }

    public void setDragonEggEndTime(long dragonEggEndTime) {
        this.dragonEggEndTime = dragonEggEndTime;
    }

    public long getDragonEggDurationTime() {
        return dragonEggDurationTime;
    }

    public void setDragonEggDurationTime(long dragonEggDurationTime) {
        this.dragonEggDurationTime = dragonEggDurationTime;
    }

    public long getDragonEndTime() {
        return dragonEndTime;
    }

    public void setDragonEndTime(long dragonEndTime) {
        this.dragonEndTime = dragonEndTime;
    }

    public String getBustPic() {
        return bustPic;
    }

    public void setBustPic(String bustPic) {
        this.bustPic = bustPic;
    }

    public long getLastLoginTime() {
        return lastLoginTime;
    }

    public String getCurGaid() {
        return curGaid;
    }

    public void setCurGaid(String curGaid) {
        this.curGaid = curGaid;
    }

    public String getPromotionId() {
        return promotionId;
    }

    public void setPromotionId(String promotionId) {
        this.promotionId = promotionId;
    }

    public int getIsBusinessman() {
        return isBusinessman;
    }

    public void setIsBusinessman(int isBusinessman) {
        this.isBusinessman = isBusinessman;
    }

    public String getPhoneDevice() {
        return phoneDevice;
    }

    public void setPhoneDevice(String phoneDevice) {
        this.phoneDevice = phoneDevice;
    }

    public void setAllianceId(String allianceId) {
        this.allianceId = allianceId;
    }





    public   void insert(){

        SqlSession session = MyBatisSessionUtil.getInstance().getSession();
        try {
            UserProfileMapper mapper = session.getMapper(UserProfileMapper.class);
            mapper.insert(this);
        } finally {
            session.close();//注意一定要finally   close!!
        }
    }

    public   void update(){
        SqlSession session = MyBatisSessionUtil.getInstance().getSession();
        try {
            UserProfileMapper mapper = session.getMapper(UserProfileMapper.class);
            mapper.updateByPrimaryKey(this);
        } finally {
            session.close();
        }
    }

    public static UserProfile getWithUid(String uid){
        SqlSession session = MyBatisSessionUtil.getInstance().getSession();
        try {
            UserProfileMapper mapper = session.getMapper(UserProfileMapper.class);
            UserProfile userProfile = mapper.selectByPrimaryKey(uid);
            return userProfile;
        } finally {
            session.close();
        }
    }

    public static UserProfile newInstance(String uid,int gold,int heart,int star,long heartTime,int l) {
        UserProfile u = new UserProfile();
        u.setUid(uid);
        u.setGold(gold);
        u.setHeart(heart);
        u.setStar(star);
        u.setHearttime(heartTime);
        u.setLevel(l);
        return u;
    }

    public static long getMaxNameIndex() {
        long idx= 0 ;
        SqlSession session = MyBatisSessionUtil.getInstance().getSession();
        try {
            UserProfileMapper mapper = session.getMapper(UserProfileMapper.class);
             idx = mapper.getMaxNameIndex();
        } finally {
            session.close();
        }
        return idx;
    }

    public Object getItemLock() {
        return itemLock;
    }



    public ISFSObject toLoginInfo(boolean isOnLogin) throws COKException {
        ISFSObject outData = new SFSObject();
        this.getLoginInfo(outData);
        return outData;
    }

    public void getLoginInfo(ISFSObject outData){
        ISFSObject userInfoObj = new SFSObject();
        userInfoObj.putUtfString("uuid", loginInfo != null ? loginInfo.getUuid() : "");
        userInfoObj.putUtfString("name", name);
        boolean isWomen = true;//isSexWomenUser();
        userInfoObj.putBool("isWomen", isWomen);
//        if(isWomen){
//            UserRoseCrown roseCrown = UserRoseCrown.selectByUidAndRoseRoundTime(uid, RoseCrownActivity.getInstance().getStartTime());
//            userInfoObj.putInt("roseCrown", roseCrown == null ? 0 : roseCrown.getRoseCrown());
//        }
        userInfoObj.putLong("gold", gold + paidGold);
        userInfoObj.putLong("crystal", this.crystal);
        userInfoObj.putLong("payTotal", payTotal);
        userInfoObj.putUtfString("uid", uid);
        userInfoObj.putInt("level", level);


    }

    public void fillLoginInfo(ISFSObject initObj) {
        initObj.putUtfString("uid",getUid());
        initObj.putLong("gold",getGold());
        initObj.putInt("star",getStar());
        initObj.putInt("heart",getHeart());
        initObj.putInt("level",getLevel() + 1);
        initObj.putLong("heartTime",getHearttime());
        initObj.putInt("fivemore",getWorldPoint());//增加5步的次数
        initObj.putUtfString("name",getName());
        initObj.putUtfString("country",getCountry());
        initObj.putUtfString("lang",getLang());
        initObj.putUtfString("pf",getPf());

    }

    /**
     * 减少玩家的金币，减少规则为：优先消耗非充值金币，然后再消耗充值金币
     *
     * @param costType 减少金币的原因类型
     * @param delta    要减少的金币值，大于0
     * @param p1       原因1
     * @param p2       原因2
     * @param errorObj 当发生异常时，会把相关信息写入到该ISFSObject中
     * @return 返回玩家的金币剩余总量
     * @throws COKException             金币总量比要减少的值小时抛出此异常
     * @throws IllegalArgumentException 1.要减少的金币数量小于0的时候抛出此异常 or 2.剩余金币数量小于0的时候抛出此异常
     */
    public synchronized long decrAllGold(LoggerUtil.GoldCostType costType, int delta, int p1, int p2, ISFSObject errorObj) throws COKException{
        if(delta < 0){
            throw new IllegalArgumentException(String.format("%s decr gold(%d) delta negative", uid, costType.ordinal()));
        }
        long oldGold, remainGold, oldPaidGold, remainPaidGold, oldTotal, remainTotal;
        int goldType;
        oldGold = gold;
        oldPaidGold = paidGold;
        oldTotal = oldGold + oldPaidGold;
        if(oldTotal < 0){
            throw new IllegalArgumentException(String.format("%s decr gold(%d) is negative", name, costType.ordinal()));
        }
        if(oldTotal < delta){
            throw new COKException(GameExceptionCode.USERGOLD_IS_NOT_ENOUGH, errorObj, "gold not enough!!Gold cost type is: " + costType.ordinal());
        }

        if(paidGold <= 0){ //如果付费金币<= 0
            goldType = 0;
            gold -= delta;
            remainGold = gold;
            remainTotal = gold;
            LoggerUtil.getInstance().recordGoldCost(uid, goldType, costType.ordinal(), p1, p2, oldGold, - delta, remainGold, 0);

//            UseCoinsFeedbackActivityManager.handleGoldDecr(this, delta);//金币变化后，检查金币累积消费等级
        }else{ //先消耗付费金币
            paidGold -= delta;
            goldType = 1;
            if(paidGold < 0){
                long cost1 = delta + paidGold;
                long cost2 = - paidGold;
                gold += paidGold;
                paidGold = 0;
                remainGold = gold;
                remainTotal = gold;
                LoggerUtil.getInstance().recordGoldCost(uid, goldType, costType.ordinal(), p1, p2, oldPaidGold, - cost1, 0, 0);
                goldType = 0;
                LoggerUtil.getInstance().recordGoldCost(uid, goldType, costType.ordinal(), p1, p2, oldGold, - cost2, remainGold, 0);

//                UseCoinsFeedbackActivityManager.handleGoldDecr(this, delta);//金币变化后，检查金币累积消费等级
            }else{
                remainTotal = gold + paidGold;
                remainPaidGold = paidGold;
                LoggerUtil.getInstance().recordGoldCost(uid, goldType, costType.ordinal(), p1, p2, oldPaidGold, - delta, remainPaidGold, 0);

//                UseCoinsFeedbackActivityManager.handleGoldDecr(this, delta);//金币变化后，检查金币累积消费等级
            }
        }

        return remainTotal;
    }

    /*public static UserProfile getLoggedUserProfile(String uid, boolean checkLock){
        return getLoggedUserProfile(uid, checkLock, null);
    }

    public static UserProfile getLoggedUserProfile(String uid, boolean checkLock, LoginInfo loginInfo){
        if(checkLock && isUserOnLogin(uid)){
            return null;
        }
        ConcurrentLock.LockValue lock = new ConcurrentLock.LockValue(ConcurrentLock.LockType.USER_LOGIN_IN, uid);
        ConcurrentLock.getInstance().lock(lock);
        Stack<String> loginDebugStack = new Stack<>();
        UserProfile userProfile;
        SqlSession session = MyBatisSessionUtil.getInstance().getBatchSession();
        try{
            userProfile = session.getMapper(UserProfileMapper.class).selectByUID(uid);
            if(userProfile == null){
                return null;
            }else if(loginInfo != null){
                userProfile.setPlayerLogin(true);
                if(StringUtils.isNotBlank(userProfile.getAppVersion())){
                    userProfile.setLastAppVersion(userProfile.getAppVersion());
                }
                if(StringUtils.isNotBlank(loginInfo.getClientVersion())){
                    userProfile.setAppVersion(loginInfo.getClientVersion());
                }
                if(StringUtils.isNotBlank(loginInfo.getPf())){
                    userProfile.setPf(loginInfo.getPf());
                }
                if(StringUtils.isNotBlank(loginInfo.getRecallId())){
                    userProfile.setPromotionId(loginInfo.getRecallId());
                }
            }
            userProfile.onLogin(session, loginDebugStack);
            session.commit();
            return userProfile;
        }catch(Exception e){
            String errorStage = loginDebugStack.isEmpty() ? "start" : loginDebugStack.pop();
            String errMsg = String.format("loading user %s in %s", uid, errorStage);
            COKLoggerFactory.monitorException(errMsg, ExceptionMonitorType.LOGIN, COKLoggerFactory.ExceptionOwner.COMMON, e);
            logger.error(COKLoggerFactory.formatLog(ExceptionMonitorType.LOGIN, COKLoginExceptionType.DATA_LOAD, uid, errorStage));
            return null;
        }finally{
            session.close();
            session = null; //huangyuanqiang
            ConcurrentLock.getInstance().unLock(lock);
        }
    }*/

    public LoginInfo getLoginInfo() {
        return loginInfo;
    }

    public static String getLastUpdateInfoTime(String fromUser) {
        return null;
    }

    public Object getMailLock() {
        return new Object();
    }

    public Optional<String> getAllianceSimpleName() {
        return Optional.fromNullable("");
    }

    public void setLoginTime(Long writelog) {

    }

    public void setLastLoginTime(long l) {

    }

    public void setLoginInfo(LoginInfo loginInfo) {

    }


    public long getLoginTime() {
        return 0;
    }

    public static String selectName(String uid) {
        return null;
    }

    public void setLastUpdateTime() {

    }

    public String getAllianceId() {
        return null;
    }

    public static UserProfile newInstance(SqlSession session, LoginInfo loginInfo) {
        UserProfile userProfile = new UserProfile();
        String[] nameIdArr = UserService.generateDefaultUserName(loginInfo);
        if(loginInfo != null && StringUtils.isNotBlank(loginInfo.getGooglePlayAccount())){
            userProfile.setName(loginInfo.getGooglePlayAccount());
        }else{
            userProfile.setName(nameIdArr[0]);
        }
        userProfile.setUid(nameIdArr[1]);
        userProfile.setLevel(1);
        int ranPicNum;
        int rd = Math.random() > 0.5 ? 1 : 0;
        if(rd == 1){
            ranPicNum = 3;
        }else{
            ranPicNum = 0;
        }
//        GameConfigManager picConfigMap = new GameConfigManager("characters");
//        String ranPic = picConfigMap.getItem(Constants.PICTURE_CODE_PREFIX + ranPicNum).get("name");//得到具体的随机图片
        String ranPic = "g052";//客户端要求初始化头像固定
        userProfile.setExp(0L);
        userProfile.setPic(ranPic);
        userProfile.setCrystal(0L);
        userProfile.setGcmRegisterId(loginInfo.getGcmRegisterId());
        userProfile.setParseRegisterId(null);
        userProfile.setCountry(loginInfo.getCountry());
        userProfile.setRegTime(System.currentTimeMillis());
        userProfile.setDeviceId(loginInfo.getDeviceId());
        userProfile.setGaid(loginInfo.getGaid());
        userProfile.setPlatform(loginInfo.getPlatform());
        userProfile.setPf(loginInfo.getPf());
        userProfile.setLang(loginInfo.getLang());
        userProfile.setAppVersion(loginInfo.getClientVersion());
        userProfile.setLastAppVersion(loginInfo.getClientVersion());
        userProfile.setLastOnlineTime(System.currentTimeMillis());
        userProfile.setAllianceId("");
        userProfile.setGmail(loginInfo.getGmail());
        userProfile.setServerId(Constants.SERVER_ID);
        userProfile.setCrossFightSrcServerId(- 1);
        userProfile.setNationalFlag(""); //由ip所在国确定
        userProfile.setDragonEggType(0);//默认为0
//        userProfile.culDragonEggEndTime();
//        int eggInitDurationTime = Integer.parseInt(new GameConfigManager("item").getItem("hatch_dragon_egg").get("k1"));    //龙蛋孵化初始持续时间，默认20小时，按秒配置
        userProfile.setDragonEggDurationTime(0L);
        userProfile.setLastLoginTime(System.currentTimeMillis());
        userProfile.setCurGaid(loginInfo.getGaid());
        userProfile.setIsBusinessman(0);
        userProfile.setDragonEndTime(- 1);


        //三消属性初始化
        userProfile.setHeart(5);
        userProfile.setGold(1000);
        userProfile.setStar(0);
        userProfile.setHearttime(0L);

        UserProfileMapper userMapper = session.getMapper(UserProfileMapper.class);
        userMapper.insert(userProfile);
//        userProfile.setNewAccount(true);
        userProfile.setPromotionId(loginInfo.getRecallId());

        return userProfile;
    }

    public UserProfile onRegister(SqlSession session) {
        //TODO business logic onRegister called here
        return this;
    }
}