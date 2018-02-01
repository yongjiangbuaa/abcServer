package com.geng.puredb.dao;

import com.geng.puredb.model.UserItem;

public interface UserItemMapper {
    int deleteByPrimaryKey(String uuid);

    int insert(UserItem record);

    int insertSelective(UserItem record);

    UserItem selectByPrimaryKey(String uuid);

    int updateByPrimaryKeySelective(UserItem record);

    int updateByPrimaryKey(UserItem record);
}