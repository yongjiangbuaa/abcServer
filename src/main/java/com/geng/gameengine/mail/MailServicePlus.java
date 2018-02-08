package com.geng.gameengine.mail;


import com.geng.core.data.*;
import com.geng.exceptions.COKException;
import com.geng.gameengine.mail.send.BatchMailSend;
import com.geng.gameengine.mail.send.CrossPersonalMailSend;
import com.geng.gameengine.mail.send.LocalPersonalMailSend;
import com.geng.gameengine.mail.send.SystermMailSend;
import com.geng.puredb.model.Mail;
import com.geng.puredb.model.UserProfile;
import com.geng.utils.Constants;
import com.geng.utils.LoggerUtil;
import com.geng.utils.properties.PropertyFileReader;
import com.geng.utils.xml.GameConfigManager;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;

import java.util.*;

/**
 * Created by Administrator on 2014/12/5.
 */
public class MailServicePlus {
    public static final String PUSH_MAIL = "push.mail";
    public  static  final String THREE_DAY_RIGHT_MAIL_ID = "111679";

    //只有 MailType.GIFT 才会走到, 目前没有
    public static ISFSObject sendBatchPersonMail(UserProfile userProfile, List<String> targetList, String title, MailType mailType, String rewardId, String contents, boolean outMail, MailSrcFuncType mailSrcFuncType) {
        new BatchMailSend().sendMail(userProfile.getUid(), targetList, title, mailType, rewardId, System.currentTimeMillis(), contents, false,mailSrcFuncType);
        ISFSObject retObj = SFSObject.newInstance();
        if (outMail) {
            retObj = new LocalPersonalMailSend().sendOutMail(userProfile.getUid(), userProfile.getUid(), title, MailType.Send.ordinal(), System.currentTimeMillis(), contents,mailSrcFuncType);
        }
        return retObj;
    }

    public static void sendTargetUsersByMailXmlWithParam(List<String> userList, String mailXmlId, String rewardId, MailType mailType, List<String> paramList,MailSrcFuncType srcFuncType) {
        sendTargetUsersByMailXmlWithParam(userList, mailXmlId, rewardId, mailType, paramList, Constants.SERVER_ID, srcFuncType);
    }
    public static void sendTargetUsersByMailXmlWithParamGlobal(String users, String mailXmlId,String rewardId, List<String> paramList, MailSrcFuncType srcFuncType){
        if(StringUtils.isBlank(users)) return;
        List<String> userList = Arrays.asList( StringUtils.split(users,","));
        sendTargetUsersByMailXmlWithParamGlobal(userList,mailXmlId,rewardId,paramList,srcFuncType);
    }
    public static void sendTargetUsersByMailXmlWithParamGlobal(List<String> userList, String mailXmlId,String rewardId, List<String> paramList, MailSrcFuncType srcFuncType) {
        /*if(null == userList || userList.size() <= 0 ) return;
        ISFSObject pushObj = SFSObject.newInstance();
        pushObj.putInt(MailGlobalChannel.SID, Constants.SERVER_ID);
        pushObj.putUtfString(MailGlobalChannel.UIDS, StringUtils.join(userList,","));
        pushObj.putUtfString(MailGlobalChannel.MAIL_XML_ID,mailXmlId);
        if(rewardId == null) pushObj.putNull(MailGlobalChannel.REWARD_ID);
        else pushObj.putUtfString(MailGlobalChannel.REWARD_ID,rewardId);
        pushObj.putUtfString(MailGlobalChannel.MAIL_PARAMS, StringUtils.join(paramList,","));
        pushObj.putInt(MailGlobalChannel.SRC_FUNC_TYPE,srcFuncType.getValue());
        R.Global().publish(MailGlobalChannel.class.getSimpleName(), pushObj.toJson());*/
    }

    public static void sendTargetUsersByMailXmlWithParam(List<String> userList, String mailXmlId, String rewardId, MailType mailType, List<String> paramList, int serverId,MailSrcFuncType srcFuncType) {
        try {
            new BatchMailSend().sendMailByXml(userList, mailXmlId, rewardId, mailType, paramList, serverId, null,srcFuncType);
        } catch (Exception e){
            LoggerUtil.getInstance().recordException(e);
        }
    }



    public static void sendAllianceMailByMailXmlWithParam(String allianceId, String mailXmlId, String rewardId, MailType mailType, List<String> paramList,MailSrcFuncType srcFuncType) {
        sendAllianceMailByMailXmlWithParam(allianceId, mailXmlId, rewardId, mailType, paramList, null,srcFuncType);
    }

