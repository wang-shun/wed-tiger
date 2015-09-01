package com.xxl.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.stereotype.Service;

import com.xxl.core.model.MonitorRecord;
import com.xxl.core.util.FileDbUtil;
import com.xxl.service.IMonitorRecordService;

/**
 * monitor record
 * 
 * @author xuxueli
 */
@Service("monitorRecordService")
public class MonitorRecordServiceImpl implements IMonitorRecordService {
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
		localDateCache.put(cacheKey, map);
		localTimCache.put(cacheKey, System.currentTimeMillis());
		return map;
	}
	
	public static void main(String[] args) {
		System.out.println(System.currentTimeMillis());
	}

}
