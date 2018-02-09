package com.geng.puredb.model;

import com.geng.core.ConcurrentLock;
import com.geng.gameengine.mail.MailFunction;
import com.geng.gameengine.mail.MailServicePlus;
import com.geng.gameengine.mail.MailType;
import com.geng.gameengine.mail.MailSrcFuncType;
import com.geng.gameengine.mail.send.SystermMailSend;
import com.geng.gameengine.reward.RewardManager;
import com.geng.puredb.dao.MailMapper;
import com.geng.utils.CommonUtils;
import com.geng.utils.MyBatisSessionUtil;
import com.geng.core.data.ISFSObject;
import com.geng.core.data.SFSObject;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;

import java.io.Serializable;
import java.util.*;

public class Mail implements Serializable, Comparable<Mail>{
    private String uid;

    private String touser;

    private String fromuser;

    private String fromname;

    private String title;

    private int status;			//0-未读 1-已读 2-已感谢

	private int type;			//MailType

    private byte[] rewardid;

    private int rewardstatus;	//0-未领取 1-已领取(没有奖励 此项也存1)

    private long createtime;

    private byte[] contents;

    private int itemidflag; 	//邮件的内容为一个dialog号

	private int saveflag;		//移至保存邮箱 0-未保存 1-已保存 2-在普通邮箱里删除后 变为2 则以后不会在普通邮箱里出现  3-已删除（为假删除，除了gm工具其它地方查不到已删除的邮件）

	private int reply;

	private String translationId;

	private int mbLevel;

    private int srctype;//标注 邮件的来源,哪个活动发的 MailSrcFuncType

	/*已下字段 非数据库字段 仅作为 程序中使用*/
	private boolean newOne;		//邮件是否 是new出来的 决定该 insert 还是 update
	public Integer groupTotal;		//统计采集个数
	public Integer groupUnread;		//统计采集未读个数
	public static int MAIL_SELECT_TYPE_TITLE = 12;

    public static ISFSObject mailToISFSObject(Mail mailItem, Map<String, Map<String, String>> userPicMap, String appVersion) {
		ISFSObject retObject = new SFSObject();
		retObject.putUtfString("uid", mailItem.getUid());
		retObject.putUtfString("fromUid", mailItem.getFromuser() == null ? "" : mailItem.getFromuser());
		if (mailItem.getType() == MailType.UpNotice.ordinal()) {
			retObject.putUtfString("version", mailItem.getFromuser() == null ? "1.0.59" : mailItem.getFromuser());
		}
		retObject.putUtfString("title", mailItem.getTitle() == null ? "" : mailItem.getTitle());
		retObject.putInt("status", mailItem.getStatus());
		retObject.putInt("type", MailFunction.serverType2Client(mailItem.getType(), appVersion));
		retObject.putUtfString("rewardId", mailItem.getRewardStr());
		retObject.putInt("rewardStatus", mailItem.getRewardstatus());
		retObject.putLong("createTime", mailItem.getCreatetime());
		retObject.putInt("itemIdFlag", mailItem.getItemidflag());
		retObject.putInt("save", mailItem.getSaveflag());
		retObject.putInt("mbLevel", mailItem.getMbLevel());
		fillExtraParams(mailItem, retObject);
		if (mailItem.getType() == MailType.Fight.ordinal()) {
			SystermMailSend.addFightMailInfo(mailItem, retObject);
		}
		putPicInfo(retObject, mailItem, userPicMap.get(mailItem.getFromuser()));
		return retObject;
	}

