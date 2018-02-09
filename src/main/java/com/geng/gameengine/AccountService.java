/**
 * @author: lifangkai@elex-tech.com
 * @date: 2013年9月6日 下午2:30:17
 */
package com.geng.gameengine;

import com.geng.core.GameEngine;
//import com.geng.gameengine.friend.FriendManager;
import com.geng.gameengine.mail.MailServicePlus;
import com.geng.gameengine.mail.MailSrcFuncType;
import com.geng.puredb.model.UserProfile;
import com.geng.utils.CommonUtils;
import com.geng.utils.Constants;
import com.geng.utils.SFSMysql;
import com.geng.utils.distributed.GlobalDBProxy;
import com.geng.utils.xml.GameConfigManager;
import com.geng.core.data.ISFSArray;
import com.geng.core.data.ISFSObject;
import com.geng.core.data.SFSArray;
import com.geng.core.data.SFSObject;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 用户服务类
 */
public class AccountService {
    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    public final static String APP_PF_GOOGLE = "market_global";
    public final static String APP_PF_GOOGLE_NEW = "market_global_new";//新游戏
    public final static String APP_PF_APPSTORE = "AppStore";
    public final static String APP_PF_CN1 = "cn1";
    public final static String APP_PF_TENCENT = "tencent";
    public final static String APP_PF_CN_MI = "cn_mi";
    public final static String APP_PF_CN_UC = "cn_uc";
    public final static String APP_PF_CN_360 = "cn_360";
    public final static String APP_PF_CN_BAIDU = "cn_baidu";
    public final static String APP_PF_CN_MIHY = "cn_mihy";
    public final static String APP_PF_CN_NEARME="cn_nearme";//OPPO
    public final static String APP_PF_CN_LENOVO="cn_lenovo";//联想
    public final static String APP_PF_CN_HUAWEI="cn_huawei";//华为
    public final static String APP_PF_CN_AM="cn_am";//金立
    public final static String APP_PF_CN_VIVO="cn_vivo";//vivo
    public final static String APP_PF_CN_KUPAI="cn_kupai";//酷派
    public final static String APP_PF_CN_PPS="cn_pps";//PPS
    public final static String APP_PF_CN_PPTV="cn_pptv";//PPTV
    public final static String APP_PF_CN_YOUKU="cn_youku";//优酷土豆
    public final static String APP_PF_CN_ANZHI="cn_anzhi";//安智
    public final static String APP_PF_CN_EWAN="cn_ewan";//益玩
    public final static String APP_PF_CN_MZW="cn_mzw";//拇指玩
    public final static String APP_PF_CN_WDJ="cn_wdj";//豌豆荚
    public final static String APP_PF_CN_WYX="cn_wyx";//sina游戏
    public final static String APP_PF_CN_KUGOU="cn_kugou";//酷狗
    public final static String APP_PF_CN_SOGOU="cn_sogou";//搜狗
    public final static String APP_PF_CN_DANGLE="cn_dangle";//当乐
    public final static String APP_PF_CN_TOUTIAO="cn_toutiao";//今日头条
    public final static String APP_PF_CN_SY37="cn_sy37";//37wan
    public final static String APP_PF_CN_MZ="cn_mz";//魅族
    public final static String APP_PF_CN_LESHI="cn_leshi";//乐视
    public final static String APP_PF_CN_OPPO="cn_oppo";//oppo
    public final static String APP_PF_CN_MEIZU="cn_meizu";//魅族
    public final static String APP_PF_CN_CAOXIE="cn_caoxie";//草鞋
    public final static String APP_PF_CN_AIQIYI = "cn_aiqiyi";//爱奇艺
    public final static String APP_PF_CN_57K = "cn_57k";//57k
    public final static String APP_PF_CN_YESHEN = "cn_yeshen";//夜神
    public final static String APP_PF_CN_BLUESTACKS = "cn_bluestacks";//蓝叠

