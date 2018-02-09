package com.geng.utils.distributed;

import com.geng.gameengine.AccountService;
import com.geng.gameengine.login.LoginInfo;
import com.geng.puredb.model.UserProfile;
import com.geng.utils.CommonUtils;
import com.geng.utils.Constants;
import com.geng.utils.SFSMysql;
import com.google.api.client.repackaged.com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.geng.core.data.*;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * Created by lifangkai on 15/3/5.
 */
public class GlobalDBProxy {

    public enum MAPPING_TYPE {
        device("deviceId", null),
        facebook("facebookAccount", "facebookAccountName"),
        google("googleAccount", "googleAccountName"),
        name("gameUserName", null),
        gaid("gaid", null),
        email("emailAccount", "passmd5"),
        AppStore("pfId", "pf"),
        qq("pfId", "pf"),//手动绑定手q
        weixin("pfId", "pf"),//手动绑定微信
        weibo("pfId", "pf"),
        tencent("pfId", "pf"),//登陆自动绑定
        vk("pfId", "pf"),
        mslive("pfId", "pf"),


        cn_mi("pfId", "pf"),
        cn_uc("pfId", "pf"),
        cn_360("pfId", "pf"),
        cn_baidu("pfId", "pf"),
        cn_nearme("pfId", "pf"),
        cn_lenovo("pfId", "pf"),
        cn_huawei("pfId", "pf"),
        cn_am("pfId", "pf"),
        cn_vivo("pfId", "pf"),
        cn_kupai("pfId", "pf"),
        cn_pps("pfId", "pf"),
        cn_pptv("pfId", "pf"),
        cn_youku("pfId", "pf"),
        cn_anzhi("pfId", "pf"),
        cn_ewan("pfId", "pf"),
        cn_mzw("pfId", "pf"),
        cn_wdj("pfId", "pf"),
        cn_wyx("pfId", "pf"),
        cn_kugou("pfId", "pf"),
        cn_sogou("pfId", "pf"),
        cn_dangle("pfId", "pf"),
        cn_toutiao("pfId", "pf"),
        cn_sy37("pfId", "pf"),
        cn_mz("pfId", "pf"),
        cn_leshi("pfId", "pf"),
        cn_oppo("pfId", "pf"),
        cn_meizu("pfId", "pf"),
        cn_caoxie("pfId", "pf"),
        cn_aiqiyi("pfId", "pf"),
        cn_57k("pfId", "pf"),
        cn_yeshen("pfId", "pf"),
        cn_bluestacks("pfId", "pf"),
        ;

        /**
         *
         * 除去fb gp appstore新增可解绑的绑定模式加入这里
         */
        public static List<String> multiPlatformList = Arrays.asList(
                weibo.toString()
                , vk.toString()
                , mslive.toString()
                , weixin.toString()
                , qq.toString()
        );
        public String getAlias() {
            return alias;
        }

        public String getValueName() {
            return valueName;
        }

        private MAPPING_TYPE(String alias, String valueName) {
            this.alias = alias;
            this.valueName = valueName;
        }

        private String alias;
        private String valueName;
    }

    public static boolean useNewBindMapping(String mappingType){
        if(MAPPING_TYPE.multiPlatformList.contains(mappingType))
            return true;
        return false;
    }

    public static boolean useNewBindMapping(MAPPING_TYPE mappingType){
        if(useNewBindMapping(mappingType.toString()))
            return true;
        if(strongBindMap.values().contains(mappingType))
            return true;
        if(weakBindMap.values().contains(mappingType))
            return true;
        return false;
    }

