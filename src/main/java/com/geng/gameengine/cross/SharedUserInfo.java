package com.geng.gameengine.cross;

import com.geng.gameengine.StatusService;
import com.geng.gameengine.UserBuildingManager;
import com.geng.puredb.model.UserBuilding;
import com.geng.puredb.model.UserProfile;
import com.geng.puredb.model.UserState;
import com.geng.utils.Constants;
import com.geng.utils.MyBatisSessionUtil;
import com.geng.core.data.ISFSObject;
import com.geng.core.data.SFSObject;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;

import java.util.List;

/**
 * 跨服共享用户信息
 * Created by lifangkai on 14/11/3.
 */
public class SharedUserInfo {
    private String uid;
    private String name;
    private String pic;
    private int picVer;
    private long power;
    private String allianceAbbrName;
    private String lang;
    private int mainBuildingLevel;
    private String allianceId;
    private int gmFlag = 0;
    private int crossFightSrcServerId = -1;
    private int vipLevel = 0;
    private long vipEndTime;
    private int rank = 0;
    private long offLineTime = 0;
    private int level = 1;
    private int stateId;
    private long stateEndTime;

    public SharedUserInfo() {
        pic = Constants.DEFAULT_USER_PIC;
    }

    public SharedUserInfo(UserProfile userProfile) {
        uid = userProfile.getUid();
        name = userProfile.getName();
        pic = userProfile.getPic();
        level = userProfile.getLevel();
        picVer = userProfile.getPicVer();
        lang = userProfile.getLang();
        if(userProfile.getUbManager()!=null) {
            mainBuildingLevel =userProfile.getUbManager().getMainBuildingLevel();
        }else{
            SqlSession session = MyBatisSessionUtil.getInstance().getSession();
            try{
                UserBuilding mainBuilding = UserBuildingManager.getBuildingByType(uid, UserBuilding.BuildingType.STRONGHOLD.getItemId(), session);
                mainBuildingLevel = mainBuilding==null?1:mainBuilding.getLevel();
            }finally {
                session.close();
            }
        }
        if (userProfile.getPlayerInfo() != null) {
            power = userProfile.getPlayerInfo().getPower();
        }
        allianceAbbrName = userProfile.getAllianceSimpleName().or("");
        allianceId = userProfile.getAllianceId();
        gmFlag = userProfile.getGmFlag();
        crossFightSrcServerId = userProfile.getCrossFightSrcServerId();
        vipLevel = userProfile.getUserVIPManager().getVIPLevel();
        vipEndTime = userProfile.getUserVIPManager().getUserVip().getVipendtime();
        if(!StringUtils.isBlank(allianceId)){
            rank = com.geng.puredb.model.AllianceMember.getRankById(uid);
        }
        offLineTime = userProfile.getOffLineTime();

        //聊天Avatar皮肤
        List<UserState> selectChatSkin = UserState.selectSkinByType2(uid, StatusService.MarchStatus.CHATSKIN.getStatusType());
        if (selectChatSkin.size() > 0) {
            UserState userState = selectChatSkin.get(0);
            stateId =  userState.getStateId();
            stateEndTime =  userState.getEndTime();
        } else {
            stateId = 0;
            stateEndTime = 0;
        }
    }

    public SharedUserInfo(String json) {
        ISFSObject obj = SFSObject.newFromJsonData(json);
        uid = obj.getUtfString("uid");
        name = obj.getUtfString("name");
        pic = obj.getUtfString("pic");
        if(obj.containsKey("level")){
            level = obj.getInt("level");
        }
        if (obj.containsKey("picVer")) {
            picVer = obj.getInt("picVer");
        }
        if (obj.containsKey("power")) {
            try {
                power = obj.getInt("power");
            } catch (ClassCastException e) {
                power = obj.getLong("power");
            }
        }
        if (obj.containsKey("mainBuildingLevel")) {
            mainBuildingLevel = obj.getInt("mainBuildingLevel");
        }
        if (obj.containsKey("lang")) {
            lang = obj.getUtfString("lang");
        }
        allianceAbbrName = obj.getUtfString("abbr");
        if(obj.containsKey("allianceId")){
            allianceId = obj.getUtfString("allianceId");
        }
        if(obj.containsKey("gmFlag")){
            gmFlag = obj.getInt("gmFlag");
        }
        if(obj.containsKey("crossFightSrcServerId")){
            crossFightSrcServerId = obj.getInt("crossFightSrcServerId");
        }
        if(obj.containsKey("vipLevel")){
            vipLevel = obj.getInt("vipLevel");
        }
        if(obj.containsKey("vipEndTime")){
            vipEndTime = obj.getLong("vipEndTime");
        }
        if(obj.containsKey("rank")){
            rank = obj.getInt("rank");
        }
        if(obj.containsKey("offLineTime")){
            offLineTime = obj.getLong("offLineTime");
        }
        if(obj.containsKey("stateId")) {
            stateId = obj.getInt("stateId");
        }
        if(obj.containsKey("stateEndTime")) {
            stateEndTime = obj.getLong("stateEndTime");
        }
    }