    public final static String APP_PF_MI_WEB="mi_web";//小米页游
    public final static String APP_PF_VK_WEB="vk";//vk页游
    public final static String APP_PF_WINDOWS_PHONE="WindowsPhone";//winphone
    public final static String APP_PF_WINDOWS_STORE="WindowsStore";//winstore
    public final static String APP_PF_WINDOWS_PC="WindowsPC";//winpc

    private enum BIND_TYPE {
        DEVICE,
        FACEBOOK,
        GOOGLEPLAY,
        EMAIL,
        APPSTORE,
        QQ,
        WEIXIN,
        WEIBO,
        TENCENT,
        VK,
        MSLIVE,
    }

    /**
     * 登录返回绑定信息
     *
     * @param userInfoObj
     */
    public static boolean toBindInfo(UserProfile userProfile, ISFSObject userInfoObj) {
//        UserLord lord  = userProfile.getUserLord();
        String uid = userProfile.getUid();
        String bindType = null;
        boolean isBind = false;
        ISFSObject bindObj = getUserBindStatus(userProfile.getUid());
        if (!StringUtils.isBlank(bindObj.getUtfString("googleAccount"))) {
            bindType = "google";
            isBind = true;
            userInfoObj.putUtfString("googlePlay", bindObj.getUtfString("googleAccount"));
        }
        if (!StringUtils.isBlank(bindObj.getUtfString("googleAccountName"))) {
            userInfoObj.putUtfString("googleAccountName", bindObj.getUtfString("googleAccountName"));
        }
        if (!StringUtils.isBlank(bindObj.getUtfString("facebookAccount"))) {
            bindType = "facebook";
            isBind = true;
            userInfoObj.putUtfString("facebook", bindObj.getUtfString("facebookAccount"));
        }
        if (!StringUtils.isBlank(bindObj.getUtfString("facebookAccountName"))) {
            userInfoObj.putUtfString("facebookAccountName", bindObj.getUtfString("facebookAccountName"));
        }
        if (!StringUtils.isBlank(bindObj.getUtfString("emailAccount"))) {
            userInfoObj.putUtfString("email", bindObj.getUtfString("emailAccount"));
            userInfoObj.putInt("emailConfirm", bindObj.getInt("emailConfirm") == null ? 0 : bindObj.getInt("emailConfirm"));
        }
        if (!StringUtils.isBlank(bindObj.getUtfString("deviceId"))) {
            userInfoObj.putUtfString("deviceId", bindObj.getUtfString("deviceId"));
        }
        if (!StringUtils.isBlank(bindObj.getUtfString("pf")) && "AppStore".equals(bindObj.getUtfString("pf")) && !StringUtils.isBlank(bindObj.getUtfString("pfId"))) {
            bindType = "AppStore";
            isBind = true;
            userInfoObj.putUtfString("appStoreId", bindObj.getUtfString("pfId"));
            ISFSObject gcObj = getGameCenterInfo(userProfile.getUid());
            if(gcObj != null && !StringUtils.isBlank(gcObj.getUtfString("gcName"))){
                userInfoObj.putUtfString("gcName", gcObj.getUtfString("gcName"));
            }
        }
        //多渠道绑定
        List<String> bindPfList = new ArrayList<>();
        if(userProfile.getPf().equals(AccountService.APP_PF_CN1)) {
            bindPfList.add(GlobalDBProxy.MAPPING_TYPE.weibo.toString());
            bindPfList.add(GlobalDBProxy.MAPPING_TYPE.weixin.toString());
        }
        if(userProfile.getPf().equals(AccountService.APP_PF_APPSTORE) && "zh_CN".equals(userProfile.getLang())) {
            bindPfList.add(GlobalDBProxy.MAPPING_TYPE.weibo.toString());
            bindPfList.add(GlobalDBProxy.MAPPING_TYPE.weixin.toString());
        }
        if(userProfile.getPf().equals(AccountService.APP_PF_TENCENT)) {
            bindPfList.add(GlobalDBProxy.MAPPING_TYPE.weibo.toString());
            bindPfList.add(GlobalDBProxy.MAPPING_TYPE.weixin.toString());
            bindPfList.add(GlobalDBProxy.MAPPING_TYPE.qq.toString());
        }
        if(userProfile.getPf().equals(AccountService.APP_PF_APPSTORE) && "ru".equals(userProfile.getLang()))
            bindPfList.add(GlobalDBProxy.MAPPING_TYPE.vk.toString());
        if((userProfile.getPf().equals(AccountService.APP_PF_GOOGLE) || userProfile.getPf().equals(AccountService.APP_PF_GOOGLE_NEW)) && "ru".equals(userProfile.getLang()))
            bindPfList.add(GlobalDBProxy.MAPPING_TYPE.vk.toString());
        if(userProfile.getPf().equals(AccountService.APP_PF_WINDOWS_PHONE) || userProfile.getPf().equals(AccountService.APP_PF_WINDOWS_STORE) || userProfile.getPf().equals(AccountService.APP_PF_WINDOWS_PC))
            bindPfList.add(GlobalDBProxy.MAPPING_TYPE.mslive.toString());
        if(bindPfList.size() > 0){
            ISFSArray bindInfo = GlobalDBProxy.getAccountBindInfo(uid, bindPfList, null);
            if(bindInfo.size() > 0){
                isBind = true;
                userInfoObj.putSFSArray("bindInfo",bindInfo);
            }
        }
        userInfoObj.putBool("bindFlag", bindType != null);
        if (bindType != null ){//&& !lord.isAcceptFirstBindAccountReward()) {
            sendRewardForFirstBindUser(userProfile, bindType);
        }
//        userInfoObj.putUtfString("bindShareStatus", lord.getFirstBindShareRewardStatusInfo());
        String accountName = bindObj.getUtfString("gameUserName");
        if(accountName == null || !userProfile.getName().equals(accountName)) {
            GlobalDBProxy.updateAccountByType(uid, GlobalDBProxy.MAPPING_TYPE.name, userProfile.getName(), null, accountName);
        }
        String pf = bindObj.getUtfString("pf");
        if (StringUtils.isNotBlank(pf) && pf.startsWith("G:") && StringUtils.isBlank(bindObj.getUtfString("pfId"))) {
            SFSMysql.getInstance().executeGlobal("update account_new set pf = ?, pfId = ? where gameUid = ?", new Object[]{GlobalDBProxy.MAPPING_TYPE.AppStore.toString(), pf, uid});
        }
        return isBind;
    }

