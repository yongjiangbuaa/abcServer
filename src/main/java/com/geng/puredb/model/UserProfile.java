package com.geng.puredb.model;

import com.geng.db.MybatisSessionUtil;
import com.geng.puredb.dao.UserProfileMapper;
import org.apache.ibatis.session.SqlSession;

public class UserProfile {
    private String uid;

    private Integer heart;

    private Integer gold;

    private Integer star;

    private Long hearttime;

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

    public  static void insert(UserProfile userProfile){

        SqlSession session = MybatisSessionUtil.getSession();
        try {
            UserProfileMapper mapper = session.getMapper(UserProfileMapper.class);
            mapper.insert(userProfile);
        } finally {
            session.close();//注意一定要finally   close!!
        }
    }

    public  static void update(UserProfile userProfile){
        SqlSession session = MybatisSessionUtil.getSession();
        try {
            UserProfileMapper mapper = session.getMapper(UserProfileMapper.class);
            mapper.updateByPrimaryKey(userProfile);
        } finally {
            session.close();
        }
    }

    public static UserProfile getWithUid(String uid){
        SqlSession session = MybatisSessionUtil.getSession();
        try {
            UserProfileMapper mapper = session.getMapper(UserProfileMapper.class);
            UserProfile userProfile = mapper.selectByPrimaryKey(uid);
            return userProfile;
        } finally {
            session.close();
        }
    }

}