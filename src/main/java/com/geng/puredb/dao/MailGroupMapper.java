package com.geng.puredb.dao;

import com.geng.puredb.model.MailGroup;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MailGroupMapper {
    int deleteByPrimaryKey(MailGroup record);

    int insert(MailGroup record);

    int replace(MailGroup record);

    void batchInsert(List<MailGroup> record);

    void batchReplace(List<MailGroup> record);

    MailGroup selectByPrimaryKey(@Param("uid") String uid, @Param("grouptype") int groupType, @Param("groupindex") String groupIndex);

    int updateByPrimaryKey(MailGroup record);

	List<MailGroup> selectByUid(String uid);

	void deleteByUid(String uid);

	List<MailGroup> selectLimitGroups(@Param("uid") String uid, @Param("num") int num, @Param("offSet") int offSet);

    List<MailGroup> selectLimitGroupsMod(@Param("uid") String uid, @Param("num") int num, @Param("offSet") int offSet);

    void deleteByRangeType(@Param("uid") String uid, @Param("typeMin") int typeMin, @Param("typeMax") int typeMax);

	List<MailGroup> selectOldMailGroup(String uid);
}