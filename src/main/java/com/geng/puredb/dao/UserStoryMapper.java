package com.geng.puredb.dao;

import com.geng.puredb.model.UserStory;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserStoryMapper {
    int deleteByPrimaryKey(String uuid);

    int insert(UserStory record);

    int insertSelective(UserStory record);

    UserStory selectByPrimaryKey(String uuid);

    int updateByPrimaryKeySelective(UserStory record);

    int updateByPrimaryKey(UserStory record);

    List<UserStory> selectByUserId(@Param("ownerId")String ownerId);
}