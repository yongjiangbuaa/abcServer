package com.geng.utils.redis;

/**
 * Created by lifangkai on 15/8/17.
 */
public enum RedisKey {
    USER_INFO, // global, append uid
    USER_SERVER, // global, append uid
    DEVICE_CHANGE_ACCOUNT_COUNT, //切换帐号次数，验证码服务。
    USER_ONLINE //用户在线
    ;

    public String suffix(String suffix) {
        return toString() + "_" + suffix;
    }

    public String prefix(String pre) {
        return pre + "_" + toString();
    }
}
