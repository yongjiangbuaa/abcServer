/**
 * @author: lifangkai@elex-tech.com
 * @date: 2013年9月5日 下午6:09:13
 */
package com.geng.utils;

import com.geng.core.GameEngine;
import com.geng.exceptions.ExceptionMonitorType;
import com.geng.puredb.model.Queue;
import com.geng.puredb.model.UserProfile;
import com.geng.utils.properties.PropertyFileReader;
import com.geng.utils.stat.StatType;
import com.geng.core.data.ISFSObject;
//import com.smartfoxserver.v2.extensions.ExtensionLogLevel;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 日志工具类
 */
public class LoggerUtil {
    private static final Logger logger = LoggerFactory.getLogger(LoggerUtil.class);
    private static final int GOLD_COST_PARAM_COUNT = 11;
    private static final int GOODS_COST_PARAM_COUNT = 10;
    private static final int PARAM_COUNT = 5;
    private static final int DATA_COUNT = 8;
    private static final int CRYSTAL_COST_PARAM_COUNT = 10;
    private static final int POWER_CHANGE_PARAM_COUNT = 17;

    public enum CrystalCostType {
        ITEM,REWARD,SELL,MAIL_AWARD,PUSH_AWARD_ADD, BUSINESSMAN, USE_ITEM
    }

    public enum GoldCostType {
        ITEM,
        CD,
        UNLOCK_STORY,
        HEART,
        STEP
        ;

        public static GoldCostType getCDType(Queue.QueueType queueType) {
            GoldCostType goldCostType = CD;

            return goldCostType;
        }

        public static GoldCostType getCDResourceType(Queue.QueueType queueType) {
            GoldCostType goldCostType = CD;

            return goldCostType;
        }
    }

    /*
     *  旧版 ALLIANCE_STAT 联盟相关统计，param1=0 apply，param1=1 join，param1=2 加入联盟，param1=3 创建联盟
     *  ALLIANCE_BUY_USER, 联盟商店用户购买到自己背包，param1=0无限商品， param1=1有限商品
     */
    public enum GameDataType {
        FUNCTION_USE, RANDOM_GENERAL, OPEN_PANEL, QUEST_REWARD, WORLD_MONSTER, ALLIANCE_STAT, ALLIANCE_TEAM_BATTLE, COLLECT_RESOURCE, ALLIANCE_DONATE,
        ALLIANCE_BUY_R4R5, ALLIANCE_BUY_USER, RESOURCE_DECREASE, EXCHANGE_POPUP, OPEN_EXCHANGE, VIP_EXTEND, CROSS_SERVER, OPEN_TREASURE, SHARE_TREASURE, TREASURE_INVITE,
        TREASURE_SHARE_PLAYER, TREASURE_INVITE_PLAYER, SIGN_IN_SHARE, SIGN_IN_REQUEST, EQUIP_ADD, EQUIP_REMOVE
    }

    //新版 联盟统计类型
    public enum AllianceStatType {
        APPLY, JOIN, INTO, CREATE, DISMISS, LEAVE, SEARCH
    }

    public enum FunctionUserType {
        SCIENCE_UPGRADE, SCIENCE_UPGRADE_DIRECT, SCIENCE_CD_CLEAR, ADD_ABILITY_POINT, LEARN_ABILITY, BUILDING_CCD, BUILDING_CCD_FREE,
        CREAT_BUILDING_DIRECT, MAKE_SOLDIER, MAKE_SOLDIER_CCD, MAKE_SOLDIER_DIRECT, MAKE_FORT, MAKE_FORT_CCD, MAKE_FORT_DIRECT, BUY_ITEM, MARCH_RESOURCE,
        MARCH_PALACE, MARCH_SCOUT, MARCH_PLAYER, ALLIANCE_TRADE, MARCH_REINFORCE, SEND_MAIL, VIP_EXTEND,
    }

    public enum GoodsGetType {
        LEVEL_UP,REWARD, REWARD_RANDOM, BUY, BUY_HOT_ITEM,
    }

    public enum GoodsUseType {

    }

    private LoggerUtil() {
    }

    public enum FunctionPoint{
        TEST(0),//测试

        ;

        private int value;

        FunctionPoint(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static int valueOfName(FunctionPoint name) {
            int type_value = name.getValue();
            return type_value;
        }
    }

