/**
 * 
 */
package com.dianping.wed.tiger.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import com.dianping.wed.tiger.ScheduleServer;
import com.dianping.wed.tiger.event.EventConfig;

/**
 * @author yuantengkai 事件执行配置工具类
 */
public class EventConfigUtil {

	/**
	 * 同步配置信息
	 * 
	 * @return
	 */
	public static List<EventConfig> syncEventConfigs() {
		List<EventConfig> eventConfigs = new ArrayList<EventConfig>();
		ConcurrentHashMap<String, List<Integer>> handlers = ScheduleServer
				.getInstance().getHandlerMap();
		int identifyCode = ScheduleServer.getInstance().getHandlerIdentifyCode();
		for (Entry<String, List<Integer>> entry : handlers.entrySet()) {
			EventConfig event = new EventConfig();
			String handlerName = entry.getKey();
			List<Integer> nodeList = entry.getValue();
			event.setHandler(handlerName);
			event.setNodeList(nodeList);
			event.setIdentifyCode(identifyCode);
			eventConfigs.add(event);
		}
		return eventConfigs;
	}

}
