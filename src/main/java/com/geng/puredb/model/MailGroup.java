package com.geng.puredb.model;

import com.geng.gameengine.mail.MailFunction;
import com.geng.gameengine.mail.MailType;
import com.geng.puredb.dao.MailGroupMapper;
import com.geng.utils.MyBatisSessionUtil;
import org.apache.ibatis.session.SqlSession;

import java.io.Serializable;
import java.util.*;

public class MailGroup implements Serializable {
	private String uid;

	private int grouptype;

	private String groupindex;

	private long updatetime;

	public static List<MailGroup> getGroupList(String uid, SqlSession session) {
		boolean isClose = false;
		if (null == session) {
			session = MyBatisSessionUtil.getInstance().getSession();
			isClose = true;
		}
		List<MailGroup> mails = new ArrayList<>();
		try {
			MailGroupMapper mailGroupMapper = session.getMapper(MailGroupMapper.class);
			mails = mailGroupMapper.selectByUid(uid);
		} finally {
			if (isClose) {
				session.close();
			}
		}
		return mails == null ? new LinkedList<MailGroup>() : mails;
	}

	public static void deleteUserGroup(String uid, SqlSession session) {
		boolean isClose = false;
		if (null == session) {
			session = MyBatisSessionUtil.getInstance().getSession();
			isClose = true;
		}
		try {
			MailGroupMapper mailGroupMapper = session.getMapper(MailGroupMapper.class);
			mailGroupMapper.deleteByUid(uid);
			if(isClose) {
				session.commit();
			}
		} finally {
			if (isClose) {
				session.close();
			}
		}
	}

	/**
	 * 删除一段(typeMin, typeMax)范围内的group
	 * @param typeMin 不包括
	 * @param typeMax 不包括
	 */
	public static void deleteRangeGroup(String uid, int typeMin, int typeMax) {
		SqlSession sqlSession = MyBatisSessionUtil.getInstance().getSession();
		try {
			MailGroupMapper mailGroupMapper = sqlSession.getMapper(MailGroupMapper.class);
			mailGroupMapper.deleteByRangeType(uid, typeMin, typeMax);
			sqlSession.commit();
		} finally {
			sqlSession.close();
		}
	}

	public static List<MailGroup> getLimitGroupList(String uid, int num, int offSet) {
		SqlSession session = MyBatisSessionUtil.getInstance().getSession();
		List<MailGroup> mails = new ArrayList<>();
		try {
			MailGroupMapper mailGroupMapper = session.getMapper(MailGroupMapper.class);
			mails = mailGroupMapper.selectLimitGroups(uid, num, offSet);
		} finally {
			session.close();
		}
		return mails == null ? new LinkedList<MailGroup>() : mails;
	}

    public static List<MailGroup> getLimitGroupListMod(String uid, int num, int offSet) {
        SqlSession session = MyBatisSessionUtil.getInstance().getSession();
        List<MailGroup> mails = new ArrayList<>();
        try {
            MailGroupMapper mailGroupMapper = session.getMapper(MailGroupMapper.class);
            mails = mailGroupMapper.selectLimitGroupsMod(uid, num, offSet);
        } finally {
            session.close();
        }
        return mails == null ? new LinkedList<MailGroup>() : mails;
    }

	public static void insertBatch(List<MailGroup> groupList) {
		if(groupList == null || groupList.isEmpty()) {
			return;
		}
		SqlSession session = MyBatisSessionUtil.getInstance().getSession();
		try {
			MailGroupMapper mailGroupMapper = session.getMapper(MailGroupMapper.class);
			mailGroupMapper.batchInsert(groupList);
			session.commit();
		} finally {
			session.close();
		}
	}

	public static void replaceBatch(List<MailGroup> groupList) {
		if(groupList == null || groupList.isEmpty()) {
			return;
		}
		SqlSession session = MyBatisSessionUtil.getInstance().getSession();
		try {
			replaceBatch(groupList, session);
			session.commit();
		} finally {
			session.close();
		}
	}

