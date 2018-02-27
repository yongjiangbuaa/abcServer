/**
 * @author: lifangkai@elex-tech.com
 * @date: 2013年9月5日 下午2:31:47
 */
package com.geng.core;

import com.geng.exceptions.ExceptionMonitorType;
import com.geng.gameengine.*;
import com.geng.gameengine.mail.MailServicePlus;
import com.geng.gameengine.mail.MailSrcFuncType;
import com.geng.gameengine.mail.MailType;
import com.geng.utils.*;
import com.geng.utils.properties.PropertyFileReader;
import com.geng.utils.xml.GameConfigManager;
import com.google.common.base.Splitter;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.geng.core.data.ISFSArray;
import com.geng.core.data.ISFSObject;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;


/**
 * 定时器
 */
public class GameScheduler {
    private static final Logger logger = LoggerFactory.getLogger(GameScheduler.class);

    private ScheduledThreadPoolExecutor taskScheduler;
    private ScheduledThreadPoolExecutor longRunningTaskScheduler;
    private ScheduledThreadPoolExecutor worldScheduler;
    private ScheduledExecutorService worldFlushScheduler;

    private GameScheduler() {
        taskScheduler = COKExecutors.newScheduledThreadPool(PropertyFileReader.getIntItem("scheduler.task.threads", "4"), newThreadFactory("TaskScheduler-%d"));
        longRunningTaskScheduler = COKExecutors.newScheduledThreadPool(PropertyFileReader.getIntItem("scheduler.longRunningTask.threads", "32"), newThreadFactory("LRTaskScheduler-%d"));
        worldScheduler = COKExecutors.newScheduledThreadPool(PropertyFileReader.getIntItem("scheduler.world.threads", "16"), newThreadFactory("WorldScheduler-%d"));
        worldFlushScheduler = Executors.newSingleThreadScheduledExecutor(newThreadFactory("World-flush-%d"));
    }

    public ScheduledExecutorService getWorldFlushScheduler() {
        return worldFlushScheduler;
    }

    public ScheduledThreadPoolExecutor getWorldScheduler() {
        return worldScheduler;
    }

    public ScheduledThreadPoolExecutor getLongRunningTaskScheduler() {
        return longRunningTaskScheduler;
    }

    public ScheduledThreadPoolExecutor getTaskScheduler() {
        return taskScheduler;
    }

    public void init() {
        logger.info("game scheduler is ready");
    }


    public static ThreadFactory newThreadFactory(String nameFormat){
		String nameF = "S" + Constants.SERVER_ID_STR + "-" + nameFormat;
        return new ThreadFactoryBuilder().setNameFormat(nameF).build();
    }

    public ScheduledFuture<?> schedule(Runnable task, int delay, TimeUnit unit) {
        if(logger.isDebugEnabled()) {
            logger.debug("Task scheduled: " + task + ", " + delay + " " + unit);
        }
        return this.taskScheduler.schedule(task, (long) delay, unit);
    }

    public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, int initialDelay, int period, TimeUnit unit) {
        return this.taskScheduler.scheduleAtFixedRate(task, (long) initialDelay, (long) period, unit);
    }

    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,
                                                         long initialDelay,
                                                         long delay,
                                                         TimeUnit unit) {
        return this.taskScheduler.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }

    public ScheduledFuture<?> scheduleLongRunningTaskAtFixedRate(Runnable task, int initialDelay, int period, TimeUnit unit) {
        return this.longRunningTaskScheduler.scheduleAtFixedRate(task, (long) initialDelay, (long) period, unit);
    }


    public void destroy(){
        List awaitTask = taskScheduler.shutdownNow();
        logger.info("task scheduler stopping. Tasks awaiting execution:" + awaitTask);
        longRunningTaskScheduler.shutdown();
        worldScheduler.shutdown();
        worldFlushScheduler.shutdown();
    }



    private static class LazyHolder {
        private static final GameScheduler INSTANCE = new GameScheduler();
    }

    public static GameScheduler getInstance() {
        return LazyHolder.INSTANCE;
    }
}
