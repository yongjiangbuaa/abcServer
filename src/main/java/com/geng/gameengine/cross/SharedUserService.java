package com.geng.gameengine.cross;

import com.geng.exceptions.ExceptionMonitorType;
import com.geng.puredb.model.UserProfile;
import com.geng.utils.*;
import com.geng.utils.distributed.GlobalDBProxy;
import com.geng.utils.myredis.MyRedis;
import com.geng.utils.myredis.R;
import com.geng.utils.properties.PropertyFileReader;
import com.geng.utils.redis.RedisKey;
import com.google.common.base.Optional;
import com.geng.core.data.ISFSArray;
import com.geng.core.data.ISFSObject;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by lifangkai on 14/11/3.
 */
public class SharedUserService {
    private static final Logger logger = LoggerFactory.getLogger(SharedUserService.class);

    private final static String CROSS_USER_INFO_KEY = "cross_user_info_global";
    private final static String CROSS_USER_SERVER_KEY = "cross_user_server_global";
    private final static String CROSS_USER_ONLINE_KEY = "cross_user_online_global";
    private final static String RATIO_OF_CHOOSE_SERVER = "RATIO_OF_CHOOSE_SERVER";

    private MyRedis getRs() {
//        return RedisSession.getGlobal(true);
        return R.Global();
    }

    private boolean isCodisOpen = false;

    public SharedUserService() {
        isCodisOpen = PropertyFileReader.getBooleanItem("codis_open", "false");//RedisSession.isCodisOpen();
    }

    public Map<String, Boolean> getUserOnlineList(List<String> uidList) {
        Map<String, Boolean> map = new HashMap<>();
        if (uidList == null || uidList.isEmpty()) {
            return map;
        }
        for (String uid : uidList) {
            map.put(uid, false);
        }
        if (isCodisOpen) {
            return getUserOnlineListCodis(map, uidList);
        }
        List<String> list = getRs().hMGet(CROSS_USER_ONLINE_KEY, uidList);
        for (String uidServer : list) {
            if (StringUtils.isBlank(uidServer)) continue;
            String[] arr = StringUtils.split(uidServer, ":");
            if (arr.length > 1) {
                map.put(arr[0], arr[1].equals("1"));
            }
        }
        return map;
    }

    private Map<String, Boolean> getUserOnlineListCodis(Map<String, Boolean> map, List<String> uidList) {
        Map<String, String> list = getRs().getBatch(RedisKey.USER_ONLINE, uidList);
        for (Map.Entry<String, String> entry : list.entrySet()) {
            String uid = entry.getKey();
            String online = entry.getValue();
            if (StringUtils.isNotBlank(online)) {
                map.put(uid, online.equals("1"));
            }
        }
        return map;
    }

    public void updateUserOnline(String uid, boolean isOnline) {
        String value = isOnline ? "1" : "0";
        if (!isCodisOpen) {
            getRs().hSet(CROSS_USER_ONLINE_KEY, uid, uid + ":" + value);
        } else {
            getRs().set(RedisKey.USER_ONLINE.suffix(uid), value);
        }
    }

    public boolean isUserOnline(String uid){
        List<String> ids = new ArrayList<>();
        ids.add(uid);
        Map<String, Boolean> userOnlineMap = getUserOnlineList(ids);
        boolean isOnline = userOnlineMap.containsKey(uid) ? userOnlineMap.get(uid) : false;
        return isOnline;
    }

    /**
     * 根据uid列表获取SharedUserInfo列表
     *
     * @param uidList
     * @return
     */
    public Map<String, SharedUserInfo> getSharedUserInfoList(List<String> uidList, boolean isNeedDefault) {
        Map<String, SharedUserInfo> userInfoMap = new HashMap<>();
        if (uidList == null || uidList.isEmpty()) {
            return userInfoMap;
        }
        Collection<String> userInfoJsonList;
        try {
            if (isCodisOpen) {
                userInfoJsonList = getRs().getBatch(RedisKey.USER_INFO, uidList).values();
            } else {
                userInfoJsonList = getRs().hMGet(CROSS_USER_INFO_KEY, uidList);
            }
        } catch (Exception jex) {
            logger.error("getSharedUserInfoList error", jex);
            return userInfoMap;
        }
        for (String json : userInfoJsonList) {
            if (StringUtils.isBlank(json)) continue;
            SharedUserInfo userInfo;
            try {
                userInfo = new SharedUserInfo(json);
            } catch (Exception e) {
                userInfo = new SharedUserInfo();
            }
            userInfoMap.put(userInfo.getUid(), userInfo);
        }
        if (isNeedDefault) {
            Collection<String> notInitUidList = CollectionUtils.subtract(uidList, userInfoMap.keySet());
            for (String uid : notInitUidList) {
                SharedUserInfo sharedUserInfo = new SharedUserInfo();
                userInfoMap.put(uid, sharedUserInfo);
            }
        }
        return userInfoMap;
    }