	public static void replaceBatch(List<MailGroup> groupList, SqlSession session) {
		if(groupList == null || groupList.isEmpty()) {
			return;
		}
		session.getMapper(MailGroupMapper.class).batchReplace(groupList);
	}

	public static List<MailGroup> synMailToGroup(List<Mail> mailList, String uid, String appVersion) {
		MailGroup.deleteUserGroup(uid, null);
		List<MailGroup> groupList = new ArrayList<>();
		Set<String> fromUserSet = new HashSet<>();
        Set<String> fromUserSetMod = new HashSet<>();
		boolean haveResource = false, haveMonster = false, haveTrade = false;
		for (Mail mail : mailList) {
			int mailType = mail.getType();
			if (mailType == MailType.MAIL_FRESHER.ordinal() || mailType == MailType.SysNotice.ordinal() || mailType == MailType.UpNotice.ordinal()
					|| mailType == MailType.AllServerWithPush.ordinal() || mailType == MailType.Fight.ordinal()) {
				continue;
			}
			int groupType = -1;
			String groupIndex = "";
			if (MailFunction.isPersonalMail(mailType, appVersion)) {
				if (!fromUserSet.contains(mail.getFromuser())) {
					fromUserSet.add(mail.getFromuser());
					groupType = MailFunction.Group_LABEL.PERSONAL.getLabel();
					groupIndex = mail.getFromuser();
				}
			} else if (mailType == MailType.ModPersonal.ordinal() || mailType == MailType.ModSend.ordinal()) {
                if (!fromUserSetMod.contains(mail.getFromuser())) {
                    fromUserSetMod.add(mail.getFromuser());
                    groupType = MailFunction.Group_LABEL.MOD.getLabel();
                    groupIndex = mail.getFromuser();
                }
            } else if (mailType == MailType.Resource.ordinal()) {
				if (!haveResource) {
					haveResource = true;
					groupType = MailFunction.Group_LABEL.RESOURCE.getLabel();
					groupIndex = "";
				}
			} else if (mailType == MailType.WORLD_MONSTER.ordinal()) {
				if (!haveMonster) {
					haveMonster = true;
					groupType = MailFunction.Group_LABEL.MONSTER.getLabel();
					groupIndex = "";
				}
			} else if (mailType == MailType.TradeResource.ordinal()) {
				if (!haveTrade) {
					haveTrade = true;
					groupType = MailFunction.Group_LABEL.TRADE.getLabel();
					groupIndex = "";
				}
			} else {
				groupType = mail.getType();
				groupIndex = mail.getUid();
			}
			if (-1 != groupType) {
				MailGroup group = new MailGroup();
				group.setUid(mail.getTouser());
				group.setGrouptype(groupType);
				group.setGroupindex(groupIndex);
				group.setUpdatetime(mail.getCreatetime());
				groupList.add(group);
			}
		}
		insertBatch(groupList);
		return groupList;
	}

	public void delete() {
		SqlSession session = MyBatisSessionUtil.getInstance().getSession();
		try {
			MailGroupMapper mailGroupMapper = session.getMapper(MailGroupMapper.class);
			mailGroupMapper.deleteByPrimaryKey(this);
			session.commit();
		} finally {
			session.close();
		}
	}

	public void save() {
		SqlSession session = MyBatisSessionUtil.getInstance().getSession();
		try {
			MailGroupMapper mailGroupMapper = session.getMapper(MailGroupMapper.class);
			mailGroupMapper.insert(this);
			session.commit();
		} finally {
			session.close();
		}
	}

	public static void saveOne(String uid, int grouptype, String groupindex, long updatetime) {
		MailGroup group = new MailGroup();
		group.setUid(uid);
		group.setGrouptype(grouptype);
		group.setGroupindex(groupindex);
		group.setUpdatetime(updatetime);
		SqlSession session = MyBatisSessionUtil.getInstance().getSession();
		try {
			MailGroupMapper mailGroupMapper = session.getMapper(MailGroupMapper.class);
			mailGroupMapper.insert(group);
			session.commit();
		} finally {
			session.close();
		}
	}

	public static void replaceOne(MailGroup group) {
		SqlSession session = MyBatisSessionUtil.getInstance().getSession();
		try {
			replaceOne(group, session);
			session.commit();
		} finally {
			session.close();
		}
	}