    /**
     * 功能、活动打点
     * @param userId
     * @param category_enum 大类型
     * @param type  小类型
     * @param int_data
     * @param var_data
     */
    public void recordFunctionPointLog(String userId, FunctionPoint category_enum, int type, int[] int_data, String[] var_data) {
        int category = FunctionPoint.valueOfName(category_enum);
        if(PropertyFileReader.getBooleanItem("scribe_enable")){
            writeLog2Scribe(userId, category, type, int_data, var_data);
        }
    }
    private static ThreadLocalStringBuilder stringBuilderThreadLocal = new ThreadLocalStringBuilder(128);

    private static final String FIELD_DELIMITER = ",";

    private void writeLog2Scribe(String userId, int category, int type, int[] int_data, String[] var_data) {
        StringBuilder stringBuilder = stringBuilderThreadLocal.get().getStringBuilder();
        long time = System.currentTimeMillis();
        stringBuilder.append(Constants.SERVER_ID).append(FIELD_DELIMITER)
                .append(userId).append(FIELD_DELIMITER)
                .append(time).append(FIELD_DELIMITER)
                .append(DateUtils.fromTimeToFromatStr(time,"yyyy-MM-dd")).append(FIELD_DELIMITER)
                .append(category).append(FIELD_DELIMITER)
                .append(type).append(FIELD_DELIMITER);
        for (int i = 0; i < 6; ++i) {
            if (int_data == null || i >= int_data.length) {
                stringBuilder.append(0).append(FIELD_DELIMITER);
            } else {
                stringBuilder.append(int_data[i]).append(FIELD_DELIMITER);
            }
        }
        for (int i = 0; i < 6; ++i) {
            if (var_data == null || i >= var_data.length) {
                stringBuilder.append(FIELD_DELIMITER);
            } else {
                stringBuilder.append(var_data[i]).append(FIELD_DELIMITER);
            }
        }
        // remove the last ","
        stringBuilder.setLength(stringBuilder.length() - 1);
        stringBuilder.append("\n");
        // write to scribe as function category
//        ScribeLogUtil.getInstance().addLog(ScribeLogUtil.ScribeCategory.CATEGORY_FUNCTION, stringBuilder.toString());
    }

    @Deprecated
    public void logBySFS(Object... args) {
        int length = args.length;
        StringBuilder sb = new StringBuilder("{}");
        for(int i=1; i<length; i++) {
            sb.append(", {}");
        }
        logger.info(sb.toString(), args);
    }
/*
    @Deprecated
    public void logBySFS(ExtensionLogLevel logLevel, Object... args) {
        int length = args.length;
        StringBuilder sb = new StringBuilder("{}");
        for(int i=1; i<length; i++) {
            sb.append(", {}");
        }
        switch (logLevel) {
            case DEBUG:
                logger.debug(sb.toString(), args);
                break;
            case INFO:
                logger.info(sb.toString(), args);
                break;
            case WARN:
                logger.warn(sb.toString(), args);
                break;
            case ERROR:
                logger.error(sb.toString(), args);
                break;
            default:
                logger.info(sb.toString(), args);
                break;

        }
    }*/

    public void recordException(Throwable e) {
        logger.error("exception", e);
    }


    public void recordDragonshardCost(Object... params) {
        if (params.length != CRYSTAL_COST_PARAM_COUNT - 2) {
            return;
        }
        String sql = "insert into crystal_cost_record(uid, userId, goldType, type, param1, param2, originalGold, cost, remainGold, time) values(?,?,?,?,?,?,?,?,?,?)";
        Object[] costParamArr = new Object[CRYSTAL_COST_PARAM_COUNT];
        int index = 0;
        costParamArr[index++] = GameService.getGUID();
        for (Object obj : params) {
            costParamArr[index++] = obj;
        }
        costParamArr[index] = System.currentTimeMillis();
        SFSMysql.getInstance().execute(sql, costParamArr);
    }