    private static HashMap<String, MAPPING_TYPE> strongBindMap = new HashMap(){
        {
            /**
             * 手游登陆，自动绑定不可解绑
             */
            put(AccountService.APP_PF_TENCENT, MAPPING_TYPE.tencent);
            put(AccountService.APP_PF_CN_MI, MAPPING_TYPE.cn_mi);
            put(AccountService.APP_PF_CN_UC, MAPPING_TYPE.cn_uc);
            put(AccountService.APP_PF_CN_360, MAPPING_TYPE.cn_360);
            put(AccountService.APP_PF_CN_BAIDU, MAPPING_TYPE.cn_baidu);
            put(AccountService.APP_PF_CN_MIHY, MAPPING_TYPE.cn_mi);
            put(AccountService.APP_PF_CN_NEARME, MAPPING_TYPE.cn_nearme);
            put(AccountService.APP_PF_CN_LENOVO, MAPPING_TYPE.cn_lenovo);
            put(AccountService.APP_PF_CN_HUAWEI, MAPPING_TYPE.cn_huawei);
            put(AccountService.APP_PF_CN_AM, MAPPING_TYPE.cn_am);
            put(AccountService.APP_PF_CN_VIVO, MAPPING_TYPE.cn_vivo);
            put(AccountService.APP_PF_CN_KUPAI, MAPPING_TYPE.cn_kupai);
            put(AccountService.APP_PF_CN_PPS, MAPPING_TYPE.cn_pps);
            put(AccountService.APP_PF_CN_PPTV, MAPPING_TYPE.cn_pptv);
            put(AccountService.APP_PF_CN_YOUKU, MAPPING_TYPE.cn_youku);
            put(AccountService.APP_PF_CN_ANZHI, MAPPING_TYPE.cn_anzhi);
            put(AccountService.APP_PF_CN_EWAN, MAPPING_TYPE.cn_ewan);
            put(AccountService.APP_PF_CN_MZW, MAPPING_TYPE.cn_mzw);
            put(AccountService.APP_PF_CN_WDJ, MAPPING_TYPE.cn_wdj);
            put(AccountService.APP_PF_CN_WYX, MAPPING_TYPE.cn_wyx);
            put(AccountService.APP_PF_CN_KUGOU, MAPPING_TYPE.cn_kugou);
            put(AccountService.APP_PF_CN_SOGOU, MAPPING_TYPE.cn_sogou);
            put(AccountService.APP_PF_CN_DANGLE, MAPPING_TYPE.cn_dangle);
            put(AccountService.APP_PF_CN_TOUTIAO, MAPPING_TYPE.cn_toutiao);
            put(AccountService.APP_PF_CN_SY37, MAPPING_TYPE.cn_sy37);
            put(AccountService.APP_PF_CN_MZ, MAPPING_TYPE.cn_mz);
            put(AccountService.APP_PF_CN_LESHI, MAPPING_TYPE.cn_leshi);
            put(AccountService.APP_PF_CN_OPPO, MAPPING_TYPE.cn_oppo);
            put(AccountService.APP_PF_CN_MEIZU, MAPPING_TYPE.cn_meizu);
            put(AccountService.APP_PF_CN_CAOXIE, MAPPING_TYPE.cn_caoxie);
            put(AccountService.APP_PF_CN_AIQIYI, MAPPING_TYPE.cn_aiqiyi);
            put(AccountService.APP_PF_CN_57K, MAPPING_TYPE.cn_57k);
            put(AccountService.APP_PF_CN_YESHEN, MAPPING_TYPE.cn_yeshen);
            put(AccountService.APP_PF_CN_BLUESTACKS, MAPPING_TYPE.cn_bluestacks);

            /**
             * 页游登陆，自动绑定不可解绑
             */
            put(AccountService.APP_PF_MI_WEB, MAPPING_TYPE.cn_mi);
        }
    };

    private static HashMap<String, MAPPING_TYPE> weakBindMap  = new HashMap(){
        {
            /**
             * 页游登陆，自动绑定可从手机解绑
             */
            put(AccountService.APP_PF_VK_WEB, MAPPING_TYPE.vk);
        }
    };

    public static boolean isAutoBind(String pf) {
        return isStrongBind(pf) || isWeakBind(pf);
    }

    //必须调用isAutoBind接口否则可能出异常
    public static MAPPING_TYPE getAutoBindMappingType(String pf){
        if(isStrongBind(pf))
            return strongBindMap.get(pf);
        if(isWeakBind(pf))
            return weakBindMap.get(pf);
        return MAPPING_TYPE.device;
    }

    /**
     * 强绑定模式，无法解绑
     * mobile登陆自动绑定
     * web登陆自动绑定
     * @param pf
     * @return
     */
    public static boolean isStrongBind(String pf) {
        return strongBindMap.keySet().contains(pf);
    }

    /**
     * 弱绑定模式，可从手机解绑
     * mobile手动绑定
     * web登陆自动绑定deviceid=pfid
     * @param pf
     * @return
     */
    public static boolean isWeakBind(String pf) {
        return weakBindMap.keySet().contains(pf);
    }

