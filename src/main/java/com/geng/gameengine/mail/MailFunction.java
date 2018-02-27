package com.geng.gameengine.mail;

import com.geng.core.ConcurrentLock;
import com.geng.exceptions.COKException;
import com.geng.exceptions.ExceptionMonitorType;
import com.geng.exceptions.GameExceptionCode;
import com.geng.gameengine.*;
//import com.geng.gameengine.chat.room.ChatUserInfoManager;
import com.geng.gameengine.mail.group.AbstractSameMailGroup;
import com.geng.puredb.dao.MailGroupMapper;
import com.geng.puredb.dao.MailMapper;
//import com.geng.puredb.dao.OldmailinsertMapper;
import com.geng.puredb.model.*;
import com.geng.utils.*;
//import com.geng.utils.event.EventLogger;
import com.google.common.collect.ArrayListMultimap;
import com.geng.core.data.ISFSArray;
import com.geng.core.data.ISFSObject;
import com.geng.core.data.SFSArray;
import com.geng.core.data.SFSObject;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import com.geng.puredb.model.UserProfile;

/**
 * Created by Administrator on 2014/11/28.
 */
public class MailFunction {
    private static final Logger logger = LoggerFactory.getLogger(MailFunction.class);
    final private static int NumMax = 7;

    public enum MAIL_LABEL {
        per_sys, save, studio, fight , mod, boss_reward
    }

    public enum Group_LABEL {
        PERSONAL(100),
        RESOURCE(200),
        MONSTER(300),
        MOD(500),
        TRADE(400);

        private int label;

        private Group_LABEL(int label) {
            this.label = label;
        }

        public int getLabel() {
            return label;
        }
    }

    /**
     * 获取邮件分类
     */
    public static int getMailLabelType(int mailType, String appVersion) {
        int typeIndex;
        if (mailType == MailType.MAIL_FRESHER.ordinal()
                || mailType == MailType.SysNotice.ordinal() || mailType == MailType.UpNotice.ordinal() || mailType == MailType.HERO_RELEASE.ordinal()) {
            typeIndex = MAIL_LABEL.save.ordinal();
        } else if (mailType == MailType.AllServerWithPush.ordinal()) {
            typeIndex = MAIL_LABEL.studio.ordinal();
        } else if (mailType == MailType.Fight.ordinal() || mailType == MailType.Detect.ordinal() || mailType == MailType.Detect_Report.ordinal() || mailType == MailType.ENCAMP.ordinal() || mailType == MailType.MONSTER_BOSS.ordinal()) {
            typeIndex = MAIL_LABEL.fight.ordinal();
        } else if (mailType == MailType.ModSend.ordinal() || mailType == MailType.ModPersonal.ordinal()) {
            typeIndex = MAIL_LABEL.mod.ordinal();
        } else if (mailType == MailType.MONSTER_BOSS_REWARD.ordinal()){
            typeIndex = MAIL_LABEL.boss_reward.ordinal();
        } else {
            typeIndex = MAIL_LABEL.per_sys.ordinal();
        }
        return typeIndex;
    }

    public static int serverType2Client(int original, String appVersion) {
        int changedType = original;
        switch (original) {
            case 21:
                changedType = 0;
                break;
            case 22:
                changedType = 1;
                break;
            case 12:
                changedType = 14;
                break;
            case 14:
                changedType = 10;
                break;
            case 7:
                changedType = 4;
                break;
            case 10:
                changedType = 21;
                break;
        }
        return changedType;
    }

    public static int clientType2Server(int original) {
        int changedType = original;
        switch (original) {
            case 0:
                changedType = 21;
                break;
            case 1:
                changedType = 22;
                break;
            case 14:
                changedType = 12;
                break;
            case 10:
                changedType = 14;
                break;
            case 4:
                changedType = 7;
                break;
            case 21:
                changedType = 10;
                break;
        }
        return changedType;
    }

    public static boolean isPersonalMail(int type, String appVersion) {
        if (MailType.Personal.ordinal() == type || MailType.Send.ordinal() == type || MailType.Alliance_ALL.ordinal() == type) {
            return true;
        } else {
            return false;
        }
    }

    public static void loginInfo(UserProfile userProfile, ISFSObject outData) {
        ConcurrentLock.LockValue lockValue = new ConcurrentLock.LockValue(ConcurrentLock.LockType.RECEIVE_SERVER_MAIL, userProfile.getUid()); //全服邮件收取加锁
        if(ConcurrentLock.getInstance().lock(lockValue)){
            try{
                receiveServerMail(userProfile);
            }finally {
                ConcurrentLock.getInstance().unLock(lockValue);
            }
        }
        getLoginInfo(userProfile, outData);
    }



    private static void receiveServerMail (UserProfile userProfile) {

    }

    private static void getLoginInfo(UserProfile userProfile, ISFSObject outData) {
        List<MailGroup> groupList = MailGroup.getGroupList(userProfile.getUid(), null);//查询mail_group 所有
        List<Mail> mailList = Mail.getMailsWithoutOrder(userProfile.getUid()); //取邮件的时候不排序，避免数据库查询太慢 huangyuanqiang
        repairMail(mailList,false);
        Map<String, Map<String, String>> fromPicMap = MailServicePlus.getFromUserMap(mailList);
        outData.putSFSArray("mails", openMailBox(userProfile, mailList, groupList, fromPicMap));
        int labelLength = MailFunction.MAIL_LABEL.values().length;
        int mailTypeReadCount[] = new int[labelLength];
        int mailTypeTotalCount[] = new int[labelLength];
        int upNoticeReadMailCount = 0;
            ArrayListMultimap<Integer, String> groupIndexMap = ArrayListMultimap.create();
        List<AbstractSameMailGroup> comparatorList = MailGroupGenerator.getAllMailGroup();
        if (mailList != null) {
            for (Mail mailItem : mailList) {
                int typeIndex = getMailLabelType(mailItem.getType(), userProfile.getAppVersion());
                boolean haveFound = false;
                for (AbstractSameMailGroup comparator : comparatorList) {
                    if (comparator.isContainsType(mailItem.getType(), userProfile.getAppVersion())) {
                        if (comparator.recordGroupIndex(mailItem, groupIndexMap)) {
                            mailTypeTotalCount[typeIndex]++;
                        }
                        haveFound = true;
                        break;
                    }
                }
                if(!haveFound) {
                    mailTypeTotalCount[typeIndex]++;
                }
                if (mailItem.getStatus() == 0) {
                    mailTypeReadCount[typeIndex]++;
                    if (mailItem.getType() == MailType.UpNotice.ordinal()) {
                        upNoticeReadMailCount++;
                    }
                }
            }
        }
        ISFSObject mailCountObj = SFSObject.newInstance();
        mailCountObj.putInt("saveR", mailTypeReadCount[MAIL_LABEL.save.ordinal()]);
        mailCountObj.putInt("saveT", mailTypeTotalCount[MAIL_LABEL.save.ordinal()]);
        mailCountObj.putInt("sysR", mailTypeReadCount[MAIL_LABEL.per_sys.ordinal()]);
        mailCountObj.putInt("sysT", mailTypeTotalCount[MAIL_LABEL.per_sys.ordinal()]);
        mailCountObj.putInt("studioR", mailTypeReadCount[MAIL_LABEL.studio.ordinal()]);
        mailCountObj.putInt("studioT", mailTypeTotalCount[MAIL_LABEL.studio.ordinal()]);
        mailCountObj.putInt("fightR", mailTypeReadCount[MAIL_LABEL.fight.ordinal()]);
        mailCountObj.putInt("fightT", mailTypeTotalCount[MAIL_LABEL.fight.ordinal()]);
        mailCountObj.putInt("modR", mailTypeReadCount[MAIL_LABEL.mod.ordinal()]);
        mailCountObj.putInt("modT", mailTypeTotalCount[MAIL_LABEL.mod.ordinal()]);
        mailCountObj.putInt("boosRewardR", mailTypeReadCount[MAIL_LABEL.boss_reward.ordinal()]);
        mailCountObj.putInt("boosRewardT", mailTypeTotalCount[MAIL_LABEL.boss_reward.ordinal()]);
        mailCountObj.putInt("upR", upNoticeReadMailCount);
        mailCountObj.putInt("total", mailList.size());
        outData.putSFSObject("mailCount", mailCountObj);
    }

    public static void repairMail(List<Mail> mailList,boolean asc) {
        //给邮件按照创建时间排列
        if(asc) {
            Collections.sort(mailList, new Comparator<Mail>() {
                public int compare(Mail o1, Mail o2) {
                    if (o1.getCreatetime() > o2.getCreatetime()) {
                        return 1;
                    }
                    if (o1.getCreatetime() < o2.getCreatetime()) {
                        return -1;
                    }
                    //如果返回0则认为前者与后者相等
                    return 0;
                }
            });
        }else{
            Collections.sort(mailList, new Comparator<Mail>() {
                public int compare(Mail o1, Mail o2) {
                    if (o1.getCreatetime() > o2.getCreatetime()) {
                        return -1;
                    }
                    if (o1.getCreatetime() < o2.getCreatetime()) {
                        return 1;
                    }
                    //如果返回0则认为前者与后者相等
                    return 0;
                }
            });
        }
//        if (mailList == null || !"AppStore".equals(userProfile.getPf())) {
//            return;
//        }
//        Mail findMail = null;
//        for (Mail mail : mailList) {
//            if (mail.getType() == MailType.UpNotice.ordinal()) {
//                if ("1.1.5".equals(mail.getFromuser())) {
//                    mail.setFromuser("1.1.3");
//                    findMail = mail;
//                    break;
//                }
//            }
//        }
//        if (findMail != null) {
//            findMail.updateFromUser();
//        }

    }