    /**
     * 根据uid获取SharedUserInfo
     *
     * @param uid
     * @return
     */
    public SharedUserInfo getSharedUserInfo(String uid) {
        String json;
        try {
            if (isCodisOpen) {
                json = getRs().get(RedisKey.USER_INFO.suffix(uid));
            } else {
                json = getRs().hGet(CROSS_USER_INFO_KEY, uid);
            }
        } catch (Exception jex) {
            logger.error("getSharedUserInfo for uid {} ", uid, jex);
            return new SharedUserInfo();
        }
        if (StringUtils.isBlank(json)) {
            return new SharedUserInfo();
        }
        return new SharedUserInfo(json);
    }

    /**
     * 更新
     *
     * @param userProfile
     */
    public void updateUserInfo(UserProfile userProfile) {
        String uid = userProfile.getUid();
        SharedUserInfo userInfo = new SharedUserInfo(userProfile);
        if (!isCodisOpen) {
//            RedisSession redisSession = null;
            try {
//                redisSession = RedisSession.getGlobal(false);
                R.Global().hSet(CROSS_USER_INFO_KEY, uid, userInfo.toString());
            } catch (Exception e) {
                COKLoggerFactory.monitorException("update user info", ExceptionMonitorType.GLOBAL_REDIS, COKLoggerFactory.ExceptionOwner.LFK, e);
            } finally {
//                if (redisSession != null) {
//                    redisSession.close();
//                }
            }
        } else {
            getRs().set(RedisKey.USER_INFO.suffix(uid), userInfo.toString());
        }
    }

    public void updateUidServerIdMap(String uid, int serverId) {
        if (!isCodisOpen) {
//            RedisSession redisSession = null;
            try {
//                redisSession = RedisSession.getGlobal(false);
                R.Global().hSet(CROSS_USER_SERVER_KEY, uid, uid + ":" + Integer.toString(serverId));
            } catch (Exception e) {
                COKLoggerFactory.monitorException(String.format("updateUidServerIdMap uid: %s sid: %d",uid, serverId),
                        ExceptionMonitorType.GLOBAL_REDIS, COKLoggerFactory.ExceptionOwner.SSL, e);
            } finally {
//                if (redisSession != null) {
//                    redisSession.close();
//                }
            }
        } else {
            getRs().set(RedisKey.USER_SERVER.suffix(uid), Integer.toString(serverId));
        }
    }

    public int getServerId(String uid) {
        if (isCodisOpen) {
            return getServerIdCodis(uid);
        }
        int serverId = 0;
        String uidServer;
        try {
            uidServer = getRs().hGet(CROSS_USER_SERVER_KEY, uid);
        } catch (Exception e) {
            COKLoggerFactory.monitorException("getServerId for uid: " + uid, ExceptionMonitorType.GLOBAL_REDIS,
                    COKLoggerFactory.ExceptionOwner.SSL, e);
            return getServerIdByGlobal(uid);
        }
        if (uidServer != null) {
            String[] arr = StringUtils.split(uidServer, ":");
            if (arr.length > 1) {
                serverId = Integer.parseInt(arr[1]);
            }
        }
        if (serverId == 0) {
            serverId = getServerIdBySuffix(uid);
        }
        return serverId;
    }

    public int getServerIdCodis(String uid) {
        int serverId = 0;
        String server;
        try {
            server = getRs().get(RedisKey.USER_SERVER.suffix(uid));
        } catch (Exception jex) {
            return getServerIdByGlobal(uid);
        }
        if (server != null) {
            serverId = Integer.parseInt(server);
        }
        if (serverId == 0) {
            serverId = getServerIdBySuffix(uid);
        }
        return serverId;
    }