	/**
	 * 邮件新增字段0-1表示
	 * 新加share字段，与reply公用一个int值
	 * 解析时候先解析reply，再解析share
	 * 如果reply = 1, share = 1, 则数据库中为二进制的11，值为3
	 * 如果reply = 0, share = 1, 则数据库中为二进制的10, 值为2
	 * 如果reply = 1, share = 0, 则数据库中为二进制的01, 值为1
	 * 以此类推
	 * 多加字段，为了不改mail表，且为状态型的字段可以采用这种方法
	* */
	public static void fillExtraParams(Mail mailItem, ISFSObject retObject){
		int value = mailItem.getReply();
		int share = 0b10;
		int reply = 0b01;
		int like = 0b100;
		int donate = 0b1000;
		if((value & share) == share){
			retObject.putInt("share", 1);
		}else{
			retObject.putInt("share", 0);
		}
		if((value & reply) == reply){
			retObject.putInt("reply", 1);
		}else{
			retObject.putInt("reply", 0);
		}
		if((value & like) == like){
			retObject.putInt("like", 1);
		}else{
			retObject.putInt("like", 0);
		}
		if((value & donate) == donate){
			retObject.putInt("donate", 1);
		}else{
			retObject.putInt("donate", 0);
		}
	}

	public static void putPicInfo(ISFSObject mailObj, Mail mailItem, Map<String, String> userPicMap) {
		boolean isContentsPicInfo = false;
		if (userPicMap != null && StringUtils.isNotBlank(mailItem.getFromuser())) {
			isContentsPicInfo = true;
		}
		String fromName = isContentsPicInfo ? userPicMap.get("name") : mailItem.getFromname();
		if(isContentsPicInfo){ //前台聊天用：如果是用户之间的私聊邮件，则加入此字段
			String lastUpdateTime = "";
//			if(mailItem.getType() == MailType.Personal.ordinal()){
//				lastUpdateTime = UserProfile.getLastUpdateInfoTime(mailItem.getFromuser()).or("");
//			}else if(mailItem.getType() == MailType.Send.ordinal()){
//				lastUpdateTime = UserProfile.getLastUpdateInfoTime(mailItem.getTouser()).or("");
//			}
//			mailObj.putUtfString("lastUpdateTime", lastUpdateTime);

//			mailObj.putLong("seqId", mailObj.getLong("createTime"));
		}
		mailObj.putUtfString("fromName", fromName == null ? "" : fromName);
		String oldName = StringUtils.isBlank(mailItem.getFromname()) ?  fromName : mailItem.getFromname();
		mailObj.putUtfString("oldName", StringUtils.isBlank(oldName) ? "" : oldName);
		if (isContentsPicInfo) {
			mailObj.putUtfString("pic", userPicMap.get("pic"));
            if (userPicMap.containsKey("picVer")) {
                mailObj.putUtfString("picVer", userPicMap.get("picVer"));
            } else {
                mailObj.putUtfString("picVer", "0");
            }
			String abbr = userPicMap.get("abbr");
			if (!StringUtils.isBlank(abbr)) {
				mailObj.putUtfString("alliance", abbr);
			}
		}
	}
    public static ISFSObject mailToChatSFSObject(Mail mailItem, boolean isGetContents, Map<String, String> userPicMap, String appVersion) {
        return mailToChatSFSObject( mailItem,  isGetContents,  userPicMap,  appVersion,true);
    }
	public static ISFSObject mailToChatSFSObject(Mail mailItem, boolean isGetContents, Map<String, String> userPicMap, String appVersion,boolean isNeed) {
		ISFSObject retObject;
		if (isGetContents) {
			retObject = MailServicePlus.transContentsObj(mailItem, appVersion, userPicMap == null ? "" : userPicMap.get("lang"));
		} else {
			retObject = new SFSObject();
		}
		retObject.putUtfString("uid", mailItem.getUid());
		retObject.putInt("type",  MailFunction.serverType2Client(mailItem.getType(), appVersion));
		retObject.putLong("createTime", mailItem.getCreatetime());
        if(isNeed) {
            retObject.putUtfString("lastUpdateTime", UserProfile.getLastUpdateInfoTime(mailItem.getTouser()));
        }else {
//            retObject.putUtfString("lastUpdateTime", "0");
        }
		if (mailItem.getType() == MailType.TradeResource.ordinal()) {
			putPicInfo(retObject, mailItem, userPicMap);
		}
		return retObject;
	}

