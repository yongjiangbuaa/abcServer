package com.geng.gameengine.mail.send;

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
public class LocalPersonalMailSend extends AbstractMailSend{

    public LocalPersonalMailSend() {
        super(true);
    }

    @Override
    protected void afterMail(boolean haveSaved, Mail mail, SqlSession session) {
        if (mail.getType() == MailType.Send.ordinal() || mail.getType() == MailType.ModSend.ordinal()) {
            mail.setStatus(1);
        }
        super.afterMail(haveSaved, mail, session);
    }

    public ISFSObject sendMail(String senderUid, String targetUid, String title, int mailType, String rewardId, long createTime, String contents,MailSrcFuncType mailSrcFuncType) {
        return sendMail(senderUid,targetUid,title,mailType,rewardId,createTime,contents,false,false,mailSrcFuncType);
    }
    public ISFSObject sendMail(String senderUid, String targetUid, String title, int mailType, String rewardId, long createTime, String contents,boolean itemIdFlag,boolean forceSetNoReward,MailSrcFuncType mailSrcFuncType) {

        Mail mailItem = send(senderUid, targetUid, title, MailType.get(mailType), rewardId, createTime, contents, itemIdFlag, true, true, true, null, forceSetNoReward,mailSrcFuncType);
        if (mailItem == null) {
            return null;
        }
        if (mailType == MailType.GIFT.ordinal()){
            LoggerUtil.getInstance().recordMailGiftLog(mailItem.getUid(), senderUid, targetUid, mailItem.getContents(), rewardId, null, createTime);
        }
        MailType sendType = MailType.Send;
        if (mailType == MailType.ModPersonal.ordinal()) {
            sendType = MailType.ModSend;
        }
        return sendOutMail(senderUid, targetUid, title, sendType.ordinal(), createTime, contents,mailSrcFuncType);
    }

    public ISFSObject sendOutMail(String senderUid, String targetUid, String title, int mailType, long createTime, String contents, MailSrcFuncType srcFuncType) {
        Mail mail = send(targetUid, senderUid, title, MailType.get(mailType), null, createTime, contents, false, true, true, false, null, false,srcFuncType);
        ISFSObject retObj = mailSendReturn(mail, targetUid);
        return retObj;
    }

    public void sendMailByXml(String senderUid, String targetUid, String mailXmlId, int mailType, String rewardId, long createTime, List<String> contentsParam,MailSrcFuncType mailSrcFuncType){
        Map<String, String> mailXml = new GameConfigManager("mail").getItem(mailXmlId);
        if (mailXml == null || mailXml.isEmpty()) {
            return;
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

        send(senderUid, targetUid, title, MailType.get(mailType), reward, createTime, contents, false, true, true, true, null, false,mailSrcFuncType);
    }
}