    public static boolean insertGlobalAccount(UserProfile userProfile, String googleAccount, String facebookAccount, String facebookAccountName, String gcName, int serverId) {
        String gameUid = userProfile.getUid();
        List<SqlParamsObj> sqlParamsObjList = new ArrayList<>();
        List<String> accountFields = new ArrayList<>(Arrays.asList("uuid", "gameUid", "server", "deviceId"));
        List<Object> accountParams = new ArrayList<>();
        LoginInfo loginInfo = userProfile.getLoginInfo();
        String uuid, gaid = null, pf = null, pfId = null;
        String deviceId;
        if (loginInfo != null) {
            deviceId = loginInfo.getDeviceId();
            if (deviceId == null) {
                deviceId = userProfile.getDeviceId();
            }
        } else {
            deviceId = userProfile.getDeviceId();
        }
//        sqlParamsObjList.add(toDeleteAccountMappingObj(gameUid, MAPPING_TYPE.name));//兼容account_new里没数据的玩家，但usermapping里有数据的玩家
//        sqlParamsObjList.add(toInsertAccountMappingObj(gameUid, MAPPING_TYPE.name, userProfile.getName()));
//        sqlParamsObjList.add(toDeleteAccountMappingObj(gameUid, MAPPING_TYPE.device));
//        sqlParamsObjList.add(toInsertAccountMappingObj(gameUid, MAPPING_TYPE.device, deviceId));
        sqlParamsObjList.add(toReplaceAccountMapping(gameUid, MAPPING_TYPE.name, userProfile.getName()));
        sqlParamsObjList.add(toReplaceAccountMapping(gameUid, MAPPING_TYPE.device, deviceId));
        if (loginInfo != null) {
            uuid = loginInfo.getUuid();
            gaid = loginInfo.getGaid();
            pf = loginInfo.getPf();
            pfId = loginInfo.getPfId();
        } else {
            uuid = LoginInfo.generateUUid(userProfile.getDeviceId());
        }
        accountParams.addAll(Arrays.asList(uuid, userProfile.getUid(), serverId, deviceId));
        if (gaid != null) {
            accountFields.add("gaid");
            accountParams.add(gaid);
//            sqlParamsObjList.add(toDeleteAccountMappingObj(gameUid, MAPPING_TYPE.gaid));
//            sqlParamsObjList.add(toInsertAccountMappingObj(gameUid, MAPPING_TYPE.gaid, gaid));
            sqlParamsObjList.add(toReplaceAccountMapping(gameUid, MAPPING_TYPE.gaid, gaid));
        }
        if (googleAccount != null) {
            accountFields.add("googleAccount");
            accountParams.add(googleAccount);
            sqlParamsObjList.add(toInsertAccountMappingObj(gameUid, MAPPING_TYPE.google, googleAccount));
        }
        if (facebookAccount != null) {
            accountFields.add("facebookAccount");
            accountParams.add(facebookAccount);
            sqlParamsObjList.add(toInsertAccountMappingObj(gameUid, MAPPING_TYPE.facebook, facebookAccount));
        }
        if (facebookAccountName != null) {
            accountFields.add("facebookAccountName");
            accountParams.add(facebookAccountName);
        }
        if (pf != null) {
            accountFields.add("pf");
            accountParams.add(pf);
            if (StringUtils.isNotBlank(pfId)) {
                //自动绑定
                if (pf.equals(AccountService.APP_PF_APPSTORE)) {
                    //这个pfId要写入account_new
                    //由于ewan帐号id超过40位，platformlogin的数据不记录
                    //pfId会记录到stat_reg
                    accountFields.add("pfId");
                    accountParams.add(pfId);
                    sqlParamsObjList.add(toInsertAccountMappingObj(gameUid, MAPPING_TYPE.AppStore, pfId));
                    if(!StringUtils.isBlank(gcName)){
                        sqlParamsObjList.add(toInsertGameCenterName(gameUid, pfId, gcName));
                    }
                }
                if(isAutoBind(pf)){
                    toInsertAccountMappingObj(sqlParamsObjList, gameUid, getAutoBindMappingType(pf), pfId);
                }
            }
        }
        accountFields.add("gameUserName");
        accountFields.add("gameUserLevel");
        accountFields.add("lastTime");
        accountParams.addAll(Arrays.asList(userProfile.getName(), userProfile.getLevel(), System.currentTimeMillis()));
        StringBuilder sb = new StringBuilder("insert into account_new(");
        sb.append(Joiner.on(',').join(accountFields)).append(") values").append("(");
        for (int i = 0; i < accountFields.size(); i++) {
            sb.append("?").append(",");
        }
        sb.setLength(sb.length() - 1);
        sb.append(")");
        sqlParamsObjList.add(0, new SqlParamsObj(sb.toString(), accountParams.toArray()));
        return SFSMysql.getInstance().executeUpdateGlobalWithTransaction(sqlParamsObjList);
    }

    //自动绑定的渠道不允许start new game
    public static boolean canStartNewGame(String uid) {
        //由于渠道数量太多，取消bindlist的判断
        ISFSArray bindInfo = getAccountBindInfo(uid, null, MAPPING_TYPE.multiPlatformList);
        if (bindInfo.size() > 0) {
            return false;
        }
        return true;
    }

