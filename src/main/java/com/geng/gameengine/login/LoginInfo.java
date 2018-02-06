/**
 * @author: lifangkai@elex-tech.com
 * @date: 2013年11月13日 下午10:18:06
 */
package com.geng.gameengine.login;


import com.geng.core.data.ISFSObject;
import com.geng.service.AccountService;
import com.geng.utils.distributed.GlobalDBProxy;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * 登录时客户端信息
 */
public class LoginInfo implements Serializable {
    private static final Logger logger = LoggerFactory.getLogger(LoginInfo.class);
    private static final String BAN_USER = "user.ban";
    private static final String CLEAR_USER = "user.cls";
    private static final String CONCURRENT_LOGIN = "user.same";
    /**
     * login
     */
    private String uuid;
    private String gameUid;
    private String clientVersion;
    /**
     * register,include login
     */
    private String gcmRegisterId;
    private String referrer;
    private String afUID;
    private String fromCountry;
    private String country = "8000";
    private String platform; //0:ios;1:android
    private String lang;
    private String phone_model;
    private String phone_version;
    private String phone_width;
    private String phone_height;
    private String deviceId;
    private String newDeviceId; //ios replace old deviceId
    private String gaid;
    private String pf;
    private String pfId;
    private String googlePlayAccount;
    private String gmail;
    private String terminal;
    private Integer gmLogin;
    private String securityCode;
    private String packageName;
    private String ip;
    private boolean isHDLogin;
    private String phoneDevice;
    private boolean isForceUpdate;
    private String pfSession;//客户端和渠道的会话标识
    private String recallId;   //客户端deep link promotion id

    private String loginParams;

    public enum AppUpdateType {
        NO, SUGGEST, FORCE
    }

    public LoginInfo(ISFSObject loginData, String ip) {
        this.loginParams = loginData.toJson();
        this.ip = ip;
        uuid = loginData.getUtfString("uuid");
        deviceId = loginData.getUtfString("deviceId");
        newDeviceId = loginData.getUtfString("newDeviceId");
        uuid = StringUtils.isBlank(uuid) ? generateUUid(deviceId) : uuid;
        gameUid = loginData.getUtfString("gameUid");
        clientVersion = loginData.getUtfString("appVersion");
        gcmRegisterId = loginData.getUtfString("gcmRegisterId");
        referrer = loginData.getUtfString("referrer");
        afUID = loginData.getUtfString("afUID");
        //前端根据系统字来判断国家
        fromCountry = loginData.getUtfString("fromCountry");
        platform = loginData.getUtfString("platform");
        lang = loginData.getUtfString("lang");
        gaid = loginData.getUtfString("gaid");
        pf = loginData.getUtfString("pf");
        pfId = loginData.getUtfString("pfId");
        gmLogin = loginData.getInt("gmLogin"); //是不是gm账号通过changeServer切过来的
        terminal = loginData.getUtfString("terminal");
        securityCode = loginData.getUtfString("SecurityCode");
        packageName = loginData.getUtfString("packageName");
        isHDLogin = loginData.containsKey("isHDLogin") && "1".equals(loginData.getUtfString("isHDLogin"));
        phoneDevice = loginData.getUtfString("phoneDevice");
        pfSession = loginData.getUtfString("pfSession");
        recallId = loginData.getUtfString("recallId");
        isForceUpdate = false;
//        useGMailOrFBName(loginData);
        parsePhone(loginData);
    }