    public static void sendAllianceMailByMailXmlWithParam(String allianceId, String mailXmlId, String rewardId, MailType mailType, List<String> paramList, String orderInfo,MailSrcFuncType srcFuncType) {

    }

    /**
     * 筛选联盟成员的appversion
     * 大于version版本
     * */
    public static void sendAllianceMailByMailXmlWithParamCheckVersion(String allianceId, String mailXmlId, String rewardId, MailType mailType, List<String> paramList, String version,MailSrcFuncType mailSrcFuncType) {

    }

    public static ISFSObject sendCrossPersonalMail(UserProfile senderUserProfile, MailType mailType, String rewardId, String targetUid, int targetServerId, String title, String contents,MailSrcFuncType mailSrcFuncType) {
        ISFSObject retInfo = new CrossPersonalMailSend().sendMail(senderUserProfile.getUid(), targetUid, targetServerId, title, mailType.ordinal(), rewardId, System.currentTimeMillis(), contents, senderUserProfile.getName(),mailSrcFuncType);
        return retInfo;
    }



    public static ISFSObject sendCrossPersonalMailByMailXml(String senderUid, String senderName, String targetUid, int targetServerId, String mailXmlId, MailType mailType, String rewardId, List<String> contentsParam, long createTime,MailSrcFuncType mailSrcFuncType){
        ISFSObject ret = new CrossPersonalMailSend().sendMailByMailXml(senderUid, senderName, targetUid, mailXmlId, mailType.ordinal(), targetServerId, rewardId, createTime, contentsParam, mailSrcFuncType);
        return ret;
    }

    public static ISFSObject sendPersonalMailByMailXml(String senderUid, String targetUid, String mailXmlId, MailType mailType, String rewardId, List<String> contentsParam, long createTime,MailSrcFuncType mailSrcFuncType){
        new LocalPersonalMailSend().sendMailByXml(senderUid, targetUid, mailXmlId, mailType.ordinal(), rewardId, createTime, contentsParam,mailSrcFuncType);
        return null;
    }

    public static void sendSystemMailWithPushContent(String targetUid, String title, MailType mailType, String rewardId, String contents, boolean itemIdFlag, long createTime,MailSrcFuncType mailSrcFuncType) {
        new SystermMailSend(true).sendMail(targetUid, mailType, title, contents, rewardId, createTime, itemIdFlag, mailSrcFuncType);
    }

    public static void sendSystemMail(String targetUid, String title, MailType mailType, String rewardId, String contents, boolean itemIdFlag, long createTime,MailSrcFuncType srcFuncType) {
        new SystermMailSend().sendMail(targetUid, mailType, title, contents, rewardId, createTime, itemIdFlag, srcFuncType);
    }

    public static void sendSystemMailWithSender(String targetUid, String title, MailType mailType, String rewardId, String contents, boolean itemIdFlag, long createTime,String sendName,MailSrcFuncType mailSrcFuncType) {
        new SystermMailSend().sendMailWithSender(targetUid, mailType, title, contents, rewardId, createTime, itemIdFlag, sendName, mailSrcFuncType);
    }

    public static void sendSystemMailMarkFromUser(String fromUser, String targetUid, String title, MailType mailType, String rewardId, String contents, boolean itemIdFlag, long createTime,MailSrcFuncType mailSrcFuncType) {
        new SystermMailSend().sendMail(fromUser, targetUid, mailType, title, contents, rewardId, createTime, itemIdFlag, mailSrcFuncType);
    }

    public static void sendSFSObjMailWithPushContent(String targetUid, String title, MailType mailType, String rewardId, ISFSObject contentsObj, boolean isDialog, long createTime,MailSrcFuncType mailSrcFuncType) {
        String contents = contentsObj.toJson();
        new SystermMailSend(true).sendMail(targetUid, mailType, title, contents, rewardId, createTime, isDialog,mailSrcFuncType);
    }

    public static void sendSFSObjMail(String targetUid, String title, MailType mailType, String rewardId, ISFSObject contentsObj, boolean isDialog, long createTime,MailSrcFuncType mailSrcFuncType) {
        String contents = contentsObj.toJson();
        new SystermMailSend().sendMail(targetUid, mailType, title, contents, rewardId, createTime, isDialog,mailSrcFuncType);
    }