    public static List<Mail> getMails(String toUser) {
    	SqlSession session = MyBatisSessionUtil.getInstance().getSession();
    	List<Mail> mails = new ArrayList<> ();
    	try {
    		MailMapper mailMapper = session.getMapper(MailMapper.class);
    		mails = mailMapper.selectByToUser(toUser);
    	} finally {
    		session.close();
    	}
    	return mails == null ? new LinkedList<Mail>() : mails;
    }
	public static List<Mail> getMailsWithoutOrder(String toUser) {
		SqlSession session = MyBatisSessionUtil.getInstance().getSession();
		List<Mail> mails = new ArrayList<> ();
		try {
			MailMapper mailMapper = session.getMapper(MailMapper.class);
			mails = mailMapper.selectByToUserWithoutOrder(toUser);
		} finally {
			session.close();
		}
		return mails == null ? new LinkedList<Mail>() : mails;
	}
    public static List<Mail> getAllMailsFromNewWorldOnline(String toUser){
		SqlSession session = MyBatisSessionUtil.getInstance().getSession();
		List<Mail> mails = new ArrayList<> ();
		try {
			MailMapper mailMapper = session.getMapper(MailMapper.class);
			mails = mailMapper.selectAllMailByToUserFromNewWorldOnline(toUser);
		} finally {
			session.close();
		}
		return mails == null ? new LinkedList<Mail>() : mails;
	}

	public static List<Mail> getMails(List<String> uidList) {
		if (uidList == null || uidList.isEmpty()) {
			return null;
		}
		SqlSession session = MyBatisSessionUtil.getInstance().getSession();
		List<Mail> mails = new ArrayList<> ();
		try {
			MailMapper mailMapper = session.getMapper(MailMapper.class);
			mails = mailMapper.selectMailIn(uidList);
		} finally {
			session.close();
		}
		return mails == null ? new LinkedList<Mail>() : mails;
	}

	public static List<Mail> selectLimitMails(String appVersion, Map<String, Object> offSetMap, int type, String fromUser) {
		SqlSession session = MyBatisSessionUtil.getInstance().getSession();
		List<Mail> mails = null;
		try {
			MailMapper mailMapper = session.getMapper(MailMapper.class);
			int mailType = 0;
			switch (type) {
				case 2:
					mails = mailMapper.selectLimitSaveMails(offSetMap);
					break;
				case 3:
					mails = mailMapper.selectLimitStudioMails(offSetMap);
					break;
				case 4:
					mails = mailMapper.selectLimitByToUserFrom((String) offSetMap.get("uid"), fromUser, 19, 23, (int) offSetMap.get("num"), (int) offSetMap.get("offSet"));
					break;
				case 5:
					if (0 == mailType) {
						mailType = MailType.Resource.ordinal();
					}
				case 7:
					if (0 == mailType) {
						mailType = MailType.WORLD_MONSTER.ordinal();
					}
				case 8:
					if (0 == mailType) {
						mailType = MailType.TradeResource.ordinal();
					}
					mails = mailMapper.selectLimitTypeMails((String) offSetMap.get("uid"), mailType, (int) offSetMap.get("num"), (int) offSetMap.get("offSet"));
					break;
				case 6:
						mails = mailMapper.selectLimitFightMails((String) offSetMap.get("uid"), (int) offSetMap.get("num"), (int) offSetMap.get("offSet"));
					break;
				case 9:
					mails = mailMapper.selectLimitByToUserFrom((String) offSetMap.get("uid"), fromUser, 22, 25, (int) offSetMap.get("num"), (int) offSetMap.get("offSet"));
					break;
				case 11:
					mails = mailMapper.selectLimitBossRewardMails((String) offSetMap.get("uid"), (int) offSetMap.get("num"), (int) offSetMap.get("offSet"));
					break;
				case 12:
					mails = mailMapper.selectRewardMailByToUserAndTitle((String) offSetMap.get("uid"),(String) offSetMap.get("title"));
					break;
			}
		} finally {
			session.close();
		}
		return mails == null ? new LinkedList<Mail>() : mails;
	}

