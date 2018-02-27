package com.geng.puredb.model;

import com.geng.gameengine.login.LoginInfo;
import com.geng.puredb.dao.StatAfMapper;
import com.geng.utils.MyBatisSessionUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;

import java.io.Serializable;

public class StatAf implements Serializable {
    private String uid;

    private String afuid;

    private String channel;

    private String campain;

    private byte[] fulldata;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid == null ? null : uid.trim();
    }

    public String getAfuid() {
        return afuid;
    }

    public void setAfuid(String afuid) {
        this.afuid = afuid == null ? null : afuid.trim();
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel == null ? null : channel.trim();
    }

    public String getCampain() {
        return campain;
    }

    public void setCampain(String campain) {
        this.campain = campain == null ? null : campain.trim();
    }

    public byte[] getFulldata() {
        return fulldata;
    }

    public void setFulldata(byte[] fulldata) {
        this.fulldata = fulldata;
    }

    public static void writelog(UserProfile userProfile, SqlSession session, LoginInfo loginInfo){
        if(StringUtils.isNotBlank(loginInfo.getAfUID())){
            StatAf stat = new StatAf();
            stat.setUid(userProfile.getUid());
            stat.setAfuid(loginInfo.getAfUID());
            stat.insert(session);
        }
    }

    public int insert(SqlSession session) {
        int ret = 0;
        if(session != null) {
            StatAfMapper mapper = session.getMapper(StatAfMapper.class);
            ret = mapper.insert(this);
        } else {
            session = MyBatisSessionUtil.getInstance().getSession();
            try {
                StatAfMapper mapper = session.getMapper(StatAfMapper.class);
                ret = mapper.insert(this);
                session.commit();
            } finally {
                session.close();
            }
        }
        return ret;
    }

    public static StatAf selectWithUid(String uid){
        StatAf statAf = null;
        SqlSession session = null;
        try{
            session = MyBatisSessionUtil.getInstance().getSession();
            StatAfMapper mapper = session.getMapper(StatAfMapper.class);
            statAf = mapper.selectByPrimaryKey(uid);
        }finally {
            if (session != null) {
                session.close();
            }
        }
        return statAf;
    }

    public static void insertCross(SqlSession session, String uid){
        StatAf record = StatAf.selectWithUid(uid);
        if (record != null) {
            session.getMapper(StatAfMapper.class).insert(record);
        }
    }
}