    private static ISFSArray openMailBox(UserProfile userProfile, List<Mail> mailList, List<MailGroup> groupList, Map<String, Map<String, String>> fromPicMap) {
        ISFSArray mailSFSArray = SFSArray.newInstance();
        if (mailList == null || mailList.isEmpty()) {
            return mailSFSArray;
        }
        int mailTypeCount[] = new int[MailFunction.MAIL_LABEL.values().length];
        List<AbstractSameMailGroup> comparatorList = MailGroupGenerator.getAllMailGroup();
        for (Mail mail : mailList) {
            boolean isGroupMail = false;
            for (AbstractSameMailGroup comparator : comparatorList) {
                if(comparator.isContainsType(mail.getType(), userProfile.getAppVersion())) {
                    isGroupMail = true;
                    break;
                }
            }
            if (isGroupMail) {
                continue;
            }
            int typeIndex = getMailLabelType(mail.getType(), userProfile.getAppVersion());
            if (mailTypeCount[typeIndex] >= NumMax) {
                continue;
            }
            try{
                ISFSObject mailSFSObject;
                mailSFSObject = Mail.mailToISFSObject(mail, fromPicMap, userProfile.getAppVersion());
                mailSFSArray.addSFSObject(mailSFSObject);
                mailTypeCount[typeIndex]++;
            }catch (Exception e){
                COKLoggerFactory.monitorException("parse json exceptions", ExceptionMonitorType.LOGIN, COKLoggerFactory.ExceptionOwner.ZC, e);
            }
        }
        if (groupList == null) {
            return mailSFSArray;
        }
        int size = groupList.size();
        for (int index = 0; index < size; index++) {
            MailGroup group = groupList.get(index);
            AbstractSameMailGroup groupComparator = MailGroupGenerator.getGroupByMailGroup(group.getGrouptype(), userProfile.getAppVersion());
            if (groupComparator == null) {
                group.delete();
                continue;
            }
            int typeIndex = groupComparator.getLabel().ordinal();
            List<Mail> groupMails = getMailByGroupIndex(mailList, group.getGrouptype(), group.getGroupindex(), userProfile.getAppVersion());
            if (groupMails == null || groupMails.isEmpty()) {
                group.delete();
                continue;
            }
            if (mailTypeCount[typeIndex] >= NumMax) {
                continue;
            }
            ISFSObject oneMailObj = groupToMailSFSObject(group.getGrouptype(), groupMails, fromPicMap, userProfile.getAppVersion(),false);
            mailTypeCount[typeIndex]++;
            mailSFSArray.addSFSObject(oneMailObj);
        }
        return mailSFSArray;
    }

    private static List<Mail> getMailByGroupIndex(List<Mail> allMails, int groupType, String groupIndex, String appVersion) {
        List<Mail> mailList = new ArrayList<>();
        AbstractSameMailGroup comparator = MailGroupGenerator.getGroupByMailGroup(groupType, appVersion);
        for (Mail mail : allMails) {
            if (comparator.isInGroup(groupIndex, mail, appVersion)) {
                mailList.add(mail);
            }
        }
        return mailList;
    }

    private static ISFSObject groupToMailSFSObject(int groupType, List<Mail> groupMails, Map<String, Map<String, String>> fromPicMap, String appVersion,boolean isNeedUpdatetime) {
        Mail oneMail = groupMails.get(0);
        ISFSObject mailObj = Mail.mailToISFSObject(oneMail, fromPicMap, appVersion);
        ISFSArray itemArray = SFSArray.newInstance();
        int total = 0, unread = 0;
        AbstractSameMailGroup comparator = MailGroupGenerator.getGroupByMailGroup(groupType, appVersion);
        switch (comparator.getGroupRule()) {
            case FROM_USER:
                ListIterator<Mail> fromUserMailIterator = groupMails.listIterator();
                while (fromUserMailIterator.hasNext()) {
                    fromUserMailIterator.next();
                    total++;
                }
                int index = 0;
                while (fromUserMailIterator.hasPrevious()) {
                    index++;
                    Mail mail = fromUserMailIterator.previous();
                    if (index > total - NumMax) {
	                    ISFSObject contentsObj = Mail.mailToChatSFSObject(mail, true, fromPicMap.get(mail.getFromuser()), appVersion, isNeedUpdatetime);
	                    if (null == contentsObj) {
		                    Mail.delete(mail.getUid());
		                    continue;
	                    } else {
		                    itemArray.addSFSObject(contentsObj);
	                    }
                    }
                    if(mail.getStatus() == 0) {
                        unread++;
                    }
                }
                mailObj.putSFSArray(comparator.getGroupString(), itemArray);
                break;
            case TYPE:
                Iterator<Mail> mailIterator = groupMails.iterator();
                while (mailIterator.hasNext()) {
                    Mail mail = mailIterator.next();
                    if (itemArray.size() < NumMax) {
	                    ISFSObject contentsObj = Mail.mailToChatSFSObject(mail, true, fromPicMap.get(mail.getFromuser()), appVersion,isNeedUpdatetime);
	                    if (null == contentsObj) {
							Mail.delete(mail.getUid());
		                    continue;
	                    } else {
		                    itemArray.addSFSObject(contentsObj);
	                    }
                    }
                    if(mail.getStatus() == 0) {
                        unread++;
                    }
                    total++;
                }
                mailObj.putSFSArray(comparator.getGroupString(), itemArray);
                break;
            case UID:
                ISFSObject contentsObj = MailServicePlus.transContentsObj(oneMail.getContentsStr(), oneMail.getType(), appVersion);
	            if (null == contentsObj) {
		            oneMail.delete(oneMail.getUid());
	            } else {
		            mailObj.putSFSObject(comparator.getGroupString(), contentsObj);
	            }
                break;
        }
        mailObj.putInt("unread", unread);
        mailObj.putInt("totalNum", total);
        return  mailObj;
    }

    public static ISFSArray getLimitGroupMails(UserProfile userProfile, int begin, int offset, int type) {
        ISFSArray mailSFSArray = SFSArray.newInstance();
        List<MailGroup> groupList;
        boolean isMod = false;
        if (type == 10) {
            groupList = MailGroup.getLimitGroupListMod(userProfile.getUid(), begin, offset);
            isMod = true;
        } else {
            groupList = MailGroup.getLimitGroupList(userProfile.getUid(), begin, offset);
        }
        if (groupList == null) {
            return mailSFSArray;
        }
        Map<String, Map<String, String>> fromPicMap = getFromUserMap(groupList, userProfile.getAppVersion());
        Set<String> fromUserSet = fromPicMap.keySet();
        List<Mail> fromUserMailList = Mail.selectMailsFromUsers(userProfile.getUid(), fromUserSet, userProfile.getAppVersion(), isMod);
        List<Mail> singleGroupList = Mail.getMails(getUidsFromGroups(groupList, userProfile.getAppVersion()));
        int size = Math.min(NumMax, groupList.size());
        for (int index = 0; index < size; index++) {
            MailGroup group = groupList.get(index);
            AbstractSameMailGroup comparator = MailGroupGenerator.getGroupByMailGroup(group.getGrouptype(), userProfile.getAppVersion());
            if (comparator == null) {
                group.delete();
                continue;
            }
            List<Mail> groupMails = null;
            switch (comparator.getGroupRule()) {
                case FROM_USER:
                    groupMails = getMailByGroupIndex(fromUserMailList, group.getGrouptype(), group.getGroupindex(), userProfile.getAppVersion());
                    break;
                case UID:
                    groupMails = getMailByGroupIndex(singleGroupList, group.getGrouptype(), group.getGroupindex(), userProfile.getAppVersion());
                    break;
                case TYPE:
                    groupMails = Mail.selectTypeMailsTimeDESC(userProfile.getUid(), comparator.getFirstType());
                    break;
            }
            if (groupMails == null || groupMails.isEmpty()) {
                group.delete();
                continue;
            }
            ISFSObject oneMailObj = groupToMailSFSObject(group.getGrouptype(), groupMails, fromPicMap, userProfile.getAppVersion(),true);
			mailSFSArray.addSFSObject(oneMailObj);
        }
        return mailSFSArray;
    }

    private static List<String> getUidsFromGroups(List<MailGroup> groups, String appVersion) {
        List<String> ret = new LinkedList<>();
        if (groups == null || groups.isEmpty()) {
            return ret;
        }
        List<AbstractSameMailGroup> comparatorList = MailGroupGenerator.getGroup(AbstractSameMailGroup.GROUP_RULE.UID);
        for (MailGroup group : groups) {
            for (AbstractSameMailGroup comparator : comparatorList) {
                if (comparator.isFindGroup(group.getGrouptype(), appVersion)) {
                    ret.add(group.getGroupindex());
                    break;
                }
            }
        }
        return ret;
    }

