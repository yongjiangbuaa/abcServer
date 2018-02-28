/**
 * @author: lifangkai@elex-tech.com
 * @date: 2013年9月6日 下午2:30:17
 */
package com.geng.gameengine;


import com.geng.core.GameEngine;
import com.geng.core.SFSException;
import com.geng.exceptions.COKException;
import com.geng.exceptions.ExceptionMonitorType;
import com.geng.exceptions.GameExceptionCode;
import com.geng.gameengine.account.AccountDeviceMapping;
import com.geng.gameengine.cross.SharedUserInfo;
import com.geng.gameengine.cross.SharedUserService;
//import com.geng.gameengine.friend.FriendManager;
import com.geng.gameengine.login.COKLoginExceptionType;
import com.geng.gameengine.login.LoginInfo;
import com.geng.gameengine.mail.MailServicePlus;
import com.geng.gameengine.mail.MailSrcFuncType;
//import com.geng.gameengine.world.finalize.WorldClearDeadAccountTask;
//import com.geng.handlers.requesthandlers.mod.common.ModService;
//import com.geng.puredb.dao.UserProfileDao;
import com.geng.handlers.Login;
import com.geng.puredb.model.*;
import com.geng.utils.*;
import com.geng.utils.distributed.GlobalDBProxy;
import com.geng.utils.distributed.SqlParamsObj;
import com.geng.utils.myredis.R;
import com.geng.utils.properties.PropertyFileReader;
import com.geng.utils.xml.GameConfigManager;
import com.google.common.base.Optional;
//import com.geng.core.User;
import com.geng.core.data.ISFSArray;
import com.geng.core.data.ISFSObject;
import com.geng.core.data.SFSObject;
//import com.smartfoxserver.v2.exceptions.SFSException;
//import com.smartfoxserver.v2.exceptions.SFSLoginException;
//import com.smartfoxserver.v2.extensions.ExtensionLogLevel;
//import com.geng.gameengine.login.LoginStrategy;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 用户服务类
 */
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private static String namePrefix;
    private static AtomicLong defaultNameIndex = new AtomicLong();
    private static final String RESOURCE_DEALER_REGISTER_FREQUENCY_KEY_PREFIX  = "user:register:freq:";
    private static final Object RESOURCE_DEALER_REGISTER_FREQUENCY_LOCK = new Object();
    static {
        namePrefix = PropertyFileReader.getItem("name_prefix", "Empire");
        defaultNameIndex = new AtomicLong(UserProfile.getMaxNameIndex());
    }

    public static String[] generateDefaultUserName(){
        return generateDefaultUserName(null);
    }
    /**
     * 0:name; 1:uid
     *
     * @return
     */
    public static String[] generateDefaultUserName(LoginInfo loginInfo) {
        if (defaultNameIndex.get() == 0) {
            defaultNameIndex = new AtomicLong(UserProfile.getMaxNameIndex());
        }
        long time = System.currentTimeMillis();
        long serverStamp = time % 10000;
        String[] nameIdArr = new String[2];
        long id = defaultNameIndex.incrementAndGet();
        String serverId = String.valueOf(Constants.SERVER_ID);
        StringBuilder sb = new StringBuilder();
//        nameIdArr[0] = sb.append(namePrefix).append(Long.toHexString(id)).append(Long.toHexString(serverStamp >> 3)).append(serverId).toString();
        // name
        if(loginInfo != null && SwitchService.isPublishSwithOn() && StringUtils.equals(loginInfo.getFromCountry(), "CN")) {//版署开关打开且是中国用户
            nameIdArr[0] = sb.append("领主").append(0).append(id).append(serverId).toString();
        }else{
            nameIdArr[0] = sb.append(namePrefix).append(0).append(Long.toHexString(id)).append(serverId).toString();
        }
        sb.delete(0, sb.length());
        // uid
        sb.append("1").append(String.format("%06d", id)).append(String.format("%04d", serverStamp));
        for (int i = 0; i < Constants.SERVER_ID_MAX - serverId.length(); i++) {
            sb.append("0");
        }
        sb.append(serverId);
        nameIdArr[1] = sb.toString();
        return nameIdArr;
    }

    /**
     * 登录处理 tcp/udp短连接
     * @param deviceId
     * @param data
     * @return
     * @throws COKException
     */
    public static UserProfile handleLogin(String deviceId,String data) throws COKException{
        LoginInfo loginInfo= transData2LoginInfo(data);
        return  handleLogin(loginInfo,"");
    }

    private static LoginInfo transData2LoginInfo(String data) {
        return null;
    }


    /**
     * 登录处理 tcp长连接
     *
     * @throws
     */
    //User对象包裹了连接。UserProfile持有User。间接持有连接。
    // 包含连接状态及用户属性  在短连接甚至无连接服务里不用,只在使用保持状态的TCP连接时使用。
    //连接时认为是在线状态。

