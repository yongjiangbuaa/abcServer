package com.geng.puredb.model;

import com.geng.db.MyBatisSessionUtil;
import com.geng.puredb.dao.UserProfileMapper;
import org.apache.ibatis.session.SqlSession;

public class UserProfile {
    private String uid;

    private Integer heart;

    private Integer gold;

    private Integer star;

    private Long hearttime;


    private Integer level;
    private Object itemLock = new Object();


    private String storyId;

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

    public int getPayTotal() {
        return 0;
    }

    public int getPf() {
        return 0;
    }

    public int getCountry() {
        return 0;
    }
}