    public static void sendSFSObjMailMarkFromUer(String fromUser, String targetUid, String title, MailType mailType, String rewardId, ISFSObject contentsObj, boolean isDialog, long createTime,MailSrcFuncType mailSrcFuncType) {
        String contents = contentsObj.toJson();
        new SystermMailSend().sendMail(fromUser, targetUid, mailType, title, contents, rewardId, createTime, isDialog, mailSrcFuncType);
    }

    public static void sendCrossMail(String targetUid,int targetServerId, String title, String contents,String rewardId, MailType mailType, MailSrcFuncType mailSrcFuncType){
        new CrossPersonalMailSend().sendSystemMail(targetUid, targetServerId, title, contents, rewardId, mailType, mailSrcFuncType);
    }

    public static void sendCrossMailByMailXml(String uid, int targetServerId, String mailXmlId, String rewardId, MailType mailType,MailSrcFuncType mailSrcFuncType){
        new CrossPersonalMailSend().sendSystemMailByMailXml(uid, targetServerId, mailXmlId, rewardId, mailType, null, null, mailSrcFuncType);
    }

    public static void sendCrossParamMailByMailXml(String uid, int targetServerId, String mailXmlId, String rewardId, MailType mailType,List<String> contentsParam,MailSrcFuncType mailSrcFuncType){
        new CrossPersonalMailSend().sendSystemMailByMailXml(uid, targetServerId, mailXmlId, rewardId, mailType, contentsParam, null, mailSrcFuncType);
    }

    public static void sendMailByMailXml(String uid, String mailXmlId, String rewardId, MailType mailType,MailSrcFuncType mailSrcFuncType) {
        new SystermMailSend().sendMailByXml(uid, mailXmlId, rewardId, mailType, null, 0, null, null, false, mailSrcFuncType);
    }

    public static void sendMailByMailXmlWithParam(String uid, String mailXmlId, String rewardId, List<String> contentsParam,MailType mailType,MailSrcFuncType mailSrcFuncType) {
        new SystermMailSend().sendMailByXml(uid, mailXmlId, rewardId, mailType, null, 0, contentsParam, null, false, mailSrcFuncType);
    }

    public static void sendMailByMailXmlWithoutCheckNum(String uid, String mailXmlId, String rewardId, MailType mailType,MailSrcFuncType mailSrcFuncType) {
        new SystermMailSend().sendMailByXmlWithSenderName(uid, mailXmlId, rewardId, mailType, null, 0, null, null, null, false, null, System.currentTimeMillis(), false, mailSrcFuncType);
    }

    public static void sendMailByMailXmlForceSetNorReward(String uid, String mailXmlId, String rewardId, MailType mailType, String customTitle,MailSrcFuncType mailSrcFuncType) {
        new SystermMailSend().sendMailByXmlWithCustomTitle(uid, mailXmlId, rewardId, mailType, null, 0, null, null, true, customTitle, mailSrcFuncType);
    }
    public static void sendMailByMailXmlWithFromUser(String uid, String mailXmlId, String rewardId, MailType mailType, String fromUser,MailSrcFuncType mailSrcFuncType) {
        new SystermMailSend().sendMailByXmlWithFromUser(uid, mailXmlId, rewardId, mailType, null, 0, null, null, fromUser, false, mailSrcFuncType);
    }
    public static void sendMailByMailXmlWithSenderName(String uid, String mailXmlId, String rewardId, MailType mailType,String senderName,MailSrcFuncType mailSrcFuncType) {
        new SystermMailSend().sendMailByXmlWithSenderName(uid, mailXmlId, rewardId, mailType, null, 0, null, null, senderName, false, null, mailSrcFuncType);
    }

    public static void sendCrossMailByXmlWithParam(String uid, int targetServer, String mailXmlId, String rewardId, MailType mailType, List<String> paramList, MailSrcFuncType mailSrcFuncType) {
        new CrossPersonalMailSend().sendSystemMailByMailXml(uid, targetServer, mailXmlId, rewardId, mailType, paramList, null, mailSrcFuncType);
    }

    public static void sendMailByMailXmlWithParam(String uid, String mailXmlId, String rewardId, MailType mailType, List<String> paramList,MailSrcFuncType mailSrcFuncType) {
        new SystermMailSend().sendMailByXml(uid, mailXmlId, rewardId, mailType, null, 0, paramList, null, false,mailSrcFuncType);
    }

    public static void sendMailByMailXmlWithParam(String uid, String mailXmlId, String rewardId, MailType mailType, List<String> paramList, String delimeter,MailSrcFuncType mailSrcFuncType) {
        new SystermMailSend().sendMailByXml(uid, mailXmlId, rewardId, mailType, null, 0, paramList, delimeter, false,mailSrcFuncType);
    }