    /**
     * 金币消耗记录
     *
     * @param params
     */
    public void recordGoldCost(Object... params) {
        if (params.length != GOLD_COST_PARAM_COUNT - 2) { //去掉uid 和time
            logger.error("recordGoldCost params error");
            return;
        }
        String sql = "insert into gold_cost_record(uid, userId, goldType, type, param1, param2, originalGold, cost, remainGold, mailSrcType,time) values(?,?,?,?,?,?,?,?,?,?,?)";
        Object[] goldCostParamArr = new Object[GOLD_COST_PARAM_COUNT];
        int index = 0;
        goldCostParamArr[index++] = GameService.getGUID();
        for (Object obj : params) {
            goldCostParamArr[index++] = obj;
        }
        goldCostParamArr[index] = System.currentTimeMillis();

        SFSMysql.getInstance().execute(sql, goldCostParamArr);
    }

    /**
     * Vip点数变化记录
     *
     */
    public void recordVipScore( String uid, int originalVipScore, int add, int remainVipScore) {
        long createTime = System.currentTimeMillis();
        String tableName = "`vip_score_record`";
        StringBuilder sqlBuilder = new StringBuilder
                ("insert into " + tableName + " (uuid, uid, originalVipScore, `add`, `remainVipScore`, `createTime`) values(?,?,?,?,?,?)" );
        Object[] valueObj = new Object[6];
        int key = 0;
        valueObj[key++] = GameService.getGUID();
        valueObj[key++] = uid;
        valueObj[key++] = originalVipScore;
        valueObj[key++] = add;
        valueObj[key++] = remainVipScore;
        valueObj[key++] = createTime;
        SFSMysql.getInstance().execute(sqlBuilder.toString(), valueObj);
    }

    /*
    * 旅行商人的热卖记录
    * */
    public void recordHotItemRecord(String uid, String goodsId, int priceType, int price){
        String uuid = GameService.getGUID();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        long buyTime = Long.parseLong(sdf.format(new Date(System.currentTimeMillis())).toString());
        SFSMysql.getInstance().execute("insert hot_goods_cost_record values (?,?,?,?,?,?)", new Object[]{uuid, uid, goodsId, priceType, price, buyTime});
    }

	/**
     * get the goods cost log table name
     * @return
     */
    public static String getGoodsCostTableName() {
        String name = "goods_cost_record";
        if(PropertyFileReader.getBooleanItem("goods_log_partition","true")){
            LocalDate localDate = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMM");
            StringBuilder builder = new StringBuilder(25);
            builder.append(name).append("_")
            .append(localDate.format(formatter));
            return builder.toString();
        }
        return name;
    }

    /**
     * 物品消耗记录
     * 参数1 玩家uid
     * 参数2 物品id
     * 参数3 增加0 扣除1
     * 参数4 类型ordinal
     * 参数5 预留参数位
     * 参数6 初始数量
     * 参数7 变化数量
     * 参数8 最终数量
     */
    public void recordGoodsCost(Object... params) {
        if (params.length != GOODS_COST_PARAM_COUNT - 2) {
            return;
        }
        String tableName = getGoodsCostTableName();
        String sql = "insert into " + tableName + "(uid, time, userId, itemId, type, param1, param2, original, cost, remain) values(?,?,?,?,?,?,?,?,?,?)";
        Object[] goldCostParamArr = new Object[GOODS_COST_PARAM_COUNT];
        int index = 0;
        goldCostParamArr[index++] = GameService.getGUID();
        goldCostParamArr[index++] = System.currentTimeMillis();
        for (Object obj : params) {
            goldCostParamArr[index++] = obj;
        }

        logger.info(COKLoggerFactory.formatLog(StatType.GOODS_COST, goldCostParamArr));
        SFSMysql.getInstance().execute(sql, goldCostParamArr);

    }

    public void recordGoodsCostBatch(List<Object> logParams) {
        if (logParams.isEmpty() || logParams.size() % GOODS_COST_PARAM_COUNT != 0) {
            return;
        }
        int insertNum = logParams.size() / GOODS_COST_PARAM_COUNT;
        String tableName = getGoodsCostTableName();
        StringBuilder sql = new StringBuilder(256);
        sql.append("insert into ").append(tableName).
                append("(uid, time, userId, itemId, type, param1, param2, original, cost, remain) values");
        for (int index = 0; index < insertNum; index++) {
            sql.append("(?,?,?,?,?,?,?,?,?,?)");
            if (index != insertNum - 1) {
                sql.append(',');
            }
            logger.info(COKLoggerFactory.formatLog(StatType.GOODS_COST, logParams.subList(index * GOODS_COST_PARAM_COUNT, index * GOODS_COST_PARAM_COUNT + GOODS_COST_PARAM_COUNT)));
        }
        SFSMysql.getInstance().execute(sql.toString(), logParams.toArray());
    }