    /**
     * 登录返回
     *
     * @param
     * @throws
     */
    /*public void getLoginInfo(ISFSObject params, ISFSObject loginRetObj) throws SFSLoginException {
        checkLoginKey(params);
        checkConcurrentLogin();
        checkTestServerRegisterCountDaily();
        boolean gmFlag = checkLogin();
        checkCheat(gmFlag);
        checkPackageName(packageName);
        getVersionInfo(gmFlag, loginRetObj);
        loginParams = null;
    }*/

//    private void checkConcurrentLogin() throws SFSLoginException {
        /*if (StringUtils.isBlank(gameUid)) {
            return;
        }
        boolean isLock = ConcurrentLock.getInstance().lockWithExpiredTime(ConcurrentLock.LockType.SAME_ACCOUNT_LOGIN, gameUid, Constants.CONCURRENT_LOGIN_LOCK_TIME);
        if (!isLock) {
            logger.error(COKLoggerFactory.formatLog(ExceptionMonitorType.LOGIN, COKLoginExceptionType.CURRENT, gameUid, ip, toString()));
            throw new SFSLoginException(CONCURRENT_LOGIN);
        }
        UserProfile preUserProfile = GameEngine.getInstance().getPresentUserProfile(gameUid);
        if (preUserProfile != null && preUserProfile.getSfsUser() != null) {
            String preUserDeviceId = null;
            if (preUserProfile.getLoginInfo() != null) {
                preUserDeviceId = preUserProfile.getLoginInfo().getDeviceId();
            }
            if (preUserDeviceId != null && deviceId != null && !preUserDeviceId.equals(deviceId)) {
                if (gmLogin != null && gmLogin == 1) {
                    GameEngine.getInstance().pushMsg("push.user.gm.off", null, preUserProfile);
                } else {
                    GameEngine.getInstance().pushMsg("push.user.off", null, preUserProfile);
                }
                preUserProfile.getSfsUser().disconnect(ClientDisconnectionReason.KICK);
            }
        }*/
//    }

/*   private boolean checkLogin() throws SFSLoginException {
        boolean gmFlag = false;
        boolean checkAccountActive = Constants.SERVER_ID != 0;
        if (!StringUtils.isBlank(gameUid)) {
            ISFSArray userArr = UserProfileDao.selectUserBanTime(gameUid);
            if (userArr == null || userArr.size() == 0) {
                raiseUserException(GameExceptionCode.ACCOUNT_NOT, COKLoginExceptionType.ACCOUNT_NOT_EXIST);
            }
            ISFSObject userObj = userArr.getSFSObject(0);
            if (StringUtils.isBlank(pf)) {
                pf = userObj.getUtfString("pf");
            }
            if (userObj.containsKey("banTime") && System.currentTimeMillis() < userObj.getLong("banTime")) {
                long banTime = userObj.getLong("banTime");
                if (banTime == Long.MAX_VALUE) {
                    raiseUserException(GameExceptionCode.ACCOUNT_NEED_REQUEST_SERVER_LIST, COKLoginExceptionType.MOVED);
                } else {
//                    if(CommonUtils.compareVersion(clientVersion, "1.1.12") <= 1){ //1.1.12版本及以后
//                        String errorCode = GlobalDBProxy.selectBanReason(gameUid);
//                        raiseGMBanLoginException(errorCode, userObj.getLong("banTime"));
//                    }else{
//                        raiseBanLoginException(COKLoginExceptionType.BAN_TIME, userObj.getLong("banTime"));
//                    }
                    if(Versions.Compare(userObj.getUtfString("appVersion"),Versions.VERSION_2_1_2)>=0)
                    {
                        raiseBanLoginExceptionWithInfo(COKLoginExceptionType.BAN_TIME, userObj.getLong("banTime"), userObj.getUtfString("name"), userObj.getInt("level"));
                    }else
                    {
                        raiseBanLoginException(COKLoginExceptionType.BAN_TIME, userObj.getLong("banTime"));
                    }

                }
            }
            if (userObj.containsKey("gmFlag")) {
                gmFlag = userObj.getInt("gmFlag") == 1 ? true : false;
            }
            checkAccountActive = false;
        }
        //自动绑定的账号验证
        if (GlobalDBProxy.isAutoBind(pf)){
            //pfId不允许为空
            if (StringUtils.isBlank(pfId)) {
                raiseUserException(GameExceptionCode.ACCOUNT_NEED_REQUEST_SERVER_LIST, COKLoginExceptionType.PF_ID_BLANK);
            }
            //创建新帐号判断是否其他区已有帐号
            if(StringUtils.isBlank(gameUid)){
                ISFSArray bindedAccountArr = GlobalDBProxy.selectAccountList(GlobalDBProxy.getAutoBindMappingType(pf), pfId);
                if (bindedAccountArr != null && bindedAccountArr.size() > 0) {
                    raiseUserException(GameExceptionCode.ACCOUNT_NEED_REQUEST_SERVER_LIST, COKLoginExceptionType.ACCOUNT_EXIST_PF_ID);
                }
                if(GlobalDBProxy.isStrongBind(pf)){
                    //强绑定模式不验证
                    //弱绑定模式要验证
                    checkAccountActive = false;
                }
            }else if (GlobalDBProxy.isStrongBind(pf) && !GlobalDBProxy.checkAccountBind(gameUid, pf, pfId)) {
                //强绑定模式帐号验证
                raiseUserException(GameExceptionCode.ACCOUNT_NEED_REQUEST_SERVER_LIST, COKLoginExceptionType.UID_PF_ID_NOT_MAPPING);
            }
        }
        //创建新号时验证该设备最后一次登陆帐号的active状态
        if (checkAccountActive) {
            ISFSObject userAccount;
            userAccount = GlobalDBProxy.selectLastUsedAccount(GlobalDBProxy.MAPPING_TYPE.device, deviceId).orNull();
            if (userAccount != null && userAccount.getInt("active") == 0) {
                if (StringUtils.isBlank(gameUid) || !gameUid.equals(userAccount.getUtfString("gameUid")))
                    raiseUserException(GameExceptionCode.ACCOUNT_NEED_REQUEST_SERVER_LIST, COKLoginExceptionType.ACCOUNT_EXIST_DEVICE);
            }
        }
        return true;
    }*/

/*
    private void getVersionInfo(boolean gmFlag, ISFSObject loginRetObj) throws SFSLoginException {

    }

    public boolean checkForceUpdate() {
        return isForceUpdate;
    }

    private void checkLoginKey(ISFSObject params) throws SFSLoginException {

    }

    private void checkCheat(boolean gmFlag) throws SFSLoginException {

    }

    private void checkPackageName(String packageName) throws SFSLoginException {

    }

    private void checkTestServerRegisterCountDaily() throws SFSLoginException {

    }

    public void raiseGMBanLoginException(String errorCode, long banExpiredTime) throws SFSLoginException{

    }

    public void raiseBanLoginExceptionWithInfo(COKLoginExceptionType loginExceptionType, long banExpiredTime,String name,int buildingLv) throws SFSLoginException {

    }

    public void raiseBanLoginException(COKLoginExceptionType loginExceptionType, long banExpiredTime) throws SFSLoginException {

    }

    public void raiseBadSecurityCodeException(COKLoginExceptionType loginExceptionType, long expiredTime) throws SFSLoginException {

    }

    public void raisePasswdException(String exceptionCode, int faliCount)throws SFSLoginException  {


    }

    public void raiseUserException(GameExceptionCode exceptionCode, COKLoginExceptionType loginExceptionType) throws SFSLoginException {
        raiseUserException(CLEAR_USER, exceptionCode, loginExceptionType);
    }

    private void raiseUserException(String errorKey, GameExceptionCode exceptionCode, COKLoginExceptionType loginExceptionType) throws SFSLoginException {

    }
*/

