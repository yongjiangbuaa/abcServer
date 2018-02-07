package com.geng.gameengine.mail.send;

import com.geng.core.GameEngine;
import com.geng.gameengine.cross.WorldService;
import com.geng.gameengine.mail.MailSrcFuncType;
import com.geng.gameengine.mail.MailType;
import com.geng.puredb.model.Mail;
import com.geng.utils.LoggerUtil;
import com.geng.utils.xml.GameConfigManager;
import com.geng.core.data.ISFSObject;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;

import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2014/12/5.
 */
public class CrossPersonalMailSend extends AbstractMailSend {

    public CrossPersonalMailSend() {
        super(true);
    }

    @Override
    protected void afterMail(boolean haveSaved, Mail mail, SqlSession session) {
        if (mail.getType() == MailType.Send.ordinal() || mail.getType() == MailType.ModSend.ordinal()) {
            mail.setStatus(1);
        }
        super.afterMail(haveSaved, mail, session);
    }

    public ISFSObject sendMail(String senderUid, String targetUid, int targetServerId, String title, int mailType, String rewardId, long createTime, String contents, String senderName, MailSrcFuncType mailSrcFuncType) {
        WorldService remoteService = GameEngine.getInstance().getRemoteWorldService(targetServerId);
        boolean flag = remoteService.sendMail(senderUid, targetUid, title, contents, mailType, rewardId, senderName,mailSrcFuncType.getValue());
        if(!flag) {
            return null;
        }
        MailType sendType = MailType.Send;
        if (mailType == MailType.ModPersonal.ordinal()) {
            sendType = MailType.ModSend;
        }

        Mail mail = send(targetUid, senderUid, title, sendType, null, createTime, contents, false, true, true, false, null, false,mailSrcFuncType);
        ISFSObject retObj = mailSendReturn(mail, targetUid);
        return retObj;
    }

    public boolean sendCross(String senderUid, String targetUid, String title, int mailType, String rewardId, long createTime, String contents, String senderName,int mailSrcFuncType) {
        Mail mail = send(senderUid, targetUid, title, MailType.get(mailType), rewardId, createTime, contents, false, true, true, true, senderName, false, MailSrcFuncType.getByValue(mailSrcFuncType));
        if (mail != null && mail.getType() == MailType.GIFT.ordinal()){
            LoggerUtil.getInstance().recordMailGiftLog(mail.getUid(), senderUid, targetUid, mail.getContents(), rewardId, senderName, createTime);
        }
        //TODO 跨服邮件
        return mail != null;
    }

    public ISFSObject sendMailByMailXml(String senderUid, String senderName, String targetUid, String mailXmlId, int mailType, int targetServerId, String rewardId, long createTime, List<String> contentsParam,MailSrcFuncType mailSrcFuncType){
        Map<String, String> mailXml = new GameConfigManager("mail").getItem(mailXmlId);
        if (mailXml == null || mailXml.isEmpty()) {
            return null;
        }
        String title = mailXml.get("title") == null ? "" : mailXml.get("title");
        String contents = mailXml.get("message");
        if (contentsParam != null && contentsParam.size() > 0) {
            String param = StringUtils.join(contentsParam, "|");
            contents = contents + "|" + param;
        }
        String reward;
        if (StringUtils.isBlank(rewardId)) {
            reward = mailXml.get("reward");
        } else {
            reward = rewardId;
        }

        WorldService remoteService = GameEngine.getInstance().getRemoteWorldService(targetServerId);
        boolean flag = remoteService.sendMail(senderUid, targetUid, title, contents, mailType, reward, senderName,mailSrcFuncType.getValue());
        if(!flag) {
            return null;
        }
        Mail mail = send(targetUid, senderUid, title, MailType.get(mailType), null, createTime, contents, false, true, true, false, null, false,mailSrcFuncType);
        ISFSObject retObj = mailSendReturn(mail, targetUid);
        return retObj;
    }

    public void sendSystemMailByMailXml(String targetUid, int targetServerId, String mailXmlId, String rewardId, MailType mailType,
                                               List<String> contentsParam, String delimeter, MailSrcFuncType mailSrcFuncType){
        Map<String, String> mailXml = new GameConfigManager("mail").getItem(mailXmlId);
        String sender = mailXml.get("sender");
        String title = mailXml.get("title");
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
        WorldService remoteService = GameEngine.getInstance().getRemoteWorldService(targetServerId);
        remoteService.sendMail(null, targetUid, title, contents, mailType.ordinal(), reward, sender, mailSrcFuncType.getValue());
    }

    public void sendSystemMail(String targetUid,int targetServerId, String title, String contents,String rewardId, MailType mailType, MailSrcFuncType mailSrcFuncType){
        WorldService remoteService = GameEngine.getInstance().getRemoteWorldService(targetServerId);
        remoteService.sendMail(null, targetUid, title, contents, mailType.ordinal(), rewardId, null, mailSrcFuncType.getValue());
    }
}