    public static ISFSObject getUserBindStatus(String gameUid) {
        ISFSObject bindObj = new SFSObject();
        ISFSArray queryResult = SFSMysql.getInstance().queryGlobal("select gameUserName, deviceId, emailAccount, googleAccount, googleAccountName, facebookAccount,facebookAccountName,emailConfirm,pf,pfId from account_new where gameUid = ?", new Object[]{gameUid});
        if (queryResult.size() == 1) {
            bindObj = queryResult.getSFSObject(0);
        }
        return bindObj;
    }

    public static ISFSObject getGameCenterInfo(String gameUid) {
        ISFSObject gcObj = new SFSObject();
        ISFSArray queryResult = SFSMysql.getInstance().queryGlobal("select gcId, gcName from gameCenter_info where gameUid = ?", new Object[]{gameUid});
        if (queryResult.size() == 1) {
            gcObj = queryResult.getSFSObject(0);
        }
        return gcObj;
    }

    /**
     * 为第一次绑定的用户发奖励mail
     *
     * @param userProfile
     */
    public static void sendRewardForFirstBindUser(UserProfile userProfile, String type) {

    }

    /**
     * 为各平台第一次分享的用户发奖励mail
     *
     * @param userProfile
     */
    public static void sendRewardForFirstShareUser(UserProfile userProfile, String type) {

    }

    public static boolean isBindFacebook(String uid) {
        ISFSObject obj = getUserBindStatus(uid);
        boolean ret = false;
        if (obj != null && obj.containsKey("facebookAccount") && StringUtils.isNotBlank(obj.getUtfString("facebookAccount"))) {
            ret = true;
        }
        return ret;
    }