    /**
     * 查看玩家绑定的账号
     * @param uid
     * @param bindPfList ①为null，表示不分pf ②不为null且有值，根据需要查询相关数据
     * @return
     */
    public static ISFSArray getAccountBindInfo(String uid, List<String> bindPfList, List<String> nonBindPfList) {
        String sql = "select * from userbindmappingreverse where gameUid = ?";
        ISFSArray bindInfo = new SFSArray();
        List<String> paramList = new ArrayList<>();
        paramList.add(uid);
        if(bindPfList != null && bindPfList.size() > 0){
            List<String> appendWhere = new ArrayList<>();
            for(String bindPf:bindPfList){
                appendWhere.add(" mappingType = ? ");
                paramList.add(bindPf);
            }
            sql += " and ("+StringUtils.join(appendWhere,"or")+")";
        }

        if(nonBindPfList != null && nonBindPfList.size() > 0){
            List<String> appendWhere = new ArrayList<>();
            for(String nonBindPf:nonBindPfList){
                appendWhere.add(" mappingType != ? ");
                paramList.add(nonBindPf);
            }
            sql += " and ("+StringUtils.join(appendWhere,"and")+")";
        }
        ISFSArray bindInfoArray = SFSMysql.getInstance().queryGlobal(sql, paramList.toArray());
        for (int i = 0; i < bindInfoArray.size(); i++) {
            String mappingType = bindInfoArray.getSFSObject(i).getUtfString("mappingType");
            String mappingValue = bindInfoArray.getSFSObject(i).getUtfString("mappingValue");
//            if (GlobalDBProxy.MAPPING_TYPE.multiPlatformList.contains(mappingType)) {
                ISFSObject bindAccount = new SFSObject();
                bindAccount.putUtfString("bindPf", mappingType);
                bindAccount.putUtfString("bindId", mappingValue);
                bindInfo.addSFSObject(bindAccount);
//            }
        }
        return bindInfo;
    }

    //自动绑定的版本，多个包可能绑定同一个账号
    public static String getRealBindPf(String bindPf){
        if(AccountService.APP_PF_CN_MIHY.equals(bindPf) || AccountService.APP_PF_MI_WEB.equals(bindPf))
            return  AccountService.APP_PF_CN_MI;
        return bindPf;
    }

    public static boolean checkAccountBind(String uid, String bindPf, String bindId) {
        String sql = "select * from userbindmapping where mappingType = ? and mappingValue = ? and gameUid = ? ";
        ISFSArray bindInfoArray = SFSMysql.getInstance().queryGlobal(sql, new Object[]{getRealBindPf(bindPf), bindId, uid});
        if (bindInfoArray.size() > 0)
            return true;
        return false;
    }

    public static ISFSArray selectAccount(String name, int conditionLevel, int limit) {
        String sql = "select gameUid from usermapping where mappingType = ? and mappingValue like ? limit ?";
        ISFSArray gameUidArray = SFSMysql.getInstance().queryGlobal(sql, new Object[]{MAPPING_TYPE.name.toString(), name + "%", limit});
        if (gameUidArray.size() == 0) {
            return gameUidArray;
        }
        StringBuilder sb = toSelectAccountBatchSQL(Arrays.asList("gameUid", "server"), gameUidArray);
        sb.append(" and server != ? and gameUserLevel >= ?");
        return SFSMysql.getInstance().queryGlobal(sb.toString(), new Object[]{Constants.SERVER_ID, conditionLevel});
    }

    public static ISFSArray selectAccount(String... facebookIdArray) {
        StringBuilder queryGlobalSql = new StringBuilder("SELECT gameUid FROM usermapping WHERE mappingType = '");
        queryGlobalSql.append(MAPPING_TYPE.facebook.toString()).append("' and mappingValue IN (");
        for (int index = 0; index < facebookIdArray.length; index++) {
            String id = facebookIdArray[index];
            queryGlobalSql.append("'").append(id).append("',");
        }
        queryGlobalSql.setLength(queryGlobalSql.length() - 1);
        queryGlobalSql.append(')');
        ISFSArray gameUidArray = SFSMysql.getInstance().queryGlobal(queryGlobalSql.toString(), new Object[]{});
        if (gameUidArray.size() == 0) {
            return gameUidArray;
        }
        StringBuilder sb = toSelectAccountBatchSQL(Arrays.asList("gameUid", "server", "facebookAccount", "active"), gameUidArray);
        ISFSArray result = SFSMysql.getInstance().queryGlobal(sb.toString(), new Object[]{});
        if (result != null) {
            Iterator<SFSDataWrapper> resultIterator = result.iterator();
            while (resultIterator.hasNext()) {
                ISFSObject record = (ISFSObject) resultIterator.next().getObject();
                if (record.getInt("active") != 0) {
                    resultIterator.remove();
                }
            }
        }
        return result;
    }

    public static ISFSArray selectAccount(String name, String conditionSql) {
        return selectAccount(null, name, conditionSql);
    }

    public static ISFSArray selectAccount(List<String> fields, String name, String conditionSql) {
        String sql = "select gameUid from usermapping where mappingType = ? and mappingValue = ?";
        ISFSArray gameUidArray = SFSMysql.getInstance().queryGlobal(sql, new Object[]{MAPPING_TYPE.name.toString(), name});
        if (gameUidArray.size() == 0) {
            return gameUidArray;
        }
        StringBuilder sb = toSelectAccountBatchSQL(fields, gameUidArray);
        if (conditionSql != null) {
            sb.append(conditionSql);
        }
        return SFSMysql.getInstance().queryGlobal(sb.toString(), new Object[]{});
    }

