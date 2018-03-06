package com.geng.puredb.model;

import com.geng.gameengine.login.LoginInfo;
import com.geng.puredb.dao.StatRegMapper;
import com.geng.utils.CommonUtils;
import com.geng.utils.MyBatisSessionUtil;
import com.geng.utils.myredis.R;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;

import java.io.Serializable;
import java.util.List;

public class StatReg  implements Serializable {
    private String uid;

    private int type;

    private long time;

    private String pf;

    private String pfid;

    private String referrer;

    private String country;

    private String ip;

    private String ipcountry;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid == null ? null : uid.trim();
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getPf() {
        return pf;
    }

    public void setPf(String pf) {
        this.pf = pf == null ? null : pf.trim();
    }

    public String getPfid() {
        return pfid;
    }

    public void setPfid(String pfid) {
        this.pfid = pfid == null ? null : pfid.trim();
    }

    public String getReferrer() {
        return referrer;
    }

    public void setReferrer(String referrer) {
        this.referrer = referrer == null ? null : referrer.trim();
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country == null ? null : country.trim();
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getIpcountry() {
        return ipcountry;
    }

    public void setIpcountry(String ipcountry) {
        this.ipcountry = ipcountry;
    }

    public static void insertCross(UserProfile userProfile, SqlSession session){
        StatReg stat = new StatReg();
        StatReg originalStat = StatReg.getWithUid(userProfile);
        stat.setUid(userProfile.getUid());
        stat.setTime(System.currentTimeMillis());
        stat.setPf(originalStat == null ? "" : originalStat.getPf());
        stat.setPfid(originalStat == null ? "" : originalStat.getPfid());
        stat.setReferrer(originalStat == null ? "" : originalStat.getReferrer());
        stat.setCountry(originalStat == null ? "" : originalStat.getCountry());
        stat.setType(2);
        stat.setIp(originalStat == null ? "" : originalStat.getIp());
        stat.insert(session);
    }

    public static void insertForAccessCkfServer(UserProfile userProfile, SqlSession session){
        StatReg stat = new StatReg();
        StatReg originalStat = StatReg.getWithUid(userProfile);
        stat.setUid(userProfile.getUid());
        stat.setTime(System.currentTimeMillis());
        stat.setPf(originalStat == null ? "" : originalStat.getPf());
        stat.setPfid(originalStat == null ? "" : originalStat.getPfid());
        stat.setReferrer(originalStat == null ? "" : originalStat.getReferrer());
        stat.setCountry(originalStat == null ? "" : originalStat.getCountry());
        stat.setType(3);
        stat.setIp(originalStat == null ? "" : originalStat.getIp());
        stat.insert(session);
    }

    public static StatReg getWithUid(UserProfile userProfile) {
        SqlSession session = MyBatisSessionUtil.getInstance().getSession();
        try {
            List<StatReg> list = session.getMapper(StatRegMapper.class).selectByPrimaryKey(userProfile.getUid());
            StatReg ret = null;
            if(list != null && list.size() > 0){
                ret = list.get(0);
                for(int i = 0; i < list.size(); i++){
                    StatReg sr = list.get(i);
                    if(sr.getType() == 2){
                        ret = sr;
                        break;
                    }
                }
            } else {
                ret = new StatReg();
                ret.setUid(userProfile.getUid());
                ret.setTime(userProfile.getRegTime());
                ret.setPf(userProfile.getPf());
                ret.setPfid("");
                ret.setReferrer("");
                String country = userProfile.getLoginInfo() == null ? "" : userProfile.getLoginInfo().getFromCountry();
                ret.setCountry(country == null ? "" : country);
                ret.setType(2);
                ret.insert(session);
                session.commit();
            }
            return  ret;
        } finally {
            session.close();
        }
    }

    public static List<StatReg> selectByUid(String uid) {
        SqlSession session = MyBatisSessionUtil.getInstance().getSession();
        try {
            return session.getMapper(StatRegMapper.class).selectByPrimaryKey(uid);
        } finally {
            session.close();
        }
    }

    public static void writelog(UserProfile userProfile, SqlSession session, LoginInfo loginInfo, String ipAddress){
        StatReg stat = new StatReg();
        String deviceId = loginInfo.getDeviceId();
        int type = 0;
        if(isStartNewGame(deviceId)){
            type = 1;
        }
        stat.setUid(userProfile.getUid());
        stat.setTime(System.currentTimeMillis());
        stat.setPf(loginInfo.getPf());
        stat.setPfid(loginInfo.getPfId());
        String temp = loginInfo.getReferrer();
        stat.setReferrer(getReferrerCode(temp));
        String countryCode = CommonUtils.ip2Country(ipAddress);
        stat.setCountry(StringUtils.isBlank(countryCode) ? loginInfo.getFromCountry() : countryCode);
        stat.setType(type);
        stat.setIp(loginInfo.getIp());
        stat.insert(session);
    }

    public static boolean isStartNewGame(String deviceId){
//        RedisSession rs = new RedisSession(true);
        long stat =  R.Local().hDel("NEW_ACCOUNT_RECORD", deviceId);
        return stat == 1;
    }

    public int insert(SqlSession session) {
        int ret = 0;
        if(session != null) {
            StatRegMapper statMapper = session.getMapper(StatRegMapper.class);
            ret = statMapper.insert(this);
        } else {
            session = MyBatisSessionUtil.getInstance().getSession();
            try {
                StatRegMapper statMapper = session.getMapper(StatRegMapper.class);
                ret = statMapper.insert(this);
                session.commit();
            } finally {
                session.close();
            }
        }
        return ret;
    }

    public static String getReferrerCode(String referrer){
        if(true)
            return "";
        if(StringUtils.isBlank(referrer))
            return "";
        referrer = referrer.trim();
        if(referrer.matches("^\\{\"app\":\\d{1,},\"t\":\\d{1,}}$")){//{"app":979966112074751,"t":1452124150}
            return "facebook";
        }
        return referrer;
    }
}
