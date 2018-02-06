package com.geng.utils;

import com.geng.exception.ExceptionMonitorType;
import com.geng.gameengine.login.COKLoginExceptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by lifangkai on 15/1/15.
 */
public class COKLoggerFactory {
    private static final ThreadLocalStringBuilder threadLocalStringBuilderHolder = new ThreadLocalStringBuilder(128);

    public static final Logger userLogger = LoggerFactory.getLogger("com.elex.cok.user");
    public static final Logger exceptionLogger = LoggerFactory.getLogger("com.elex.cok.MoniterTestException");
    public static final Logger USER_RESOURCE_LOGGER = LoggerFactory.getLogger("com.elex.cok.es.user.resource");

    public static final Logger zhengchengLogger = LoggerFactory.getLogger("zhengcheng@elex-tech.com");
    public static final Logger wangzhiyuanLogger = LoggerFactory.getLogger("wangzhiyuan@elex-tech.com");

    public enum ExceptionOwner {
        COMMON(null),
        LFK("lifangkai@elex-tech.com"),
        ZC("zhengcheng@elex-tech.com"),
        LYJ("liyongjun@elex-tech.com"),
        WZY("wangzhiyuan@elex-tech.com"),
        BSL("bushaolei@elex-tech.com"),
        HFL("haofanlu@elex-tech.com"),
        SSL("shushenglin@elex-tech.com"),
        SJY("sunjiayue@elex-tech.com"),
        GWP("guowenpeng@elex-tech.com"),
        XDL("xudelu@elex-tech.com"),
        QB("qinbinbin@elex-tech.com"),
        XL("xulin@elex-tech.com"),
        GY("gaoyun@elex-tech.com"),
    	GJ("gujun@elex-tech.com"),
        SP("shupei@elex-tech.com"),
        HP("hanpeng@elex-tech.com"),
        TJP("tangjianpei@elex-tech.com"),
        GYJ("gengyongjaing@elex-tech.com"),
        HYQ("huangyuanqiang@elex-tech.com");

        ExceptionOwner(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        private String name;
    }

    /**
     * 异常监控方法
     *
     * @param msg   异常描述
     * @param type  异常类型，方便分类统计和过滤
     * @param owner 方便只关注自己的异常
     * @param t     异常类，Throwable字类
     */
    public static void monitorException(String msg, ExceptionMonitorType type, ExceptionOwner owner, Throwable t) {
        exceptionLogger.error("[" + type.toString() + "] " + msg, type.toString(), owner.getName(), Constants.SERVER_ID, t);
    }

    public static void monitorException(String msg, ExceptionMonitorType type, ExceptionOwner owner) {
        exceptionLogger.error("[" + type.toString() + "] " + msg, type.toString(), owner.getName(), Constants.SERVER_ID);
    }

    public static String formatLog(ExceptionMonitorType type, String user, String desc) {
        return formatLog(type, "", user, "", desc);
    }

    public static String formatLog(ExceptionMonitorType type, String user, String ip, String desc) {
        return formatLog(type, "", user, ip, desc);
    }

    public static String formatLog(ExceptionMonitorType type, COKLoginExceptionType subType, String user, String desc) {
        return formatLog(type, subType, user, "", desc);
    }

    public static String formatLog(ExceptionMonitorType type, COKLoginExceptionType subType, String user, String ip, String desc) {
        return formatLog(type, subType.toString(), user, ip, desc);
    }

    public static String formatLog(ExceptionMonitorType type, String subTypeString, String user, String ip, String desc) {
        StringBuilder log = threadLocalStringBuilderHolder.get().getStringBuilder();
        log.append(Constants.LOG_SEPARATOR).append(subTypeString)//子类型
                .append(Constants.LOG_SEPARATOR).append(user)
                .append(Constants.LOG_SEPARATOR).append(ip)
                .append(Constants.LOG_SEPARATOR).append(desc);
        return log.toString();
    }
/**
    public static String formatLog(StatType type, Object[] array) {
        StringBuilder log = threadLocalStringBuilderHolder.get().getStringBuilder();
        log.append(Constants.SERVER_ID);
        log.append(Constants.LOG_SEPARATOR).append(type.toString());
        if (array != null) {
            for (Object obj : array) {
                log.append(Constants.LOG_SEPARATOR).append(obj);
            }
        }
        return log.toString();
    }

    public static String formatLog(StatType type, List<Object> array) {
        StringBuilder log = threadLocalStringBuilderHolder.get().getStringBuilder();
        log.append(Constants.SERVER_ID);
        log.append(Constants.LOG_SEPARATOR).append(type.toString());
        if (array != null) {
            for (Object obj : array) {
                log.append(Constants.LOG_SEPARATOR).append(obj);
            }
        }
        return log.toString();
    }**/
}