    private void parsePhone(ISFSObject loginData) {
        if (loginData.containsKey("mt")) {
            String mt = loginData.getUtfString("mt");
            if ("0".equals(platform)) {
                //CommonUtils.compareVersion(clientVersion, "1.7.5") == 0
//                if(Versions.Compare(clientVersion,Versions.VERSION_1_7_5) == 1 ){//大于1.7.5
                    String[] mtArr = StringUtils.split(mt, "|");
                    if (mtArr.length == 2) {
                        phone_model = mtArr[1];
                        phone_version = mtArr[0];
                    }
//                }else{
//                    String[] mtArr = StringUtils.split(mt, " ");
//                    if (mtArr.length == 2) {
//                        phone_model = mtArr[0];
//                        phone_version = mtArr[1];
//                    }
//                }
            } else if ("1".equals(platform)) {
                String[] mtArr = StringUtils.split(mt, ",");
                if (mtArr.length == 3) {
                    phone_model = StringUtils.split(mtArr[0], ":")[1];
                    phone_version = StringUtils.split(mtArr[2], ":")[1];
                }
            }
        }
        if (loginData.containsKey("phone_screen")) {
            String[] screenArr = StringUtils.split(loginData.getUtfString("phone_screen"), "*");
            phone_width = screenArr[0];
            phone_height = screenArr[1];
        }
    }

/*    private void useGMailOrFBName(ISFSObject loginData) {
        String googlePlayStr = loginData.getUtfString("googlePlay");
        boolean isWebClient = isWebClient();
        if (!StringUtils.isBlank(googlePlayStr) || isWebClient) {
            gmail = googlePlayStr;
            if (StringUtils.isBlank(gameUid)) {
                String gpName;
                if (isWebClient) {
                    gpName = loginData.getUtfString("FBName") == null ? "" : loginData.getUtfString("FBName");
                } else {
                    int atIndex = StringUtils.indexOf(googlePlayStr, '@');
                    gpName = StringUtils.substring(googlePlayStr, 0, atIndex);
                }
                ISFSArray nameArr = UserProfileDao.selectNameByName(gpName);
                if (nameArr.size() == 0) {
                    int stat = ServerCommunicationService.lock(ServerCommunicationService.USER_NAME, gpName);
                    try {
                        if (stat == 1)
                            googlePlayAccount = gpName;
                    } catch (Exception e) {
                        LoggerUtil.getInstance().recordException(e);
                    } finally {
                        ServerCommunicationService.unlock(ServerCommunicationService.USER_NAME, gpName);
                    }
                }
            }
        }
    }*/

