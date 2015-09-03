package com.dianping.wed.tiger.monitor.core.thread;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.dianping.wed.tiger.monitor.core.util.FileDbUtil;

/**
 * 消息队列
 * 
 * @author xuxueli 2015-9-1 16:57:16
 */
public class MonitorThreadHelper {

	private static MonitorThreadHelper helper = new MonitorThreadHelper();

	/**
	 * 如果数据接收处理不过来，则丢弃
	 */
	private ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 10, 200L,
			TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(20000),
			new ThreadPoolExecutor.DiscardPolicy());

	/**
	 * 异步处理监控数据
	 * 
	 * @param originData
	 */
	public static void dealMonitorDataAsync(final String originData) {
		helper.executor.execute(new Runnable() {
			@Override
			public void run() {
				FileDbUtil.dealReceiveData(originData);
			}
		});
	}

}