    /**
     * 日志记录方法
     *
     * @param userId
     * @param type
     */
    public void recordGameData(String userId, int type, int[] pArr,
                               int[] dataArr) {
        if (pArr == null) {
            pArr = new int[]{};
        }
        if (dataArr == null) {
            dataArr = new int[]{};
        }
        if (pArr.length == 0 && dataArr.length == 0) {
            recordGameData(userId, type);
            return;
        }
        StringBuilder sqlStringBuilder = new StringBuilder(
                "insert into logstat(uid,user,timeStamp,type,");
        int index = 0;
        int objLen = 4 + pArr.length + dataArr.length;
        Object[] objArr = new Object[objLen];
        objArr[index++] = GameService.getGUID();
        objArr[index++] = userId;
        objArr[index++] = System.currentTimeMillis();
        objArr[index++] = type;

        final String pName = "param";
        final String dName = "data";
        for (int p = 0; p < pArr.length; p++) {
            if (p == pArr.length - 1) {
                if (dataArr.length == 0) {
                    sqlStringBuilder.append(pName).append(p + 1)
                            .append(") values(");
                } else {
                    sqlStringBuilder.append(pName).append(p + 1).append(",");
                }
            } else {
                sqlStringBuilder.append(pName).append(p + 1).append(",");
            }
            objArr[index++] = pArr[p];
        }
        for (int d = 0; d < dataArr.length; d++) {
            if (d == dataArr.length - 1) {
                sqlStringBuilder.append(dName).append(d + 1)
                        .append(") values(");
            } else {
                sqlStringBuilder.append(dName).append(d + 1).append(",");
            }
            objArr[index++] = dataArr[d];
        }
        for (int ph = 0; ph < objLen; ph++) {
            if (ph == objLen - 1) {
                sqlStringBuilder.append("?)");
            } else {
                sqlStringBuilder.append("?,");
            }
        }
        SFSMysql.getInstance().execute(sqlStringBuilder.toString(), objArr);
    }

    public void recordGameData(String userId, int type) {
        SFSMysql.getInstance().execute(
                "insert into logstat(uid,user,timeStamp,type) values(?,?,?,?)",
                new Object[]{GameService.getGUID(), userId,
                        System.currentTimeMillis(), type});
    }

    public void recordGameLog(String userId, int category, int type, int[] param, String[] data) {
        StringBuilder sqlBuilder = new StringBuilder("insert into logrecord(uid,user,timeStamp,category,type ");
        if (param.length > 0) {
            for (int i = 1; i <= param.length; i++) {
                sqlBuilder.append(",param" + i);
            }

        }
        if (data.length > 0) {
            for (int j = 1; j <= data.length; j++) {
                sqlBuilder.append(",data" + j);
            }
        }
        sqlBuilder.append(") values(?,?,?,?,?");
        int size = 5 + param.length + data.length;
        Object[] valueObj = new Object[size];
        int key = 0;
        valueObj[key++] = GameService.getGUID();
        valueObj[key++] = userId;
        valueObj[key++] = System.currentTimeMillis();
        valueObj[key++] = category;
        valueObj[key++] = type;
        if (param.length > 0) {
            for (int i = 0; i < param.length; i++) {
                sqlBuilder.append(",?");
                valueObj[key++] = param[i];
            }

        }
        if (data.length > 0) {
            for (int j = 0; j < data.length; j++) {
                sqlBuilder.append(",?");
                valueObj[key++] = data[j];
            }
        }
        sqlBuilder.append(")");
        SFSMysql.getInstance().execute(sqlBuilder.toString(), valueObj);
    }

