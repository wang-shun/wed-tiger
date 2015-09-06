package com.dianping.wed.tiger.monitor.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.dianping.wed.tiger.monitor.core.model.MonitorRecord;

/**
 * monitor record
 * @author xuxueli
 */
public interface IMonitorService {

	/**
	 * 查询监控数据
	 * @param handlerName
	 * @param monitorTime
	 * @return
	 */
	public Map<String, List<MonitorRecord>> queryMonitorData(String handlerName, Date monitorTimeFrom, Date monitorTimeTo);

	/**
	 * 处理接收的监控数据
	 * @param originData
	 */
	public void receiveData(String originData);
	
}