    public static boolean isAccountHasBeenBind(String uid) {
        boolean ret = false;
        ISFSObject obj = getUserBindStatus(uid);
        if (obj != null) {
            if (obj.containsKey("facebookAccount") && !StringUtils.isBlank(obj.getUtfString("facebookAccount"))) {
                ret = true;
            } else if (obj.containsKey("googleAccount") && !StringUtils.isBlank(obj.getUtfString("googleAccount"))) {
                ret = true;
            } else if (obj.containsKey("pf") && obj.getUtfString("pf").equals("AppStore") && obj.containsKey("pf") && !StringUtils.isBlank(obj.getUtfString("pfId"))) {
                ret = true;
            } else if (GlobalDBProxy.getAccountBindInfo(uid, null, null).size() > 0){
                ret = true;
            }
        }
        return ret;
    }

    /**
     * 取消设备ID绑定
     */
    public static void cancelBind(int type, String account) {
        String valueName = "";
        GlobalDBProxy.MAPPING_TYPE mappingType;
        if (type == BIND_TYPE.DEVICE.ordinal()) {
            mappingType = GlobalDBProxy.MAPPING_TYPE.device;
        } else if (type == BIND_TYPE.FACEBOOK.ordinal()) {
            mappingType = GlobalDBProxy.MAPPING_TYPE.facebook;
//            FriendManager.getInstance().cancelBind(account);
//            GameEngine.getInstance().getUserProfile(account).getContactManager().cancelFbBind();//更新fb好友
        } else if (type == BIND_TYPE.GOOGLEPLAY.ordinal()) {
            mappingType = GlobalDBProxy.MAPPING_TYPE.google;
        } else if (type == BIND_TYPE.EMAIL.ordinal()) {
            mappingType = GlobalDBProxy.MAPPING_TYPE.email;
        } else if (type == BIND_TYPE.APPSTORE.ordinal()) {
            mappingType = GlobalDBProxy.MAPPING_TYPE.AppStore;
            valueName = GlobalDBProxy.MAPPING_TYPE.AppStore.toString();
        } else if (type == BIND_TYPE.QQ.ordinal()) {
            mappingType = GlobalDBProxy.MAPPING_TYPE.qq;
            valueName = GlobalDBProxy.MAPPING_TYPE.qq.toString();
        } else if (type == BIND_TYPE.WEIXIN.ordinal()) {
            mappingType = GlobalDBProxy.MAPPING_TYPE.weixin;
            valueName = GlobalDBProxy.MAPPING_TYPE.weixin.toString();
        } else if (type == BIND_TYPE.WEIBO.ordinal()) {
            mappingType = GlobalDBProxy.MAPPING_TYPE.weibo;
            valueName = GlobalDBProxy.MAPPING_TYPE.weibo.toString();
        } else if (type == BIND_TYPE.VK.ordinal()) {
            mappingType = GlobalDBProxy.MAPPING_TYPE.vk;
            valueName = GlobalDBProxy.MAPPING_TYPE.vk.toString();
        } else if (type == BIND_TYPE.MSLIVE.ordinal()) {
            mappingType = GlobalDBProxy.MAPPING_TYPE.mslive;
            valueName = GlobalDBProxy.MAPPING_TYPE.mslive.toString();
        } else {
            //其余绑定方式不允许解除绑定
            return;
        }
//        GlobalDBProxy.updateAccountByType(account, mappingType, "", valueName);
    }

