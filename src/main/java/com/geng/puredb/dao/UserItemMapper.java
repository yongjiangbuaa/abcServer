package com.geng.puredb.dao;

import com.geng.puredb.model.UserItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserItemMapper {
    int deleteByOwnerIdAndItemId(@Param("ownerId") String ownerId, @Param("itemIdList") List<String> itemIdList);
    int delete(String uuid);
    int insert(UserItem record);
    List<UserItem> select(String ownerUid);
    int update(UserItem record);

    List<UserItem> selectItemById(
            @Param("ownerId") String ownerId,
            @Param("itemId") String itemId,
            @Param("value") int value);

    UserItem selectItemByUUID(String uuid);
    List<UserItem> selectItemListByUUIDs(String[] uuids);
    List<UserItem> selectItemListByItemIds(@Param("ownerId") String ownerId, @Param("itemIdList") List<String> itemIds);
    int insertBatch(List<UserItem> items);
}