	public static List<Mail> selectMailsFromUsers(String toUser, Set<String> fromUserSet, String appVersion, boolean isMod) {
		if (fromUserSet == null || fromUserSet.isEmpty()) {
			return null;
		}
		SqlSession session = MyBatisSessionUtil.getInstance().getSession();
		MailMapper mailMapper = session.getMapper(MailMapper.class);
		try {
			List<Mail> mailList;
			if (isMod) {
				mailList = mailMapper.selectInToUserFromMod(toUser, fromUserSet);
			} else {
				mailList = mailMapper.selectInToUserFrom(toUser, fromUserSet);
			}
			return mailList;
		} finally {
			session.close();
		}
	}

	public static List<Mail> selectTypeMailsTimeDESC(String toUser, int mailType) {
		SqlSession session = MyBatisSessionUtil.getInstance().getSession();
		MailMapper mailMapper = session.getMapper(MailMapper.class);
		try {
			List<Mail> mailList = mailMapper.selectTypeMailsTimeDESC(toUser, mailType);
			return mailList;
		} finally {
			session.close();
		}
	}

	public static List<Mail> selectTypeMailsTimeASC(String toUser, int mailType) {
		SqlSession session = MyBatisSessionUtil.getInstance().getSession();
		MailMapper mailMapper = session.getMapper(MailMapper.class);
		try {
			List<Mail> mailList = mailMapper.selectTypeMailsTimeASC(toUser, mailType);
			return mailList;
		} finally {
			session.close();
		}
	}

	public static void updateSaveMailByUids(List<String> uidList) {
		SqlSession session = MyBatisSessionUtil.getInstance().getSession();
		try {
			MailMapper mailMapper = session.getMapper(MailMapper.class);
			mailMapper.updateSaveFlagByUids(uidList);
			session.commit();
		} finally {
			session.close();
		}
	}

	/**
	 * 假删除 邮件（如果以保存 是删不掉的）
	 */
    public static int batchDelete(Collection<String> uids) {
    	SqlSession session = MyBatisSessionUtil.getInstance().getSession();
    	int res = -1;
    	try {
    		MailMapper mailMapper = session.getMapper(MailMapper.class);
    		res = mailMapper.batchDelete(uids);
    		session.commit();
    	} finally {
    		session.close();
    	}
    	return res;
    }
	/**
	 * 真删除 邮件（如果以保存 是删不掉的）
	 */
	public static int batchRealDelete(Collection<String> uids) {
		SqlSession session = MyBatisSessionUtil.getInstance().getSession();
		int res = -1;
		try {
			MailMapper mailMapper = session.getMapper(MailMapper.class);
			res = mailMapper.batchRealDelete(uids);
			session.commit();
		} finally {
			session.close();
		}
		return res;
	}

	/**
	 * 删除 邮件（如果以保存 是删不掉的）
	 */
	public static int batchDeleteByRewardStatus(Collection<String> uids) {
		SqlSession session = MyBatisSessionUtil.getInstance().getSession();
		int res = -1;
		try {
			MailMapper mailMapper = session.getMapper(MailMapper.class);
			for(String uid: uids){
				res = mailMapper.deleteByRewardStatus(uid);
			}
			session.commit();
		} finally {
			session.close();
		}
		return res;
	}


	public static int delete(String uid){
		return delete(uid, null);
	}

	public static int delete(String uid, SqlSession session) {
		boolean autoClose = false;
		if(session == null){
			session = MyBatisSessionUtil.getInstance().getSession();
			autoClose = true;
		}
		int res = -1;
		try {
			MailMapper mailMapper = session.getMapper(MailMapper.class);
			res = mailMapper.deleteByPrimaryKey(uid);
			if(autoClose) {
				session.commit();
			}
		} finally {
			if(autoClose){
				session.close();
			}
		}
		return res;
	}
    
