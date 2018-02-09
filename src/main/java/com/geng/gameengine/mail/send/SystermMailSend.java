package com.geng.gameengine.mail.send;

import com.geng.gameengine.mail.MailSrcFuncType;
import com.geng.gameengine.mail.MailType;
import com.geng.gameengine.reward.RewardManager;
//import com.geng.gameengine.world.normal.AbstractWorldFightHandler;
import com.geng.puredb.model.Mail;
import com.geng.utils.CommonUtils;
import com.geng.utils.LoggerUtil;
import com.geng.utils.xml.GameConfigManager;
import com.geng.core.data.ISFSObject;
import com.geng.core.data.SFSObject;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2014/12/5.
 */
public class SystermMailSend extends AbstractMailSend{
    private String fromUid = null;

    //xmlMail
    private String additionalAwardItemId = null;
    private int awardNum = 0;
    private String fromName = null;
    private int share = 0;
    private int donate = 0;

    public SystermMailSend(){
        super();
    }

    public SystermMailSend(boolean isPushContent){
        super(isPushContent);
    }

    @Override
    protected void addPushInfo(Mail mail, ISFSObject pushObj) {
        if (mail.getType() == MailType.Fight.ordinal()) {
            addFightMailInfo(mail, pushObj);
        }
    }

    @Override
    protected  int getMailMaxNum(MailType mailType) {
        if (MailType.Resource == mailType || MailType.WORLD_MONSTER == mailType) {
            return 10;
        } else {
            return super.getMailMaxNum(mailType);
        }
    }

    @Override
    protected List<Mail> checkExistMail(String toUser, MailType mailType) {
        if (MailType.Resource == mailType || MailType.WORLD_MONSTER == mailType) {
            List<Mail> existMailList = Mail.selectTypeMailsTimeASC(toUser, mailType.ordinal());
            int maxNum = getMailMaxNum(mailType);
            if (existMailList != null && existMailList.size() > maxNum) {
                int needDeleteNum = existMailList.size() - maxNum;
                List<String> deleteUidList = new LinkedList<>();
                Iterator<Mail> mailIterator = existMailList.iterator();
                for (int deleteCount = 1; mailIterator.hasNext(); deleteCount++) {
                    if (deleteCount > needDeleteNum) {
                        break;
                    }
                    Mail mail = mailIterator.next();
                    deleteUidList.add(mail.getUid());
                    mailIterator.remove();
                }
                Mail.batchDeleteByRewardStatus(deleteUidList);
            }
            return existMailList;
        } else {
            return super.checkExistMail(toUser, mailType);
        }
    }

    public static void addFightMailInfo(Mail mail, ISFSObject mailObj) {
       /* ISFSObject contentsObj = SFSObject.newFromJsonData(mail.getContentsStr());
        ISFSObject preViewObj = SFSObject.newInstance();
        if(preViewObj.containsKey("warPoint")) {
            preViewObj.putUtfString("warPoint", contentsObj.getUtfString("warPoint"));
        }
        preViewObj.putUtfString("atkName", contentsObj.getSFSObject("atkUser").getUtfString("name"));
        ISFSObject defUser = contentsObj.getSFSObject("defUser");
        if (defUser.containsKey("npcId")) {
            preViewObj.putUtfString("npcId", defUser.getUtfString("npcId"));
        } else {
            preViewObj.putUtfString("defName", defUser.getUtfString("name"));
        }
        String winnerUid = contentsObj.getUtfString("winner");
        String selfUid = mail.getTouser();
        if (StringUtils.isBlank(winnerUid)) {
            preViewObj.putInt("win", AbstractWorldFightHandler.FightResult.DRAW.ordinal());
        } else if (selfUid.equals(winnerUid)) {
            preViewObj.putInt("win", AbstractWorldFightHandler.FightResult.WIN.ordinal());
        } else {
            preViewObj.putInt("win", AbstractWorldFightHandler.FightResult.FAIL.ordinal());
        }
        mailObj.putSFSObject("contents", preViewObj);*/
    }