    public static Map<String, Map<String, String>> getFromUserMap(List<MailGroup> groupList, String appVersion) {
        Set<String> fromUserSet = new HashSet<>();
        if (groupList != null) {
            List<AbstractSameMailGroup> comparatorList = MailGroupGenerator.getGroup(AbstractSameMailGroup.GROUP_RULE.FROM_USER);
            for (MailGroup group : groupList) {
                for (AbstractSameMailGroup comparator : comparatorList) {
                    if (comparator.isFindGroup(group.getGrouptype(), appVersion) && !StringUtils.isBlank(group.getGroupindex())) {
                            fromUserSet.add(group.getGroupindex());
                    }
                }
            }
        }
        Map<String, Map<String, String>> userPicMap = MailServicePlus.getUserPicMap(fromUserSet);
        return userPicMap;
    }

    /**
     * 批量获取邮件
     * @param type 2系统通知邮件 3cok-studio 4个人 5采集 6战斗 7野怪 8-联盟援助 9-Mod 10-Mod中间层 11世界boss奖励 1000其余
     */
    public static ISFSObject getLimitMails(UserProfile userProfile, int begin, int offset, int type, String fromUser) throws COKException {
        if ((4 == type && StringUtils.isBlank(fromUser)) || (9 == type && StringUtils.isBlank(fromUser))) {
            throw new COKException(GameExceptionCode.INVALID_OPT, "type 4 or 8 but not fromUser");
        }
        ISFSArray mailSFSArray;
        if (1000 == type || 10 == type) {
            mailSFSArray = getLimitGroupMails(userProfile, begin, offset, type);
        } else {
            Map<String, Object> selectParam = new HashMap();
            selectParam.put("uid", userProfile.getUid());
            selectParam.put("num", begin < 0 ? 0 : begin);
            selectParam.put("offSet", offset);
            List<Mail> mailList = Mail.selectLimitMails(userProfile.getAppVersion(), selectParam, type, fromUser);
            Map<String, Map<String, String>> fromPicMap = MailServicePlus.getFromUserMap(mailList);
            mailSFSArray = SFSArray.newInstance();
            for (Mail mailItem : mailList) {
                ISFSObject mailSFSObject;
                if (4 == type || 5 == type || 7 == type || 8 == type || 9 == type) {
                    mailSFSObject = Mail.mailToChatSFSObject(mailItem, true, fromPicMap.get(mailItem.getFromuser()), userProfile.getAppVersion());
	                if (null == mailSFSObject) {
		                mailItem.delete(mailItem.getUid());
		                continue;
	                }
                } else {
                    mailSFSObject = Mail.mailToISFSObject(mailItem, fromPicMap, userProfile.getAppVersion());
                    String contents = mailItem.getContentsStr();
                    ISFSObject contentsObj = MailServicePlus.transContentsObj(contents, mailItem.getType(), userProfile.getAppVersion());
	                if (contentsObj == null) {
		                mailItem.delete(mailItem.getUid());
		                continue;
	                }
                    mailSFSObject.putSFSObject("detail", contentsObj);
                }
                mailSFSArray.addSFSObject(mailSFSObject);
            }
        }
        ISFSObject retObj = SFSObject.newInstance();
        retObj.putSFSArray("result", mailSFSArray);
        return retObj;
    }

    /**
     * 获取邮件最大值配置的索引 用于检测数据库中邮件达到上限
     * "item" 里 "mail" 对应着 k1 k2 k3 三项最大值的配置
     */
    public static int getMailMaxIndex(int mailType) {
        int typeIndex;
        if (mailType == MailType.MAIL_FRESHER.ordinal()
                || mailType == MailType.SysNotice.ordinal() || mailType == MailType.UpNotice.ordinal()) {
            typeIndex = MAIL_LABEL.save.ordinal();
        } else if (mailType == MailType.AllServerWithPush.ordinal()) {
            typeIndex = MAIL_LABEL.studio.ordinal();
        } else if (mailType == MailType.ModPersonal.ordinal() || mailType == MailType.ModSend.ordinal()) {
            typeIndex = MAIL_LABEL.mod.ordinal();
        } else if (mailType == MailType.MONSTER_BOSS_REWARD.ordinal()){
            typeIndex = MAIL_LABEL.boss_reward.ordinal();
        } else {
            typeIndex = MAIL_LABEL.per_sys.ordinal();
        }
        return typeIndex;
    }

    /**
     * 标记已读
     */
    public static void markReadStatus (String ownerId, String uid, int type) throws COKException {
        Mail mailItem;
        mailItem = Mail.getWithUid(uid);
        if (mailItem != null && mailItem.getStatus() == 0) {
            mailItem.setStatus(1);
            mailItem.update(null);
//            ChatUserInfoManager.setMailLastUpdateTime(mailItem.getTouser(), mailItem.getUid());
            if (mailItem.getType() == MailType.GIFT.ordinal()){
//                LoggerUtil.getInstance().updateMailGiftLog(uid, 0, mailItem.getCreatetime());
            }
        }
    }

    public static void markReadStatusBatch(UserProfile userProfile, List<String> uidList){
        if(uidList.size() > 0){
//            ChatUserInfoManager.setMailLastUpdateTime(userProfile.getUid(), uidList);
            Object[] querys = new Object[uidList.size()];
            StringBuilder sb = new StringBuilder("update mail set status = 1 where uid in(");
            for(int i=0;i<uidList.size();i++){
                sb.append("?,");
                querys[i] = uidList.get(i);
            }
            sb.setLength(sb.length() - 1);
            sb.append(")");
            SFSMysql.getInstance().execute(sb.toString(), querys);
        }
    }

    /**
     * 保存邮件
     */
    public static ISFSObject saveMail(UserProfile userProfile, String uid, int type) throws COKException {
        Mail mailItem = Mail.getWithUid(uid);
        if (mailItem != null) {
            if (mailItem.getSaveflag() > 0) {
                return null;
            }
            mailItem.setSaveflag(1);
            mailItem.update(null);
//            ChatUserInfoManager.setMailLastUpdateTime(mailItem.getTouser(), mailItem.getUid());
            ISFSObject retObj = SFSObject.newInstance();
            retObj.putUtfString("save", "success");
            return retObj;
        }
        return null;
    }

    /**
     * 取消保存
     */
    public static ISFSObject cancelSave(String uid, int type) throws COKException {
        Mail mailItem = Mail.getWithUid(uid);
        if (mailItem == null || mailItem.getSaveflag() == 0) {
            return null;
        }
        ISFSObject retObj = SFSObject.newInstance();
        mailItem.setSaveflag(0);
        mailItem.update(null);
//        ChatUserInfoManager.setMailLastUpdateTime(mailItem.getTouser(), mailItem.getUid());
        retObj.putUtfString("cancel", "success");
        return retObj;
    }

    public static ISFSObject saveMailBatch(UserProfile userProfile, List<String> uidList, int flag){
//        if(uidList.size() > 0){
//            ChatUserInfoManager.setMailLastUpdateTime(userProfile.getUid(), uidList);
//        }else{
//            return null;
//        }
        Object[] querys = new Object[1+uidList.size()];
        querys[0] = flag;
        StringBuilder sb = new StringBuilder("update mail set saveFlag = ? where uid in(");
        for(int i=0;i<uidList.size();i++){
            sb.append("?,");
            querys[i+1] = uidList.get(i);
        }
        sb.setLength(sb.length() - 1);
        sb.append(")");
        SFSMysql.getInstance().execute(sb.toString(), querys);
        ISFSObject retObj = SFSObject.newInstance();
        retObj.putUtfString("save", "success");
        return retObj;
    }

    /**
     * 根据主键查看
     */
    public static ISFSObject getContents(UserProfile userProfile, String uid, String type) throws COKException {
        String[] uidArray = StringUtils.split(uid, '|');
//        if (uidArray == null || uidArray.length < 1) {
//            throw new COKException(GameExceptionCode.PARAM_ILLEGAL, "no mail uids"); //TODO:参数有问题，没传uid
//        }
        List<String> ordinalMailUidList = Arrays.asList(uidArray);
        ISFSArray retArray = SFSArray.newInstance();
        if (!ordinalMailUidList.isEmpty()) {
            List<Mail> mails = Mail.getMails(ordinalMailUidList);
            for (Mail mailItem : mails) {
                ISFSObject mailObj = MailServicePlus.transContentsObj(mailItem.getContentsStr(), mailItem.getType(), userProfile.getAppVersion());
	            if (mailObj == null) {
		            mailItem.delete(mailItem.getUid());
		            continue;
	            }
                mailObj.putLong("createTime", mailItem.getCreatetime());
                mailObj.putUtfString("u", mailItem.getUid());
                retArray.addSFSObject(mailObj);
            }
        }
        ISFSObject retObj = SFSObject.newInstance();
        retObj.putSFSArray("result", retArray);
        return retObj;
    }

