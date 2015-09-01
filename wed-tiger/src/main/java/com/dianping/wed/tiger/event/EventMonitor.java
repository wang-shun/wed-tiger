/**
 * 
 */
package com.dianping.wed.tiger.event;

import java.io.IOException;
import java.util.Date;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.wed.tiger.ScheduleServer;
import com.dianping.wed.tiger.monitor.MonitorStatistics;

/**
 * @author yuantengkai
 *
 */
public class EventMonitor {

	private static final Logger logger = LoggerFactory
			.getLogger(EventMonitor.class);

	public static final int SUCCESS = 1;

	private static final EventMonitor instance = new EventMonitor();

	private ConcurrentHashMap<String, MonitorStatistics> monitorMap;

	private AtomicBoolean sendThreadInit = new AtomicBoolean(false);

	private EventMonitor() {
		monitorMap = new ConcurrentHashMap<String, MonitorStatistics>(32);
	}

	public static EventMonitor getInstance() {
		return instance;
	}

	public void record(String handler, int success, int cost) {
		MonitorStatistics ms = monitorMap.get(handler);
		if (ms == null) {
			ms = new MonitorStatistics();
			MonitorStatistics exist = monitorMap.putIfAbsent(handler, ms);
			if (exist != null) {
				ms = exist;
			}
		}
		if (success == EventMonitor.SUCCESS) {
			ms.addSuccess(cost);
		} else {
			ms.addFail();
		}
		if (sendThreadInit.compareAndSet(false, true)) {
			startSend();
		}

	}

	private void startSend() {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(2000);
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
								"Event-Monitor happens InterruptedException exception,",
								e);
					} catch (Throwable t) {
						logger.error("Event-Monitor happens unknow exception,",
								t);
					}
				}

			}

		});
		t.setDaemon(true);
		t.setName("Event-Monitor");
		t.start();
	}

	private void httpSend(String parameter) throws HttpException, IOException {
		HttpClient client = new HttpClient();
		String url = ScheduleServer.getInstance().getMonitorIP()+"?tm=" + parameter;
		HttpMethod method = new GetMethod(url);
		client.executeMethod(method);
		if (method.getStatusCode() != 200) {
			logger.warn("send monitor returns fail,url=" + url + ",httpcode:"
					+ method.getStatusCode());
		}
	}
}