    public void recordGameData(String userId, int type, int[] allData) {
        List<Integer> pList = new ArrayList<>();
        List<Integer> dataList = new ArrayList<>();
        if (allData.length > PARAM_COUNT) {
            for (int i = 0; i < PARAM_COUNT; i++) {
                pList.add(allData[i]);
            }
            for (int j = PARAM_COUNT; j < allData.length; j++) {
                dataList.add(allData[j]);
            }
        } else {
            recordGameData(userId, type, allData, new int[]{});
            return;
        }
        if (dataList.size() > DATA_COUNT) {
            return;
        }
        int[] pArr = new int[pList.size()];
        for (int k = 0; k < pList.size(); k++) {
            pArr[k] = pList.get(k);
        }
        int[] dataArr = new int[dataList.size()];
        for (int h = 0; h < pList.size(); h++) {
            dataArr[h] = pList.get(h);
        }
        recordGameData(userId, type, pArr, dataArr);
    }

    public void recordMailGiftLog(Object... params){
        try {
            if (params.length != 7) {
                return;
            }
            String sql = "insert into mail_gift_log(uid, fromUid, toUid, contents, rewards, fromName, sendTime, toName, isOpened, isRewarded, isThanks, receiveTime) values(?,?,?,?,?,?,?,?,?,?,?,?)";
            Object[] paramArr = new Object[12];
            int index = 0;
            for (Object obj : params) {
                paramArr[index++] = obj;
            }
            String fromName = (paramArr[5] == null)? "": paramArr[5].toString();
            if (StringUtils.isBlank(fromName)){
                String fromUid = paramArr[1].toString();
                UserProfile senderUserProfile = GameEngine.getInstance().getPresentUserProfile(fromUid);
                if (senderUserProfile == null){
                    senderUserProfile = UserProfile.getWithUid(fromUid);
                }
                if (senderUserProfile != null){
                    fromName = senderUserProfile.getName();
                }
            }
            String toName = "";
            String toUid = paramArr[2].toString();
            UserProfile targetUserProfile = GameEngine.getInstance().getPresentUserProfile(toUid);
            if (targetUserProfile == null){
                targetUserProfile = UserProfile.getWithUid(toUid);
            }
            if (targetUserProfile != null){
                toName = targetUserProfile.getName();
            }
            paramArr[5] = fromName;
            paramArr[index++] = toName;
            paramArr[index++] = 0;
            paramArr[index++] = 0;
            paramArr[index++] = 0;
            paramArr[index] = 0;
            SFSMysql.getInstance().executeGlobal(sql, paramArr);
        }catch (Exception e){
            LoggerUtil.getInstance().recordException(e);
        }
    }

    public void updateMailGiftLog(String mailUid, int logType, long sendTime){
        try {
            Object[] paramArr = new Object[]{mailUid, sendTime};
            StringBuilder sqlStringBuilder = new StringBuilder("update mail_gift_log set ");
            switch (logType){
                case 0:
                    sqlStringBuilder.append("isOpened = 1, receiveTime = ").append(System.currentTimeMillis());
                    break;
                case 1:
                    sqlStringBuilder.append("isRewarded = 1");
                    break;
                case 2:
                    sqlStringBuilder.append("isThanks = 1");
                    break;
                default:
                    return;
            }
            sqlStringBuilder.append(" where uid = ? and sendTime = ?");
            SFSMysql.getInstance().executeGlobal(sqlStringBuilder.toString(), paramArr);
        }catch (Exception e){
            LoggerUtil.getInstance().recordException(e);
        }
    }

    public void recordLotteryLog(Object... params){
        try {
            if (params.length != 9) {
                return;
            }
            String sql = "insert into lottery_log(uid, type, superMode, lotteryId, lotteryInfo, result, position, cost, name, createTime) values(?,?,?,?,?,?,?,?,?,?)";
            Object[] paramArr = new Object[10];
            int index = 0;
            for (Object obj : params) {
                paramArr[index++] = obj;
            }
            paramArr[index] = System.currentTimeMillis();
            SFSMysql.getInstance().execute(sql, paramArr);
        }catch (Exception e){
            LoggerUtil.getInstance().recordException(e);
        }
    }

    public void recordPayAction(String uid, String exchangeId, String pf){
        try {
            String sql = "insert into pay_action(uid, exchangeId, pf, createTime) values(?,?,?,?) ON DUPLICATE KEY UPDATE pf=VALUES(pf)";
            Object[] paramArr = new Object[]{uid, exchangeId, pf, System.currentTimeMillis()};
            SFSMysql.getInstance().execute(sql, paramArr);
        }catch (Exception e){
            LoggerUtil.getInstance().recordException(e);
        }
    }