    /**
    * 根据邮件对象领取
    * */
    public static ISFSObject receiveReward(UserProfile userProfile, Mail mailItem, SqlSession session, boolean isAuto) throws COKException{
        ISFSObject retObject = null;
        /*if (mailItem == null) {
            return null;
        } else if (StringUtils.isEmpty(mailItem.getRewardStr())) {
            throw new COKException(GameExceptionCode.PARAM_ILLEGAL, "rewardId in mail is null"); //TODO: 少传参数
        } else if (mailItem.getRewardstatus() == 1) {
            throw new COKException(GameExceptionCode.REWARD_CLAIMED, "mail reward cann't receive again"); //TODO: 奖励已经领取过了
        } else if (!mailItem.getTouser().equals(userProfile.getUid())){
            throw new COKException(GameExceptionCode.PARAM_ILLEGAL, "mail doesn't belong to this user"); //TODO: 参数不合法
        } else {
            if (mailItem.getType() == MailType.UpNotice.ordinal()) {
                //CommonUtils.compareVersion(userProfile.getAppVersion(), mailItem.getFromuser()) == 2
                if (Versions.Compare(userProfile.getAppVersion(), mailItem.getFromuser()) <0 ) {
                    throw new COKException(GameExceptionCode.MAIL_UP_VERSION_FOR_REWARD, "need higher app version");
                }
            }
            if (mailItem.getMbLevel() > 0 && userProfile.getUbManager().getMainBuildingLevel() < mailItem.getMbLevel()) {
                ISFSObject errObj = SFSObject.newInstance();
                errObj.putInt("mbLevel", mailItem.getMbLevel());
                throw new COKException(GameExceptionCode.MAIL_MBLEVEL, errObj, "need higher mbLevel");
            }

            boolean isRewardSafeResource = SwitchConstant.SafeResourceGetSwitch.isSwitchOpen();//安全资源开关
            retObject = RewardManager.sendActivityReward(userProfile, mailItem.getCreatetime(), mailItem.getRewardStr(), isRewardSafeResource, LoggerUtil.GoldCostType.PUSH_AWARD_ADD,mailItem.getSrctype());
            mailItem.setStatus(1);
            mailItem.setRewardstatus(1);
            mailItem.setSaveflag(3); //奖励邮件领取以后自动删除（假删） huangyuanqiang
            logger.error("hyq set mail to be deleted 1");
            mailItem.update(session);
            if (mailItem.getType() == MailType.GIFT.ordinal()){ //邮件送礼
                LoggerUtil.getInstance().updateMailGiftLog(mailItem.getUid(), 1, mailItem.getCreatetime());
            }else if (mailItem.getType() == MailType.GIFT_EXCHANGE.ordinal()){ //礼包赠送
                LoggerUtil.getInstance().updateExchangeGiftLog(mailItem.getUid(), mailItem.getCreatetime(), isAuto ? 2 : 1);
            }else if (mailItem.getType() == MailType.GIFT_ALLIANCE.ordinal()){ //联盟礼包
                LoggerUtil.getInstance().recordMailRewardLog(userProfile.getUid(), mailItem.getUid(), mailItem.getType(), isAuto ? 2 : 1);
            }else if (StringUtils.equals(mailItem.getTitle(), "114144")) { //累充
                LoggerUtil.getInstance().recordMailRewardLog(userProfile.getUid(), mailItem.getUid(), mailItem.getType(), isAuto ? 2 : 1);
            }
            List<Mail> recordMails = new ArrayList<Mail>();
            recordMails.add(mailItem);
            recordMailReceiveReward(recordMails);
            ChatUserInfoManager.setMailLastUpdateTime(userProfile.getUid(),mailItem.getUid());
        }*/
        return retObject;
    }
    /**
     * 根据主键领取
     */
    public static ISFSObject receiveReward(UserProfile userProfile, String uid) throws COKException {
        Mail mailItem = Mail.getWithUid(uid);
        return receiveReward(userProfile, mailItem, null, false);
    }



    /**
    *  一键领取奖励  uidList邮件uid
    * */
    public static ISFSObject rewardBatch(UserProfile userProfile, List<String> uidList, List<String> labelList){
        // group中不会有奖励
        ISFSObject retObj = SFSObject.newInstance();
        String ownerId = userProfile.getUid();
        List<Mail> allChosenMails = new ArrayList<>();
        if(uidList.size() > 0){
            allChosenMails.addAll(Mail.getMails(uidList));
//            ChatUserInfoManager.setMailLastUpdateTime(userProfile.getUid(),uidList);
        }
        SqlSession session = MyBatisSessionUtil.getInstance().getBatchSession();
        try{
            for(String label: labelList){
                List<Mail> typeMails = Mail.getSizeMail(ownerId, Integer.parseInt(label), session);
                if(typeMails != null)
                    allChosenMails.addAll(typeMails);
            }

            if(allChosenMails.size() > 0){
                Map<String, Integer> rewardMap = new HashMap<>();
                List<Mail> recordMails = new ArrayList<Mail>();
                int count = 0;
                for(Mail mail: allChosenMails){
                    if(mail.getRewardstatus() == 0){
//                        if (mail.getMbLevel() > 0 && userProfile.getUbManager().getMainBuildingLevel() < mail.getMbLevel()) {
//                            continue;
//                        }
                        addUpToRewardObj(rewardMap, mail.getRewardStr());
                        mail.setStatus(1);
                        mail.setRewardstatus(1);
                        mail.setSaveflag(3); //奖励邮件领取以后自动删除（假删） huangyuanqiang
                        logger.error("hyq set mail to be deleted 2");
                        mail.update(null);
                        count++;
                        recordMails.add(mail);
                    }
                }
                logger.error("hyq set mails to be deleted with size: "+count);
                recordMailReceiveReward(recordMails);
                String rewardStr = resolveRewardObj(rewardMap);
                if(StringUtils.isNotBlank(rewardStr)) {
                    boolean isRewardSafeResource = SwitchConstant.SafeResourceGetSwitch.isSwitchOpen();//1为安全资源,2为非安全资源
//                    retObj = RewardManager.sendActivityReward(userProfile, allChosenMails.get(0).getCreatetime(), rewardStr, isRewardSafeResource, LoggerUtil.GoldCostType.PUSH_AWARD_ADD,MailSrcFuncType.MAIL_BATCH.getValue());
                }
            }
			session.commit();
		}finally {
            session.close();
        }
        return retObj;
    }

    /**
    * 邮件奖励拼在一起
    * */
    private static void addUpToRewardObj(Map<String, Integer> reward, String rewardItem){
        if(StringUtils.isBlank(rewardItem)) return;
        String[] items = StringUtils.split(rewardItem, "|");
        for(String item: items){
            String[] rArr = StringUtils.split(item, ",");
            String category = rArr[0]; // wood,food,goods etc..
            String itemId = rArr[1]; // wood|food|etc.itemid = 0,  goods.itemId = 200310...
            String key = category + "," + itemId;
            int num = Integer.parseInt(rArr[2]);
            if(reward.containsKey(key)){
                reward.put(key, num + reward.get(key));
            }else{
                reward.put(key, num);
            }
        }
    }

    /**
    * 解析邮件奖励对象，组成字符串，与addUptoRewardObj配合
    * */
    private static String resolveRewardObj(Map<String, Integer> rewardObj){
        StringBuilder retSb = new StringBuilder();
        Iterator<Map.Entry<String, Integer>> iterator = rewardObj.entrySet().iterator();
        while(iterator.hasNext()){
            Map.Entry<String, Integer> entry = iterator.next();
            String key = entry.getKey();
            String num = String.valueOf(entry.getValue());
            retSb.append(key).append(",").append(num).append("|"); // goods,200020,3|wood,0,200
        }
        if(retSb.length() > 0){
            retSb.deleteCharAt(retSb.length() - 1);
        }
        return retSb.toString();
    }

    /**
     * 一键删除邮件
     */
    public static ISFSObject deleteMailsBatch(UserProfile userProfile, List<String> uidList, List<String> labelList) {
        String ownerUid = userProfile.getUid();
        List<Mail> mailList = Mail.getMails(uidList);
        if (uidList.size() > 0) {
            Mail.batchDeleteByRewardStatus(uidList);
//            ChatUserInfoManager.setMailLastUpdateTime(userProfile.getUid(),uidList);
        }
        List<AbstractSameMailGroup> comparatorList = MailGroupGenerator.getAllMailGroup();
        SqlSession session = MyBatisSessionUtil.getInstance().getBatchSession();
        ISFSObject retObject = new SFSObject();
        try{
            if(mailList != null && mailList.size() > 0){
                Set<String> delUser = new HashSet<>();
                Set<String> types = new HashSet<>();
                for (Mail mail : mailList) {
                    if(mail.getRewardstatus() != 0){
                        for  (AbstractSameMailGroup comparator : comparatorList) {
                            if (comparator.isContainsType(mail.getType(), userProfile.getAppVersion())){
                                if (comparator.getGroupRule() == AbstractSameMailGroup.GROUP_RULE.UID) {
                                    MailGroup.deleteOne(session, mail.getTouser(), mail.getType(), mail.getUid());
                                } else {
                                    //删除这个group中的所有邮件
                                    comparator.deleteMailInGroup(session, ownerUid, comparator.getGroupIndex(mail));
                                    MailGroup.deleteOne(session, mail.getTouser(), mail.getType(), mail.getUid());
                                }
                                break;
                            }
                        }
                        if(mail.getType() == MailType.InviteMovePoint.ordinal()){
                            invalidateInviteMail(mail,session);
                        }
                        if(mail.getType() == MailType.ModSend.ordinal() || mail.getType() == MailType.ModSendMedia.ordinal() || mail.getType() == MailType.ModPersonal.ordinal() || mail.getType() == MailType.ModPersonalMedia.ordinal()){
                            delUser.add(mail.getFromuser());
                            types.add("2");
                        }else if(mail.getType() == MailType.Send.ordinal() || mail.getType() == MailType.SendMedia.ordinal() || mail.getType() == MailType.Personal.ordinal() || mail.getType() == MailType.PersonalMedia.ordinal() || mail.getType() == MailType.Alliance_ALL.ordinal()){
                            delUser.add(mail.getFromuser());
                            types.add("1");
                            types.add("20");
                        }
                    }
                }
//                if(delUser.size() > 0){
//                    com.geng.gameengine.chat.ChatServerProxy.getInstance().delMsg(userProfile.getUid(), delUser.toArray(new String[delUser.size()]), types.toArray(new String[types.size()]));
//                }
            }
			session.commit();
		}finally {
            session.close();
        }
        ISFSObject remainObj = deleteMailByLabel(userProfile, labelList, null);
        retObject.putSFSObject("RetObj", remainObj);
        return retObject;
    }