    public SharedUserInfo(String uid, ISFSObject obj) {
        this.uid = uid;
        name = obj.getUtfString("name");
        pic = obj.getUtfString("pic");
        if (obj.containsKey("picVer")) {
            picVer = obj.getInt("picVer");
        }
        if(obj.containsKey("lv")){
            level = obj.getInt("lv");
        }
        lang = obj.getUtfString("lang");
        if (obj.containsKey("power")) {
            power = obj.getLong("power");
        }
        allianceAbbrName = obj.getUtfString("abbr");
        if(obj.containsKey("level")) {
            mainBuildingLevel = obj.getInt("level");
        }
        if(obj.containsKey("allianceId")){
            allianceId = obj.getUtfString("allianceId");
        }
        if(obj.containsKey("gmFlag")){
            gmFlag = obj.getInt("gmFlag");
        }
        if(obj.containsKey("crossFightSrcServerId")){
            crossFightSrcServerId = obj.getInt("crossFightSrcServerId");
        }
        if(obj.containsKey("vipLevel")){
            vipLevel = obj.getInt("vipLevel");
        }
        if(obj.containsKey("vipEndTime")){
            vipEndTime = obj.getLong("vipEndTime");
        }
        if(obj.containsKey("rank")){
            rank = obj.getInt("rank");
        }
        if(obj.containsKey("offLineTime")){
            offLineTime = obj.getLong("offLineTime");
        }
    }

    public String getUid() {
        return uid == null ? "" : uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name == null ? "" : name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPic() {
        return pic == null ? "" : pic;
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

    public String getLang(){
        return lang == null ? "" : lang;
    }

    public void setLang(String lang){
        this.lang = lang;
    }

    public long getPower() {
        return power;
    }

    public void setPower(long power) {
        this.power = power;
    }

    public String getAllianceAbbrName() {
        return allianceAbbrName == null ? "" : allianceAbbrName;
    }

    public void setAllianceAbbrName(String allianceAbbrName) {
        this.allianceAbbrName = allianceAbbrName;
    }

    public String getAllianceId() { return allianceId == null ? "" : allianceId; }

    public void setAllianceId(String allianceId) { this.allianceId = allianceId; }

    public int getGmFlag() { return gmFlag; }

    public void setGmFlag(int gmFlag) { this.gmFlag = gmFlag; }

    public int getCrossFightSrcServerId() { return crossFightSrcServerId; }

    public void setCrossFightSrcServerId(int crossFightSrcServerId) { this.crossFightSrcServerId = crossFightSrcServerId; }

    public int getVipLevel() { return vipLevel; }

    public void setVipLevel(int vipLevel) { this.vipLevel = vipLevel; }

    public long getVipEndTime() { return vipEndTime; }

    public void setVipEndTime(int vipEndTime) { this.vipEndTime = vipEndTime; }

    public int getRank() { return rank; }

    public void setRank(int rank) { this.rank = rank; }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getStateId() {
        return stateId;
    }

    public void setStateId(int stateId) {
        this.stateId = stateId;
    }

    public long getStateEndTime() {
        return stateEndTime;
    }

    public void setStateEndTime(long stateEndTime) {
        this.stateEndTime = stateEndTime;
    }

    @Override
    public String toString() {
        return toSFSObj().toJson();
    }

    public ISFSObject toSFSObj() {
        ISFSObject obj = new SFSObject();
        if (uid != null) {
            obj.putUtfString("uid", uid);
        }
        if (name != null) {
            obj.putUtfString("name", name);
        }
        if (pic != null) {
            obj.putUtfString("pic", pic);
        }
        obj.putInt("level", level);
        obj.putInt("picVer", picVer);
        if (power > 0) {
            obj.putLong("power", power);
        }
        if (allianceAbbrName != null) {
            obj.putUtfString("abbr", allianceAbbrName);
        }
        if (lang != null) {
            obj.putUtfString("lang", lang);
        }
        if (mainBuildingLevel > 0) {
            obj.putInt("mainBuildingLevel", mainBuildingLevel);
        }
        if( allianceId != null) {
            obj.putUtfString("allianceId", allianceId);
        }
        obj.putInt("gmFlag", gmFlag);
        obj.putInt("crossFightSrcServerId", crossFightSrcServerId);
        obj.putInt("vipLevel", vipLevel);
        if (vipEndTime > 0) {
            obj.putLong("vipEndTime",vipEndTime);
        }
        if(offLineTime > 0){
            obj.putLong("offLineTime", offLineTime);
        }
        obj.putInt("rank",rank);

        if(stateEndTime > System.currentTimeMillis()) {
            obj.putLong("stateEndTime", stateEndTime);
            obj.putInt("stateId",stateId);
        }

        return obj;
    }

    public int getMainBuildingLevel() {
        return mainBuildingLevel;
    }

    public void setMainBuildingLevel(int mainBuildingLevel) {
        this.mainBuildingLevel = mainBuildingLevel;
    }

    public boolean isDefault() {
        return name == null;
    }
}