    public static int deleteAll(String userUid, int type) {
    	SqlSession session = MyBatisSessionUtil.getInstance().getSession();
    	int res = -1;
    	try {
    		MailMapper mailMapper = session.getMapper(MailMapper.class);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("ownerId", userUid);
			map.put("type", type);
    		res = mailMapper.deleteAll(map);
    		session.commit();
    	} finally {
    		session.close();
    	}
    	return res;
    }
	public static List<Mail> getAllSizeMailWithoutOrder(String userUid, SqlSession session){
		boolean isClose = false;
		if (session == null) {
			session = MyBatisSessionUtil.getInstance().getSession();
			isClose = true;
		}
		List<Mail> res = null;
		try {
			MailMapper mailMapper = session.getMapper(MailMapper.class);
			res = mailMapper.selectByToUserWithoutOrder(userUid);
		} finally {
			if (isClose) {
				session.close();
			}
		}
		return res;
	}
	public static List<Mail> getAllSizeMail(String userUid, SqlSession session){
		boolean isClose = false;
		if (session == null) {
			session = MyBatisSessionUtil.getInstance().getSession();
			isClose = true;
		}
		List<Mail> res = null;
		try {
			MailMapper mailMapper = session.getMapper(MailMapper.class);
			res = mailMapper.selectByToUserASC(userUid);
		} finally {
			if (isClose) {
				session.close();
			}
		}
		return res;
	}
    
    public static List<Mail> getSizeMail(String userUid, int type, SqlSession session) {
		boolean isClose = false;
		if (session == null) {
			session = MyBatisSessionUtil.getInstance().getSession();
			isClose = true;
		}
    	List<Mail> res = null;
    	try {
    		MailMapper mailMapper = session.getMapper(MailMapper.class);
			if (type == MailFunction.MAIL_LABEL.per_sys.ordinal()) {
				res = mailMapper.selectPerSysUids(userUid);
			} else if (type == MailFunction.MAIL_LABEL.mod.ordinal()) {
                res = mailMapper.selectModUids(userUid);
            } else if (type == MailFunction.MAIL_LABEL.save.ordinal()){
				 res = mailMapper.selectSaveOnlyUids(userUid);
			} else if (type == MailFunction.MAIL_LABEL.studio.ordinal()){
				res = mailMapper.selectStudioUids(userUid);
			} else if (type == MailFunction.MAIL_LABEL.fight.ordinal()){
				res = mailMapper.selectFightUids(userUid);
			} else if (type == MailFunction.MAIL_LABEL.boss_reward.ordinal()){
				res = mailMapper.selectBossRewardUids(userUid);
			} else {
				res = mailMapper.selectSaveUids(userUid);
			}
    	} finally {
			if (isClose) {
				session.close();
			}
    	}
    	return res;
    }

    public int save(SqlSession session) {
		boolean isClose = false;
		if (session == null) {
			isClose = true;
			session = MyBatisSessionUtil.getInstance().getSession();
		}
    	int res = -1;
    	try {
    		MailMapper mailMapper = session.getMapper(MailMapper.class);
    		res = mailMapper.insert(this);
			if(isClose){
				session.commit();
			}
    	} finally {
			if (isClose) {
				session.close();
			}
    	}
    	return res;
    }

	public static int insertBatch(List<Mail> mailList, SqlSession session) {
		if (mailList == null || mailList.size() < 1) {
			return 0;
		}
		boolean isClose = false;
		if (session == null) {
			isClose = true;
			session = MyBatisSessionUtil.getInstance().getSession();
		}
		int res = -1;
		try {
			MailMapper mailMapper = session.getMapper(MailMapper.class);
			res = mailMapper.insertBatch(mailList);
			if(isClose) {
				session.commit();
			}
		} finally {
			if (isClose) {
				session.close();
			}
		}
		return res;
	}

    public int update(SqlSession session) {
		boolean isClose = false;
		if (session == null) {
			isClose = true;
			session = MyBatisSessionUtil.getInstance().getSession();
		}
		int res = -1;
		try {
			MailMapper mailMapper = session.getMapper(MailMapper.class);
			res = mailMapper.updateSimpleFields(this);
			if(isClose) {
				session.commit();
			}
		} finally {
			if (isClose) {
				session.close();
			}
		}
		return res;
	}

