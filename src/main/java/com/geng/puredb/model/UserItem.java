package com.geng.puredb.model;

import com.geng.db.MybatisSessionUtil;
import com.geng.puredb.dao.UserItemMapper;
import com.geng.utils.GameService;
import org.apache.ibatis.session.SqlSession;

public class UserItem {
    private String uuid;

    private String ownerid;

    private String itemid;

    private Integer count;

    private Integer value;

    private Long vanishtime;

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

    public String getItemid() {
        return itemid;
    }

    public void setItemid(String itemid) {
        this.itemid = itemid == null ? null : itemid.trim();
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public Long getVanishtime() {
        return vanishtime;
    }

    public void setVanishtime(Long vanishtime) {
        this.vanishtime = vanishtime;
    }

    public  static void insert(UserItem i){

        SqlSession session = MybatisSessionUtil.getSession();
        try {
            UserItemMapper mapper = session.getMapper(UserItemMapper.class);
            mapper.insert(i);
        } finally {
            session.close();//注意一定要finally   close!!
        }
    }

    public  static void update(UserItem i){
        SqlSession session = MybatisSessionUtil.getSession();
        try {
            UserItem mapper = session.getMapper(UserItem.class);
            mapper.update(i);
        } finally {
            session.close();
        }
    }



    public static UserItem newInstance(String uid,String itemid) {
        UserItem u = new UserItem();
        u.setUuid(GameService.getGUID());
        u.setItemid(itemid);
        u.setOwnerid(uid);
        u.setCount(0);
        u.setValue(0);
        u.setVanishtime(Long.MAX_VALUE);
        return u;
    }
}