    public void recordNoticeCost(Object... params){
        try {
            if (params.length != 4) {
                return;
            }
            String sql = "insert into notice_cost_log(uid, type, itemId, cost, createTime) values(?,?,?,?,?)";
            Object[] paramArr = new Object[5];
            int index = 0;
            for (Object obj : params) {
                paramArr[index++] = obj;
            }
            paramArr[index] = System.currentTimeMillis();
            SFSMysql.getInstance().execute(sql, paramArr);
        }catch (Exception e){
            LoggerUtil.getInstance().recordException(e);
        }
    }

    public void recordExchangeGiftLog(Object... params){
        try {
            if (params.length != 5) {
                return;
            }
            String sql = "insert into exchange_gift_log(mailId, senderId, receiverId, exchangeId, sendTime, status, updateTime) values(?,?,?,?,?,?,?)";
            Object[] paramArr = new Object[7];
            int index = 0;
            for (Object obj : params) {
                paramArr[index++] = obj;
            }
            paramArr[index++] = 0;
            paramArr[index] = System.currentTimeMillis();
            SFSMysql.getInstance().execute(sql, paramArr);
        }catch (Exception e){
            LoggerUtil.getInstance().recordException(e);
        }
    }

    public void updateExchangeGiftLog(String mailId, long sendTime, int status){
        try {
            Object[] paramArr = new Object[]{status, System.currentTimeMillis(), mailId, sendTime};
            String sql = "update exchange_gift_log set status = ?, updateTime = ? where mailId = ? and sendTime = ?";
            SFSMysql.getInstance().execute(sql, paramArr);
        }catch (Exception e){
            LoggerUtil.getInstance().recordException(e);
        }
    }

    public void recordPayTotalLog(Object... params){
        try {
            if (params.length != 4) {
                return;
            }
            String sql = "insert into pay_total_log(uid, level, gold, type, createTime) values(?,?,?,?,?)";
            Object[] paramArr = new Object[5];
            int index = 0;
            for (Object obj : params) {
                paramArr[index++] = obj;
            }
            paramArr[index] = System.currentTimeMillis();
            SFSMysql.getInstance().execute(sql, paramArr);
        }catch (Exception e){
            LoggerUtil.getInstance().recordException(e);
        }
    }

    public void recordExchangeAllianceLog(String orderInfo) {
        try {
            if (StringUtils.isBlank(orderInfo)) return;
            String sql = "insert into exchange_alliance_log(uid,orderId,allianceId,createTime,status) values(?,?,?,?,?)";
            String[] orderArr = StringUtils.split(orderInfo, '|');
            Object[] paramArr = new Object[5];
            int index = 0;
            for (String str : orderArr) {
                paramArr[index++] = str;
            }
            paramArr[index] = 0;
            SFSMysql.getInstance().execute(sql, paramArr);
        } catch (Exception e) {
            COKLoggerFactory.monitorException("record exchange_alliance_log error", ExceptionMonitorType.OTHER, COKLoggerFactory.ExceptionOwner.HFL, e);
        }
    }

    public void updateExchangeAllianceLog(String orderInfo, int status) {
        try {
            if (StringUtils.isBlank(orderInfo)) return;
            String[] orderArr = StringUtils.split(orderInfo, '|');
            Object[] paramArr = new Object[]{status, orderArr[0], orderArr[1], orderArr[3]};
            String sql = "update exchange_alliance_log set status = ? where uid = ? and orderId = ? and createTime = ?";
            SFSMysql.getInstance().execute(sql, paramArr);
        } catch (Exception e) {
            COKLoggerFactory.monitorException("update exchange_alliance_log error", ExceptionMonitorType.OTHER, COKLoggerFactory.ExceptionOwner.HFL, e);
        }
    }

    public void recordMailRewardLog(String uid, String mailId, int type, int status) {
        try {
            String sql = "insert into mail_reward_log(uid,mailId,type,createTime,status) values(?,?,?,?,?)";
            Object[] paramArr = new Object[]{uid, mailId, type, System.currentTimeMillis(), status};
            SFSMysql.getInstance().execute(sql, paramArr);
        } catch (Exception e) {
            COKLoggerFactory.monitorException("record mail_reward_log error", ExceptionMonitorType.OTHER, COKLoggerFactory.ExceptionOwner.HFL, e);
        }
    }