    public static void sendMailByMailXmlWithParam(String uid, String mailXmlId, String rewardId, MailType mailType, List<String> paramList, boolean forceSetNoReward, long createTime,MailSrcFuncType mailSrcFuncType) {
        new SystermMailSend().sendMailByXml(uid, mailXmlId, rewardId, mailType, null, 0, paramList, null, forceSetNoReward, createTime,mailSrcFuncType);
    }

    public static void sendMailByMailXmlWithParam(String uid, String mailXmlId, String rewardId, MailType mailType, List<String> paramList, long createTime,MailSrcFuncType mailSrcFuncType) {
        new SystermMailSend().sendMailByXml(uid, mailXmlId, rewardId, mailType, null, 0, paramList, null, false, createTime,mailSrcFuncType);
    }

    public static void sendMailByMailXml(String uid, String mailXmlId, String rewardId, MailType mailType, String additionalAwardItemId, int awardNum,MailSrcFuncType mailSrcFuncType) {
        new SystermMailSend().sendMailByXml(uid, mailXmlId, rewardId, mailType, additionalAwardItemId, awardNum, null, null, false,mailSrcFuncType);
    }

    public static void sendWholeServerMail(String title, String contents, Long sendTime, Long endRegTime, String rewardId, boolean isDialog, int mbLevel,MailSrcFuncType mailSrcFuncType){
        sendWholeServerMail(title, contents, sendTime, endRegTime, endRegTime, rewardId, isDialog, Constants.SERVER_ID, mbLevel,mailSrcFuncType);
    }
    public static void sendWholeServerMail(String title, String contents, Long sendTime, Long endRegTime, Long lastOnlineTime, String rewardId, boolean isDialog, int serverId, int mbLevel,MailSrcFuncType mailSrcFuncType){
        sendWholeServerMail(title, null, contents, sendTime, endRegTime, lastOnlineTime, rewardId, isDialog, serverId, mbLevel,mailSrcFuncType);
    }

    public static void sendWholeServerMail(String title, MailType mailType, String contents, Long sendTime, Long endRegTime, Long lastOnlineTime, String rewardId, boolean isDialog, int serverId, int mbLevel,MailSrcFuncType mailSrcFuncType){


    }

    public static void sendWholeServerMail(String title, String contents, Long sendTime, String rewardId, boolean isDialog, int mbLevel,MailSrcFuncType mailSrcFuncType) {
        sendWholeServerMail(title, contents, sendTime, null, rewardId, isDialog, mbLevel,mailSrcFuncType);
    }

    public static void sendWholeServerMailByXml(String mailXmlId, long sendTime,MailSrcFuncType mailSrcFuncType) {
        sendWholeServerMailByXml(mailXmlId, null, sendTime,mailSrcFuncType);
    }

    public static void sendWholeServerMailByXml(String mailXmlId, List<String> contentsParamList, long sendTime,MailSrcFuncType mailSrcFuncType) {
        sendWholeServerMailByXml(mailXmlId, null, contentsParamList, sendTime,mailSrcFuncType);
    }

    public static void sendWholeServerMailByXml(String mailXmlId, String rewardId, List<String> contentsParamList, long sendTime,MailSrcFuncType mailSrcFuncType) {
        sendWholeServerMailByXml(mailXmlId, rewardId, contentsParamList, sendTime, null, null, Constants.SERVER_ID,mailSrcFuncType);
    }

    public static void sendWholeServerMailByXml(String mailXmlId, String rewardId, List<String> contentsParamList, long sendTime, Long regTime, Long lastOnlineTime, int serverId,MailSrcFuncType mailSrcFuncType) {
        Map<String, String> mailXMl = new GameConfigManager("mail").getItem(mailXmlId);
        if (mailXMl != null && !mailXMl.isEmpty()) {
            String contents = mailXMl.get("message");
            if (contentsParamList != null && !contentsParamList.isEmpty()) {
                String param = StringUtils.join(contentsParamList, '|');
                contents = contents + '|' + param;
            }
            String title = mailXMl.get("title");
            String reward;
            if (StringUtils.isBlank(rewardId)) {
                reward = mailXMl.get("reward");
            } else {
                reward = rewardId;
            }
            int mbLevel = 0;
            if ("11608".equals(mailXmlId) && "1".equals(new GameConfigManager("item").getItem("function_on7").get("k4"))) {
                mbLevel = 7;
            }
            sendWholeServerMail(title, contents, sendTime, regTime, lastOnlineTime, reward, true, serverId, mbLevel,mailSrcFuncType);
        }
    }
	
