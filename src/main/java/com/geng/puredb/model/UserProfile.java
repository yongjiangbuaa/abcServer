package com.geng.puredb.model;

import com.geng.core.data.ISFSObject;
import com.geng.core.data.SFSObject;
import com.geng.db.MyBatisSessionUtil;
import com.geng.exception.COKException;
import com.geng.gameengine.login.LoginInfo;
import com.geng.puredb.dao.UserProfileMapper;
import org.apache.ibatis.session.SqlSession;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class UserProfile {
    private String uid;

    private Integer heart;

    private Integer gold;

    private Integer star;

    private Long hearttime;


    private String name;
    private Integer level;
    private long crystal;
    private long paidGold;



    private long payTotal;
    private String gcmRegisterId;
    private String parseRegisterId;
    private String banGMName;
    private String pic;
    private int picVer;
    private String country;



    private String deviceId;
    private String gaid;
    private String platform;
    private String pf;
    private String lang;
    private Object itemLock = new Object();


    private String storyId;
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

    public Integer getGold() {
        return gold;
    }

    public void setGold(Integer gold) {
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

    public String getPhoneDevice() {
        return phoneDevice;
    }

    public void setPhoneDevice(String phoneDevice) {
        this.phoneDevice = phoneDevice;
    }




    public   void insert(){

        SqlSession session = MyBatisSessionUtil.getSession();
        try {
            UserProfileMapper mapper = session.getMapper(UserProfileMapper.class);
            mapper.insert(this);
        } finally {
            session.close();//注意一定要finally   close!!
        }
    }

    public   void update(){
        SqlSession session = MyBatisSessionUtil.getSession();
        try {
            UserProfileMapper mapper = session.getMapper(UserProfileMapper.class);
            mapper.updateByPrimaryKeySelective(this);
        } finally {
            session.close();
        }
    }

    public static UserProfile getWithUid(String uid){
        SqlSession session = MyBatisSessionUtil.getSession();
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
        SqlSession session = MyBatisSessionUtil.getSession();
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

    private void getLoginInfo(ISFSObject outData){
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
        initObj.putInt("gold",getGold());
        initObj.putInt("star",getStar());
        initObj.putInt("heart",getHeart());
        initObj.putInt("level",getLevel());
        initObj.putLong("heartTime",getHearttime());
    }
}