    public enum AllianceRecordType {
        INVITE_RECORD, INVITE_RESPONSE, SHARE_BATTLE_REPORT, SHARE_DETECT_REPORT
    }

    /**
     * 联盟相关统计：
     * 联盟邀请迁城、联盟迁城应答、分享战报、分享侦查报告  to be continued...
     * @param type 哪种类型的联盟统计
     * @param fromUid
     * @param toUid @Nullable
     * @param logTime @Nullable
     * @param extraParaObj 其他参数，以json格式存入数据库中的extra字段
     * */
    public void recordAllianceRelatedStats(AllianceRecordType type, String fromUid, String toUid, Long logTime, ISFSObject extraParaObj){
        try{
            if(logTime == null)
                logTime = System.currentTimeMillis();
            String exPara = null;
            if(extraParaObj != null && extraParaObj.size() > 0)
                exPara = extraParaObj.toJson();
            String uid = GameService.getGUID();
            SFSMysql.getInstance().execute("insert into alliance_stats values (?,?,?,?,?,?)",
                    new Object[]{uid, type.ordinal(), fromUid, toUid, logTime, exPara});
        }catch (Exception e){
            COKLoggerFactory.zhengchengLogger.error("alliance statics error", e);
        }
    }

    public void recordExceptionUser(String uid, int type) {
        try {
            //1:没城点,2:参数加密,3:buidCode,4:包验证失败,5:盗号,6:gaid,7:资源援助,8:赠送50%行军上限,9:扫地图,10,11,12:扫地图,13:动态参数
            long now = System.currentTimeMillis();
            String sql = "insert into exception_user(uid, type, day, count, updateTime) values(?,?,?,?,?) ON DUPLICATE KEY UPDATE count=count+VALUES(count),updateTime=VALUES(updateTime)";
            SFSMysql.getInstance().execute(sql, new Object[]{uid, type, DateUtils.fromTimeToFromatStr(now, "yyyyMMdd"), 1, now});
        } catch (Exception e) {
        }
    }

    public void recordExceptionUser(String uid, int type, int count) {
        try {
            long now = System.currentTimeMillis();
            String sql = "insert into exception_user(uid, type, day, count, updateTime) values(?,?,?,?,?) ON DUPLICATE KEY UPDATE count=VALUES(count),updateTime=VALUES(updateTime)";
            SFSMysql.getInstance().execute(sql, new Object[]{uid, type, DateUtils.fromTimeToFromatStr(now, "yyyyMMdd"), count, now});
        } catch (Exception e) {
        }
    }

    public void recordPowerChange(String uid,long power,long powerChange,int reason,int questpower,int playerPower,int talentPower,int heroPower,long armyPower,int buildingPower,int sciencePower,long fortPower,int equipPower,long dragonPower) {
        long now = System.currentTimeMillis();
        String uuid = GameService.getGUID();
        long timeStamp = now;
        int day = Integer.parseInt(new SimpleDateFormat("YYYYMMdd").format(now));
        StringBuilder updateValueBuilder = new StringBuilder(32);
        updateValueBuilder.append("('")
                .append(uuid).append("',")
                .append(timeStamp).append(",")
                .append(day).append(",'")
                .append(uid).append("',")
                .append(power).append(",")
                .append(powerChange).append(",")
                .append(reason).append(",")
                .append(questpower).append(",")
                .append(playerPower).append(",")
                .append(talentPower).append(",")
                .append(heroPower).append(",")
                .append(armyPower).append(",")
                .append(buildingPower).append(",")
                .append(sciencePower).append(",")
                .append(fortPower).append(",")
                .append(equipPower).append(",")
                .append(dragonPower).append(")");
//        PowerLogUtil.getInstance().writeLog(updateValueBuilder.toString());
        logger.info(updateValueBuilder.toString());
    }
    private static class LazyHolder {
        private static final LoggerUtil INSTANCE = new LoggerUtil();
    }

    public static LoggerUtil getInstance() {
        return LazyHolder.INSTANCE;
    }
}