	public int updateAllFields(SqlSession session) {
		boolean isClose = false;
		if (session == null) {
			isClose = true;
			session = MyBatisSessionUtil.getInstance().getSession();
		}
		int res = -1;
		try {
			MailMapper mailMapper = session.getMapper(MailMapper.class);
			res = mailMapper.updateByPrimaryKey(this);
			if(isClose) {
				session.commit();
			}
		} finally {
			if (isClose) {
				session.close();
			}
		}
		return res;
	}

	public static List<Integer> selectPersonStatus(String fromUser, UserProfile toUser) {
		SqlSession session = MyBatisSessionUtil.getInstance().getSession();
		List<Integer> res;
		try {
			MailMapper mailMapper = session.getMapper(MailMapper.class);
			res = mailMapper.selectPersonStatus(toUser.getUid(), fromUser);
		} finally {
			session.close();
		}
		return res;
	}

    public static List<Integer> selectPersonStatusMod(String fromUser, UserProfile toUser) {
        SqlSession session = MyBatisSessionUtil.getInstance().getSession();
        List<Integer> res;
        try {
            MailMapper mailMapper = session.getMapper(MailMapper.class);
            res = mailMapper.selectPersonStatusMod(toUser.getUid(), fromUser);
        } finally {
            session.close();
        }
        return res;
    }

	public static int updateMailStatus(String fromUser, String toUser) {
		SqlSession session = MyBatisSessionUtil.getInstance().getSession();
    	int res = -1;
    	try {
    		MailMapper mailMapper = session.getMapper(MailMapper.class);
    		res = mailMapper.updateMailStatus(toUser, fromUser);
			session.commit();
    	} finally {
			session.close();
    	}
    	return res;
    }

    public static int updateMailStatusMod(String fromUser, String toUser) {
        SqlSession session = MyBatisSessionUtil.getInstance().getSession();
        int res = -1;
        try {
            MailMapper mailMapper = session.getMapper(MailMapper.class);
            res = mailMapper.updateMailStatusMod(toUser, fromUser);
            session.commit();
        } finally {
            session.close();
        }
        return res;
    }

	public static int updateMailStatus(String toUser, int type) {
		SqlSession session = MyBatisSessionUtil.getInstance().getSession();
		int res = -1;
		try {
			MailMapper mailMapper = session.getMapper(MailMapper.class);
			res = mailMapper.updateMailStatusByType(toUser, type);
			session.commit();
		} finally {
			session.close();
		}
		return res;
	}

	public static int deleteAllFromUser(String fromUser, String toUser) {
		SqlSession session = MyBatisSessionUtil.getInstance().getSession();
		int res = -1;
		try {
			MailMapper mailMapper = session.getMapper(MailMapper.class);
			res = mailMapper.deleteAllFromUser(toUser, fromUser);
			session.commit();
		} finally {
			session.close();
		}
		return res;
	}

    public static int deleteAllFromUserMod(String fromUser, String toUser) {
        SqlSession session = MyBatisSessionUtil.getInstance().getSession();
        int res = -1;
        try {
            MailMapper mailMapper = session.getMapper(MailMapper.class);
            res = mailMapper.deleteAllFromUserMod(toUser, fromUser);
            session.commit();
        } finally {
            session.close();
        }
        return res;
    }

	public static int deleteByType(String toUser, int type) {
		SqlSession session = MyBatisSessionUtil.getInstance().getSession();
		int res = -1;
		try {
			MailMapper mailMapper = session.getMapper(MailMapper.class);
			res = mailMapper.deleteByType(toUser, type);
			session.commit();
		} finally {
			session.close();
		}
		return res;
	}

	public static int updateMailSaveFlag(String fromUser, int flag, String toUser) {
		SqlSession session = MyBatisSessionUtil.getInstance().getSession();
		int res = -1;
		try {
			MailMapper mailMapper = session.getMapper(MailMapper.class);
			res = mailMapper.updateMailSaveFlag(toUser, fromUser, flag);
			session.commit();
		} finally {
			session.close();
		}
		return res;
	}