    public static ISFSArray selectAccountList(MAPPING_TYPE mappingType, String mappingValue) {
        String sql = "select gameUid from usermapping where mappingType = ? and mappingValue = ?";
        if (GlobalDBProxy.useNewBindMapping(mappingType)) {
            sql = "select gameUid from userbindmapping where mappingType = ? and mappingValue = ?";
        }
        ISFSArray gameUidArray = SFSMysql.getInstance().queryGlobal(sql, new Object[]{mappingType.toString(), mappingValue});
        ISFSArray retArray = null;
        if (gameUidArray != null && gameUidArray.size() > 0) {
            StringBuilder sb = toSelectAccountBatchSQL(null, gameUidArray);
            retArray = SFSMysql.getInstance().queryGlobal(sb.toString(), new Object[0]);
        }
        return retArray;
    }

    public static Optional<ISFSObject> selectLastUsedAccount(MAPPING_TYPE mappingType, String mappingValue) {
        ISFSObject retObj = null;
        String sql = "select gameUid from usermapping where mappingType = ? and mappingValue = ?";
        if (GlobalDBProxy.useNewBindMapping(mappingType)) {
            sql = "select gameUid from userbindmapping where mappingType = ? and mappingValue = ?";
        }
        ISFSArray gameUidArray = SFSMysql.getInstance().queryGlobal(sql, new Object[]{mappingType.toString(), mappingValue});
        if (gameUidArray.size() > 0) {
            StringBuilder sb = toSelectAccountBatchSQL(null, gameUidArray);
            sb.append(" order by lastTime desc limit 1");
            ISFSArray array = SFSMysql.getInstance().queryGlobal(sb.toString(), new Object[]{});
            retObj = orderByLastTimeLimitOne(array).orNull();
        }
        return Optional.fromNullable(retObj);
    }

    public static Optional<ISFSObject> orderByLastTimeLimitOne(ISFSArray array) {
        ISFSObject retObj = null;
        long maxLastTime = 0;
        for (int i = 0; i < array.size(); i++) {
            ISFSObject obj = array.getSFSObject(i);
            if (obj.containsKey("lastTime") && obj.getLong("lastTime") > maxLastTime) {
                maxLastTime = obj.getLong("lastTime");
                retObj = array.getSFSObject(i);
            }
        }
        return Optional.fromNullable(retObj);
    }

    public static Optional<String> checkAccount(String nameOrUid, int serverId) {
        String gameUid = null;
        ISFSArray arr = new SFSArray();
        if (nameOrUid.matches("\\d+")) {
            String sql = "SELECT gameUid FROM account_new WHERE gameUid=? AND server=?";
            arr = SFSMysql.getInstance().queryGlobal(sql, new String[]{nameOrUid, "" + serverId});
        }
        ISFSArray arrByName = new SFSArray();
        if (arr.size() < 1) {
            arrByName = selectAccount(Arrays.asList("gameUid"), nameOrUid, " and server='" + serverId + "'");
        }
        if (arr.size() > 0) {
            gameUid = arr.getSFSObject(0).getUtfString("gameUid");
        } else if (arrByName.size() > 0) {
            gameUid = arrByName.getSFSObject(0).getUtfString("gameUid");
        }
        return Optional.fromNullable(gameUid);
    }

    public static boolean deleteAccount(String gameUid) {
        List<String> sqlList = new ArrayList<>();
        sqlList.add("delete from account_new where gameUid = '" + gameUid + "'");
        sqlList.add("delete from usermapping where gameUid = '" + gameUid + "'");
        sqlList.add("delete from userbindmapping where gameUid = '" + gameUid + "'");
        sqlList.add("delete from userbindmappingreverse where gameUid = '" + gameUid + "'");
        sqlList.add("delete from gameCenter_info where gameUid = '" + gameUid + "'");
        return SFSMysql.getInstance().executeGlobalBatch(false, sqlList);
    }

    /**
     * 去除旧账号绑定，绑定到新账号上
     * @param oldUid
     * @param newUid
     * @param mappingType
     * @param account
     * @param accountName
     * @return
     */
    public static boolean updateDeviceCorrelation(String oldUid, String newUid, MAPPING_TYPE mappingType, String account, String accountName){
        List<SqlParamsObj> uidSqlParamsList = GlobalDBProxy.toUpdateAccountByType(oldUid, mappingType, "", "",  null);//解绑
        List<SqlParamsObj> uidSqlParamsList2 = GlobalDBProxy.toUpdateAccountByType(newUid, mappingType, account, accountName, null);//解绑
        uidSqlParamsList.addAll(uidSqlParamsList2);
        uidSqlParamsList.add(delDeviceCorrelationState(newUid));
        return SFSMysql.getInstance().executeUpdateGlobalWithTransaction(uidSqlParamsList);
    }