    /**
    * 根据邮件文件夹类型来删
    * */
    public static ISFSObject deleteMailByLabel(UserProfile userProfile, List<String> labelList, SqlSession session){
        ISFSObject remainMailObj = new SFSObject();
        String ownerUid = userProfile.getUid();
        List<String> allMailUids = new ArrayList<>();
        for(String label: labelList){
            List<Mail> typeMails = Mail.getSizeMail(ownerUid, Integer.parseInt(label), session);
            int remainUnReadCount = 0;
            int remainCount = 0;
            for(Mail mail: typeMails){
                if(mail.getRewardstatus() == 0){
                    remainCount++;
                    if(mail.getStatus() == 0){
                        remainUnReadCount++;
                    }
                }
                allMailUids.add(mail.getUid());
            }
            remainMailObj.putInt(label, remainUnReadCount);
            remainMailObj.putInt("total" + label, remainCount);
        }
        if(allMailUids.size() > 0)
            Mail.batchDeleteByRewardStatus(allMailUids);
        return remainMailObj;
    }

    /**
     * 删除邮件
     */
    public static ISFSObject deleteMails(UserProfile userProfile, String[] uidsArray, int num, boolean oldVersion) {
        String ownerUid = userProfile.getUid();
        int nums = 0;
        List<String> ordinaryUids = new LinkedList<>();
        for (int i = 0; i < uidsArray.length; i++) {
            ordinaryUids.add(uidsArray[i]);
        }
//        ChatUserInfoManager.setMailLastUpdateTime(userProfile.getUid(),ordinaryUids);
        List<Mail> mailList = Mail.getMails(ordinaryUids);
        if (ordinaryUids.size() > 0) {
            nums += Mail.batchDelete(ordinaryUids);
        }
        ArrayListMultimap<Integer, String> groupIndexMap = ArrayListMultimap.create();
        List<AbstractSameMailGroup> comparatorList = MailGroupGenerator.getAllMailGroup();
        for (Mail mail : mailList) {
	        if (mail.getRewardstatus() == 0 || mail.getSaveflag() == 1) {
		        continue;
	        }
            if(mail.getType() == MailType.InviteMovePoint.ordinal()){
                invalidateInviteMail(mail,null);
            }
            for  (AbstractSameMailGroup comparator : comparatorList) {
                if (comparator.isContainsType(mail.getType(), userProfile.getAppVersion())){
                    if (comparator.getGroupRule() == AbstractSameMailGroup.GROUP_RULE.UID) {
                        MailGroup.deleteOne(null, mail.getTouser(), mail.getType(), mail.getUid());
                    } else {
                        comparator.recordGroupIndex(mail, groupIndexMap);
                    }
                    break;
                }
            }
        }
        SqlSession session = MyBatisSessionUtil.getInstance().getBatchSession();
        try {
            for (int rule : groupIndexMap.keySet()){
                AbstractSameMailGroup comparator = MailGroupGenerator.getGroup(comparatorList, rule);
                for (String groupIndex : groupIndexMap.get(rule)) {
                    if (comparator.getGroupRule() == AbstractSameMailGroup.GROUP_RULE.TYPE) {
                        groupIndex = "";
                    }
                    List<Mail> groupMails = comparator.selectLimitMail(session, ownerUid, groupIndex, 0, 1);
                    if (groupMails == null || groupMails.isEmpty()) {
                        MailGroup.deleteOne(session, ownerUid, comparator.getGroupType(), groupIndex);
                    } else {
                        MailGroup.updateOne(session, ownerUid, comparator.getGroupType(), groupIndex, groupMails.get(0).getCreatetime());
                    }
                }
            }
			session.commit();
		} finally {
            session.close();
        }
        ISFSObject retObject = new SFSObject();
        retObject.putInt("delete", nums);
        return retObject;
    }

    public static void deleteGroup(UserProfile userProfile, ISFSObject params, boolean isMod) {
        String uid = userProfile.getUid();
        int groupType = -1;
        String groupIndex = "";
        if (params.containsKey("fromUser")) {
            String fromUser = params.getUtfString("fromUser");
            if (isMod) {
                Mail.deleteAllFromUserMod(fromUser, uid);
                groupType = MailFunction.Group_LABEL.MOD.getLabel();
            } else {
                Mail.deleteAllFromUser(fromUser, uid);
                groupType = MailFunction.Group_LABEL.PERSONAL.getLabel();
            }
            groupIndex = fromUser;
        } else if (params.containsKey("type")) {
            int type = MailFunction.clientType2Server(params.getInt("type"));
            Mail.deleteByType(uid, type);
            List<AbstractSameMailGroup> groupComparators = MailGroupGenerator.getGroup(AbstractSameMailGroup.GROUP_RULE.TYPE);
            for (AbstractSameMailGroup group : groupComparators) {
                if (group.isContainsType(type, userProfile.getAppVersion())) {
                    groupType = group.getGroupType();
                    break;
                }
            }
            groupIndex = "";
        }
        if (-1 != groupType) {
            MailGroup.deleteOne(null, uid, groupType, groupIndex);
        }
    }

    /**
     * 跨服，邮件迁移
     * @param session
     */
    public static void insert(SqlSession session, UserProfile userProfile) {
        String uid = userProfile.getUid();
        String deviceID = userProfile.getDeviceId();
        List<Mail> mailList = Mail.getMails(uid);
        MailMapper mailMapper = session.getMapper(MailMapper.class);
        if(!mailList.isEmpty()) {
            mailMapper.insertBatch(mailList);
        }
        List<MailGroup> groupList = MailGroup.getGroupList(uid, null);
        if (groupList != null && !groupList.isEmpty()) {
            session.getMapper(MailGroupMapper.class).batchInsert(groupList);
        }/*
        List<ServerMailRecord> mailUidRecords = ServerMailRecord.getServerMailRecords(uid, null);
        if (mailUidRecords != null && !mailUidRecords.isEmpty()) {
            ServerMailRecord.insertBatch(mailUidRecords, session);
        }
        List<String> deviceIdRecord = DeviceMailRecordKey.getServerMailRecords(deviceID, null);
        if (deviceIdRecord != null && ! deviceIdRecord.isEmpty()) {
            List<DeviceMailRecordKey> deviceRecords = new ArrayList<>();
            for (String serverPushUid : deviceIdRecord) {
                DeviceMailRecordKey newRecord = new DeviceMailRecordKey(deviceID, serverPushUid);
                deviceRecords.add(newRecord);
            }
            DeviceMailRecordKey.replaceBatch(deviceRecords, session);
        }
        List<OldmailinsertKey> oldmailinsertKeys = OldmailinsertKey.selectByUid(uid, null);
        if (oldmailinsertKeys != null && !oldmailinsertKeys.isEmpty()) {
            session.getMapper(OldmailinsertMapper.class).insertBatch(oldmailinsertKeys);
        }*/
    }

    /**
    * 根据内容翻译邮件
    * */
    public static void translateMailByContents(Mail mailItem, ISFSObject outData, String contents, UserProfile targetUser){
        ISFSObject translateObj = null;
        if(targetUser == null) return;
        try{
            translateObj = null;//TranslationService.getInstance().translateByContent(contents, CommonUtils.formatLang(targetUser.getLang()), false);
        }catch (Exception e){
            translateObj = null;
        }
        if(translateObj != null && translateObj.containsKey("originalLang") && translateObj.containsKey("translationMsg")){
            outData.putUtfString("originalLang", translateObj.getUtfString("originalLang"));
            outData.putUtfString("translationMsg", translateObj.getUtfString("translationMsg"));
            if(translateObj.containsKey("uuid")){
                String translationId = translateObj.getUtfString("originalLang") + "|" + translateObj.getUtfString("uuid");
                mailItem.setTranslationId(translationId);
                mailItem.update(null);
            }
        }
    }
    /**
     * 翻译邮件的translationId取翻译结果
     * */
    public static void translateMailByTranslationId(ISFSObject mailObj, String contents, String lang){
        ISFSObject translateObj = null;
        if(lang == null) return;
        try{
            if(!mailObj.containsKey("translationId")){
                return;
            }
            String translationId = mailObj.getUtfString("translationId");
            String[] idArr = StringUtils.split(translationId, "|");
            String fromLang = idArr[0];
            String uuid = idArr[1];
            translateObj = null;//TranslationService.getInstance().translateByUuid(uuid, contents, fromLang, CommonUtils.formatLang(lang), false);
        }catch (Exception e){
            translateObj = null;
        }
        if(translateObj != null && translateObj.containsKey("originalLang") && translateObj.containsKey("translationMsg")){
            mailObj.putUtfString("originalLang", translateObj.getUtfString("originalLang"));
            mailObj.putUtfString("translationMsg", translateObj.getUtfString("translationMsg"));
        }
    }