    @Override
    protected void afterMail(boolean haveSaved, Mail mail, SqlSession session) {
        if (StringUtils.isNotBlank(fromUid)) {
            mail.setFromuser(fromUid);
        }
        if (StringUtils.isNotBlank(additionalAwardItemId)) {
            String rewardIdStr = CommonUtils.fromByteToString(mail.getRewardid());
            String newRewardIdStr = RewardManager.appendGoodsStr(rewardIdStr, additionalAwardItemId, awardNum);
            mail.setRewardid(CommonUtils.fromStringToByte(newRewardIdStr));
        }
        if (StringUtils.isNotBlank(fromName)) {
            mail.setFromname(fromName);
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
         * 存入的时候如果share = 0, share << 1变成0b00再加reply
         * 存入的时候如果share = 1, share << 1办成0b10再加reply
         * 保证位正确
         * */
        mail.setReply(mail.getReply() + (share << 1) + (donate << 3));
        super.afterMail(haveSaved, mail, session);
    }

    public void sendMail(String toUser, MailType mailType, String title, String contents, String rewardId, long createTime, boolean isDialog,MailSrcFuncType mailSrcFuncType) {
        send(null, toUser, title, mailType, rewardId, createTime, contents, isDialog, true, true, true, null, false,mailSrcFuncType);
    }

    public void sendMail(String fromUid, String toUser, MailType mailType, String title, String contents, String rewardId, long createTime, boolean isDialog, MailSrcFuncType srcFuncType) {
        this.fromUid = fromUid;
        send(null, toUser, title, mailType, rewardId, createTime, contents, isDialog, false, true, true, null, false,srcFuncType);
    }

    public void sendMailWithSender(String toUser, MailType mailType, String title, String contents, String rewardId, long createTime, boolean isDialog,String sendName,MailSrcFuncType mailSrcFuncType) {
        this.fromUid = fromUid;
        send(null, toUser, title, mailType, rewardId, createTime, contents, isDialog, true, true, true, sendName, false,mailSrcFuncType);
    }


    public void sendMailByXmlWithSenderName(String toUser, String mailXmlId, String rewardId, MailType mailType,
                                            String additionalAwardItemId, int awardNum, List<String> contentsParam,
                                            String delimeter, String senderName, boolean forceSetNoReward, String customTitle,MailSrcFuncType mailSrcFuncType){
        sendMailByXmlWithSenderName(toUser, mailXmlId, rewardId, mailType, additionalAwardItemId, awardNum, contentsParam, delimeter, senderName, forceSetNoReward, customTitle, System.currentTimeMillis(), true,mailSrcFuncType);
    }

    public void sendMailByXmlWithSenderName(String toUser, String mailXmlId, String rewardId, MailType mailType,
                                            String additionalAwardItemId, int awardNum, List<String> contentsParam,
                                            String delimeter, String senderName, boolean forceSetNoReward, String customTitle,
                                            long createTime, boolean isCheckNum,MailSrcFuncType mailSrcFuncType) {
        Map<String, String> mailXml = new GameConfigManager("mail").getItem(mailXmlId);
        if (mailXml == null || mailXml.isEmpty()) {
            return ;
        }
        String sender;
        if(null == senderName){
            sender = mailXml.get("sender");
        }else{
            sender = senderName;
        }
        String title;
        if (StringUtils.isBlank(customTitle)) {
            title = mailXml.get("title");
        } else {
            title = customTitle;
        }
        String contents = mailXml.get("message");
        if (contentsParam != null && contentsParam.size() > 0) {
            if(StringUtils.isBlank(delimeter)){
                delimeter = "|";
            }
            String param = StringUtils.join(contentsParam, delimeter);
            contents = contents + delimeter + param;
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
        this.additionalAwardItemId = additionalAwardItemId;
        this.awardNum = awardNum;
        this.fromName = sender;
        if(mailXml.containsKey("share")){
            this.share = Integer.parseInt(mailXml.get("share"));
        }
        boolean itemIdFlag = (mailType == MailType.GIFT_EXCHANGE) ? false : true;
        Mail mailItem = send(null, toUser, title, mailType, reward, createTime, contents, itemIdFlag, false, isCheckNum, true, null, forceSetNoReward,mailSrcFuncType);
        if (mailItem != null && mailType == MailType.GIFT_EXCHANGE && contentsParam.size() > 5){
//            LoggerUtil.getInstance().recordExchangeGiftLog(mailItem.getUid(), contentsParam.get(4), toUser, contentsParam.get(5), mailItem.getCreatetime());
        }
        if(mailItem != null) {
            LoggerUtil.getInstance().logBySFS("mail send success to " + mailItem.getTouser() + "[uid:" + mailItem.getUid() + ",title:" + mailItem.getTitle() + ",reward:" + mailItem.getRewardStr() + "]");
        }
    }


    public  void sendMailByXml(String toUser, String mailXmlId, String rewardId, MailType mailType,
                               String additionalAwardItemId, int awardNum, List<String> contentsParam,
                               String delimeter, boolean forceSetNoReward,MailSrcFuncType mailSrcFuncType) {
        sendMailByXmlWithSenderName(toUser, mailXmlId, rewardId, mailType, additionalAwardItemId, awardNum, contentsParam, delimeter, null, forceSetNoReward, null,mailSrcFuncType);
    }

    public  void sendMailByXml(String toUser, String mailXmlId, String rewardId, MailType mailType,
                               String additionalAwardItemId, int awardNum, List<String> contentsParam,
                               String delimeter, boolean forceSetNoReward, long createTime,MailSrcFuncType mailSrcFuncType) {
        sendMailByXmlWithSenderName(toUser, mailXmlId, rewardId, mailType, additionalAwardItemId, awardNum, contentsParam, delimeter, null, forceSetNoReward, null, createTime, true,mailSrcFuncType);
    }

    public void sendMailByXmlWithFromUser(String toUser, String mailXmlId, String rewardId, MailType mailType,
                                          String additionalAwardItemId, int awardNum, List<String> contentsParam,
                                          String delimeter, String fromUser, boolean forceSetNoReward,MailSrcFuncType mailSrcFuncType){
        this.fromUid = fromUser;
        sendMailByXml(toUser, mailXmlId, rewardId, mailType, additionalAwardItemId, awardNum, contentsParam, delimeter, forceSetNoReward,mailSrcFuncType);
    }

    public  void sendMailByXmlWithCustomTitle(String toUser, String mailXmlId, String rewardId, MailType mailType,
                               String additionalAwardItemId, int awardNum, List<String> contentsParam,
                               String delimeter, boolean forceSetNoReward, String customTitle,MailSrcFuncType mailSrcFuncType) {
        sendMailByXmlWithSenderName(toUser, mailXmlId, rewardId, mailType, additionalAwardItemId, awardNum, contentsParam, delimeter, null, forceSetNoReward, customTitle,mailSrcFuncType);
    }
}