    public static SqlParamsObj delDeviceCorrelationState(String gameUid){
        return new SqlParamsObj("delete from correlation_info where gameUid = ?", new Object[]{gameUid});
    }

    public static boolean updateAccountByType(String gameUid, MAPPING_TYPE mappingType, String mappingValue, String mappingValuePlus) {
        return updateAccountByType(gameUid, mappingType, mappingValue, mappingValuePlus, null);
    }

    public static boolean updateAccountByType(String gameUid, MAPPING_TYPE mappingType, String mappingValue, String mappingValuePlus, String oldMappingValue) {
        List<SqlParamsObj> sqlParamsList = toUpdateAccountByType(gameUid, mappingType, mappingValue, mappingValuePlus, oldMappingValue);
        return SFSMysql.getInstance().executeUpdateGlobalWithTransaction(sqlParamsList);
    }

    public static List<SqlParamsObj> toUpdateAccountByType(String gameUid, MAPPING_TYPE mappingType, String mappingValue, String mappingValuePlus, String oldMappingValue) {
//        if (oldMappingValue == null) {
//            oldMappingValue = selectAccountProperty(gameUid, mappingType).orNull();
//        }
//        oldMappingValue = null; //todo: delete question
        List<SqlParamsObj> sqlParamsList = new ArrayList<>();
        if (!GlobalDBProxy.useNewBindMapping(mappingType)) {
            List<Object> params = new ArrayList<>();
            StringBuilder sb = new StringBuilder("UPDATE account_new SET ").append(mappingType.getAlias()).append(" = ? ");
            params.add(mappingValue);
            if (mappingType.getValueName() != null) {
                if(mappingType == MAPPING_TYPE.AppStore){//gameCenter绑定，gameCenter名字另外存储
                    sb.append(" , pf = 'AppStore'");
                }else{
                    sb.append(" , ").append(mappingType.getValueName()).append(" = ? ");
                    params.add(mappingValuePlus);
                }
            }
            sb.append(" where gameUid = ?");
            params.add(gameUid);
            sqlParamsList.add(new SqlParamsObj(sb.toString(), params.toArray()));
        }

//        if (oldMappingValue != null) {
//            sqlParamsList.add(new SqlParamsObj("DELETE FROM usermapping where mappingValue = ? AND mappingType = ? AND gameUid = ?", new Object[]{oldMappingValue, mappingType.toString(), gameUid}));
//        }
        if (GlobalDBProxy.useNewBindMapping(mappingType)) {
            sqlParamsList.add(new SqlParamsObj("DELETE FROM userbindmapping where gameUid = ? and mappingType = ?", new Object[]{gameUid, mappingType.toString()}));
            sqlParamsList.add(new SqlParamsObj("DELETE FROM userbindmappingreverse where gameUid = ? and mappingType = ?", new Object[]{gameUid, mappingType.toString()}));
        } else {
            sqlParamsList.add(new SqlParamsObj("DELETE FROM usermapping where mappingType = ? AND gameUid = ?", new Object[]{mappingType.toString(), gameUid}));
            if(mappingType == MAPPING_TYPE.AppStore){//gameCenter绑定，删除旧名字
                sqlParamsList.add(new SqlParamsObj("DELETE FROM gameCenter_info where  gameUid = ?", new Object[]{gameUid}));
            }
        }
        if (StringUtils.isNotBlank(mappingValue)) {
            toInsertAccountMappingObj(sqlParamsList, gameUid, mappingType, mappingValue);
            if(mappingType == MAPPING_TYPE.AppStore){
                sqlParamsList.add(toInsertGameCenterName(gameUid, mappingValue, mappingValuePlus));
            }
        }
        return sqlParamsList;
    }

    public static Map<String, String> selectAccountFields(String gameUid, Collection<String> fields) {
        Map<String, String> map = new HashMap<>();
        if (fields.isEmpty()) {
            return map;
        }
        ISFSArray array = SFSMysql.getInstance().queryGlobal("select " + StringUtils.join(fields, ',') + " from account_new where gameUid = ? order by lastTime desc limit 1", new Object[]{gameUid});
        if (array.size() == 1) {
            ISFSObject obj = array.getSFSObject(0);
            for (String f : fields) {
                String v = obj.getUtfString(f);
                if (StringUtils.isNotBlank(v)) {
                    map.put(f, v);
                }
            }
        }
        return map;
    }

