package com.geng.gameengine.account;

import com.geng.utils.SFSMysql;
import com.geng.utils.distributed.GlobalDBProxy;
import com.geng.core.data.ISFSArray;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;

/**
 * Created by lifangkai on 15/7/20.
 * 一个账户对应多个设备ID
 * 为解决手游盗号问题，登录时检测gameUid和deviceId之间有无联系
 */
public class AccountDeviceMapping {
    private static final Logger logger = LoggerFactory.getLogger(AccountDeviceMapping.class);

    public enum AccountDeviceMappingType {
        REPAIR, REGISTER, CHANGE, IOS_CHANGE, GM_REPAIR, LOGIN_REPAIR, LOGIN2_REPAIR
    }
    /**
     * 增加一条gameUid和deviceId的绑定
     *
     * @param gameUid
     * @param deviceId
     * @return
     */
    public static void addMapping(String gameUid, String deviceId, AccountDeviceMappingType type) {
        if(type == AccountDeviceMappingType.CHANGE && containsDeviceId(gameUid, deviceId)) {
            return;
        }
        String sql = "insert into account_device_mapping(gameUid, deviceId, type, time) values(?,?,?,?)";
        try {
            SFSMysql.getInstance().executeGlobalWithException(sql, new Object[]{gameUid, deviceId, type.ordinal(), System.currentTimeMillis()});
        } catch (Exception e) {
            logger.error("AccountDeviceMapping, uid {} bind device {}, for {}", gameUid, deviceId, type.toString());
        }
    }

    /**
     * 根据gameUid查看是否与此deviceId匹配
     *
     * @param gameUid
     * @param deviceId
     * @return
     */
    public static boolean containsDeviceId(String gameUid, String deviceId) {
        String sql = "select gameUid, deviceId from account_device_mapping where gameUid = ? and deviceId = ?";
        try {
            ISFSArray array = SFSMysql.getInstance().queryGlobal(sql, new Object[]{gameUid, deviceId});
            if (array != null && array.size() > 0) {
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("AccountDeviceMapping, uid {} is mapping device {}", gameUid, deviceId);
            return true;
        }
    }

    /**
     * 修复数据
     * @param gameUid
     * @param userProfileDeviceId
     */
    public static void repair(String gameUid, String userProfileDeviceId) {
        if(containsGameUid(gameUid)) {
            return;
        }
        Map<String, String> map = GlobalDBProxy.selectAccountFields(gameUid, Arrays.asList("deviceId"));
        String globalDeviceId = map.get("deviceId");
        if(StringUtils.isBlank(userProfileDeviceId) && StringUtils.isBlank(globalDeviceId)) {
            return;
        }
        if(userProfileDeviceId == null) {
            addMapping(gameUid, globalDeviceId, AccountDeviceMappingType.REPAIR);
        } else if(userProfileDeviceId.equals(globalDeviceId)) {
            addMapping(gameUid, userProfileDeviceId, AccountDeviceMappingType.REPAIR);
        } else {
            addMapping(gameUid, userProfileDeviceId, AccountDeviceMappingType.REPAIR);
            if(globalDeviceId != null) {
                addMapping(gameUid, globalDeviceId, AccountDeviceMappingType.REPAIR);
            }
        }
    }

    private static boolean containsGameUid(String gameUid) {
        String sql = "select gameUid from account_device_mapping where gameUid = ? limit 1";
        try {
            ISFSArray array = SFSMysql.getInstance().queryGlobal(sql, new Object[]{gameUid});
            if (array != null && array.size() > 0) {
                return true;
            }
            return false;
        } catch (Exception e) {
            logger.error("AccountDeviceMapping, is contains uid {}", gameUid);
            return true;
        }
    }

}