	public static void sendWholeServerMailByXmlWithType(String mailXmlId, MailType mailType, String rewardId,
			List<String> contentsParamList, long sendTime, Long regTime, Long lastOnlineTime, int mbLevelMin,
			int serverId,MailSrcFuncType mailSrcFuncType) {
		Map<String, String> mailXMl = new GameConfigManager("mail").getItem(mailXmlId);
		if (mailXMl != null && !mailXMl.isEmpty()) {
			String contents = mailXMl.get("message");
			if (contentsParamList != null && !contentsParamList.isEmpty()) {
				String param = StringUtils.join(contentsParamList, '|');
				contents = contents + '|' + param;
			}
			String title = mailXMl.get("title");
			String reward;
			if (StringUtils.isBlank(rewardId)) {
				reward = mailXMl.get("reward");
			} else {
				reward = rewardId;
			}
			int mbLevel = 0;
			if ("11608".equals(mailXmlId) && "1".equals(GameConfigManager.getString("item", "function_on7", "k4"))) {
				mbLevel = 7;
			}
			sendWholeServerMail(title, mailType, contents, sendTime, regTime, lastOnlineTime, reward, true,
					serverId, mbLevel,mailSrcFuncType);
		}
	}



    public static String getRewardIdByMailId(String mailId) {
        Map<String, String> mailXml = new GameConfigManager("mail").getItem(mailId);
        String rewardId = null;
        if (mailXml != null && mailXml.containsKey("reward")) {
            rewardId = mailXml.get("reward");
        }
        return rewardId;
    }

    /**
     * 获得 系统功能 邮件中 相关数据的大约值
     */
    public static int getProbablyNum(int num) {
        int randomResult = 0;
        if (num > 0) {
            int beginRandom = (int) (num * 0.8);
            int endRandom = (int) (num * 1.2);
            randomResult = new Random().nextInt(endRandom) % (endRandom - beginRandom + 1) + beginRandom;
            if (randomResult > 100) {
                randomResult = randomResult / 100 * 100;
            }
        }
        return randomResult;
    }

    public static ISFSObject transContentsObj(String contents, int mType, String appVersion){
        return transContentsObj(contents, mType, appVersion, false, null, null);
    }



    public static ISFSObject transContentsObj(String contents, int mType, String appVersion, boolean isTranslate, String lang, String translationId) {
        ISFSObject mailObj;
        MailType mailType = MailType.get(mType);
        if (mailType.contentsIsObj()) {
            if(mailType == MailType.Detect_Report){ //反侦察邮件是dialog号， 侦查邮件是contentsObj, 但都为Detect_Report类型，此处特殊判断，如果是反侦查邮件就不去解析json
                if(!contents.startsWith("{")) {
                    mailObj = SFSObject.newInstance();
                    contents = StringUtils.replaceEach(contents, new String[]{"\\n"}, new String[]{"\n"});
                    mailObj.putUtfString("contents", contents);
                    return mailObj;
                }
            }
	        try {
		        mailObj = SFSObject.newFromJsonData(contents);
		        if (MailType.Fight.ordinal() == mType) {
			        changeOldFormat(mailObj.get("defHelpReport"), mailObj, "defHelpReport", appVersion);
			        changeOldFormat(mailObj.get("atkHelpReport"), mailObj, "atkHelpReport", appVersion);
                    fixedFightSkMail(mailObj);
		        } else if (MailType.Detect_Report.ordinal() == mType) {
                    fixedDetectSkMail(mailObj);
                }
	        } catch (Exception e) {
		        return null;
	        }
        } else {
            mailObj = SFSObject.newInstance();
            contents =  StringUtils.replaceEach(contents, new String[]{"\\n"}, new String[]{"\n"});
            mailObj.putUtfString("contents", contents);
            if(isTranslate){
                if(PropertyFileReader.getRealBooleanItem("mail_translation", "true"))
                    if(lang != null)
                        if(mType == 22 || mType == 24){
                            if(StringUtils.isNotBlank(translationId))
                                mailObj.putUtfString("translationId", translationId);
                            MailFunction.translateMailByTranslationId(mailObj, contents, lang);
                        }
            }
        }
        return mailObj;
    }