    public static StringBuilder toSelectAccountBatchSQL(List<String> fields, ISFSArray gameUidArray) {
        List<String> uidList = new ArrayList<>();
        for (int i = 0; i < gameUidArray.size(); i++) {
            if (gameUidArray.getSFSObject(i).containsKey("gameUid")) {
                String uid = gameUidArray.getSFSObject(i).getUtfString("gameUid");
                uidList.add(uid.trim());
            }
            if (gameUidArray.getSFSObject(i).containsKey("uid")) {
                String uid = gameUidArray.getSFSObject(i).getUtfString("uid");
                uidList.add(uid.trim());
            }
        }
        return toSelectAccountBatchSQL(fields, uidList);
    }

    public static StringBuilder toSelectAccountPreparedBatchSQL(List<String> fields, Collection<String> gameUidList) {
        StringBuilder sb = new StringBuilder(64);
        sb.append("select ");
        if (fields == null || fields.isEmpty()) {
            sb.append(" * ");
        } else {
            for(String f : fields){
                sb.append(f).append(",");
            }
            // remove the last ","
            sb.setLength(sb.length() - 1);
        }
        sb.append(" from account_new where gameUid in(");
        for(int i = 0; i < gameUidList.size(); ++i){
            sb.append("?,");
        }
        sb.setLength(sb.length() - 1);
        sb.append(")");
        return sb;
    }

    public static StringBuilder toSelectAccountBatchSQL(List<String> fields, Collection<String> gameUidList) {
        StringBuilder sb = new StringBuilder("select ");
        if (fields == null || fields.isEmpty()) {
            sb.append(" * ");
        } else {
            sb.append(Joiner.on(',').skipNulls().join(fields));
        }
        sb.append(" from account_new where gameUid in(");
        for (String uid : gameUidList) {
            sb.append("'").append(uid).append("',");
        }
        sb.setLength(sb.length() - 1);
        sb.append(")");
        return sb;
    }

    public static ISFSObject getAccountInfoByUid(String gameUid){
        ISFSObject retObj = null;
        String sql = "select * from account_new where gameUid = ?";
        ISFSArray result = SFSMysql.getInstance().queryGlobal(sql, new Object[]{gameUid});
        if(result != null && result.size() > 0){
            retObj = result.getSFSObject(0);
        }

        return retObj;
    }

    /**
     * 旧版绑定，新加绑定请使用下面一个方法
     *
     * @param gameUid
     * @param mappingType
     * @param mappingValue
     * @return
     */
    public static SqlParamsObj toInsertAccountMappingObj(String gameUid, MAPPING_TYPE mappingType, String mappingValue) {
        return new SqlParamsObj("insert usermapping(gameUid, mappingType, mappingValue) values(?, ?, ?)", new Object[]{gameUid, mappingType.toString(), mappingValue});
    }

    public static SqlParamsObj toInsertGameCenterName(String gameUid, String mappingValue, String mappingValuePlus){
        mappingValuePlus = CommonUtils.filterEmoji(mappingValuePlus);
        return new SqlParamsObj("insert gameCenter_info(gameUid, gcId, gcName) values(?, ?, ?)", new Object[]{gameUid, mappingValue, mappingValuePlus});
    }

    private static void toInsertAccountMappingObj(List<SqlParamsObj> sqlParamsList, String gameUid, MAPPING_TYPE mappingType, String mappingValue) {
        if (GlobalDBProxy.useNewBindMapping(mappingType)) {
            sqlParamsList.add(new SqlParamsObj("insert userbindmapping(gameUid, mappingType, mappingValue) values(?, ?, ?)", new Object[]{gameUid, mappingType.toString(), mappingValue}));
            sqlParamsList.add(new SqlParamsObj("insert userbindmappingreverse(gameUid, mappingType, mappingValue) values(?, ?, ?)", new Object[]{gameUid, mappingType.toString(), mappingValue}));
        } else {
            sqlParamsList.add(toInsertAccountMappingObj(gameUid, mappingType, mappingValue));
        }
    }

    public static SqlParamsObj toDeleteAccountMappingObj(String gameUid, MAPPING_TYPE mappingType) {
        return new SqlParamsObj("delete from usermapping where gameUid=? and mappingType=?", new Object[]{gameUid, mappingType.toString()});
    }

    private static SqlParamsObj toReplaceAccountMapping(String gameUid, MAPPING_TYPE mappingType, String mappingValue) {
        return new SqlParamsObj("replace into usermapping(gameUid, mappingType, mappingValue) values(?,?,?)",  new Object[]{gameUid, mappingType.toString(), mappingValue});
    }

