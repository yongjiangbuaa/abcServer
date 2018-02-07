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

    public void logBySFS(String parse_blob_exception) {

    }

    public void recordGoldCost(String uid, int goldType, int ordinal, int p1, int p2, long oldGold, long i, long remainGold, int i1) {

    }

    public enum GoodsUseType {
    }

    public enum GoodsGetType {
         LEVEL_UP, BUY;
    }
    public static LoggerUtil getInstance() {
        return LazyHolder.INSTANCE;
    }

    public enum FunctionPoint {
        DUNGEONS

    }

    public enum GoldCostType {ITEM}

    private static class LazyHolder {
        private static final LoggerUtil INSTANCE = new LoggerUtil();
    }
}