    /**
     * 根据uid批量翻译
     * */
    public static ISFSObject batchTranslate(List<String> mailUid, UserProfile userProfile){
        if(mailUid == null || mailUid.size() == 0) return null;
        ISFSObject retObj = SFSObject.newInstance();
        ISFSArray mailContentsArr = SFSArray.newInstance();
        List<Mail> mails = Mail.getMails(mailUid);
        for(Mail mail: mails){
            ISFSObject mailObj = SFSObject.newInstance();
            mailObj.putUtfString("uid", mail.getUid());
            if(StringUtils.isNotBlank(mail.getTranslationId())){
                mailObj.putUtfString("translationId", mail.getTranslationId());
            }
            if(!MailType.get(mail.getType()).contentsIsObj()){
                String contents = mail.getContentsStr();
                contents =  StringUtils.replaceEach(contents, new String[]{"\\n"}, new String[]{"\n"});
                mailObj.putUtfString("contents", contents);
                translateMailByTranslationId(mailObj, contents, userProfile.getLang());
            }
            mailContentsArr.addSFSObject(mailObj);
        }
        retObj.putSFSArray("result", mailContentsArr);
        return retObj;
    }

    /**
     * 联盟迁城邀请邮件 使其接受和邀请两个按钮失效
    * */
    public static void invalidateInviteMail(String mailId, boolean isAccept) {
        try{
            Mail inviteMail = Mail.getWithUid(mailId);
            ISFSObject contents = SFSObject.newFromJsonData(CommonUtils.fromByteToString(inviteMail.getContents()));
            String inviterUid = contents.getUtfString("inviterUid");
            String inviteeUid = contents.getUtfString("inviteeUid");
            contents.putInt("deal",1);
            inviteMail.setContents(CommonUtils.fromStringToByte(contents.toJson()));
            inviteMail.update(null);
//            AllianceInviteMove.delete(inviterUid, inviteeUid);
            //统计
//            if(isAccept)
//                LoggerUtil.getInstance().recordAllianceRelatedStats(LoggerUtil.AllianceRecordType.INVITE_RESPONSE, inviterUid, inviteeUid, null, null);
        }catch(Exception e){
            COKLoggerFactory.zhengchengLogger.error("invalidate alliance invite move error", e);
        }
    }

    /**
     * 删除邮件的时候，联盟邀请迁城也删除
    * */
    public static void invalidateInviteMail(Mail inviteMail,SqlSession session) {
        try{
            ISFSObject contents = SFSObject.newFromJsonData(CommonUtils.fromByteToString(inviteMail.getContents()));
            String inviterUid = contents.getUtfString("inviterUid");
            String inviteeUid = contents.getUtfString("inviteeUid");
            contents.putInt("deal", 1);
            inviteMail.setContents(CommonUtils.fromStringToByte(contents.toJson()));
            inviteMail.update(session);
//            AllianceInviteMove.delete(inviterUid, inviteeUid);
        }catch(Exception e){
            COKLoggerFactory.zhengchengLogger.error("invalidate alliance invite move error", e);
        }
    }

    /**
     * 根据seqId取邮件,仅限个人邮件
     * */
    public static ISFSObject getMailBySeqId(String toUser, String fromUserName, long start, long end){
        ISFSObject retObj = new SFSObject();
        UserService.UserServerHelper userServerHelper = UserService.selectGameUidAndServerGlobal(fromUserName);
        String fromUid = userServerHelper.getUid();
        if (userServerHelper.isEmpty()) {
            return retObj;
        }
        ISFSArray mailSFSArray = SFSMysql.getInstance().query("select * from mail where toUser = ? and fromUser = ? and saveFlag != 3 and createTime > ? and createTime <= ?",
                new Object[]{toUser, fromUid, start ,end});
        if(mailSFSArray != null && mailSFSArray.size() > 0){
            for(int i = 0 ; i < mailSFSArray.size(); i++){
                ISFSObject mailObj = mailSFSArray.getSFSObject(i);
                String contents = CommonUtils.fromByteToString(mailObj.getByteArray("contents"));
                contents = contents == null ? "" : contents.trim();
                mailObj.putUtfString("contents", StringUtils.replaceEach(contents, new String[]{"\\n"}, new String[]{"\n"}));
                mailObj.putInt("type", MailFunction.serverType2Client(mailObj.getInt("type"), null));
            }
        }
        retObj.putSFSArray("msg", mailSFSArray);
        return retObj;
    }

    /**
     * 根据前台传过来的seqId，并返回服务器最新seqId和最早seqId，以及最新的<=20封邮件
     * */
    public static ISFSObject getLatestMailsBySeqId(String toUser, String fromUserName, long seqId){
        ISFSObject retObj = new SFSObject();
        UserService.UserServerHelper userServerHelper = UserService.selectGameUidAndServerGlobal(fromUserName);
        String fromUid = userServerHelper.getUid();
        if (userServerHelper.isEmpty()) {
            return retObj;
        }
        ISFSArray mailSFSArray = SFSMysql.getInstance().query("select * from mail where toUser = ? and fromUser = ? and saveFlag != 3",
                new Object[]{toUser, fromUid});
        if(mailSFSArray != null && mailSFSArray.size() > 0){
            long maxSeqId = Long.MIN_VALUE;
            long minSeqId = Long.MAX_VALUE;
            for(int i = 0 ; i < mailSFSArray.size(); i++){
                ISFSObject mailObj = mailSFSArray.getSFSObject(i);
                long createTime = mailObj.getLong("createTime");
                if(createTime > maxSeqId){
                    maxSeqId = createTime;
                }else if(createTime < minSeqId){
                    minSeqId = createTime;
                }
                if(createTime <= seqId){
                    continue;
                }
                String contents = CommonUtils.fromByteToString(mailObj.getByteArray("contents"));
                contents = contents == null ? "" : contents.trim();
                mailObj.putUtfString("contents", StringUtils.replaceEach(contents, new String[]{"\\n"}, new String[]{"\n"}));
                mailObj.putInt("type", MailFunction.serverType2Client(mailObj.getInt("type"), null));
            }
            retObj.putLong("maxSeqId", maxSeqId);
            retObj.putLong("minSeqId", minSeqId);
            retObj.putSFSArray("msg", mailSFSArray);
        }
        return retObj;
    }

    public static ISFSObject getUpdateMailByLastUpdateTime(UserProfile userProfile, long lastUpdatetime){
        ISFSObject retObj = new SFSObject();
        ISFSArray updateMails = new SFSArray();
        ISFSArray delMails = new SFSArray();
        Map<String,String> mailUids = null;// ChatUserInfoManager.getMailIdsByLastUpdateTime(userProfile.getUid(), lastUpdatetime);
        if(mailUids != null && mailUids.size() > 0){
            List<Mail> mails = Mail.getMails(new ArrayList<String>(mailUids.keySet()));
            for(Mail mail : mails){
                if(!mail.getTouser().equals(userProfile.getUid())){
                    //不是本人的邮件，删掉
//                    ChatUserInfoManager.removeMailLastUpdateTime(userProfile.getUid(),mail.getUid());
                    continue;
                }
                ISFSObject updateM = new SFSObject();
                updateM.putUtfString("uid",mail.getUid());
                updateM.putInt("status", mail.getStatus());
                updateM.putInt("rewardStatus",mail.getRewardstatus());
                updateM.putInt("saveFlag",mail.getSaveflag());
                long time = 0;
                try {
                    time = Long.parseLong(mailUids.get(mail.getUid()));
                }catch (Exception e){

                }
                updateM.putLong("mailLastUpdateTime", time);
                updateMails.addSFSObject(updateM);
                mailUids.remove(mail.getUid());
            }
            //剩下的邮件id说明数据库里已经没有了，属于中途有过修改然后又删除的邮件。
            for(String mid : mailUids.keySet()){
                delMails.addUtfString(mid);
            }
        }
        retObj.putSFSArray("update",updateMails);
        retObj.putSFSArray("delete",delMails);
        return retObj;
    }

    public static ISFSObject getNewsMailFromMailUid(UserProfile userProfile, String uid){
        return getNewsMailFromMailUid(userProfile, uid, 200, 0);
    }