    public static ISFSArray selectOtherServersModUsersInfo() {
        String sql = "select * from mod_info where server != ?";
        ISFSArray modArray = SFSMysql.getInstance().queryGlobal(sql, new Object[]{Integer.toString(Constants.SERVER_ID)});
        Map<String, String> uidLangMap = new HashMap<>();
        for (int i = 0; i < modArray.size(); i++) {
            ISFSObject obj = modArray.getSFSObject(i);
            String gameUid = obj.getUtfString("uid");
            uidLangMap.put(gameUid, obj.getUtfString("lang"));
        }
        if (uidLangMap.isEmpty()) {
            return new SFSArray();
        }
        StringBuilder sb = toSelectAccountBatchSQL(Arrays.asList("gameUid", "server", "gameUserName", "lastTime"), uidLangMap.keySet()).append(" order by lastTime desc");
        ISFSArray result = SFSMysql.getInstance().queryGlobal(sb.toString(), new Object[]{});
        for (int i = 0; i < result.size(); i++) {
            ISFSObject obj = result.getSFSObject(i);
            obj.putUtfString("lang", uidLangMap.get(obj.getUtfString("gameUid")));
        }
        return result;
    }

    public static boolean banAccount(String uid) {
        return SFSMysql.getInstance().executeGlobal("UPDATE account_new SET active = 1 WHERE gameUid = ?", new Object[]{uid});
    }

    public static String selectBanReason(String uid){
        ISFSArray result = SFSMysql.getInstance().queryGlobal("select reason from ban_record where uid = ? and serverId = ? order by opDate desc limit 1", new Object[]{uid, Constants.SERVER_ID});
        if(result != null && result.size() > 0){
            ISFSObject resultObj = result.getSFSObject(0);
            if(resultObj.containsKey("reason")){
                return resultObj.getUtfString("reason");
            }
        }
        return "";
    }


    public static ISFSObject getCorralationInfo(String uid, String correlationStr){
        ISFSArray result = null;
        if(StringUtils.isBlank(uid) && StringUtils.isBlank(correlationStr)){
            return null;
        }else if(StringUtils.isBlank(uid)){//根据correlationStr查找
            String sql = "select gameUid, code, pf, time from correlation_info where code = ? limit 1";
            result = SFSMysql.getInstance().queryGlobal(sql, new Object[]{correlationStr});
        }else if(StringUtils.isBlank(correlationStr)){
            String sql = "select gameUid, code, pf, time from correlation_info where gameUid = ? limit 1";
            result = SFSMysql.getInstance().queryGlobal(sql, new Object[]{uid});
        }else{
            String sql = "select gameUid, code, pf, time from correlation_info where gameUid = ? and code = ? limit 1";
            result = SFSMysql.getInstance().queryGlobal(sql, new Object[]{uid, correlationStr});
        }

        if(result != null && result.size() > 0){
            return result.getSFSObject(0);
        }

        return null;
    }

    public static boolean insertCorralationInfo(String uid, String correlationStr, String pf, int type){
        boolean ret = false;
        long now = System.currentTimeMillis();
        if(type == 0){//插入
            ret = SFSMysql.getInstance().executeGlobal("insert correlation_info (gameUid, code, pf, time) values (?, ?, ?, ?)", new Object[]{uid, correlationStr, pf, now});
        }else if(type == 1){//更新
            ret = SFSMysql.getInstance().executeGlobal("update correlation_info set code = ?, pf = ?, time = ? where gameUid = ?", new Object[]{correlationStr, pf, now, uid});
        }

        return ret;
    }

    public static long getLastMoveServerTime(String uid){
        long ret = 0l;
        ISFSArray result = SFSMysql.getInstance().queryGlobal("select time from move_server_record where uid = ? order by time desc limit 1", new Object[]{uid});
        if(result != null && result.size() > 0){
            ISFSObject obj = result.getSFSObject(0);
            if(obj.containsKey("time")){
                ret = obj.getLong("time");
            }
        }
        return ret;
    }

    public static String getUserPfId(UserProfile userProfile, String pf) {
        String pfId = null;
        if (userProfile.getLoginInfo() != null && userProfile.getLoginInfo().getPfSession() != null) {
            pfId = userProfile.getLoginInfo().getPfId();
        } else {
            List<String> bindPfList = new ArrayList<>();
            bindPfList.add(pf);
            ISFSArray bindInfo = GlobalDBProxy.getAccountBindInfo(userProfile.getUid(), bindPfList, null);
            if (bindInfo != null && bindInfo.size() != 0 && bindInfo.getSFSObject(0) != null) {
                ISFSObject bindObj = bindInfo.getSFSObject(0);
                if (bindObj.containsKey("bindId")) {
                    pfId = bindObj.getUtfString("bindId");
                }
            }
        }

        return pfId;
    }

    public static ISFSObject getDeeplink(String gaid, String promotionId) {
        ISFSObject retObj = null;
        String sql = "select * from deeplink where gaid = ? and promotionId = ?";
        ISFSArray result = SFSMysql.getInstance().queryGlobal(sql, new Object[]{gaid, promotionId});
        if (result != null && result.size() > 0) {
            retObj = result.getSFSObject(0);
        }

        return retObj;
    }
}
