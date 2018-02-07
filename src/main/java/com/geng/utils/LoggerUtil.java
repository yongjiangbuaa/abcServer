/**
 * @author: lifangkai@elex-tech.com
 * @date: 2013年9月5日 下午6:09:13
 */
package com.geng.utils;


/**
 * 日志工具类
 */
public class LoggerUtil {

    public void recordException(Exception e) {

    }

    public enum GoodsUseType {
    }

    public enum GoodsGetType {
         LEVEL_UP, REWARD_RANDOM;
    }
    public static LoggerUtil getInstance() {
        return LazyHolder.INSTANCE;
    }
    private static class LazyHolder {
        private static final LoggerUtil INSTANCE = new LoggerUtil();
    }
}