    public static ISFSObject getNewsMailFromMailUid(UserProfile userProfile, String uid, int limitCount, long createTime){
        String toUser = userProfile.getUid();
        ISFSObject retObj = new SFSObject();
        ISFSArray mailSFSArray = null;

        if(StringUtils.isBlank(uid) || uid.equals("0")){
            mailSFSArray = SFSMysql.getInstance().query("select * from mail where toUser = ? and saveFlag != 3 order by createTime desc limit " + limitCount, new Object[]{toUser});
        }else{
            Mail mailItem = Mail.getWithUid(uid);
            if (mailItem != null) {
                long lasttime = mailItem.getCreatetime();
                mailSFSArray = SFSMysql.getInstance().query("select * from mail where toUser = ? and saveFlag != 3 and createTime >= ? and uid != ? order by createTime desc",
                    new Object[]{toUser, lasttime, uid});
            }else{
                mailSFSArray = SFSMysql.getInstance().query("select * from mail where toUser = ? and saveFlag != 3 and createTime >= ? order by createTime desc",
                        new Object[]{toUser, createTime});
            }
        }
        Set<String> allFromUser = new HashSet<String>();
        if(mailSFSArray != null && mailSFSArray.size() > 0){
            ISFSArray sysMails = new SFSArray();
            ISFSArray personMails = new SFSArray();
            Map<String,ISFSObject> userMails = new HashMap<String,ISFSObject>();
            for(int i = 0 ; i < mailSFSArray.size(); i++){
                ISFSObject mailObj = mailSFSArray.getSFSObject(i);
                String contents = CommonUtils.fromByteToString(mailObj.getByteArray("contents"));
                contents = contents == null ? "" : contents.trim();
                mailObj.putUtfString("contents", StringUtils.replaceEach(contents, new String[]{"\\n"}, new String[]{"\n"}));
                String rewardids = CommonUtils.fromByteToString(mailObj.getByteArray("rewardId"));
                rewardids = rewardids == null ? "" : rewardids.trim();
                mailObj.removeElement("rewardId");
                mailObj.putUtfString("rewardId",rewardids);
                mailObj.putUtfString("save",""+mailObj.getInt("saveFlag"));
                if(mailObj.containsKey("fromUser") && !StringUtils.isBlank(mailObj.getUtfString("fromUser"))){
                    if(mailObj.getInt("type") == MailType.TradeResource.ordinal()){
                        allFromUser.add(mailObj.getUtfString("fromUser"));
                    }
                }
                if(mailObj.getInt("type") == MailType.InviteMovePoint.ordinal()){
                    ISFSObject contentsObj = SFSObject.newFromJsonData(contents);
                    String inviterUid = contentsObj.getUtfString("inviterUid");
                    allFromUser.add(inviterUid);
                    mailObj.putUtfString("fromUser",inviterUid);
                }
                if(isPersonalMail(mailObj.getInt("type"),"") || mailObj.getInt("type") == MailType.ModSend.ordinal() || mailObj.getInt("type") == MailType.ModPersonal.ordinal()){
                    addToPersonMail(mailObj,userMails,userProfile);
                }else{
                    //CommonUtils.compareVersion(userProfile.getAppVersion(),"1.1.8") == 2
//                    if(Versions.Compare(userProfile.getAppVersion(),Versions.VERSION_1_1_8) == -1 ){
//                        //1.1.8之前版本的，如果出现超大数据，就直接跳过这个邮件。
//                        if(isContentTooLarge(mailObj.getUtfString("contents"))){
//                            continue;
//                        }
//                    }else{
                        changeContentsToArray(mailObj,"contents","contentsArr");
//                    }
                    mailObj.putInt("type", MailFunction.serverType2Client(mailObj.getInt("type"), null));
                    sysMails.addSFSObject(mailObj);
                }
            }
            if(!allFromUser.isEmpty()){
                Map<String, Map<String, String>> fromPicMap = MailServicePlus.getUserPicMap(allFromUser);
                for (int i=0;i < sysMails.size();i++){
                    ISFSObject mailObj = sysMails.getSFSObject(i);
                    if(mailObj.containsKey("fromUser") && !StringUtils.isBlank(mailObj.getUtfString("fromUser"))  && fromPicMap.containsKey(mailObj.getUtfString("fromUser"))){
                        Map<String, String> userPicMap = fromPicMap.get(mailObj.getUtfString("fromUser"));
                        if(userPicMap.containsKey("name") && StringUtils.isBlank(mailObj.getUtfString("fromName"))){
                            String fromName = userPicMap.get("name");
                            mailObj.putUtfString("fromName",fromName);
                        }
                        if(userPicMap.containsKey("pic")){
                            mailObj.putUtfString("pic",userPicMap.get("pic"));
                        }
                        if(userPicMap.containsKey("picVer")){
                            mailObj.putUtfString("picVer",userPicMap.get("picVer"));
                        }else{
                            mailObj.putUtfString("picVer","0");
                        }
                    }
                }
            }
            retObj.putSFSArray("msg", mailSFSArray);
            retObj.putSFSArray("sysmsg", sysMails);
            for (String k : userMails.keySet()){
                personMails.addSFSObject(userMails.get(k));
            }
            retObj.putSFSArray("usermsg", personMails);
        }
        return retObj;
    }

    public static ISFSObject addToPersonMail(ISFSObject mail,Map<String,ISFSObject> userMails,UserProfile userProfile){
        String fromUser = mail.getUtfString("fromUser");
        if(userMails.containsKey(fromUser)){
            ISFSObject usermain = userMails.get(fromUser);
            ISFSObject chatObj = getMailContentObj(mail,userProfile);
            usermain.getSFSArray("chats").addSFSObject(chatObj);
            if(usermain.getLong("createTime") < mail.getLong("createTime")){
                usermain.putUtfString("uid",mail.getUtfString("uid"));
                usermain.putLong("createTime",mail.getLong("createTime"));
            }
            userMails.put(fromUser,usermain);
        }else{
            ISFSObject chatObj = getMailContentObj(mail, userProfile);
            ISFSArray chats = new SFSArray();
            chats.addSFSObject(chatObj);
            mail.putSFSArray("chats",chats);
            mail.putInt("type", MailFunction.serverType2Client(mail.getInt("type"), null));
            userMails.put(fromUser,mail);
        }
        return mail;
    }
    private static ISFSObject getMailContentObj(ISFSObject mail,UserProfile userProfile){
        String content = mail.getUtfString("contents");
        int type = mail.getInt("type");
        String appversion = userProfile.getAppVersion();
        String translationId = mail.getUtfString("translationId");
        ISFSObject contentObj = MailServicePlus.transContentsObj(content,type,appversion,true,userProfile.getLang(),translationId);
        contentObj.putUtfString("uid",mail.getUtfString("uid"));
        contentObj.putInt("type",MailFunction.serverType2Client(type,appversion));
        contentObj.putLong("createTime",mail.getLong("createTime"));
        contentObj.putInt("status",mail.getInt("status"));
        if(!StringUtils.isBlank(mail.getUtfString("fromUser"))){
//            contentObj.putUtfString("lastUpdateTime",UserProfile.getLastUpdateInfoTime(mail.getUtfString("fromUser")).or("0"));
        }else{
            contentObj.putUtfString("lastUpdateTime","0");
        }
        return contentObj;
    }

    public static boolean isContentTooLarge(String content){
        return content.length() >= Short.MAX_VALUE;
    }

    public static void changeContentsToArray(ISFSObject mailObj,String fromKey,String toKey){
        int length = 5000;//先按5000来分割
        if(mailObj.containsKey(fromKey)){
            ISFSArray contentArr = new SFSArray();
            String contents = mailObj.getUtfString(fromKey);
            for(int i = 0;i < contents.length();i += length){
                if(i + length >= contents.length()){
                    contentArr.addUtfString(contents.substring(i));
                }else{
                    contentArr.addUtfString(contents.substring(i,i+length));
                }
            }
            mailObj.putUtfString(fromKey,"");
            mailObj.putSFSArray(toKey,contentArr);
        }
    }

