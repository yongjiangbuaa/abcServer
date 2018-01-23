package com.geng.puredb.dao;

import com.geng.puredb.model.UserProfile;

public interface UserProfileMapper {
    int deleteByPrimaryKey(String uid);

    int insert(UserProfile record);

    int insertSelective(UserProfile record);

    UserProfile selectByPrimaryKey(String uid);

    int updateByPrimaryKeySelective(UserProfile record);

    int updateByPrimaryKey(UserProfile record);
}