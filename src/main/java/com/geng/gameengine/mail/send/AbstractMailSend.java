package com.geng.gameengine.mail.send;

import com.geng.core.GameEngine;
import com.geng.core.GameExecutor;
import com.geng.exceptions.COKException;
import com.geng.exceptions.ExceptionMonitorType;
//import com.geng.gameengine.ChatService;
//import com.geng.gameengine.ParsePushService;
//import com.geng.gameengine.PlayerParsePushInfo;
//import com.geng.gameengine.cross.CrossKingdomFightManager;
//import com.geng.gameengine.cross.SharedUserInfo;
//import com.geng.gameengine.cross.SharedUserService;
import com.geng.gameengine.mail.*;
import com.geng.gameengine.mail.group.AbstractSameMailGroup;
//import com.geng.gameengine.manager.AppVersionManager;
//import com.geng.gameengine.world.core.UserWorld;
import com.geng.puredb.model.Mail;
import com.geng.puredb.model.MailGroup;
import com.geng.puredb.model.UserProfile;
import com.geng.utils.*;
import com.geng.utils.xml.GameConfigManager;
import com.google.common.base.Optional;
import com.geng.core.data.ISFSArray;
import com.geng.core.data.ISFSObject;
import com.geng.core.data.SFSArray;
import com.geng.core.data.SFSObject;
//import com.smartfoxserver.v2.extensions.ExtensionLogLevel;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2014/12/4.
 */
public abstract class AbstractMailSend {
    private static final Logger logger = LoggerFactory.getLogger(AbstractMailSend.class);
    public static final String PUSH_MAIL = "push.mail";
//    private static final ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("Mail_Push_%d").build();
//    private static  Executor pushExecutor = Executors.newFixedThreadPool(15, threadFactory);
    protected boolean needPushContents;

    protected  int getMailMaxNum(MailType mailType) {
        int kType = MailFunction.getMailMaxIndex(mailType.ordinal());
        Map<String, String> itemXml = new GameConfigManager("item").getItem("mail");
        return Integer.parseInt(itemXml.get("k" + (kType == 4 ? 6 : kType + 1)));
    }

    protected  List<Mail> checkExistMail(String toUser, MailType mailType) {
//        if(mailType != null){
//            int kType = MailFunction.getMailMaxIndex(mailType.ordinal());
//            return Mail.getSizeMail(toUser, kType, null);
//        }else{
//        }
        List<Mail> mailList =  Mail.getAllSizeMailWithoutOrder(toUser, null);
        MailFunction.repairMail(mailList,true); //让邮件按照创建时间升序排列
        return mailList;
    }

    protected void addPushInfo(Mail mail, ISFSObject pushObj) {
    }