    public static ISFSObject selectMailByCategory(UserProfile userProfile,int category,String fromUser,int begin, int offset){
        int[] types = getMailTypeByCategory(category);
        if(types == null || types.length == 0){
            return null;
        }
        Object[] querys = new Object[3+types.length];
        String toUser = userProfile.getUid();
        querys[0] = toUser;
        StringBuilder sb = new StringBuilder(
                "select * from mail where toUser = ? and saveFlag != 3 and (type in ("
        );
        for(int i=0;i<types.length;i++){
            sb.append("?,");
            querys[i+1] = types[i];
        }
        sb.setLength(sb.length() - 1);
        sb.append(")");
        if(category == 5){
            sb.append(" or title=115429)");
        }else{
            sb.append(")");
        }
        sb.append(" order by createTime DESC limit ?, ?");
        querys[querys.length - 2] = begin;
        querys[querys.length - 1] = offset;

        ISFSArray mailSFSArray = SFSMysql.getInstance().query(sb.toString(),querys);
        ISFSArray mailresult = SFSArray.newInstance();
        if(mailSFSArray != null && mailSFSArray.size() > 0){
            List<Mail> mailList = new ArrayList<Mail>();
            for(int i = 0 ; i < mailSFSArray.size(); i++){
                ISFSObject mailObj = mailSFSArray.getSFSObject(i);
                Mail mail = new Mail();
                mail.setUid(mailObj.getUtfString("uid"));
                mail.setTouser(mailObj.getUtfString("toUser"));
                mail.setFromuser(mailObj.getUtfString("fromUser"));
                mail.setFromname(mailObj.getUtfString("fromName"));
                mail.setTitle(mailObj.getUtfString("title"));
                if(category == 1 && "115429".equalsIgnoreCase(mailObj.getUtfString("title"))){
                    //取系统邮件但发现title是联盟指令时，这条邮件就不返回
                    continue;
                }
                mail.setStatus(mailObj.getInt("status"));
                mail.setType(mailObj.getInt("type"));
                mail.setRewardid(mailObj.getByteArray("rewardId"));
                mail.setRewardstatus(mailObj.getInt("rewardStatus"));
                mail.setCreatetime(mailObj.getLong("createTime"));
                mail.setContents(mailObj.getByteArray("contents"));
                mail.setItemidflag(mailObj.getInt("itemIdFlag"));
                mail.setSaveflag(mailObj.getInt("saveFlag"));
                mail.setReply(mailObj.getInt("reply"));
                mail.setTranslationId(mailObj.getUtfString("translationId"));
                if(mailObj.containsKey("mbLevel")){
                mail.setMbLevel(mailObj.getInt("mbLevel"));
                }
                mailList.add(mail);
            }
            Map<String, Map<String, String>> fromPicMap = MailServicePlus.getFromUserMap(mailList);
            for (Mail mailItem : mailList) {
                ISFSObject mailSFSObject;
                if (4 == category || 7 == category ) {
                    mailSFSObject = Mail.mailToChatSFSObject(mailItem, true, fromPicMap.get(mailItem.getFromuser()), userProfile.getAppVersion());
                    Mail.putPicInfo(mailSFSObject,mailItem,fromPicMap.get(mailItem.getFromuser()));
                    if(!StringUtils.isBlank(mailItem.getFromuser())){
                        mailSFSObject.putUtfString("fromUser",mailItem.getFromuser());
                    }
                    if (null == mailSFSObject) {
                        mailItem.delete(mailItem.getUid());
                        continue;
                    }
                } else {
                    mailSFSObject = Mail.mailToISFSObject(mailItem, fromPicMap, userProfile.getAppVersion());
                    String contents = mailItem.getContentsStr();
                    ISFSObject contentsObj = MailServicePlus.transContentsObj(contents, mailItem.getType(), userProfile.getAppVersion());
                    if (contentsObj == null) {
                        mailItem.delete(mailItem.getUid());
                        continue;
                    }
                    mailSFSObject.putSFSObject("detail", contentsObj);
                }
                mailresult.addSFSObject(mailSFSObject);
            }
        }
        ISFSObject retObj = SFSObject.newInstance();
        retObj.putSFSArray("result", mailresult);
        return retObj;
    }

    public static ISFSObject getMailStatusAll(UserProfile userProfile){
        String toUser = userProfile.getUid();
        ISFSArray mailSFSArray = SFSMysql.getInstance().query("select type,status,title from mail where toUser = ? and saveFlag != 3",new Object[]{toUser});
        Map<Integer,Integer> typeCategory = getTypeCategory();
        ISFSObject rest = new SFSObject();
        //第一层是7种类别，第二层是两种状态，0未读，1已读
        int[][] statusAll = new int[7][2];

        if(mailSFSArray != null && mailSFSArray.size() > 0){
            for(int i = 0; i < mailSFSArray.size(); i++){
                ISFSObject mailstatus = mailSFSArray.getSFSObject(i);
                int type = mailstatus.getInt("type");
                String title = mailstatus.getUtfString("title");
                if("115429".equalsIgnoreCase(title)){
                    //联盟指令虽然是系统类别，但要算到联盟类别里
                    int status = mailstatus.getInt("status");
                    statusAll[4][status]++;
                    continue;
                }
                if(typeCategory.containsKey(type)){
                    int category = typeCategory.get(type);
                    int status = mailstatus.getInt("status");
                    statusAll[category-1][status]++;
                }
            }
        }
        for (int i = 0;i < statusAll.length;i++){
            String category = ""+(i+1);
            int[] categoryStatus = statusAll[i];
            int unread = categoryStatus[0];
            int read = categoryStatus[1];
            ISFSObject obj = new SFSObject();
            obj.putInt("total",unread+read);
            obj.putInt("unread",unread);
            rest.putSFSObject(category,obj);
        }
        return rest;
    }

    public static ISFSObject getMailstatus(UserProfile userProfile,int category){
        int[] types = getMailTypeByCategory(category);
        if(types == null || types.length == 0){
            return null;
        }
        Object[] querys = new Object[1+types.length];
        String toUser = userProfile.getUid();
        querys[0] = toUser;
        StringBuilder sb = new StringBuilder(
                "select status,count(status) as count from mail where toUser = ? and saveFlag != 3 and type in ("
        );
        for(int i=0;i<types.length;i++){
            sb.append("?,");
            querys[i+1] = types[i];
        }
        sb.setLength(sb.length() - 1);
        sb.append(") group by status");
        ISFSArray mailSFSArray = SFSMysql.getInstance().query(sb.toString(),querys);
        ISFSObject rest = new SFSObject();
        long unread = 0;
        long read = 0;
        if(mailSFSArray != null && mailSFSArray.size() > 0){
            for(int i = 0; i < mailSFSArray.size(); i++){
                ISFSObject mailstatus = mailSFSArray.getSFSObject(i);
                int status = mailstatus.getInt("status");
                if(status == 0){
                    unread = mailstatus.getLong("count");
                }
                if(status == 1){
                    read = mailstatus.getLong("count");
                }
            }
        }
        rest.putLong("total", unread + read);
        rest.putLong("unread", unread);
        return rest;
    }

    public static Map<Integer,int[]> getCategoryMap(){
        int[] system = new int[]{
                MailType.System.ordinal(),
                MailType.MAIL_FRESHER.ordinal(),
                MailType.Cure_Soldier.ordinal(),
                MailType.GIFT.ordinal(),
                MailType.GIFT_EXCHANGE.ordinal(),
                MailType.GIFT_ALLIANCE.ordinal(),
        };
        int[] announcement = new int[]{
                MailType.AllServerWithPush.ordinal(),
                MailType.SysNotice.ordinal(),
                MailType.UpNotice.ordinal(),
        };
        int[] event = new int[]{
                MailType.MONSTER_BOSS_REWARD.ordinal(),
        };
        int[] messages = new int[]{
                MailType.Send.ordinal(),
                MailType.Personal.ordinal(),
                MailType.Alliance_ALL.ordinal(),
        };
        int[] alliance = new int[]{
                MailType.AllianceInvite.ordinal(),
                MailType.AllianceApply.ordinal(),
                MailType.InviteMovePoint.ordinal(),
                MailType.KickAllianceUser.ordinal(),
                MailType.RefuseAllianceApply.ordinal(),
                MailType.TradeResource.ordinal(),
        };
        int[] battle = new int[]{
                MailType.Detect.ordinal(),
                MailType.Fight.ordinal(),
                MailType.Detect_Report.ordinal(),
                MailType.WORLD_MONSTER.ordinal(),
                MailType.MONSTER_BOSS.ordinal(),
                MailType.ENCAMP.ordinal(),
                MailType.WORLD_NEW_EXPLORE.ordinal(),
                MailType.Resource.ordinal(),
        };
        int[] mod = new int[]{
                MailType.ModSend.ordinal(),
                MailType.ModPersonal.ordinal(),
        };
        Map<Integer,int[]> categorys = new HashMap<Integer, int[]>();
        categorys.put(1,system);
        categorys.put(2,announcement);
        categorys.put(3,event);
        categorys.put(4,messages);
        categorys.put(5,alliance);
        categorys.put(6,battle);
        categorys.put(7,mod);
        return categorys;
    }

    public static Map<Integer,Integer> getTypeCategory(){
        Map<Integer,int[]> categorys = getCategoryMap();
        Map<Integer,Integer> types = new HashMap<Integer, Integer>();
        for (int category : categorys.keySet()){
            int[] category_types = categorys.get(category);
            for(int type : category_types){
                types.put(type,category);
            }
        }
        return types;
    }

    public static int[] getMailTypeByCategory(int category){
        Map<Integer,int[]> categorys = getCategoryMap();
        if(categorys.containsKey(category)){
            return categorys.get(category);
        }
        return null;
    }

    private static void recordMailReceiveReward(List<Mail> mails){
        if(mails.isEmpty()){
            return;
        }
        //RedisSession rs = new RedisSession(false);
        try{
            for (Mail mailItem : mails) {
                String user = mailItem.getTouser();
                String mailid = mailItem.getUid();
                long time = System.currentTimeMillis();
                String reward = mailItem.getRewardStr();
                ISFSObject obj = new SFSObject();
                obj.putUtfString("uid", mailid);
                obj.putUtfString("reward", reward);
                obj.putUtfString("title",mailItem.getTitle());
                obj.putLong("time",time);
                obj.putUtfString("user",user);
                obj.putInt("type",mailItem.getType());
                obj.putInt("srcType",mailItem.getSrctype());
//                EventLogger.getInstance().info(EventLogger.EventEnum.MAIL_REWARD,obj.toJson());
                //String recordKey = "MAIL_REWARD_RECEIVE_RECORD:" + user;
               // rs.zAdd(recordKey, obj.toJson(), time);
            }
        }finally {
           // rs.close();
        }
    }

  }