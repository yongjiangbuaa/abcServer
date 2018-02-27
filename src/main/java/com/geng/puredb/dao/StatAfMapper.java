package com.geng.puredb.dao;

import com.geng.puredb.model.StatAf;

public interface StatAfMapper {

    int insert(StatAf record);

    StatAf selectByPrimaryKey(String uid);

}