    public static int updateMailSaveFlagMod(String fromUser, int flag, String toUser) {
        SqlSession session = MyBatisSessionUtil.getInstance().getSession();
        int res = -1;
        try {
            MailMapper mailMapper = session.getMapper(MailMapper.class);
            res = mailMapper.updateMailSaveFlagMod(toUser, fromUser, flag);
            session.commit();
        } finally {
            session.close();
        }
        return res;
    }

	public static int updateMailSaveFlag(String toUser, int type, int flag) {
		SqlSession session = MyBatisSessionUtil.getInstance().getSession();
		int res = -1;
		try {
			MailMapper mailMapper = session.getMapper(MailMapper.class);
			res = mailMapper.updateMailSaveFlagByType(toUser, type, flag);
			session.commit();
		} finally {
			session.close();
		}
		return res;
	}

	public void updateFromUser() {
		SqlSession session = MyBatisSessionUtil.getInstance().getSession();
		try {
			session.getMapper(MailMapper.class).updateFromUser(this);
			session.commit();
		} finally {
			session.close();
		}
	}

    public static Mail getWithUid(String uid) {
    	SqlSession session = MyBatisSessionUtil.getInstance().getSession();
    	Mail mailItem = new Mail();
    	try {
    		MailMapper mailMapper = session.getMapper(MailMapper.class);
    		mailItem = mailMapper.selectByPrimaryKey(uid);
    	} finally {
    		session.close();
    	}
    	return mailItem;
    }

	public static String getInsertSqlHead() {
		return "INSERT INTO mail (uid, toUser, fromUser, fromName, title, `status`, type, rewardStatus, itemIdFlag, createTime, saveFlag, contents, rewardId) VALUES ";
	}


	/**
	 * @Override
	 * 用于批量更新时避免死锁
	 */
	public int compareTo(Mail o) {
		return uid.compareTo(o.getUid());
	}

	public String getInsertSqlValue() {
		StringBuilder ret = new StringBuilder("(");
		ret.append('\'').append(uid).append("',");
		ret.append('\'').append(touser).append("',");
		if (fromuser == null) {
			ret.append('\'').append("").append("',");
		} else {
			ret.append('\'').append(StringUtils.replaceEach(fromuser, new String[]{"'"}, new String[]{"\\'"})).append("',");
		}
		if (fromname == null) {
			ret.append('\'').append("").append("',");
		} else {
			ret.append('\'').append(StringUtils.replaceEach(fromname, new String[]{"'"}, new String[]{"\\'"})).append("',");
		}
		if (title == null) {
			ret.append('\'').append("").append("',");
		} else {
			ret.append('\'').append(StringUtils.replace(title, "'", "\\'")).append("',");
		}
		ret.append(status).append(',');
		ret.append(type).append(',');
		ret.append(rewardstatus).append(',');
		ret.append(itemidflag).append(',');
		ret.append(createtime).append(',');
		ret.append(saveflag).append(',');
		String contentStr =  StringUtils.replaceEach(getContentsStr(), new String[]{"'"}, new String[]{"\\'"});
		ret.append('\'').append(contentStr).append("',");
		ret.append('\'').append(getRewardStr()).append('\'');
		ret.append(')');
		return ret.toString();
	}

	public Mail() {
	}

	public Mail(String uid, String fromUid, String targetUid, String title, String rewardId, long createTime,
				String contents, MailType mailType, int saveFlag, boolean itemIdFlag, String fromName, int mbLevel,MailSrcFuncType srctype) {
		init(uid, fromUid, targetUid, title, rewardId, createTime, contents, mailType, saveFlag, itemIdFlag, fromName, mbLevel, false,srctype);
	}
	public Mail(String uid, String fromUid, String targetUid, String title, String rewardId, long createTime,
				String contents, MailType mailType, int saveFlag, boolean itemIdFlag, String fromName, int mbLevel, boolean forceSetNoReward,MailSrcFuncType srctype) {
		init(uid, fromUid, targetUid, title, rewardId, createTime, contents, mailType, saveFlag, itemIdFlag, fromName, mbLevel, forceSetNoReward,srctype);
	}