    //增加最后一个字段,MailSrcFuncType
    protected Mail send(String senderUid, String targetUid, String title, MailType mailType, String rewardId,
                        long createTime, String contents, boolean itemIdFlag, boolean isSave, boolean isCheckNum,
                        boolean isPush, String senderName, boolean forceSetNoReward,MailSrcFuncType srctype){
        UserProfile targetUserProfile = GameEngine.getInstance().getPresentUserProfile(targetUid);
        boolean targetIsOffLine = false;
        if (targetUserProfile == null) {
            targetIsOffLine = true;
            targetUserProfile = UserProfile.getWithUid(targetUid);
        }
        if (targetUserProfile == null || MailType.Send != mailType && StringUtils.isNotBlank(senderUid) && MailFunction.isPersonalMail(mailType.ordinal(), targetUserProfile.getAppVersion())
        {// && ChatService.checkShield(targetUserProfile.getUid(), senderUid)) {
            return null;
        }
        UserProfile senderUserProfile = null;
        if (StringUtils.isNotBlank(senderUid)) {
            senderUserProfile = GameEngine.getInstance().getPresentUserProfile(senderUid);
            if (senderUserProfile == null) {
                senderUserProfile = UserProfile.getWithUid(senderUid);
            }
        }
        SqlSession session = null;
        if(true){//!GameEngine.getInstance().isCrossFightServer()){
           /* UserWorld targetUserWorld = targetUserProfile.getUserWorld();
            if(targetUserWorld == null){
                targetUserWorld = UserWorld.getWithUid(targetUid);
            }
            if(targetUserWorld.isUserAccessInCkfServer()){
                int crossServerId = CrossKingdomFightManager.getInstance().getCrossServerId();
                Optional<SqlSession> optional = MyBatisSessionUtil.getInstance().getBatchSession(crossServerId);
                if (!optional.isPresent()){
                    COKLoggerFactory.monitorException(String.format("can not get remote server %s db session when send mail", crossServerId), ExceptionMonitorType.CROSS_KINGDOM_FIGHT, COKLoggerFactory.ExceptionOwner.BSL);
                }
                session = optional.get();
            }*/
        }else {
            if(targetUserProfile.getCrossFightSrcServerId() == -1 && targetUserProfile.getBanTime() == Long.MAX_VALUE){
                if(StringUtils.isNotBlank(senderUid)) {
                    int srcServerId = senderUserProfile.getCrossFightSrcServerId();
                    Optional<SqlSession> optional = MyBatisSessionUtil.getInstance().getBatchSession(srcServerId);
                    if (!optional.isPresent()) {
                        COKLoggerFactory.monitorException(String.format("can not get remote server %s db session when send mail to src server", srcServerId), ExceptionMonitorType.CROSS_KINGDOM_FIGHT, COKLoggerFactory.ExceptionOwner.BSL);
                    }
                    session = optional.get();
                }
            }
        }
        if(session == null){
            session = MyBatisSessionUtil.getInstance().getBatchSession(srcServerId);
        }
        Mail mail;
        try {
            String fromName = "";
            if (senderUserProfile != null) {
                fromName = senderUserProfile.getName();
            }else if (StringUtils.isNotBlank(senderName)){
                fromName = senderName;
            }
            //CommonUtils.compareVersion(targetUserProfile.getAppVersion(), "1.0.90") == 2
//            if (mailType == MailType.GIFT_EXCHANGE && targetUserProfile != null && Versions.Compare(targetUserProfile.getAppVersion(),Versions.VERSION_1_0_90) == -1 ) {
//                contents = "My Lord, your friend  has just sent gift to you as a gift. The gift is shown below. Don't forget to appreciate the generosity of your friend!";
//            }
            if(srctype == null){
                srctype = MailSrcFuncType.MAIL_DEFAULT;
            }
            mail = mail(senderUid, targetUserProfile, title, mailType, rewardId, createTime, contents, itemIdFlag, isSave, session, isCheckNum, fromName, forceSetNoReward,srctype);
            afterMail(isSave, mail, session);
            MailGroup group = getMailGroup(mail, targetUserProfile.getAppVersion());
            saveMailGroup(true, group, session);
            session.commit();
        } catch (Exception e) {
            session.rollback();
            throw e;
        } finally {
            session.close();
        }
        if (isPush) {
            pushMail(mail, mail.getFromuser());
        }
//        if (targetIsOffLine) {
            parseNotifyUser(senderUserProfile != null ? senderUserProfile.getName() : senderName, targetUserProfile, mail);
//        }
        return mail;
    }

    protected void parseNotifyUser(String fromName, UserProfile targetUserProfile, Mail mailItem) {
        if (MailType.Personal.ordinal() == mailItem.getType() || MailType.ModPersonal.ordinal() == mailItem.getType()) {
            if (targetUserProfile == null) {
                return;
            }
            /*Map<String, String> function_on = new GameConfigManager("item").getItem("function_on2");
            ParsePushService.PushTarget exceptTarget = StringUtils.equals("1", function_on.get("k5")) ? null : ParsePushService.PushTarget.IOS;
            String functionOn = function_on.get("k3");
            if ("1".equals(functionOn)) {
                String content = LocalLangCache.getItem(targetUserProfile.getLang(), "105671");
                if (StringUtils.isBlank(content)) {
                    LoggerUtil.getInstance().logBySFS(ExtensionLogLevel.WARN, String.format("pushMsgByParse dialog id 105671 config err"));
                    return;
                }
                String pushMsg = String.format(content, fromName, mailItem.getContentsStr());
                //"1.0.83"   targetUserProfile.isOlderThanVersion(Versions.VERSION_1_0_83)
                ParsePushService.pushMessageExceptTarget(mailItem.getTouser(), pushMsg, PlayerParsePushInfo.PUSH_TYPE.MAIL,
						null, false , exceptTarget, ParsePushService.ParseStatType.PERSONAL_MAIL);
            }*/
        }
    }

    protected Mail mailWithoutCheckSum(String fromUid, String targetUid, String title, MailType mailType, String rewardId, long createTime, String contents,
                                       boolean itemIdFlag, boolean isSave, SqlSession session, boolean isCheckNum, String fromName, MailSrcFuncType mailSrcFuncType){
        MailCheckInfo checkInfo = new MailCheckInfo(null, 1, 1, 0);
        String mailUid = StringUtils.isBlank(checkInfo.getFirstMailUid()) ? GameService.getGUID(): checkInfo.getFirstMailUid();
        Mail mailItem = new Mail(mailUid, fromUid, targetUid, title, rewardId, createTime, contents, mailType, checkInfo.getAlreadySaveFlag(), itemIdFlag, fromName, 0,mailSrcFuncType);
        mailItem.groupTotal = checkInfo.getGroupTotal();
        mailItem.groupUnread = checkInfo.getGroupUnread();
        if (MailType.Send.ordinal() == mailItem.getType() || MailType.ModSend.ordinal() == mailItem.getType()) {
            mailItem.setStatus(1);
            mailItem.groupUnread--;
        }
        if (StringUtils.isBlank(checkInfo.getFirstMailUid())) {
            mailItem.setNewOne(true);
        }
        if (isSave) {
            saveMail(mailItem, session);
        }
        return mailItem;
    }

    protected Mail mail(String fromUid, UserProfile targetUserProfile, String title, MailType mailType, String rewardId, long createTime, String contents,
                        boolean itemIdFlag, boolean isSave, SqlSession session, boolean isCheckNum, String fromName, boolean forceSetNoReward, MailSrcFuncType srctype) {
        MailCheckInfo checkInfo = new MailCheckInfo(null, 1, 1, 0);

        String mailUid = StringUtils.isBlank(checkInfo.getFirstMailUid()) ? GameService.getGUID(): checkInfo.getFirstMailUid();
        Mail mailItem = new Mail(mailUid, fromUid, targetUserProfile.getUid(), title, rewardId, createTime, contents, mailType, checkInfo.getAlreadySaveFlag(), itemIdFlag, fromName, 0, forceSetNoReward,srctype);
        mailItem.groupTotal = checkInfo.getGroupTotal();
        mailItem.groupUnread = checkInfo.getGroupUnread();
        if (MailType.Send.ordinal() == mailItem.getType() || MailType.ModSend.ordinal() == mailItem.getType()) {
            mailItem.setStatus(1);
            mailItem.groupUnread--;
        }
        if (StringUtils.isBlank(checkInfo.getFirstMailUid())) {
            mailItem.setNewOne(true);
        } else {
            logger.info("{} mail max {} => to Type {}", new Object[]{targetUserProfile.getUid(), checkInfo.getFirstMailUid(), mailType});
            mailItem.setNewOne(false);
        }
        if (isSave) {
            saveMail(mailItem, session);
        }
        return mailItem;
    }

    private void saveMail(Mail mail, SqlSession session) {
        Object lock = null;
        if (mail.getType() == MailType.Resource.ordinal()) {
            UserProfile userProfile = GameEngine.getInstance().getPresentUserProfile(mail.getTouser());
            if (userProfile != null) {
                lock = userProfile.getMailLock();
            }
        }
        if (lock != null) {
            synchronized (lock) {
                if (mail.isNewOne()) {
                    mail.save(session);
                } else {
                    mail.updateAllFields(session);
                }
            }
        } else {
            if (mail.isNewOne()) {
                mail.save(session);
            } else {
                mail.updateAllFields(session);
            }
        }
    }

    protected void afterMail(boolean haveSaved, Mail mail, SqlSession session) {
        if (haveSaved) {
            return;
        }
        saveMail(mail, session);
    }

    private Map<String, String> getSenderUserInfo(Mail mail, String senderUid) {
        Map<String, String> retMap = new HashMap<>();
        if (mail.getType() != MailType.UpNotice.ordinal() && StringUtils.isNotBlank(senderUid)) {
            UserProfile userProfile = GameEngine.getInstance().getPresentUserProfile(senderUid);
            if (userProfile != null) {
                retMap.put("pic", userProfile.getPic());
                retMap.put("picVer", String.valueOf(userProfile.getPicVer()));
                retMap.put("name", userProfile.getName());
                retMap.put("abbr", userProfile.getAllianceSimpleName().or(""));
            } else {
                Map<String, String> queryInfoMap = MailServicePlus.getUserPicMap(senderUid);
                if (queryInfoMap != null) {
                    retMap = queryInfoMap;
                }
            }
        }
        return retMap;
    }

    protected void pushMail(final Mail mailItem, final String senderUid) {
        pushMail(mailItem, senderUid, Constants.SERVER_ID);
    }

    protected void pushMail(final Mail mailItem, final String senderUid, final int serverId) {
        if (mailItem == null) {
            return;
        }
		GameExecutor.getInstance().getCommonExecutor().execute(new Runnable() {
            @Override
            public void run() {
                ISFSObject pushInfo = new SFSObject();
                if (needPushContents) {
                    String contents = mailItem.getContentsStr();
                    pushInfo.putUtfString("contents", contents);
                    //战斗邮件不翻译邮件内容
                    if(mailItem.getType() != MailType.Fight.ordinal() && mailItem.getType() != MailType.WORLD_MONSTER.ordinal() && mailItem.getType() != MailType.ENCAMP.ordinal()){
                        MailFunction.translateMailByContents(mailItem, pushInfo, contents, UserProfile.getWithUid(mailItem.getTouser()));
                    }
                }
                UserProfile targetUser = GameEngine.getInstance().getPresentUserProfile(mailItem.getTouser());
                if (targetUser != null || serverId != Constants.SERVER_ID) { //本服targetUser不为null的情况下push  或者  跨服推送
                    String appVersion;
                    if(targetUser != null){
                        appVersion = targetUser.getAppVersion();
                    }else{
//                        appVersion = AppVersionManager.getItem("app_version", "1.1.5");
                    }
                    pushInfo.putUtfString("uid", mailItem.getUid());
                    pushInfo.putUtfString("title", mailItem.getTitle());
                    pushInfo.putInt("type", MailFunction.serverType2Client(mailItem.getType(), appVersion));
                    pushInfo.putLong("createTime", mailItem.getCreatetime());
                    pushInfo.putUtfString("rewardId", mailItem.getRewardStr());
                    pushInfo.putInt("itemIdFlag", mailItem.getItemidflag());
                    if (mailItem.getType() == MailType.UpNotice.ordinal()) {
                        pushInfo.putUtfString("version", mailItem.getFromuser());
                    } else {
                        pushInfo.putUtfString("fromUid", mailItem.getFromuser() == null ? "" : mailItem.getFromuser());
                    }
                    Map<String, String> senderUserInfo = senderUid == null ? null : getSenderUserInfo(mailItem, senderUid);
                    Mail.putPicInfo(pushInfo, mailItem, senderUserInfo);
                    AbstractSameMailGroup groupComparator = MailGroupGenerator.getGroupByMailType(mailItem.getType(), appVersion);
                    if (groupComparator != null) {
                        if (groupComparator.getGroupRule() == AbstractSameMailGroup.GROUP_RULE.TYPE) {
                            String arrayKey = groupComparator.getGroupString();
                            ISFSArray collectArray = SFSArray.newInstance();
                            ISFSObject chatObj = Mail.mailToChatSFSObject(mailItem, true, senderUserInfo, appVersion);
                            if (null != chatObj) {
                                collectArray.addSFSObject(chatObj);
                            } else {
                                return;
                            }
                            pushInfo.putSFSArray(arrayKey, collectArray);
                        }
                        pushInfo.putInt("unread", mailItem.groupUnread == null ? 0 : mailItem.groupUnread);
                        pushInfo.putInt("totalNum", mailItem.groupTotal == null ? 0 : mailItem.groupTotal);
                    }
                    addPushInfo(mailItem, pushInfo);
                    /*if(com.geng.gameengine.chat.ChatKeys.isMailNeedAllField()){
                        pushInfo.putInt("rewardStatus",mailItem.getRewardstatus());
                        pushInfo.putInt("status",mailItem.getStatus());
                        pushInfo.putUtfString("contentsLocal", StringUtils.replaceEach(mailItem.getContentsStr(),new String[]{"\\n"}, new String[]{"\n"}));
                        pushInfo.putInt("saveFlag",mailItem.getSaveflag());
                        pushInfo.putUtfString("toUser", mailItem.getTouser());
                        pushInfo.putInt("reply",mailItem.getReply());
                        if(!pushInfo.containsKey("fromName")){
                        pushInfo.putUtfString("fromName",mailItem.getFromname());
                        }
                        pushInfo.putUtfString("translationId", StringUtils.isBlank(mailItem.getTranslationId()) ? "" : mailItem.getTranslationId());
                        pushInfo.putUtfString("fromUser", StringUtils.isBlank(mailItem.getFromuser()) ? "" : mailItem.getFromuser());
                    }*/
                    if(mailItem.getType() == MailType.Fight.ordinal())
                        pushInfo.putSFSObject("fightContents", SFSObject.newFromJsonData(mailItem.getContentsStr()));
                    if(serverId == Constants.SERVER_ID) {
                        GameEngine.getInstance().pushMsg(PUSH_MAIL, pushInfo, targetUser);
                    }else{
                        String pushJson = pushInfo.toJson();
                        GameEngine.getInstance().pushMsgToRemoteUser(serverId, mailItem.getTouser(), PUSH_MAIL, pushJson);
                    }
                }
            }
        });
    }

    protected MailGroup getMailGroup(Mail mail, String targetAppVersion) {
        AbstractSameMailGroup groupComparator = MailGroupGenerator.getGroupByMailType(mail.getType(), targetAppVersion);
        MailGroup group = null;
        if (groupComparator != null) {
            String groupIndex = groupComparator.getGroupIndex(mail);
            int groupType = groupComparator.getGroupType(mail);
            group = new MailGroup();
            group.setUid(mail.getTouser());
            group.setGrouptype(groupType);
            group.setGroupindex(groupIndex);
            group.setUpdatetime(mail.getCreatetime());
        }
        return group;
    }

    private void saveMailGroup(boolean isSave, MailGroup group, SqlSession session) {
        if (isSave && group != null) {
            MailGroup.replaceOne(group, session);
        }
    }

    private void    getCheckInfo(MailCheckInfo checkInfo, MailType mailType, String fromUid, UserProfile targetUserProfile){
        int alreadySaveFlag = 0, groupUnread = 1, groupTotal = 1;
        String firstMailUid = null;
        String targetUid = targetUserProfile.getUid();
        String targetAppVersion = targetUserProfile.getAppVersion();
        List<Mail> existMailList =  checkExistMail(targetUid, mailType);
        int numMax = getMailMaxNum(mailType);
        AbstractSameMailGroup groupComparator = MailGroupGenerator.getGroupByMailType(mailType.ordinal(), targetAppVersion);
        String groupIndex;
        Mail firstMail = null;
        logger.error("==============================================");
        logger.error("hyq begin to check mails with size: " + existMailList.size());
        //删除超量邮件（currently: 500封）
        if (existMailList != null && existMailList.size() >= numMax) {
            List<Mail> unsavedMailList = new ArrayList<>();
            //去除已保存邮件
            for(Mail mail: existMailList){
                if(!StringUtils.isBlank(mail.getRewardStr()) && mail.getRewardstatus() == 0){
                    //邮件奖励不为空而且未领取，就不自动删。
                    continue;
                }
                if(mail.getSaveflag() == 0 || mail.getSaveflag() == 3){ //未保存或者已经假删除的都可以被真删除掉
                    unsavedMailList.add(mail);
                }
            }
            if(unsavedMailList.size() == 0){ //全都是保存过的就不管了
                unsavedMailList = existMailList;
            }

            int size = unsavedMailList.size();
            List<String> deleteUids = new ArrayList<>();
            //unsavedMailList邮件里是按时间从早到晚排序的,所以计算出要删除的数量后，从第一个开始删除。
            int delnum = existMailList.size() - numMax;
            if(delnum > size){
                delnum = size;
            }
            for(int i = 0; i < delnum; i++){
                Mail deleteMail = unsavedMailList.get(i);
                if(deleteMail.getRewardstatus() == 0){
                    try {
                        //如果被替换的这封邮件是封没领奖的邮件 自动把奖领了
                        MailFunction.receiveReward(targetUserProfile, deleteMail, null, true);
                    }catch (COKException e){
//                        COKLoggerFactory.monitorException("mail reward exceptions", ExceptionMonitorType.CMD, COKLoggerFactory.ExceptionOwner.ZC, e);
                    }
                }
                String mailUid = deleteMail.getUid();
                deleteUids.add(mailUid);
            }
            if(!deleteUids.isEmpty()){
                logger.error("hyq end to delete mails with size: " + deleteUids.size());
                logger.error("==============================================");
                Mail.batchRealDelete(deleteUids);
            }
        }
        if (groupComparator != null) {
            if (groupComparator.getGroupRule() == AbstractSameMailGroup.GROUP_RULE.FROM_USER) {
                groupIndex = fromUid;
            } else {
                groupIndex = "";
            }
            if (existMailList != null) {
                if (firstMail != null) {
                    if (groupComparator.isInGroup(groupIndex, firstMail, targetAppVersion)) {
                        groupTotal = 0;
                        if (firstMail.getStatus() == 0) {
                            groupUnread = 0;
                        }
                    }
                }
                for (Mail mail : existMailList) {
                    if (groupComparator.isInGroup(groupIndex, mail, targetAppVersion)) {
                        if (alreadySaveFlag == 0 && mail.getSaveflag() > 0) {
                            alreadySaveFlag = 1;
                        }
                        if (mail.getStatus() == 0) {
                            groupUnread++;
                        }
                        groupTotal++;
                    }
                }
            }
        }
        checkInfo.setFirstMailUid(firstMailUid);
        checkInfo.setAlreadySaveFlag(alreadySaveFlag);
        checkInfo.setGroupUnread(groupUnread);
        checkInfo.setGroupTotal(groupTotal);
    }

    protected ISFSObject mailSendReturn(Mail mail, String seeUid) {
        ISFSObject retObj = SFSObject.newInstance();
        retObj.putUtfString("uid", mail.getUid());
        retObj.putUtfString("toUid", seeUid);
//        SharedUserInfo toUserInfo = new SharedUserService().getSharedUserInfo(seeUid);
//        if (StringUtils.isNotBlank(toUserInfo.getAllianceAbbrName())) {
//            retObj.putUtfString("alliance", toUserInfo.getAllianceAbbrName());
//        }
//        if (StringUtils.isNotBlank(toUserInfo.getPic())) {
//            retObj.putUtfString("pic", toUserInfo.getPic());
//        }
//        retObj.putInt("picVer", toUserInfo.getPicVer());
        retObj.putInt("unread", mail.groupUnread == null ? 0 : mail.groupUnread);
        retObj.putInt("totalNum", mail.groupTotal == null ? 0 : mail.groupTotal);
        return retObj;
    }

    protected AbstractMailSend(boolean needPushContents) {
        this.needPushContents = needPushContents;
    }

    protected AbstractMailSend() {
        needPushContents = false;
    }

    protected class MailCheckInfo {
        private String firstMailUid;
        private int groupUnread;
        private int groupTotal;
        private int alreadySaveFlag;

        public MailCheckInfo(String firstMailUid, int groupUnread, int groupTotal, int alreadySaveFlag) {
            this.firstMailUid = firstMailUid;
            this.groupUnread = groupUnread;
            this.groupTotal = groupTotal;
            this.alreadySaveFlag = alreadySaveFlag;
        }

        public void setFirstMailUid(String firstMailUid) {
            this.firstMailUid = firstMailUid;
        }

        public void setGroupUnread(int groupUnread) {
            this.groupUnread = groupUnread;
        }

        public void setGroupTotal(int groupTotal) {
            this.groupTotal = groupTotal;
        }

        public void setAlreadySaveFlag(int alreadySaveFlag) {
            this.alreadySaveFlag = alreadySaveFlag;
        }

        public String getFirstMailUid() {
            return firstMailUid;
        }

        public int getGroupUnread() {
            return groupUnread;
        }

        public int getGroupTotal() {
            return groupTotal;
        }

        public int getAlreadySaveFlag() {
            return alreadySaveFlag;
        }
    }
}
