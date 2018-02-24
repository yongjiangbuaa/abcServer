package com.geng.gameengine.mail.send;

import com.geng.core.GameEngine;
import com.geng.exceptions.ExceptionMonitorType;
import com.geng.gameengine.mail.MailFunction;
import com.geng.gameengine.mail.MailSrcFuncType;
import com.geng.gameengine.mail.MailType;
import com.geng.puredb.model.Mail;
import com.geng.puredb.model.MailGroup;
import com.geng.puredb.model.UserProfile;
import com.geng.utils.*;
import com.geng.utils.xml.GameConfigManager;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;

import java.util.*;

/**
 * Created by Administrator on 2014/12/7.
 */
public class BatchMailSend extends AbstractMailSend {
    private String fromName = null;
	private int donate = 0;

	public boolean sendMail(String senderUid, List<String> targetUidList, String title, MailType mailType, String rewardId, long createTime, String contents, boolean itemIdFlag,MailSrcFuncType srcFuncType) {
		return sendMail(senderUid, targetUidList, title, mailType, rewardId, createTime, contents, itemIdFlag, Constants.SERVER_ID,srcFuncType);
	}

	public boolean sendMail(String senderUid, List<String> targetUidList, String title, MailType mailType, String rewardId, long createTime, String contents, boolean itemIdFlag, int serverId,MailSrcFuncType srcFuncType) {
		return send(senderUid, targetUidList, title, mailType, rewardId, createTime, contents, itemIdFlag, true, true, serverId, null, false,srcFuncType);
	}

	public boolean sendMailByXml(List<String> targetUidList, String mailXmlId, String rewardId, MailType mailType, List<String> contentsParam, String orderInfo,MailSrcFuncType srcFuncType) {
		return sendMailByXml(targetUidList, mailXmlId, rewardId, mailType, contentsParam, Constants.SERVER_ID, orderInfo,srcFuncType);
	}

	public boolean sendMailByXml(List<String> targetUidList, String mailXmlId, String rewardId, MailType mailType, List<String> contentsParam, int serverId, String orderInfo,MailSrcFuncType srcFuncType) {
		return sendMailByXml(targetUidList, mailXmlId, rewardId, mailType, contentsParam, serverId, orderInfo, false,srcFuncType);
	}

    public boolean sendMailByXml(List<String> targetUidList, String mailXmlId, String rewardId, MailType mailType, List<String> contentsParam, int serverId, String orderInfo, boolean forceSetNoReward, MailSrcFuncType srctype) {
        Map<String, String> mailXml = new GameConfigManager("mail").getItem(mailXmlId);
        if (mailXml == null || mailXml.isEmpty()) {
            return false;
        }
        String sender = mailXml.get("sender");
        String title = mailXml.get("title");
        String contents = mailXml.get("message");
		if (contentsParam != null && contentsParam.size() > 0) {
			String param = StringUtils.join(contentsParam, '|');
			contents = contents + '|' + param;
		}
        String reward;
        if (StringUtils.isBlank(rewardId)) {
            reward = mailXml.get("reward");
        } else {
            reward = rewardId;
        }
        if (mailType == null) {
			String typeInXml = mailXml.get("type");
			//尝试从mail.xml配置文件中读取type属性来设定mailType
			if (StringUtils.isBlank(typeInXml)) {
				mailType = MailType.SysNotice;
			} else {
				int mailTypeIndex = Integer.parseInt(typeInXml);
				//设定的值在MailType的范围内
				if (mailTypeIndex >= 0 && mailTypeIndex < MailType.values().length) {
					mailType = MailType.get(mailTypeIndex);
				} else {
					mailType = MailType.System;
				}
			}
        }
		if(mailXml.containsKey("donate")){
			donate = Integer.parseInt(mailXml.get("donate"));
		}
        this.fromName = sender;
        return send(null, targetUidList, title, mailType, reward, System.currentTimeMillis(), contents, true, true, true, serverId, orderInfo, forceSetNoReward,srctype);
    }