    public Map<String, Integer> getServerId(Set<String> uidSet) {
        if (isCodisOpen) {
            return getServerIdCodis(uidSet);
        }
        Map<String, Integer> map = new HashMap<>();
        List<String> uidList = new ArrayList<>();
        try {
            for (String uid : uidSet) {
                uidList.add(uid);
                map.put(uid, getServerIdBySuffix(uid));
            }
            if (uidList.isEmpty()) {
                return map;
            }
        } catch (Exception e) {
            return map;
        }
        List<String> list;
        try {
            list = getRs().hMGet(CROSS_USER_SERVER_KEY, uidList);
        } catch (Exception e) {
            COKLoggerFactory.monitorException("getServerId for uid list: " + uidList, ExceptionMonitorType.GLOBAL_REDIS,
                                COKLoggerFactory.ExceptionOwner.SSL, e);
            getServerIdByGlobalDB(map, uidSet);
            return map;
        }
        for (String uidServer : list) {
            if (StringUtils.isBlank(uidServer)) continue;
            String[] arr = StringUtils.split(uidServer, ":");
            if (arr.length > 1) {
                map.put(arr[0], Integer.parseInt(arr[1]));
            }
        }
        return map;
    }

    public Map<String, Integer> getServerIdCodis(Set<String> uidSet) {
        Map<String, Integer> map = new HashMap<>();
        Map<String, String> list;
        try {
            list = getRs().getBatch(RedisKey.USER_SERVER, uidSet);
            for (Map.Entry<String, String> entry : list.entrySet()) {
                String uid = entry.getKey();
                String server = entry.getValue();
                if (StringUtils.isNotBlank(server)) {
                    map.put(uid, Integer.parseInt(server));
                }
            }
        } catch (Exception e) {
            COKLoggerFactory.monitorException("getServerId for uid list: " + uidSet, ExceptionMonitorType.GLOBAL_REDIS,
                                            COKLoggerFactory.ExceptionOwner.SSL, e);
            getServerIdByGlobalDB(map, uidSet);
            return map;
        }
        return map;
    }

    private static void getServerIdByGlobalDB(Map<String, Integer> map, Set<String> uidSet) {
        String sql = GlobalDBProxy.toSelectAccountPreparedBatchSQL(Arrays.asList("gameUid", "server"), uidSet).toString();
        ISFSArray array = SFSMysql.getInstance().queryGlobal(sql, uidSet.toArray());
        for (int i = 0; i < array.size(); i++) {
            ISFSObject obj = array.getSFSObject(i);
            if (obj.containsKey("gameUid") && obj.containsKey("server")) {
                map.put(obj.getUtfString("gameUid"), Integer.parseInt(obj.getUtfString("server")));
            }
        }
    }

    public static int getServerIdByGlobal(String uid) {
        ISFSArray array = SFSMysql.getInstance().queryGlobal("select server from account_new where gameUid = ?", new String[]{uid});
        if (array != null && array.size() == 1) {
            ISFSObject obj = array.getSFSObject(0);
            if (obj.containsKey("server")) {
                return Integer.parseInt(obj.getUtfString("server"));
            }
        }
        return Constants.SERVER_ID;
    }

    public static Optional<Integer> selectServerId(String uid) {
        Integer serverId = null;
        ISFSArray array = SFSMysql.getInstance().queryGlobal("select server from account_new where gameUid = ?", new String[]{uid});
        if (array != null && array.size() == 1) {
            ISFSObject obj = array.getSFSObject(0);
            if (obj.containsKey("server")) {
                serverId = Integer.valueOf(obj.getUtfString("server"));
            }
        }
        return Optional.fromNullable(serverId);
    }

    public static boolean isCurrServer(int serverId) {
        return serverId == Constants.SERVER_ID;
    }

    public static int getServerIdBySuffix(String uid) {
        String sub = StringUtils.substring(uid, uid.length() - Constants.SERVER_ID_MAX);
        try {
            int serverId = Integer.parseInt(sub) % 1000000;
            return serverId;
        } catch (NumberFormatException e) {
            LoggerUtil.getInstance().recordException(e);
            return Constants.SERVER_ID;
        }
    }

    public Map<String, Integer> getServerListRatio(){
        Map<String, Integer> serverRatio = new HashMap<>();
        try{
            String ratioStr = getRs().get(RATIO_OF_CHOOSE_SERVER);
            if(ratioStr != null && !ratioStr.isEmpty()){
                String [] serverRatioArr = StringUtils.split(ratioStr, ";");
                for(String ratio: serverRatioArr){
                    String[] serverRatioInfo = StringUtils.split(ratio, ":");
                    if(serverRatioInfo != null && serverRatioInfo.length == 2){
                        serverRatio.put(serverRatioInfo[0], Integer.parseInt(serverRatioInfo[1]));
                    }
                }
            }
        }catch (Exception e){
            COKLoggerFactory.monitorException("get serverRatio error", ExceptionMonitorType.GLOBAL_REDIS, COKLoggerFactory.ExceptionOwner.GWP, e);
        }

        return serverRatio;
    }
}