	public static void replaceOne(MailGroup group, SqlSession session) {
		session.getMapper(MailGroupMapper.class).replace(group);
	}

	public void update() {
		SqlSession session = MyBatisSessionUtil.getInstance().getSession();
		try {
			MailGroupMapper mailGroupMapper = session.getMapper(MailGroupMapper.class);
			mailGroupMapper.updateByPrimaryKey(this);
			session.commit();
		} finally {
			session.close();
		}
	}

	public static MailGroup getOne(String uid, int grouptype, String groupindex) {
		SqlSession session = MyBatisSessionUtil.getInstance().getSession();
		try {
			MailGroupMapper mailGroupMapper = session.getMapper(MailGroupMapper.class);
			MailGroup groupRet  = mailGroupMapper.selectByPrimaryKey(uid, grouptype, groupindex);
			return groupRet;
		} finally {
			session.close();
		}
	}

	public static int getGroupTypeFromMailType(int mailType, String appVersion) {
		int mailLabel = MailFunction.getMailLabelType(mailType, appVersion);
		if (mailLabel != MailFunction.MAIL_LABEL.per_sys.ordinal() && mailLabel != MailFunction.MAIL_LABEL.mod.ordinal()) {
			return -1;
		}
		int ret;
		if (MailFunction.isPersonalMail(mailType, appVersion)) {
			ret = MailFunction.Group_LABEL.PERSONAL.getLabel();
		} else if (mailType == MailType.Resource.ordinal()) {
			ret = MailFunction.Group_LABEL.RESOURCE.getLabel();
		} else if (mailType == MailType.WORLD_MONSTER.ordinal()) {
			ret = MailFunction.Group_LABEL.MONSTER.getLabel();
		} else if (mailType == MailType.ModPersonal.ordinal() || mailType == MailType.ModSend.ordinal()) {
            ret = MailFunction.Group_LABEL.MOD.getLabel();
		} else if (mailType == MailType.TradeResource.ordinal()) {
			ret = MailFunction.Group_LABEL.TRADE.getLabel();
		} else {
			ret = mailType;
		}
		return ret;
	}

	public static void deleteOne(SqlSession session, String uid, int grouptype, String groupindex) {
		MailGroup group = new MailGroup();
		group.setUid(uid);
		group.setGrouptype(grouptype);
		group.setGroupindex(groupindex);
		boolean isClose = false;
		if (session == null) {
			session = MyBatisSessionUtil.getInstance().getSession();
			isClose = true;
		}
		try {
			MailGroupMapper mailGroupMapper = session.getMapper(MailGroupMapper.class);
			mailGroupMapper.deleteByPrimaryKey(group);
			if(isClose) {
				session.commit();
			}
		} finally {
			if (isClose) {
				session.close();
			}
		}
	}

	public static void updateOne(SqlSession session, String uid, int grouptype, String groupindex, long updatetime) {
		MailGroup group = new MailGroup();
		group.setUid(uid);
		group.setGrouptype(grouptype);
		group.setGroupindex(groupindex);
		group.setUpdatetime(updatetime);
		boolean isClose = false;
		if (session == null) {
			session = MyBatisSessionUtil.getInstance().getSession();
			isClose = true;
		}
		try {
			MailGroupMapper mailGroupMapper = session.getMapper(MailGroupMapper.class);
			mailGroupMapper.updateByPrimaryKey(group);
			if(isClose) {
				session.commit();
			}
		} finally {
			if (isClose) {
				session.close();
			}
		}
	}

	public String getGroupindex() {
		return groupindex;
	}

	public void setGroupindex(String groupindex) {
		this.groupindex = groupindex == null ? null : groupindex.trim();
	}

	public int getGrouptype() {
		return grouptype;
	}

	public void setGrouptype(int grouptype) {
		this.grouptype = grouptype;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid == null ? null : uid.trim();
	}

    public long getUpdatetime() {
        return updatetime;
    }

    public void setUpdatetime(long updatetime) {
        this.updatetime = updatetime;
    }
}