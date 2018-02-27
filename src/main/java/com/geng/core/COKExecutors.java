package com.geng.core;

import java.util.concurrent.*;

/**
 * 统一管理线程池的创建
 * 
 * @author jiangmin.wu
 *
 */
public class COKExecutors {
	public static ExecutorService newFixedThreadPool(int nThreads) {
		return new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>());
	}

	public static ExecutorService newFixedThreadPool(int nThreads, ThreadFactory threadFactory) {
		return new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>(), threadFactory);
	}

	public static ThreadPoolExecutor newThreadPoolExecutor(int corePoolSize, int maxPoolSize, int maxQueueSize, ThreadFactory factory){
		return new StandardThreadExecutor(corePoolSize, maxPoolSize, maxQueueSize, factory);
	}

	public static ScheduledExecutorService newSingleThreadScheduledExecutor() {
		ScheduledThreadPoolExecutor scheduled = new ScheduledThreadPoolExecutor(1);
		scheduled.setRemoveOnCancelPolicy(true);
		return scheduled;
	}

	public static ScheduledExecutorService newSingleThreadScheduledExecutor(ThreadFactory threadFactory) {
		ScheduledThreadPoolExecutor scheduled = new ScheduledThreadPoolExecutor(1, threadFactory);
		scheduled.setRemoveOnCancelPolicy(true);
		return scheduled;
	}

	public static ScheduledThreadPoolExecutor newScheduledThreadPool(int corePoolSize) {
		ScheduledThreadPoolExecutor scheduled = new ScheduledThreadPoolExecutor(corePoolSize);
		scheduled.setRemoveOnCancelPolicy(true);
		return scheduled;
	}

	public static ScheduledThreadPoolExecutor newScheduledThreadPool(int corePoolSize, ThreadFactory threadFactory) {
		ScheduledThreadPoolExecutor scheduled = new ScheduledThreadPoolExecutor(corePoolSize, threadFactory);
		scheduled.setRemoveOnCancelPolicy(true);
		return scheduled;
	}

	/** Cannot instantiate. */
	private COKExecutors() {
	}
}