    public boolean isWebClient() {
        return "facebook".equals(terminal);
    }

    private boolean isIOS() {
        return GlobalDBProxy.MAPPING_TYPE.AppStore.toString().equals(pf);
    }


    /**
     * 根据设备ID和注册时间生成UUID
     */
    public static String generateUUid(String deviceId) {
        return deviceId + System.currentTimeMillis();
    }

    /**
     * ios用户，更新deviceId
     *
     * @return
     */
    public boolean isUpdateIOSPlayerDeviceId() {
        return StringUtils.isNotBlank(gameUid)
                && GlobalDBProxy.MAPPING_TYPE.AppStore.toString().equals(pf)
                && StringUtils.isNotBlank(deviceId)
                && StringUtils.isNotBlank(newDeviceId)
                && !deviceId.equals(newDeviceId);
    }


    /**
     * 强绑定渠道登录时进行验证，并获取pfId
     */
/*    private void checkAutoBindLogin() throws SFSLoginException{

    }*/

    /**
     * @return the uuid
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * @param uuid the uuid to set
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getGameUid() {
        return gameUid;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public String getGcmRegisterId() {
        return gcmRegisterId;
    }

    public String getCountry() {
        return country ;
    }

    public String getPlatform() {
        return platform;
    }

    public String getLang() {
        return lang;
    }

    public String getDeviceId() {
        return deviceId;
    }

    /**
     * @return the phone_model
     */
    public String getPhone_model() {
        return phone_model;
    }

    /**
     * @return the phone_version
     */
    public String getPhone_version() {
        return phone_version;
    }

    /**
     * @return the phone_width
     */
    public String getPhone_width() {
        return phone_width;
    }

    /**
     * @return the phone_height
     */
    public String getPhone_height() {
        return phone_height;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getGaid() {
        return gaid;
    }

    public String getPf() {
        return pf;
    }

    public String getPfId() {
        return pfId;
    }

    public String getReferrer() {
        return referrer;
    }

    public String getAfUID(){
        return afUID;
    }

    public String getFromCountry() {
        return fromCountry;
    }

    public String getGooglePlayAccount() {
        return googlePlayAccount;
    }

    public String getGmail() {
        return gmail;
    }

    public String getTerminal() {
        return terminal;
    }

    public String getNewDeviceId() {
        return newDeviceId;
    }

    private boolean isGooglePlay() {
        return AccountService.APP_PF_GOOGLE.equals(pf) || AccountService.APP_PF_GOOGLE_NEW.equals(pf);
    }

    private boolean isAppStore() {
        return "AppStore".equals(pf);
    }

    private boolean isRegister() {
        return StringUtils.isBlank(gameUid);
    }

    @Override
    public String toString() {
        return loginParams != null ? loginParams : "";
    }

    public boolean isHDLogin() {
        return isHDLogin;
    }

    public void setIp(String ip) {
        if(StringUtils.isNotBlank(ip)) {
            this.ip = ip;
        }
    }

    public String getIp() {
        return ip;
    }
    public String getPackageName() {
        return packageName;
    }

    public String getPfSession() {
        return pfSession;
    }

    public String getRecallId() {
        return recallId;
    }

    public void setRecallId(String recallId) {
        this.recallId = recallId;
    }
}
