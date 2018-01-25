package com.geng.puredb.dao;

import com.geng.puredb.model.UidBind;

public interface UidBindMapper {
    int deleteByPrimaryKey(String bindid);

    int insert(UidBind record);

    int insertSelective(UidBind record);

    UidBind selectByPrimaryKey(String bindid);

    int updateByPrimaryKeySelective(UidBind record);

    int updateByPrimaryKey(UidBind record);
}