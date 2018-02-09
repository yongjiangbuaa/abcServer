package com.geng.gameengine.account;

/**
 * Created by guowenpeng
 * 2016/5/26.
 */


import com.geng.exceptions.COKException;
import com.geng.exceptions.GameExceptionCode;
import com.geng.gameengine.AccountService;
import com.geng.gameengine.UserService;
import com.geng.puredb.model.UserProfile;
import com.geng.utils.Constants;
import com.geng.utils.distributed.GlobalDBProxy;
import com.geng.utils.xml.GameConfigManager;
import com.geng.core.data.ISFSObject;
import com.geng.core.data.SFSObject;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * 关联设备(IOS设备和安卓设备上的账号互相切换
 */
public class DeviceCorrelationManager {
    private static final String CORRELATION_CODE = "abcdefghijklmnopqrstuvwxyz0123456789";
    private static int corralationLen = 12;//设备关联码长度


    /**
     * 生成设备关联码
     * @param userProfile
     * @return
     */
    public static ISFSObject generateCorrelationCode(UserProfile userProfile, String pf, String pfId) throws COKException{

        long now = System.currentTimeMillis();
        ISFSObject retObj = SFSObject.newInstance();
        ISFSObject bindObj = AccountService.getUserBindStatus(userProfile.getUid());
        if(!pf.equals("AppStore")){//安卓平台
            if(StringUtils.isBlank(bindObj.getUtfString("googleAccount"))){//没有绑定google账号
                throw new COKException(GameExceptionCode.ACCOUNT_NOT_BIND_GP, "googlePlay is not bind");
            }
            if(!StringUtils.equals(bindObj.getUtfString("googleAccount"), pfId)){
                throw new COKException(GameExceptionCode.ACCOUNT_GP_NOT_SAME, "googlePlay account is not bind account");
            }
            if(!StringUtils.isBlank(bindObj.getUtfString("pfId"))){//已经绑定了gameCenter
                throw new COKException(GameExceptionCode.ACCOUNT_CORRELATED, "gameUid has correlated gc");
            }
        }
        if(pf.equals("AppStore")){//IOS平台
            if(StringUtils.isBlank(bindObj.getUtfString("pfId"))){//没有绑定gameCenter
                throw new COKException(GameExceptionCode.ACCOUNT_NOT_BIND_GC, "gameCenter is not bind");
            }
            if(!StringUtils.equals(bindObj.getUtfString("pfId"), pfId)){
                throw new COKException(GameExceptionCode.ACCOUNT_GC_NOT_SAME, "gameCenter account is not bind account");
            }
            if(!StringUtils.isBlank(bindObj.getUtfString("googleAccount"))){
                throw new COKException(GameExceptionCode.ACCOUNT_CORRELATED, "gameUid has correlated gp");
            }
        }

        ISFSObject correlationObj = GlobalDBProxy.getCorralationInfo(userProfile.getUid(), null);//以前生成的关联码
        if(correlationObj != null){
            long expireTime = correlationObj.getLong("time") + getValidTime();
            if(expireTime > now + 10*1000){//误差
                retObj.putUtfString("correlation", correlationObj.getUtfString("code"));
                retObj.putInt("time", (int)(expireTime/1000));
                return retObj;
            }
        }

        boolean ret = false;
        int maxNum = 10;//最大生成次数
        String correlationString = null;
        while (maxNum > 0){//防止生成重复的关联码
            maxNum--;
            correlationString = random(corralationLen);
            ISFSObject obj =  GlobalDBProxy.getCorralationInfo(null, correlationString);
            if(obj == null && correlationObj == null){//新生成
                ret = GlobalDBProxy.insertCorralationInfo(userProfile.getUid(), correlationString, userProfile.getLoginInfo().getPf(), 0);//插入
                break;
            }else if((obj != null && obj.getUtfString("gameUid").equals(userProfile.getUid())) || (obj == null && correlationObj != null)){//该账号生成的关联码已经过期
                ret = GlobalDBProxy.insertCorralationInfo(userProfile.getUid(), correlationString, userProfile.getLoginInfo().getPf(), 1);//更新
                break;
            }
            correlationString = null;
        }

        if(ret == false && maxNum > 0){
            throw new COKException(GameExceptionCode.INVALID_OPT, "insert correlation_info error");
        }else{
            retObj.putUtfString("correlation", correlationString);
            retObj.putInt("time", (int)((now + getValidTime())/1000));
        }

        return retObj;
    }

    /**
     * 关联设备
     * @param userProfile
     * @param correlationCode
     * @param opType 0:根据关联码查询关联的游戏账号信息; 1:进行设备关联
     * @return
     * @throws COKException
     */
    public static ISFSObject correlateNewDevice(UserProfile userProfile, String pf, String pfId, String correlationCode, int opType)throws COKException{
        ISFSObject bindObj = AccountService.getUserBindStatus(userProfile.getUid());//当前账号
        if(!pf.equals("AppStore")){//安卓平台
            if(StringUtils.isBlank(bindObj.getUtfString("googleAccount"))) {//没有绑定google账号
                throw new COKException(GameExceptionCode.ACCOUNT_NOT_BIND_GP, "googlePlay is not bind");
            }
            if(!StringUtils.equals(bindObj.getUtfString("googleAccount"), pfId)){
                throw new COKException(GameExceptionCode.ACCOUNT_GP_NOT_SAME, "googlePlay account is not bind account");
            }
        }
        if(pf.equals("AppStore")){//IOS平台
            if(StringUtils.isBlank(bindObj.getUtfString("pfId"))){//没有绑定gameCenter
                throw new COKException(GameExceptionCode.ACCOUNT_NOT_BIND_GC, "gameCenter is not bind");
            }
            if(!StringUtils.equals(bindObj.getUtfString("pfId"), pfId)){
                throw new COKException(GameExceptionCode.ACCOUNT_GC_NOT_SAME, "gameCenter account is not bind account");
            }
        }

        //检查关联码是否可用
        ISFSObject correlationObj = GlobalDBProxy.getCorralationInfo(null, correlationCode);
        if(correlationObj == null){
            throw new COKException(GameExceptionCode.INVALID_OPT, "correlationCode is invalid");
        }
        if(correlationObj.getLong("time") + getValidTime() < System.currentTimeMillis()){//过期
            throw new COKException(GameExceptionCode.ACCOUNT_CORRELATION_TIMEOUT, "correlationCode is timeout");
        }
        if(StringUtils.equals(userProfile.getLoginInfo().getPf(), correlationObj.getUtfString("pf"))){//同一个平台不能关联;//todo 以后可能IOS和安卓不止一个平台，这里需要进行修改
            throw new COKException(GameExceptionCode.ACCOUNT_CORRELATION_UNAVAILABLE, "correlationCode is same device");
        }

        String gameUid = correlationObj.getUtfString("gameUid");//关联账号
        ISFSObject correlateAccount = GlobalDBProxy.getAccountInfoByUid(gameUid);
        if(correlateAccount == null){
            throw new COKException(GameExceptionCode.INVALID_OPT, "correlateAccount is null");
        }
        if(correlationObj.getUtfString("pf").equals("AppStore") && StringUtils.isNotBlank(correlateAccount.getUtfString("googleAccount"))){//切换到安卓设备,但该账号已经绑定了googlePlay
            throw new COKException(GameExceptionCode.ACCOUNT_CORRELATED, "correlateAccount has bind googlePlay");
        }
        if(!correlationObj.getUtfString("pf").equals("AppStore") && StringUtils.isNotBlank(correlateAccount.getUtfString("pfId"))){//切换到IOS设备,但该账号已经绑定了gameCenter
            throw new COKException(GameExceptionCode.ACCOUNT_CORRELATED, "correlateAccount has bind gameCenter");
        }

        ISFSObject newAccountInfo = null;
        if(opType == 0){
            newAccountInfo = getAccountInfo(correlateAccount);
        }else if(opType == 1){//
            GlobalDBProxy.MAPPING_TYPE type;
            String account;
            String accountName;
            if(userProfile.getLoginInfo().getPf().equals("AppStore")){
                type = GlobalDBProxy.MAPPING_TYPE.AppStore;
                account = bindObj.getUtfString("pfId");
                ISFSObject gcObj = AccountService.getGameCenterInfo(userProfile.getUid());
                accountName = gcObj == null ? "" : gcObj.getUtfString("gcName");
            }else{
                type = GlobalDBProxy.MAPPING_TYPE.google;
                account = bindObj.getUtfString("googleAccount");
                accountName = bindObj.getUtfString("googleAccountName");
            }

            GlobalDBProxy.updateDeviceCorrelation(userProfile.getUid(), gameUid, type, account, accountName);//绑定到新账号上
            newAccountInfo = UserService.bindAccount(userProfile, 2, type.toString(), account, null, null);//切换账号
        }

        return newAccountInfo;
    }


    /**
     *生成随机字符串
     * @param length 随机字符串的长度
     * @return
     */
    private static String random(int length){
        StringBuilder builder = new StringBuilder(length);
        Random random = new Random();
        int len = CORRELATION_CODE.length();
        for(int i = 0; i < length; ++i){
            int number = random.nextInt(len);
            builder.append(CORRELATION_CODE.charAt(number));
        }
        return builder.toString();
    }

    /**
     * 设备关联码的有效期
     * @return
     */
    private static long getValidTime(){
        String validTime = new GameConfigManager("item").getItem("account").get("k10");
        return validTime == null ? 120 * 1000l : Integer.parseInt(validTime) * 1000l;
    }

    private static ISFSObject getAccountInfo(ISFSObject accountObj){
        ISFSObject serverObj = new SFSObject();
        Map<String, HashMap<String, String>> serverMapList = new GameConfigManager("servers").getGroupMap(Constants.BIG_ZONE_ID);
        String server = accountObj.getUtfString("server");
        HashMap<String, String> serverMap = serverMapList.get(server);
        if (serverMap != null){
            for (Map.Entry<String, String> entry : serverMap.entrySet()) {
                serverObj.putUtfString(entry.getKey(), entry.getValue());
            }
            serverObj.putUtfString("uuid", accountObj.getUtfString("uuid"));
            String gameUid = accountObj.getUtfString("gameUid");
            serverObj.putUtfString("gameUid", gameUid);
            serverObj.putUtfString("gameUserName", accountObj.getUtfString("gameUserName"));
            serverObj.putUtfString("gameUserLevel", Integer.toString(accountObj.getInt("gameUserLevel")));
        }else{
            LoggerFactory.getLogger(DeviceCorrelationManager.class).warn("correlation device: but serverId is not in servers.xml");
        }

        return serverObj;
    }

}
