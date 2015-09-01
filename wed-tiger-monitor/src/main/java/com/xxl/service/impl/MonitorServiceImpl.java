package com.xxl.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.xxl.core.model.MonitorRecord;
import com.xxl.core.thread.MonitorThreadHelper;
import com.xxl.core.util.FileDbUtil;
import com.xxl.service.IMonitorService;

/**
 * monitor record
 * 
 * @author xuxueli
 */
@Service("monitorService")
public class MonitorServiceImpl implements IMonitorService {
	private static Logger logger = LoggerFactory.getLogger(FileDbUtil.class);
	
	private static final SimpleDateFormat formatDate = new SimpleDateFormat("yyyyMMdd");
	private static ConcurrentMap<String, Map<String, List<MonitorRecord>>> localDateCache = new ConcurrentHashMap<String, Map<String,List<MonitorRecord>>>();
	private static ConcurrentMap<String, Long> localTimCache = new ConcurrentHashMap<String, Long>();
	
	/*
	 * load monitor data
	 * @see com.xxl.service.IMonitorRecordService#loadMonitorInfo(java.lang.String, java.util.Date)
	 */
	@Override
	public Map<String, List<MonitorRecord>> loadMonitorData(String hadleName, Date monitorTime) {
		String cacheKey = formatDate.format(monitorTime).concat("_").concat(hadleName);
		
		Map<String, List<MonitorRecord>> cacheDate = localDateCache.get(cacheKey);
		Long cacheTim = localTimCache.get(cacheKey);
		if (cacheDate!=null && cacheTim!=null && System.currentTimeMillis() - cacheTim < 60 * 1000) {
			return cacheDate;
		}
		
		Map<String, List<MonitorRecord>> map = FileDbUtil.loadMonitorData(hadleName, monitorTime);
		if (MapUtils.isNotEmpty(map)) {
			localDateCache.put(cacheKey, map);
			localTimCache.put(cacheKey, System.currentTimeMillis());
		}
		return map;
	}

	/*
	 * push data
	 * @see com.xxl.service.IMonitorService#pushData(java.lang.String)
	 */
	@Override
	public void pushData(String originData) {
		logger.info("push data start :{}", originData);
		MonitorRecord record = FileDbUtil.parseLineData(originData);
		if (record != null) {
			MonitorThreadHelper.pushData(originData);
		} else {
			logger.info("push data fail:{}", originData);
		}
	}

}
