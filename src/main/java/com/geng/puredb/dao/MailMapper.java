package com.geng.puredb.dao;

import com.geng.puredb.model.Mail;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface MailMapper {
    int deleteByPrimaryKey(String uid);

    int insert(Mail record);

    Mail selectByPrimaryKey(String uid);

    int updateByPrimaryKey(Mail record);

    int updateSimpleFields(Mail record);

    List<Mail> selectByToUser(String uid);

	List<Mail> selectByToUserWithoutOrder(String uid);

    List<Mail> selectAllMailByToUserFromNewWorldOnline(String uid);

    List<Mail> selectByToUserASC(String uid);

    int batchDelete(Collection<String> uids);
	int batchRealDelete(Collection<String> uids);

    int batchDeleteByRewardStatus(Collection<String> uids);

    int deleteAll(Map<String, Object> map);

    int deleteUserMails(String uid);

	int deleteByRewardStatus(String uid);

	List<Mail> selectLimitByToUserFrom(@Param("toUser") String toUser, @Param("fromUser") String fromUser, @Param("typeMin") int typeMin, @Param("typeMax") int typeMax, @Param("num") int num, @Param("offSet") int offSet);

	List<Mail> selectLimitTypeMails(@Param("toUser") String toUser, @Param("type") int type, @Param("num") int num, @Param("offSet") int offSet);

	List<Mail> selectLimitOldFightMails(@Param("toUser") String toUser, @Param("num") int num, @Param("offSet") int offSet);
	List<Mail> selectLimitOldIIFightMails(@Param("toUser") String toUser, @Param("num") int num, @Param("offSet") int offSet);
	List<Mail> selectLimitFightMails(@Param("toUser") String toUser, @Param("num") int num, @Param("offSet") int offSet);

	List<Integer> selectPersonStatus(@Param("toUser") String toUser, @Param("fromUser") String fromUser);

    List<Integer> selectPersonStatusMod(@Param("toUser") String toUser, @Param("fromUser") String fromUser);

	List<Mail> selectLimitBossRewardMails(@Param("toUser") String toUser, @Param("num") int num, @Param("offSet") int offSet);

	List<Mail> selectRewardMailByToUserAndTitle(@Param("toUser") String toUser, @Param("title") String title);

	int updateMailStatus(@Param("toUser") String toUser, @Param("fromUser") String fromUser);

    int updateMailStatusMod(@Param("toUser") String toUser, @Param("fromUser") String fromUser);

	int updateMailStatusByType(@Param("toUser") String toUser, @Param("type") int type);

	int updateMailSaveFlag(@Param("toUser") String toUser, @Param("fromUser") String fromUser, @Param("saveFlag") int saveFlag);

    int updateMailSaveFlagMod(@Param("toUser") String toUser, @Param("fromUser") String fromUser, @Param("saveFlag") int saveFlag);

	int updateMailSaveFlagByType(@Param("toUser") String toUser, @Param("type") int type, @Param("saveFlag") int saveFlag);

	int deleteAllFromUser(@Param("toUser") String toUser, @Param("fromUser") String fromUser);

    int deleteAllFromUserMod(@Param("toUser") String toUser, @Param("fromUser") String fromUser);

	int deleteByType(@Param("toUser") String toUser, @Param("type") int type);

	List<Mail> selectInToUserFrom(@Param("toUser") String toUser, @Param("fromUsers") Set<String> fromUsers);

	List<Mail> selectInToUserFromMod(@Param("toUser") String toUser, @Param("fromUsers") Set<String> fromUsers);

	List<Mail> selectTypeMailsTimeDESC(@Param("toUser") String toUser, @Param("type") int type);

	List<Mail> selectTypeMailsTimeASC(@Param("toUser") String toUser, @Param("type") int type);

    List<Mail> selectPerSysUids(String ownerId);

    List<Mail> selectModUids(String ownerId);

	List<Mail> selectSaveUids(String ownerId);

	List<Mail> selectSaveOnlyUids(String ownerId);

	List<Mail> selectFightUids(String ownerId);

	List<Mail> selectStudioUids(String ownerId);

	int insertBatch(List<Mail> mailList);

	List<Mail> selectMailIn(List<String> uids);

	void updateSaveFlagByUids(List<String> uids);

	List<Mail> selectLimitSaveMails(Map<String, Object> offSetMap);

	List<Mail> selectLimitStudioMails(Map<String, Object> offSetMap);

	List<Mail> selectOldMail(String uid);

	List<Mail> selectBossRewardUids(String ownerId);

	void updateFromUser(Mail mail);
}