    //增加MailSrcFuncType
	protected boolean send(String senderUid, List<String> targetUidList, String title, MailType mailType, String rewardId, long createTime, String contents, boolean itemIdFlag, boolean isCheckNum, boolean isPush, int serverId, String orderInfo, boolean forceSetNoReward,MailSrcFuncType srctype) {
		if (targetUidList == null || targetUidList.isEmpty()) {
			return false;
		}
		if (mailType == MailType.Alliance_ALL) {
			needPushContents = true;
		}
		UserProfile senderUserProfile = null;
		int srcServerId = -1;
		if (StringUtils.isNotBlank(senderUid)) {
			senderUserProfile = GameEngine.getInstance().getPresentUserProfile(senderUid);
			if (senderUserProfile == null) {
				senderUserProfile = UserProfile.getWithUid(senderUid);
			}
			srcServerId = senderUserProfile.getCrossFightSrcServerId();
		}
		List<UserProfile> cacheUserProfileList = new LinkedList<>();
		Iterator<String> uidIterator = targetUidList.iterator();
		List<String> needQueryUid = new LinkedList<>();
		while (uidIterator.hasNext()) {
			String uid = uidIterator.next();
			UserProfile targetUserProfile = GameEngine.getInstance().getPresentUserProfile(uid);
			if (targetUserProfile != null) {
				cacheUserProfileList.add(targetUserProfile);
			} else {
				needQueryUid.add(uid);
			}
		}
		List<UserProfile> queryUserProfileList = null;// UserProfileDao.selectByUidList(needQueryUid);
		if (queryUserProfileList != null && !queryUserProfileList.isEmpty()) {
			cacheUserProfileList.addAll(queryUserProfileList);
		}
		List<Mail> insertMailList = new LinkedList<>();
		List<Mail> updateMaiList = new LinkedList<>();
		List<MailGroup> groupList = new LinkedList<>();
		MailType orignType = mailType;
		String orignContents = contents;
		for (UserProfile targetUserProfile : cacheUserProfileList) {
			if(srcServerId == -1){
				srcServerId = targetUserProfile.getCrossFightSrcServerId();
			}
			if (StringUtils.isNotBlank(senderUid) && MailFunction.isPersonalMail(mailType.ordinal(), targetUserProfile.getAppVersion())){// && ChatService.checkShield(targetUserProfile.getUid(), senderUid)) {
				continue;
			}
			if (orignType == MailType.GIFT_ALLIANCE) {
				//"1.1.10"
//				if (targetUserProfile.isOlderThanVersion(Versions.VERSION_1_1_10)) {
//					mailType = MailType.System;
//					contents = StringUtils.split(orignContents, '|')[0];
//				} else {
					mailType = orignType;
					contents = orignContents;
//				}
			}
			String fromName = "";
			if (senderUserProfile != null) {
				fromName = senderUserProfile.getName();
			}
			Mail mail = mail(senderUid, targetUserProfile, title, mailType, rewardId, createTime, contents, itemIdFlag, false, null, isCheckNum, fromName, forceSetNoReward,srctype);
			afterMail(true, mail, null);
			if (StringUtils.isNotBlank(this.fromName)){
                mail.setFromname(this.fromName);
            }
            MailGroup group = getMailGroup(mail, targetUserProfile.getAppVersion());
			if (mail.isNewOne()) {
				insertMailList.add(mail);
			} else {
				updateMaiList.add(mail);
			}
			if (group != null) {
				groupList.add(group);
			}
		}
		SqlSession session;
		if(serverId == Constants.SERVER_ID) {
			session = MyBatisSessionUtil.getInstance().getBatchSession();
		}else {
			 session = MyBatisSessionUtil.getInstance().getBatchSession();
			if (null == session){
				COKLoggerFactory.monitorException(String.format("can not get remote server %s db session", serverId), ExceptionMonitorType.CROSS_KINGDOM_FIGHT, COKLoggerFactory.ExceptionOwner.BSL);
			}
		}
		try {
			Mail.insertBatch(insertMailList, session);
			Collections.sort(updateMaiList);
			for (Mail mail : updateMaiList) {
				mail.updateAllFields(session);
			}
			MailGroup.replaceBatch(groupList, session);
			session.commit();
//			LoggerUtil.getInstance().updateExchangeAllianceLog(orderInfo, 1);
		} catch (Exception e) {
			session.rollback();
//			LoggerUtil.getInstance().updateExchangeAllianceLog(orderInfo, 2);
			throw e;
		} finally {
			session.close();
		}
		if (isPush) {
			for (Mail mail : insertMailList) {
				pushMail(mail, senderUid, serverId);
			}
			for (Mail mail : updateMaiList) {
				pushMail(mail, senderUid, serverId);
			}
		}
		/*
		*  跨服战活动相关逻辑
		*/

		return true;
	}

	@Override
	protected void afterMail(boolean isSaved, Mail mail, SqlSession session){
		mail.setReply(mail.getReply() + (donate << 3));
		super.afterMail(isSaved, mail, session);
	}
}
