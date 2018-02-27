package com.geng.puredb.dao;

import com.geng.puredb.model.StatReg;

import java.util.List;

public interface StatRegMapper {
    int deleteByPrimaryKey(String uid);

    int insert(StatReg record);

    List<StatReg> selectByPrimaryKey(String uid);
}
