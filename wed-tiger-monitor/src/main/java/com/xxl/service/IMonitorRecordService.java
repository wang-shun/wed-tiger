package com.xxl.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.xxl.core.model.MonitorRecord;

/**
 * monitor record
 * @author xuxueli
 */
public interface IMonitorRecordService {

	/**
	 * load monitor data
	 * @param hadleName
	 * @param monitorTime
	 * @return
	 */
	Map<String, List<MonitorRecord>> loadMonitorData(String hadleName, Date monitorTime);
	
}
