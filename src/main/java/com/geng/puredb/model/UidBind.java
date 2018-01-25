package com.geng.puredb.model;

import com.geng.db.MybatisSessionUtil;
import com.geng.puredb.dao.UidBindMapper;
import org.apache.ibatis.session.SqlSession;

public class UidBind {
    private String bindid;

    private String uid;

    private Integer type;

    private Long time;

    public String getBindid() {
        return bindid;
    }

    public void setBindid(String bindid) {
        this.bindid = bindid == null ? null : bindid.trim();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid == null ? null : uid.trim();
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public  static void insert(UidBind bind){

        SqlSession session = MybatisSessionUtil.getSession();
        try {
            UidBindMapper mapper = session.getMapper(UidBindMapper.class);
            mapper.insert(bind);
            session.commit();//TODO 全局autocommit
        } finally {
            session.close();//注意一定要finally   close!!
        }
    }

    public  static void update(UidBind UidBind){
        SqlSession session = MybatisSessionUtil.getSession();
        try {
            UidBindMapper mapper = session.getMapper(UidBindMapper.class);
            mapper.updateByPrimaryKeySelective(UidBind);
            session.commit();
        } finally {
            session.close();
        }
    }

    public static UidBind getWithbindId(String bindId){
        SqlSession session = MybatisSessionUtil.getSession();
        try {
            UidBindMapper mapper = session.getMapper(UidBindMapper.class);
            UidBind UidBind = mapper.selectByPrimaryKey(bindId);
            return UidBind;
        } finally {
            session.close();
        }
    }

    public static UidBind newInstance(String uid,String bindId,int type,long time) {
        UidBind u = new UidBind();
        u.setUid(uid);
        u.setBindid(bindId);
        u.setType(type);
        u.setTime(time);
        return u;
    }
}