    public static GlobalDBProxy.MAPPING_TYPE toMappingType(String type) {
        GlobalDBProxy.MAPPING_TYPE mappingType;
        if (type.equals("google")) {
            mappingType = GlobalDBProxy.MAPPING_TYPE.google;
        } else if (type.equals("facebook")) {
            mappingType = GlobalDBProxy.MAPPING_TYPE.facebook;
        } else if (type.equals("mail")) {
            mappingType = GlobalDBProxy.MAPPING_TYPE.email;
        } else if (type.equals("deviceId")) {
            mappingType = GlobalDBProxy.MAPPING_TYPE.device;
        } else if (type.equals("AppStore")) {
            mappingType = GlobalDBProxy.MAPPING_TYPE.AppStore;
        } else if (type.equals("qq")) {
            mappingType = GlobalDBProxy.MAPPING_TYPE.qq;
        } else if (type.equals("weixin")) {
            mappingType = GlobalDBProxy.MAPPING_TYPE.weixin;
        } else if (type.equals("weibo")) {
            mappingType = GlobalDBProxy.MAPPING_TYPE.weibo;
        } else if (type.equals("vk")) {
            mappingType = GlobalDBProxy.MAPPING_TYPE.vk;
        } else if (type.equals("mslive")) {
            mappingType = GlobalDBProxy.MAPPING_TYPE.mslive;
        } else {
            throw new IllegalArgumentException("");
        }
        return mappingType;
    }

    public static String getFirstBindMail(String type){
        GlobalDBProxy.MAPPING_TYPE mappingType = toMappingType(type);
        String mailId = null;
        if(!SwitchConstant.BindSwitch.isSwitchOpen()) {
            return mailId;
        }
        Map<String, String> bindMap = GameConfigManager.getItem("sys_config", "BindSwitch");
        switch (mappingType) {
            case google:
                mailId = Constants.BIND_FIRST_GOOGLE_MAIL_ID;
                break;
            case facebook:
                if(bindMap.get("FB_BindSwitch").equals("1")) {
                    mailId = bindMap.get("FB_mail_bind");
                }
                break;
            case AppStore:
                mailId = Constants.BIND_FIRST_APPSTORE_MAIL_ID;
                break;
            case weibo:
                if(bindMap.get("WB_BindSwitch").equals("1")) {
                    mailId = bindMap.get("WB_mail_bind");
                }
                break;
            case weixin:
                if(bindMap.get("WX_BindSwitch").equals("1")) {
                    mailId = bindMap.get("WX_mail_bind");
                }
                break;
            case vk:
                mailId = Constants.BIND_FIRST_VK_MAIL_ID;
                break;
        }
        return mailId;
    }

    public static String getFirstShareMail(String type){
        GlobalDBProxy.MAPPING_TYPE mappingType = toMappingType(type);
        String mailId = null;
        if(!SwitchConstant.BindSwitch.isSwitchOpen()) {
            return mailId;
        }
        Map<String, String> bindMap = GameConfigManager.getItem("sys_config", "BindSwitch");
        switch (mappingType) {
            case facebook:
                if(bindMap.get("FB_BindSwitch").equals("1")) {
                    mailId = bindMap.get("FB_mail_share");
                }
                break;
            case weibo:
                if(bindMap.get("WB_BindSwitch").equals("1")) {
                    mailId = bindMap.get("WB_mail_share");
                }
                break;
            case weixin:
                if(bindMap.get("WX_BindSwitch").equals("1")) {
                    mailId = bindMap.get("WX_mail_share");
                }
                break;
        }
        return mailId;
    }

    public static ISFSArray getGlobalAccountByType(String type, String account) {
        ISFSArray array = new SFSArray();
        ISFSObject obj = GlobalDBProxy.selectLastUsedAccount(AccountService.toMappingType(type), account).orNull();
        if (obj != null) {
            array.addSFSObject(obj);
        }
        return array;
    }

    public static void updateGlobalAccountByType(String uid, String type,
                                                 String account, String accountName, String pass) {
        GlobalDBProxy.MAPPING_TYPE mappingType = toMappingType(type);
        if (mappingType == GlobalDBProxy.MAPPING_TYPE.email) {
            accountName = CommonUtils.makeMD5(pass);
//        }else{
//            byte[] s = null;
//            try {
//                s = accountName.getBytes("utf-8");
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }
//
//            if (s != null) {
//                accountName= Base64.getEncoder().encodeToString(s);
//            }
        }
        GlobalDBProxy.updateAccountByType(uid, mappingType, account, accountName);
    }



}
