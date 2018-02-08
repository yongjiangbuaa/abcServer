package com.geng.core;

import com.geng.utils.properties.PropertyFileReader;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * Author: shushenglin
 * Date:   16/9/28 10:24
 */
public class GameExecutor {
	private static final GameExecutor instance = new GameExecutor();

	private ThreadPoolExecutor httpRequestExecutor ;
	private ThreadPoolExecutor commonExecutor;


	public static GameExecutor getInstance() {
		return instance;
	}

	private GameExecutor() {
		init();
	}

	public ThreadPoolExecutor getHttpRequestExecutor() {
		return httpRequestExecutor;
	}

	public ThreadPoolExecutor getCommonExecutor() {
		return commonExecutor;
	}

	private void init() {
		int httpCorePoolSize = PropertyFileReader.getIntItem("pool.http.threads", "4");
		int httpMaxPoolSize = PropertyFileReader.getIntItem("pool.http.threads.max", "32");
		int httpMaxQueueSize = PropertyFileReader.getIntItem("pool.http.queue.size", "1024");
		httpRequestExecutor = new StandardThreadExecutor(httpCorePoolSize, httpMaxPoolSize, httpMaxQueueSize,
				GameScheduler.newThreadFactory("Http-pool-%d"));

		int commonPoolCoreSize = PropertyFileReader.getIntItem("pool.common.threads", "4");
		int commonPoolMaxSize = PropertyFileReader.getIntItem("pool.common.threads", "16");
		int commonPoolMaxQueueSize = PropertyFileReader.getIntItem("pool.common.threads", "1024");
		commonExecutor = new StandardThreadExecutor(commonPoolCoreSize, commonPoolMaxSize, commonPoolMaxQueueSize,
				GameScheduler.newThreadFactory("Common-pool-%d"));
	}

	public void shutdown(){
		if(httpRequestExecutor != null){
			httpRequestExecutor.shutdown();
			httpRequestExecutor = null;
		}
		if (commonExecutor != null){
			commonExecutor.shutdown();
			commonExecutor = null;
		}
	}
}
