package com.geng.puredb.dao;

import com.geng.db.MybatisSessionUtil;
import com.geng.puredb.model.UserItem;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.SqlSession;

import java.util.List;

public interface UserItemMapper {

    int insert(UserItem record);

    int insertSelective(UserItem record);

    UserItem selectByPrimaryKey(String uuid);

    int updateByPrimaryKeySelective(UserItem record);

    int updateByPrimaryKey(UserItem record);


    int deleteByOwnerIdAndItemId(@Param("ownerId")String ownerId, @Param("itemIdList")List<String> itemIdList);

    int delete(String uuid);

    List<UserItem> select(String ownerUid);

    List<UserItem> selectItemById(
            @Param("ownerId") String ownerId,
            @Param("itemId") String itemId,
            @Param("value") int value);

    List<UserItem> selectItemListByUUIDs(String[] uuids);
    List<UserItem> selectItemListByItemIds(@Param("ownerId")String ownerId,@Param("itemIdList")List<String> itemIds);
    int insertBatch(List<UserItem> items);

    public static List<UserItem> getItems(String uid) {
        SqlSession session = MybatisSessionUtil.getInstance().getSession();
        try {
            UserItemMapper userItemMapper = session.getMapper(UserItemMapper.class);
            return userItemMapper.select(uid);
        } finally {
            session.close();
        }
    }


}