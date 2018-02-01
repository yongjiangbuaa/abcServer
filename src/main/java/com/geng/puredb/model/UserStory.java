package com.geng.puredb.model;

import com.geng.db.MybatisSessionUtil;
import com.geng.puredb.dao.UserStoryMapper;
import com.geng.utils.GameService;
import org.apache.ibatis.session.SqlSession;

import java.util.ArrayList;
import java.util.List;

public class UserStory {
    private String uuid;

    private String ownerid;

    private String storyid;

    private Integer subid;

    private Integer type;

    private Long updatetime;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid == null ? null : uuid.trim();
    }

    public String getOwnerid() {
        return ownerid;
    }

    public void setOwnerid(String ownerid) {
        this.ownerid = ownerid == null ? null : ownerid.trim();
    }

    public String getStoryid() {
        return storyid;
    }

    public void setStoryid(String storyid) {
        this.storyid = storyid == null ? null : storyid.trim();
    }

    public Integer getSubid() {
        return subid;
    }

    public void setSubid(Integer subid) {
        this.subid = subid;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Long getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(Long updatetime) {
        this.updatetime = updatetime;
    }

    public   void insert(){

        SqlSession session = MybatisSessionUtil.getSession();
        try {
            UserStoryMapper mapper = session.getMapper(UserStoryMapper.class);
            mapper.insert(this);
        } finally {
            session.close();//注意一定要finally   close!!
        }
    }

    public   void update(){
        SqlSession session = MybatisSessionUtil.getSession();
        try {
            UserStoryMapper mapper = session.getMapper(UserStoryMapper.class);
            mapper.updateByPrimaryKeySelective(this);
        } finally {
            session.close();
        }
    }



    public static UserStory newInstance(String uid,String storyid) {
        UserStory u = new UserStory();
        u.setUuid(GameService.getGUID());
        u.setStoryid(storyid);
        u.setOwnerid(uid);
        u.setSubid(0);
        u.setType(0);
        u.setUpdatetime(System.currentTimeMillis());
        return u;
    }

    public static List<UserStory> getByUserId(String uid){
        List<UserStory> list = null;
        SqlSession session = MybatisSessionUtil.getSession();
        try {
            UserStoryMapper mapper = session.getMapper(UserStoryMapper.class);
            list = mapper.selectByUserId(uid);
        } finally {
            session.close();
        }
        return list;
    }
}