	private void init(String uid, String fromUid, String targetUid, String title, String rewardId, long createTime,
				String contents, MailType mailType, int saveFlag, boolean itemIdFlag, String fromName, int mbLevel, boolean forceSetNoReward,MailSrcFuncType srctype) {
		setFromuser(fromUid);
		setFromname(fromName == null ? "" : fromName);
		setTouser(targetUid);
		setTitle(title == null ? "" : title);
		setRewardstatus(1);
		setReply(0);
		String rewardString = null;
		if(!StringUtils.isBlank(rewardId)) {
			if (StringUtils.contains(rewardId, ',')) {
				rewardString = rewardId;
			} else {
				rewardString = RewardManager.getRewardString(rewardId, targetUid);
			}
		}
		if (!StringUtils.isBlank(rewardString)) {
			setRewardid(CommonUtils.fromStringToByte(rewardString));
			if (!forceSetNoReward) {
				setRewardstatus(0);
			}
		}
		setCreatetime(createTime);
		setContents(CommonUtils.fromStringToByte(contents));
		setType(mailType.ordinal());
		setStatus(0);
		setSaveflag(saveFlag);
		setItemidflag(itemIdFlag ? 1 : 0);
		setUid(uid);
		setMbLevel(mbLevel);
        setSrctype(srctype.getValue());
	}

	public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid == null ? null : uid.trim();
    }

    public String getTouser() {
        return touser;
    }

    public void setTouser(String touser) {
        this.touser = touser == null ? null : touser.trim();
    }

    public String getFromuser() {
        return fromuser;
    }

    public void setFromuser(String fromuser) {
        this.fromuser = fromuser == null ? null : fromuser.trim();
    }

    public String getFromname() {
        return fromname;
    }

    public void setFromname(String fromname) {
        this.fromname = fromname == null ? null : fromname.trim();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null ? null : title.trim();
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public byte[] getRewardid() {
        return rewardid;
    }

    public void setRewardid(byte[] rewardid) {
        this.rewardid = rewardid;
    }

    public int getRewardstatus() {
        return rewardstatus;
    }

    public void setRewardstatus(int rewardstatus) {
        this.rewardstatus = rewardstatus;
    }

    public long getCreatetime() {
        return createtime;
    }

    public void setCreatetime(long createtime) {
        this.createtime = createtime;
    }

    public byte[] getContents() {
		return contents;
    }

    public void setContents(byte[] contents) {
        this.contents = contents;
    }

	public int getSaveflag() {
		return saveflag;
	}

	public void setSaveflag(int saveFlag) {
		this.saveflag = saveFlag;
	}

	public int getItemidflag() {
		return itemidflag;
	}

	public void setItemidflag(int itemIdFlag) {
		this.itemidflag = itemIdFlag;
	}

	public String getRewardStr() {
		String ret = CommonUtils.fromByteToString(rewardid);
		return ret == null ? "" : ret.trim();
	}

	public String getContentsStr() {
		String ret = CommonUtils.fromByteToString(contents);
		return ret == null ? "" : ret.trim();
	}

	public boolean isNewOne() {
		return newOne;
	}

	public void setNewOne(boolean newOne) {
		this.newOne = newOne;
	}

	public int getReply() {
		return reply;
	}

	public void setReply(int reply) {
		this.reply = reply;
	}

	public String getTranslationId() {
		return translationId;
	}

	public void setTranslationId(String translationId) {
		this.translationId = translationId;
	}

	public int getMbLevel() { return mbLevel; }

	public void setMbLevel(int mbLevel) { this.mbLevel = mbLevel; }

    public int getSrctype() {
        return srctype;
    }

    public void setSrctype(int srctype) {
        this.srctype = srctype;
    }
}