    private static void fixedDetectSkMail(ISFSObject mailObj) {
        try {
            ISFSArray abilityArr = mailObj.getSFSArray("ability");
            if (abilityArr != null) {
                String problemId[] = {"622300", "622200", "622100", "622000", "620700"};
                for (int index = 0; index < abilityArr.size(); index++) {
                    ISFSObject abilityObj = abilityArr.getSFSObject(index);
                    for (String problem : problemId) {
                        if (abilityObj.containsKey(problem)) {
                            abilityObj.removeElement(problem);
                        }
                    }
                }
            }
        } catch (Exception e) {
            // do nothing
        }
    }

    private static void fixedFightSkMail(ISFSObject mailObj) {
        try {
            if (mailObj != null) {
                ISFSArray defGenArray = mailObj.getSFSArray("defGen");
                if (defGenArray != null) {
                    for (int defGenIndex = 0; defGenIndex < defGenArray.size(); defGenIndex++) {
                        ISFSObject defGenObj = defGenArray.getSFSObject(defGenIndex);
                        if (defGenObj != null) {
                            defGenObj.removeElement("skill");
                            defGenObj.removeElement("ability");
                        }
                    }
                }
                ISFSArray atkGenArray = mailObj.getSFSArray("atkGen");
                if (atkGenArray != null) {
                    for (int atkGenIndx = 0; atkGenIndx < atkGenArray.size(); atkGenIndx++) {
                        ISFSObject atkGenObj = atkGenArray.getSFSObject(atkGenIndx);
                        if (atkGenObj != null) {
                            atkGenObj.removeElement("skill");
                            atkGenObj.removeElement("ability");
                        }
                    }
                }
                ISFSArray atkHelpReportArr = mailObj.getSFSArray("atkHelpReport");
                if (atkHelpReportArr != null) {
                    for (int index = 0; index < atkHelpReportArr.size(); index++) {
                        ISFSObject helper = atkHelpReportArr.getSFSObject(index);
                        if (helper != null) {
                            ISFSArray genIno = helper.getSFSArray("genInfo");
                            if (genIno != null) {
                                for (int genIndex = 0; genIndex < genIno.size(); genIndex++) {
                                    ISFSObject infoObj = genIno.getSFSObject(genIndex);
                                    infoObj.removeElement("skill");
                                    infoObj.removeElement("ability");
                                }
                            }
                        }
                    }
                }
                ISFSArray defHelpReportArr = mailObj.getSFSArray("defHelpReport");
                if (defHelpReportArr != null) {
                    for (int index = 0; index < defHelpReportArr.size(); index++) {
                        ISFSObject helper = defHelpReportArr.getSFSObject(index);
                        if (helper != null) {
                            ISFSArray genIno = helper.getSFSArray("genInfo");
                            if (genIno != null) {
                                for (int genIndex = 0; genIndex < genIno.size(); genIndex++) {
                                    ISFSObject infoObj = genIno.getSFSObject(genIndex);
                                    infoObj.removeElement("skill");
                                    infoObj.removeElement("ability");
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // do nothing
        }
    }

    private static void changeOldFormat(SFSDataWrapper objWrapper, ISFSObject mailObj, String key, String appVersion) {
        if (objWrapper == null) {
            return;
        }
        if (objWrapper.getTypeId().equals(SFSDataType.SFS_OBJECT)) {
            ISFSArray replaceArray = SFSArray.newInstance();
            ISFSObject obj = (ISFSObject) objWrapper.getObject();
            Iterator<Map.Entry<String, SFSDataWrapper>> iterator = obj.iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, SFSDataWrapper> entry = iterator.next();
                ISFSObject armyInfoObj = SFSObject.newInstance();
                armyInfoObj.putUtfString("name", entry.getKey());
                armyInfoObj.putSFSArray("armyInfo", (ISFSArray) entry.getValue().getObject());
                replaceArray.addSFSObject(armyInfoObj);
            }
            mailObj.putSFSArray(key, replaceArray);
        }
    }

    public static ISFSObject getUserInfoInMail(UserProfile userProfile) {
        ISFSObject userObj = SFSObject.newInstance();
        userObj.putUtfString("uid", userProfile.getUid());
        userObj.putUtfString("name", userProfile.getName());
        userObj.putInt("lv", userProfile.getLevel());
        userObj.putUtfString("pic", userProfile.getPic() == null ? "" : userProfile.getPic());
        userObj.putInt("picVer", userProfile.getPicVer());
        return userObj;
    }


    public static Map<String, Map<String, String>> getUserPicMap(Set<String> allFromUser) {
        return null;
    }

    public static Map<String, Map<String, String>> getFromUserMap(List<Mail> mailList) {
        return null;
    }
}
