package com.dianping.wed.tiger.monitor.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.dianping.wed.tiger.monitor.core.constant.CommonDic.ReturnCodeEnum;
import com.dianping.wed.tiger.monitor.core.exception.WebException;
import com.dianping.wed.tiger.monitor.core.model.MonitorRecord;
import com.dianping.wed.tiger.monitor.core.thread.MonitorThreadHelper;
import com.dianping.wed.tiger.monitor.core.util.FileDbUtil;
import com.dianping.wed.tiger.monitor.service.IMonitorService;

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
	 * load monitor data	// 注意002：此处注释为查询缓存，推荐线上打开注释缓存，可大大增加查询效率，默认缓存时间60s
	 * @see com.dianping.wed.tiger.monitor.service.IMonitorRecordService#loadMonitorInfo(java.lang.String, java.util.Date)
	 */
	@Override
	public Map<String, List<MonitorRecord>> loadMonitorData(String handlerName, Date monitorTimeFrom, Date monitorTimeTo) {
		/*String cacheKey = formatDate.format(monitorTime).concat("_").concat(handlerName);
		
		Map<String, List<MonitorRecord>> cacheDate = localDateCache.get(cacheKey);
		Long cacheTim = localTimCache.get(cacheKey);
		if (cacheDate!=null && cacheTim!=null && System.currentTimeMillis() - cacheTim < 60 * 1000) {
			return cacheDate;
		}*/
		
		Map<String, List<MonitorRecord>> map = FileDbUtil.loadMonitorData(handlerName, monitorTimeFrom);
		Map<String, List<MonitorRecord>> resultMap = new HashMap<String, List<MonitorRecord>>();
		if (MapUtils.isNotEmpty(map)) {
			for (Entry<String, List<MonitorRecord>> item : map.entrySet()) {
				List<MonitorRecord> list = new ArrayList<MonitorRecord>();
				if (CollectionUtils.isNotEmpty(item.getValue())) {
					for (MonitorRecord record : item.getValue()) {
						if (record.getMonitorTime().after(monitorTimeFrom) && record.getMonitorTime().before(monitorTimeTo)) {
							list.add(record);
						}
					}
					resultMap.put(item.getKey(), list);
				}
				
			}
		}
		
		/*if (MapUtils.isNotEmpty(map)) {
			localDateCache.put(cacheKey, map);
			localTimCache.put(cacheKey, System.currentTimeMillis());
		}*/
		return resultMap;
	}

	/*
	 * push data
	 * @see com.dianping.wed.tiger.monitor.service.IMonitorService#pushData(java.lang.String)
	 */
	@Override
	public void pushData(String originData) {
		logger.info("push data start :{}", originData);
		MonitorRecord record = FileDbUtil.parseLineData(originData);
		if (record == null) {
			logger.info("push data fail:{}", originData);
			throw new WebException(ReturnCodeEnum.FAIL.code(),"数据解析错误.");
		} 
		MonitorThreadHelper.pushData(originData);
	}

}
