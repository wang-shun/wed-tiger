/**
 * 
 */
package com.dianping.wed.tiger.monitor;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.wed.tiger.ScheduleServer;

/**
 * @author yuantengkai
 *
 */
public class EventMonitor {

	private static final Logger logger = LoggerFactory
			.getLogger(EventMonitor.class);

	public static final int SUCCESS = 1;

	private static final EventMonitor instance = new EventMonitor();

	private BlockingQueue<MonitorDetail> monitorQueue;

	private ConcurrentHashMap<String, MonitorStatistics> monitorMap;

	private AtomicBoolean sendThreadInit = new AtomicBoolean(false);

	private AtomicBoolean queueThreadInit = new AtomicBoolean(false);

	private EventMonitor() {
		monitorMap = new ConcurrentHashMap<String, MonitorStatistics>(32);
		monitorQueue = new LinkedBlockingQueue<MonitorDetail>(10000);
	}

	public static EventMonitor getInstance() {
		return instance;
	}

	/**
	 * 记录监控数据
	 * 
	 * @param handler
	 * @param success
	 * @param cost
	 */
	public void record(String handler, int success, int cost) {
		monitorQueue.offer(new MonitorDetail(handler, success, cost));
		if (queueThreadInit.compareAndSet(false, true)) {
			startQueueDeal();
		}
	}

	/**
	 * 监控数据处理
	 */
	private void startQueueDeal() {
		Thread queueThread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {
						MonitorDetail mDetail = monitorQueue.take();
						MonitorStatistics mStatistics = monitorMap.get(mDetail
								.getHandler());
						if (mStatistics == null) {
							mStatistics = new MonitorStatistics();
							MonitorStatistics exist = monitorMap.putIfAbsent(
									mDetail.getHandler(), mStatistics);
							if (exist != null) {
								mStatistics = exist;
							}
						}
						if (mDetail.getSuccess() == EventMonitor.SUCCESS) {
							mStatistics.addSuccess(mDetail.getCostTime());
						} else {
							mStatistics.addFail();
						}
						if (sendThreadInit.compareAndSet(false, true)) {
							startSend();
						}
					} catch (InterruptedException e) {
						logger.error(
								"Event-Monitor-Queue happens InterruptedException exception,",
								e);
					}
				}
			}
		});
		queueThread.setDaemon(true);
		queueThread.setName("Event-Monitor-Queue");
		queueThread.start();

	}

	/**
	 * 监控数据发送
	 */
	private void startSend() {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(2 * 60 * 1000);
						for (Entry<String, MonitorStatistics> e : monitorMap
								.entrySet()) {
							long timestamp = new Date().getTime();
							String handler = e.getKey();
							MonitorStatistics ms = e.getValue();
							int totalCount = ms.getTotalCount();
							int successCount = ms.getSuccessCount();
							int failCount = ms.getFailCount();
							int avg = ms.getAvgCost();
							int max = ms.getMaxCost();
							int min = ms.getMinCost();
							StringBuilder sb = new StringBuilder();
							sb.append(timestamp).append("|");
							sb.append(handler).append("|");
							sb.append(
									ScheduleServer.getInstance()
											.getServerName()).append("|");
							sb.append(totalCount).append("|");
							sb.append(successCount).append("|");
							sb.append(failCount).append("|");
							sb.append(avg).append("|");
							sb.append(max).append("|");
							sb.append(min);
							httpSend(sb.toString());
						}
						monitorMap.clear();
					} catch (InterruptedException e) {
						logger.error(
								"Event-Monitor-Send happens InterruptedException exception,",
								e);
					} catch (Throwable t) {
						logger.error(
								"Event-Monitor-Send happens unknow exception,",
								t);
					}
				}

			}

		});
		t.setDaemon(true);
		t.setName("Event-Monitor-Send");
		t.start();
	}

	private void httpSend(String parameter) throws HttpException, IOException {
		HttpClient client = new HttpClient();
		String encodeParam = URLEncoder.encode(parameter, "utf-8");
		String url = ScheduleServer.getInstance().getMonitorIP()
				+ "/tiger/monitor?tm=" + encodeParam;
		HttpMethod method = new GetMethod(url);
		client.executeMethod(method);
		if (method.getStatusCode() != 200) {
			logger.warn("send monitor returns fail,url=" + url + ",httpcode:"
					+ method.getStatusCode());
		}
	}
}