//    public static UserProfile handleLogin(User user, LoginInfo loginInfo, String address) throws SFSException {
//    }


    //不包含连接对象的方式  没有登录状态。 不缓存登录对象
    public static UserProfile handleLogin(LoginInfo loginInfo, String address) throws COKException {
        UserProfile userProfile;
        String gameUid = loginInfo.getGameUid();
        boolean isRegister = false;
        if (StringUtils.isBlank(gameUid)) {
            isRegister = true;
            userProfile = register(loginInfo, address);
            new SharedUserService().updateUserInfo(userProfile);//注册时把其放入resis(gobal),防止在其未退出时其它使用redis查该用户的信息而找不到
        } else {
//            userProfile = UserProfile.getLoggedUserProfile(gameUid, false, loginInfo);//TODO 从缓存层取已注册过的用户
            userProfile = UserProfile.getWithUid(gameUid);
            if (userProfile == null) {
                throw new COKException(GameExceptionCode.UID_NOT_EXIST,"uid not exist!!");
//                throw new SFSException(String.format("load user:%s error", gameUid));
            }
            userProfile.setLoginInfo(loginInfo);
        }
        saveLoginInfo(userProfile);//In Redis
        GameEngine.getInstance().addUserProfile(userProfile);
        userProfile.setLoginTime(StatLogin.writelog(userProfile, address));

        //新手逻辑.....
        if(isRegister){

//            ItemManager.addItem(userProfile, GoodsType.WORLD_NEW_POINT_MV_CITY_V2.getGoodsId(), 2, 0, true, LoggerUtil.GoodsGetType.UPGRADE_BUILDING);
//            MailServicePlus.sendMailByMailXml(userProfile.getUid(), "11629", null, null, MailSrcFuncType.newUserItem);

            //分渠道新手逻辑
            String pf=userProfile.getPf();
            if(SwitchConstant.ChinaUserMailSwitch.isSwitchOpen()) {

            }
        }
        return userProfile;
    }



    public static void updateIOSPlayerDeviceId(UserProfile userProfile, ISFSObject loginRetObj) {
        LoginInfo loginInfo = userProfile.getLoginInfo();
        if(loginInfo == null || !loginInfo.isUpdateIOSPlayerDeviceId()) {
            return;
        }
        String oldDeviceId = loginInfo.getDeviceId();
        String newDeviceId = loginInfo.getNewDeviceId();
        ISFSArray accountsWithOld = GlobalDBProxy.selectAccountList(GlobalDBProxy.MAPPING_TYPE.device, oldDeviceId);
        ISFSArray accountsWithNew = GlobalDBProxy.selectAccountList(GlobalDBProxy.MAPPING_TYPE.device, newDeviceId);
        boolean isOldBlank = accountsWithOld == null || accountsWithOld.size() == 0;
        boolean isNewBlank = accountsWithNew == null || accountsWithNew.size() == 0;
        if(isOldBlank && !isNewBlank) {
            loginRetObj.putBool("updateDeviceId", true);
            return;
        }
        List<SqlParamsObj> sqlParamsList = new ArrayList<>();
        if(!isOldBlank && isNewBlank) {
            List<String> mappingUids = new ArrayList<>();
            for(int i=0; i<accountsWithOld.size(); i++) {
                ISFSObject object = accountsWithOld.getSFSObject(i);
                String gameUid = object.getUtfString("gameUid");
                if(StringUtils.isNotBlank(gameUid)) {
                    mappingUids.add(gameUid);
                    List<SqlParamsObj> uidSqlParamsList = GlobalDBProxy.toUpdateAccountByType(gameUid, GlobalDBProxy.MAPPING_TYPE.device, newDeviceId, null, oldDeviceId);
                    if(!uidSqlParamsList.isEmpty()) {
                        sqlParamsList.addAll(uidSqlParamsList);
                    }
                    logger.info("IOS device change, {} from {} to {} Begin", gameUid, oldDeviceId, newDeviceId);
                }
            }
            boolean flag = SFSMysql.getInstance().executeUpdateGlobalWithTransaction(sqlParamsList);
            if(flag) {
                loginRetObj.putBool("updateDeviceId", true);
                for(String mappingUid : mappingUids) {
                    AccountDeviceMapping.addMapping(mappingUid, newDeviceId, AccountDeviceMapping.AccountDeviceMappingType.IOS_CHANGE);
                }
                logger.info("IOS device change, from {} to {} Successfully", oldDeviceId, newDeviceId);
            } else {
                logger.info("IOS device change, from {} to {} Fail", oldDeviceId, newDeviceId);
            }
        }
        if(!isOldBlank && !isNewBlank) {
            logger.info("IOS device change, from {} to {}, All is not blank", oldDeviceId, newDeviceId);
        }
    }

    /**
     * 缓存登录信息 主要用到deviceId获取
     * @param userProfile
     */
    private static void saveLoginInfo(UserProfile userProfile) {
        LoginInfo loginInfo = userProfile.getLoginInfo();
        if (loginInfo != null && StringUtils.isNotBlank(loginInfo.getDeviceId())) {
            final String loginInfoKey = "LOGIN_INFO" + Constants.SERVER_ID;
            ISFSObject info = SFSObject.newInstance();
            info.putUtfString("deviceId", loginInfo.getDeviceId());
//            new RedisSession()
            R.Local().hsetnx(loginInfoKey, userProfile.getUid(), info.toJson());
        }
    }
        /**
     * 处理用户注册
     *
     * @return
     */
    private static UserProfile register(LoginInfo loginInfo, String ipAddress) throws COKException {
        SqlSession session = MyBatisSessionUtil.getInstance().getBatchSession();
        UserProfile userProfile = null;
        try {
            userProfile =  UserProfile.newInstance(session, loginInfo)
                    .onRegister(session);
            StatReg.writelog(userProfile, session, loginInfo, ipAddress);
            StatAf.writelog(userProfile, session, loginInfo);
            session.commit();
            userProfile.setLoginInfo(loginInfo);
            UserService.insertGlobalAccount(userProfile);
            AccountDeviceMapping.addMapping(userProfile.getUid(), loginInfo.getDeviceId(), AccountDeviceMapping.AccountDeviceMappingType.REGISTER);
            UserService.insertPhoneStatInfo(userProfile);
            userProfile.setLastUpdateTime();
        } catch (Exception e) {
            session.rollback();
            COKLoggerFactory.monitorException(loginInfo.toString(), ExceptionMonitorType.REGISTER, COKLoggerFactory.ExceptionOwner.COMMON, e);
            throw new COKException(GameExceptionCode.INVALID_OPT,"register error");
        } finally {
            session.close();
            session = null; //huangyuanqiang
            if (userProfile != null && GameEngine.getInstance().isTestServer()) {
//                RedisSession rs = null;
                try {
//                    rs = new RedisSession(false);
                    String normalRegister = R.Local().hGet(Constants.TEST_SERVER_REGISTER_NORMAL, loginInfo.getDeviceId());
                    if (normalRegister == null) {
//                        new NPCPlayer(userProfile);
                        R.Local().hincrBy(Constants.TEST_SERVER_REGISTER_COUNT_DAILY, String.valueOf(Constants.SERVER_ID), 1);
//                        userProfile.getAchievementManager().addNewOnStrongHolderLevel();
//                        userProfile.getAchievementManager().triggerAchievements(null);
                    }
                } catch (Exception e) {
                    logger.error("set npc player error", e);
                } finally {
//                    if (rs != null) {
//                        rs.close();
//                    }
                }
            }
        }
        return userProfile;
    }

    public static Optional<String> getName(String uid) {
        String name;
        UserProfile userProfile = GameEngine.getInstance().getPresentUserProfile(uid);
        if (userProfile != null) {
            name = userProfile.getName();
        } else {
            name = UserProfile.selectName(uid);
        }
        return Optional.fromNullable(name);
    }

    /**
     * 返回玩家的name、头像和战斗力
     *
     * @param uid
     * @return
     */
    public static ISFSObject getUserNamePicAndPower(String uid) {
        ISFSObject nppObj = new SFSObject();
        UserProfile userProfile = GameEngine.getInstance().getPresentUserProfile(uid);
        String name = "", pic = "", allianceSimpleName = "";
        int level = 0;
        long power = 0;
        int picVer = 0 ;
        if (userProfile != null ){//&& userProfile.getPlayerInfo() != null) {
            name = userProfile.getName();
            level = userProfile.getLevel();
            pic = userProfile.getPic();
            picVer = userProfile.getPicVer();
            power = 0;//userProfile.getPlayerInfo().getPower();
            Optional<String> optionalAllianceName = userProfile.getAllianceSimpleName();
            if (optionalAllianceName.isPresent()) {
                allianceSimpleName = optionalAllianceName.get();
            }
        } else {
            ISFSArray array = SFSMysql.getInstance().query("select u.name, u.level, u.pic, u.picVer, p.power, a.abbr from userprofile u inner join playerinfo p on u.uid = p.uid" +
                    " left join alliance a on u.allianceId = a.uid where u.uid = ?", new String[]{uid});
            if (array != null && array.size() == 1) {
                ISFSObject obj = array.getSFSObject(0);
                if (obj.containsKey("name")) {
                    name = obj.getUtfString("name");
                }
                if (obj.containsKey("level")) {
                    level = obj.getInt("level");
                }
                if (obj.containsKey("pic")) {
                    pic = obj.getUtfString("pic");
                }
                if (obj.containsKey("picVer")) {
                    picVer = obj.getInt("picVer");
                }
                if (obj.containsKey("power")) {
                    power = obj.getLong("power");
                }
                if (obj.containsKey("abbr") && obj.getUtfString("abbr") != null) {
                    allianceSimpleName = obj.getUtfString("abbr");
                }
            }
        }
        nppObj.putUtfString("uid", uid);
        nppObj.putInt("level", level);
        nppObj.putUtfString("name", name);
        nppObj.putUtfString("pic", pic);
        nppObj.putInt("picVer", picVer);
        nppObj.putLong("power", power);
        nppObj.putUtfString("abbr", allianceSimpleName);
        return nppObj;
    }

    /*public static Optional<SharedUserInfo> selectUserNamePicAndPower(String uid) {
        Integer serverId = SharedUserService.selectServerId(uid).orNull();
        if(serverId == null) {
            return Optional.absent();
        }
        String sql = "select u.name, u.level as lv, u.pic, u.picVer, u.lang, u.offLineTime, p.power, a.abbr, ub.level from userprofile u inner join playerinfo p on u.uid = p.uid" +
                " inner join user_building ub on u.uid = ub.uid and ub.itemId = '" + UserBuilding.BuildingType.STRONGHOLD.getItemId() + "'" +
                " left join alliance a on u.allianceId = a.uid " +
                " where u.uid = ?";
        ISFSObject obj = null;
        if(serverId == Constants.SERVER_ID) {
            ISFSArray array = SFSMysql.getInstance().query(sql, new String[]{uid});
            if (array != null && array.size() == 1) {
                obj = array.getSFSObject(0);
            }
        } else {
            Map<String, Object> map = SFSMysql.getInstance().queryCrossServer(serverId, sql , uid );
            if(map != null) {
                obj = CommonUtils.map2SFSObj(map);
            }
        }
        SharedUserInfo userInfo = null;
        if (obj != null) {
            userInfo = new SharedUserInfo(uid, obj);
        }
        return Optional.fromNullable(userInfo);
    }**/

    private static void insertPhoneStatInfo(UserProfile userProfile) {
        LoginInfo loginInfo = userProfile.getLoginInfo();
        if (loginInfo != null) {
            SFSMysql.getInstance()
                    .execute(
                            "insert into stat_phone values(?,?,?,?,?)",
                            new Object[]{userProfile.getUid(), loginInfo.getPhone_model(),
                                    loginInfo.getPhone_version(),
                                    loginInfo.getPhone_width(),
                                    loginInfo.getPhone_height()});
        }
    }

    /**
     * 绑定账号
     *
     * @param userProfile
     * @param type
     * @param account
     * @return
     */
    public static ISFSObject bindAccount(UserProfile userProfile, int optType, String type,
                                         String account, String pass, String accountName) throws COKException {
        ISFSObject serverObj = new SFSObject();
        ISFSArray alreadyBindServer = AccountService.getGlobalAccountByType(type, account);
        if (alreadyBindServer.size() > 0 && (optType == 1 || optType == 2)) {
            ISFSObject record = alreadyBindServer.getSFSObject(0);
            if (type.equals("mail")) {
                if (record.getInt("emailConfirm") == null || record.getInt("emailConfirm") == 0) {
                    //邮箱未激活
                    serverObj.putInt("status", 1);
                    return serverObj;
                } else {
                    //邮箱已激活 但密码错误
                    String passMd5 = CommonUtils.makeMD5(pass);
                    if (!passMd5.equals(record.getUtfString("passmd5"))) {
                        serverObj.putInt("status", 2);
                        return serverObj;
                    }
                }
            }

            Map<String, HashMap<String, String>> serverMapList = new GameConfigManager("servers").getGroupMap(Constants.BIG_ZONE_ID);
            String server = record.getUtfString("server");
            HashMap<String, String> serverMap = serverMapList.get(server);
            if (serverMap != null) {
                for (Map.Entry<String, String> entry : serverMap.entrySet()) {
                    serverObj.putUtfString(entry.getKey(), entry.getValue());
                }
                serverObj.putUtfString("uuid", record.getUtfString("uuid"));
                String gameUid = record.getUtfString("gameUid");
                Map<String, Object> userMap = SFSMysql.getInstance().queryCrossServer(Integer.parseInt(server), "select banTime from userprofile where uid='" + gameUid + "'");
                if (userMap != null && userMap.containsKey("banTime") && ((Long.parseLong(userMap.get("banTime").toString()) - System.currentTimeMillis() - 60000L) > 0) ){
                    throw new COKException(GameExceptionCode.ACCOUNT_BIND_BAND, "account is band");
                }

                serverObj.putUtfString("gameUid", gameUid);
                serverObj.putUtfString("gameUserName",
                        record.getUtfString("gameUserName"));
                serverObj.putUtfString("gameUserLevel",
                        Integer.toString(record.getInt("gameUserLevel")));
                if (0 != record.getInt("active")) {
                    SFSMysql.getInstance().executeGlobal("UPDATE account_new SET active = 0 WHERE gameUid = ?", new Object[]{record.getUtfString("gameUid")});
                }
//                SFSMysql.getInstance().updateCrossServer(Integer.parseInt(server), "update userprofile set banTime = 0 where uid = ?", gameUid);
                AccountDeviceMapping.addMapping(gameUid, userProfile.getDeviceId(), AccountDeviceMapping.AccountDeviceMappingType.CHANGE);
            } else {
                LoggerUtil.getInstance().logBySFS(String.format("[%s] bind account, but %s is not int servers", userProfile.getUid(), server));
            }
        } else if (alreadyBindServer.size() > 0 && optType == 3) {
            ISFSObject record = alreadyBindServer.getSFSObject(0);
            String gameUid = record.getUtfString("gameUid");
            boolean flag = GlobalDBProxy.deleteAccount(gameUid);
            if (!flag) {
                throw new COKException(GameExceptionCode.INVALID_OPT, "delete batch error");
            }
            if (type.equals("google")) {
                insertGlobalAccount(userProfile, account, null, null, Constants.SERVER_ID);
            } else if (type.equals("facebook")) {
                insertGlobalAccount(userProfile, null, account, null, Constants.SERVER_ID);
            } else if( type.equals("AppStore")){
                insertGlobalAccount(userProfile, accountName, Constants.SERVER_ID);
            } else {
                LoggerUtil.getInstance().logBySFS( "");
            }
        } else if (optType == 1) {
            AccountService.updateGlobalAccountByType(userProfile.getUid(), type, account, accountName, pass);
            if (type.equals("mail")) {
                // FIXME: change mail content
                String serverMail = "test@gmail.com";
                String contents = "http://10.1.5.59:8080/gameservice/email/confirm?login=" + account + "&id=" + userProfile.getUid();
                String title = "COK-MailBind-Test";
//                EmailUtil.sendEmail(serverMail, account, title, contents, true);
            }
            if (type.equals("facebook")) {
//                FriendManager.getInstance().bindFaceBook(userProfile, account);
            }
            AccountService.sendRewardForFirstBindUser(userProfile, type);
        } else if (optType == 2) {
            throw new COKException(GameExceptionCode.ACCOUNT_BIND_NOT_EXISTS);
        } else if (optType == 3) {
            throw new COKException(GameExceptionCode.INVALID_OPT);
        }
        return serverObj;
    }

    public static String getDeviceId(UserProfile userProfile) {
        String deviceId = null;
        if (userProfile.getLoginInfo() != null) {
            deviceId = userProfile.getLoginInfo().getDeviceId();
        }
        if (StringUtils.isBlank(deviceId)) {
            final String loginInfoKey = "LOGIN_INFO" + Constants.SERVER_ID;
//            new RedisSession()
            String jsonInfo = R.Local().hGet(loginInfoKey, userProfile.getUid());
            if (StringUtils.isNotBlank(jsonInfo)) {
                ISFSObject info = SFSObject.newFromJsonData(jsonInfo);
                deviceId = info.getUtfString("deviceId");
            }
        }
        return deviceId;
    }

    /**
     * @param type 2为在新服开启新账号 3为在老服开启新账号
     */
    public static void changeNewAccount(UserProfile userProfile, int type) throws COKException {
        String gameUid = userProfile.getUid();
        ISFSArray retArray = SFSMysql.getInstance().queryGlobal("SELECT * FROM account_new WHERE gameUid = ?", new Object[]{gameUid});
        if (retArray.size() < 1) {
            throw new COKException(GameExceptionCode.INVALID_OPT, "no account in account_new");
        }
        //防止其他包登陆自动绑定的帐号start new game
        if(!GlobalDBProxy.canStartNewGame(gameUid))
            throw new COKException(GameExceptionCode.INVALID_OPT, "need to change pf account to changeNewAccount");
        //开始新游戏时，如果原账号没有任何绑定，并且大本<=6级，则清除城点
//        removeCityIfSmallAccount(userProfile);
        String deviceId = getDeviceId(userProfile);
        int deviceBindTimes = 0;
        ISFSArray accountUidArray = null;
        if (StringUtils.isNotBlank(deviceId)) {
            accountUidArray = GlobalDBProxy.selectAccountList(GlobalDBProxy.MAPPING_TYPE.device, deviceId);
            if (accountUidArray != null && accountUidArray.size() > 0) {
                for (int index = 0; index < accountUidArray.size(); index++) {
                    ISFSObject accountObj = accountUidArray.getSFSObject(index);
                    String uid = accountObj.getUtfString("gameUid");
                    if (!StringUtils.equals(uid, gameUid)) {
                        SFSMysql.getInstance().executeGlobal("UPDATE account_new SET active = ? WHERE gameUid = '" + uid + "'", new Object[]{type});
                    }
                    int server = accountObj.containsKey("server") ? Integer.parseInt(accountObj.getUtfString("server")) : -1;
                    if (-1 != server && SharedUserService.isCurrServer(server)) {
                        deviceBindTimes++;
                    }
                }
            }
        } else {
            deviceBindTimes = 1;
        }
        final String deviceMoveCityKey = "_DEVICE_MOVE_" + deviceId;
//        new RedisSession(true)
        String deviceMoveOutTimes = R.Local().get(Constants.SERVER_ID + deviceMoveCityKey);
        int moveOutTimes = StringUtils.isBlank(deviceMoveOutTimes) ? 0 :Integer.parseInt(deviceMoveOutTimes);
        if ((deviceBindTimes + moveOutTimes) >= Integer.parseInt(new GameConfigManager("item").getItem("account").get("k1"))) {
            if(!hasValidServer(accountUidArray)){
                throw new COKException(GameExceptionCode.INVALID_OPT, "account too much");
            }
            type = 2;
        }
        SFSMysql.getInstance().executeGlobal("UPDATE account_new SET active = ? WHERE gameUid = ?", new Object[]{type, gameUid});
//        RedisSession rs = new RedisSession(true);
        R.Local().hSet("NEW_ACCOUNT_RECORD", userProfile.getDeviceId(), "true"); //用户走注册流程时stat_reg中为1（开启新游戏）
        userProfile.setParseRegisterId(null);
        userProfile.update();
    }

    public static void changeNewName(UserProfile userProfile, String oldNickName, String nickName) throws COKException {
        String gameUid = userProfile.getUid();
        ISFSArray retArray = SFSMysql.getInstance().queryGlobal("SELECT * FROM account_new WHERE gameUid = ?", new Object[]{gameUid});
        if (retArray.size() < 1) {
            throw new COKException(GameExceptionCode.INVALID_OPT, "no account in account_new");
        }

        boolean success = GlobalDBProxy.updateAccountByType(gameUid, GlobalDBProxy.MAPPING_TYPE.name, nickName, null, oldNickName);
        if (!success) {
            throw new COKException(GameExceptionCode.INVALID_OPT);
        }
        userProfile.update();

        try {
           /* if(userProfile.getLoverKeepsakeManager() != null) {
                userProfile.getLoverKeepsakeManager().afterChangeNewName(nickName);
            }
            if(userProfile.getPuzzleManager() != null) {
                userProfile.getPuzzleManager().afterChangeNewName(nickName);
            }*/
        } catch (Exception e) {
            logger.error("change lover name error");
            COKLoggerFactory.monitorException("update puzzle cash sender name or lover name error", ExceptionMonitorType.OTHER, COKLoggerFactory.ExceptionOwner.SP, e);
        }

    }

    public static void updateGaid(UserProfile userProfile, String gaid) {
        String oldGaid = userProfile.getGaid();
        userProfile.setGaid(gaid);
        userProfile.update();
        GlobalDBProxy.updateAccountByType(userProfile.getUid(), GlobalDBProxy.MAPPING_TYPE.gaid, gaid, null, oldGaid);
    }

    private static void insertGlobalAccount(UserProfile userProfile) {
        LoginInfo loginInfo = userProfile.getLoginInfo();
        String faceBookAccount = null;
        String faceBookName = null;
        if (loginInfo != null && "facebook".equals(loginInfo.getPf())) {
            faceBookAccount = loginInfo.getDeviceId();
            if (StringUtils.isNotBlank(loginInfo.getGooglePlayAccount())) {
                faceBookName = loginInfo.getGooglePlayAccount();
            }
        }
        insertGlobalAccount(userProfile, null, faceBookAccount, faceBookName, Constants.SERVER_ID);
    }

    public static void insertGlobalAccount(UserProfile userProfile, int serverId) {
        insertGlobalAccount(userProfile, null, null, null, serverId);
    }

    private static void insertGlobalAccount(UserProfile userProfile, String googleAccount, String facebookAccount, String facebookAccountName, int serverId) {
        GlobalDBProxy.insertGlobalAccount(userProfile, googleAccount, facebookAccount, facebookAccountName, null, serverId);
    }

    private static void insertGlobalAccount(UserProfile userProfile, String gcName, int serverId){
        GlobalDBProxy.insertGlobalAccount(userProfile, null, null, null, gcName, serverId);
    }

    public static boolean moveGlobalData(String uid, int serverId) {
        ISFSArray arr = SFSMysql.getInstance().queryGlobal("select gameUid from account_new where gameUid = ?", new String[]{uid});
        if (arr == null || arr.size() == 0) {
            return false;
        }
        String sql = "update account_new set server = ? where gameUid = ?";
        return SFSMysql.getInstance().executeGlobal(sql, new Object[]{serverId, uid});
    }

    public static void updateGlobalAccount(UserProfile userProfile) {
        String executeSql = "update account_new set gameUserLevel = ?, lastTime = ? where gameUid = ?";
        SFSMysql.getInstance().executeGlobal(
                executeSql,
                new Object[]{userProfile.getLevel(),
                        System.currentTimeMillis(), userProfile.getUid()});
    }

    public static UserServerHelper selectGameUidAndServerGlobal(String userName) {
        ISFSArray array = GlobalDBProxy.selectAccount(userName, " order by lastTime desc limit 1");
        ISFSObject obj = GlobalDBProxy.orderByLastTimeLimitOne(array).orNull();
        if (obj == null) {
            return new UserServerHelper();
        }
        String gameUid = obj.getUtfString("gameUid");
        int server = obj.containsKey("server") ? Integer.parseInt(obj.getUtfString("server")) : Constants.SERVER_ID;
        return new UserServerHelper(gameUid, server);
    }

    public static Optional<PairUtil<String>> selectGameNameAndServerGlobal(String uid) {
        ISFSArray array = SFSMysql.getInstance().queryGlobal("select gameUserName, server from account_new where gameUid = ? order by lastTime desc limit 1", new String[]{uid});
        if (array == null || array.size() == 0) {
            return Optional.absent();
        }
        ISFSObject obj = array.getSFSObject(0);
        String gameUid = obj.getUtfString("gameUserName");
        String server = obj.containsKey("server") ? obj.getUtfString("server") : null;
        PairUtil<String> nameServerPair = new PairUtil<>(gameUid, server);
        return Optional.of(nameServerPair);
    }

    public static Optional<ISFSObject> selectGameNameAndServerAndDeviceGlobal(String uid) {
        ISFSArray array = SFSMysql.getInstance().queryGlobal("select gameUserName, server, deviceId from account_new where gameUid = ? order by lastTime desc limit 1", new String[]{uid});
        if (array == null || array.size() == 0) {
            return Optional.absent();
        }
        ISFSObject obj = array.getSFSObject(0);
        return Optional.of(obj);
    }

    public static Optional<String> selectServerGlobal(String uid) {
        ISFSArray array = SFSMysql.getInstance().queryGlobal("select server from account_new where gameUid = ? order by lastTime desc limit 1", new String[]{uid});
        if (array == null || array.size() == 0) {
            return Optional.absent();
        }
        ISFSObject obj = array.getSFSObject(0);
        String server = obj.containsKey("server") ? obj.getUtfString("server") : null;
        return Optional.of(server);
    }

    public static int getDeviceBindTimes(int serverId, String deviceId) {
        ISFSArray accountUidArray = GlobalDBProxy.selectAccountList(GlobalDBProxy.MAPPING_TYPE.device, deviceId);
        int ret = 0;
        if (accountUidArray != null && accountUidArray.size() > 0) {
            for (int index = 0; index < accountUidArray.size(); index++) {
                ISFSObject accountObj = accountUidArray.getSFSObject(index);
                int server = accountObj.containsKey("server") ? Integer.parseInt(accountObj.getUtfString("server")) : -1;
                if (-1 != server && server == serverId) {
                    ret++;
                }
            }
        }
        final String deviceMoveCityKey = serverId + "_DEVICE_MOVE_" + deviceId;
        String deviceMoveOutTimes;
        if (serverId == Constants.SERVER_ID) {
//            new RedisSession(true)
            deviceMoveOutTimes = R.Local().get(deviceMoveCityKey);
        } else {
//            new RedisSession(serverId, true)
            deviceMoveOutTimes = R.Remote(serverId).get(deviceMoveCityKey);
        }
        int moveOutTimes = StringUtils.isBlank(deviceMoveOutTimes) ? 0 :Integer.parseInt(deviceMoveOutTimes);
        return ret + moveOutTimes;
    }

    public static void removeCityIfSmallAccount(UserProfile userProfile) {
    }

    public static class UserServerHelper {
        private String uid;
        private int server;

        public UserServerHelper() {
        }

        public UserServerHelper(String uid, int server) {
            this.uid = uid;
            this.server = server;
        }

        public boolean isEmpty() {
            boolean ret;
            if (GameEngine.getInstance().isTestServer()) {
                ret = uid == null;
            } else {
                ret = uid == null || server == 0;
            }
            return ret;
        }

        public String getUid() {
            return uid;
        }

        public void setUid(String uid) {
            this.uid = uid;
        }

        public int getServer() {
            return server;
        }

        public void setServer(int server) {
            this.server = server;
        }
    }

    private static boolean hasValidServer(ISFSArray accountUidArray){
       /* if(accountUidArray == null || accountUidArray.size() == 0)  return true;
        Map<String, Integer> serverRatioMap = new SharedUserService().getServerListRatio();
        if(serverRatioMap == null || serverRatioMap.size() == 0)    return true;
        Map<Integer, Integer> serverAccountNum = new HashMap<>();
        int maxnum = Integer.parseInt(new GameConfigManager("item").getItem("account").get("k1"));
        boolean ret = false;

        //各个服务器中的账号数
        for (int index = 0; index < accountUidArray.size(); index++){
            ISFSObject accountObj = accountUidArray.getSFSObject(index);
            int server = accountObj.containsKey("server") ? Integer.parseInt(accountObj.getUtfString("server")) : -1;
            if(-1 != server){
                int num = serverAccountNum.get(server) == null ? 0 : serverAccountNum.get(server);
                serverAccountNum.put(server, num+1);
            }
        }

        //已开服中的账号数是否达上限
        for(String serverStr: serverRatioMap.keySet()){
            int serverId = Integer.parseInt(serverStr);
            if(serverId == Constants.SERVER_ID) continue;
            if(!serverAccountNum.containsKey(serverId) || serverAccountNum.get(serverId) < maxnum){
                ret = true;
                break;
            }
        }*/
        return true;
    }
}
