package com.dianping.wed.tiger.monitor.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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

	private static Logger logger = LoggerFactory
			.getLogger(MonitorServiceImpl.class);

	private static final SimpleDateFormat FormatDate_yyyyMMdd = new SimpleDateFormat(
			"yyyyMMdd");

	private static ConcurrentMap<String, Map<String, List<MonitorRecord>>> localDateCache = new ConcurrentHashMap<String, Map<String, List<MonitorRecord>>>(
			32);

	private static ConcurrentMap<String, Long> localTimeCache = new ConcurrentHashMap<String, Long>(
			32);
	
	private static ConcurrentMap<String, Object> localObjCache = new ConcurrentHashMap<String, Object>();

	@Override
	public Map<String, List<MonitorRecord>> queryMonitorData(
			String handlerName, Date monitorTimeFrom, Date monitorTimeTo) {

		Map<String, List<MonitorRecord>> wholeMap = new HashMap<String, List<MonitorRecord>>();

		// ========cache deal======
		String cacheKey = FormatDate_yyyyMMdd.format(monitorTimeFrom)
				.concat("_").concat(handlerName);

		String cacheDateKey = FormatDate_yyyyMMdd.format(new Date());

		Long cacheDate = localTimeCache.get(cacheDateKey);
		if (cacheDate == null) {// 一天清空一次本地缓存
			Long exist = localTimeCache.putIfAbsent(cacheDateKey, 1L);
			if (exist == null) {
				localDateCache.clear();
				localTimeCache.clear();
				localTimeCache.put(cacheDateKey, 1L);
			}
		} else {
			Long cacheTim = localTimeCache.get(cacheKey);

			Map<String, List<MonitorRecord>> cacheData = localDateCache
					.get(cacheKey);

			if (cacheData != null && cacheTim != null
					&& System.currentTimeMillis() - cacheTim < 60 * 1000) {// 60s内缓存
				wholeMap = cacheData;
			}
		}
		// key-hostname
		Map<String, List<MonitorRecord>> resultMap = new HashMap<String, List<MonitorRecord>>();

		if (MapUtils.isEmpty(wholeMap)) {
			wholeMap = FileDbUtil
					.queryMonitorData(handlerName, monitorTimeFrom);
			if (MapUtils.isNotEmpty(wholeMap)) {
				localDateCache.put(cacheKey, wholeMap);
				localTimeCache.put(cacheKey, System.currentTimeMillis());
			}
		}
		// =======end========
		if (MapUtils.isNotEmpty(wholeMap)) {
			// 过滤出符合条件的时间
			for (Entry<String, List<MonitorRecord>> item : wholeMap.entrySet()) {
				List<MonitorRecord> list = new ArrayList<MonitorRecord>();
				if (CollectionUtils.isNotEmpty(item.getValue())) {
					for (MonitorRecord record : item.getValue()) {
						if (record.getMonitorTime().after(monitorTimeFrom)
								&& record.getMonitorTime()
										.before(monitorTimeTo)) {
							list.add(record);
						}
					}
					resultMap.put(item.getKey(), list);
				}
			}
		}
		return resultMap;
	}

	@Override
	public void receiveData(String originData) {
		if (logger.isInfoEnabled()) {
			logger.info("receive data start :{}", originData);
		}
		MonitorRecord record = FileDbUtil.parseLineData(originData);
		if (record == null) {
			logger.warn("parase receive data fail:{}", originData);
			throw new WebException(ReturnCodeEnum.FAIL.code(), "数据解析错误.");
		}
		MonitorThreadHelper.dealMonitorDataAsync(originData);
	}

	@SuppressWarnings("unchecked")
	@Override
	public HashSet<String> queryMonitorHandler(Date monitorTime) {
		if (monitorTime == null) {
			return null;
		}
		String timKey = "queryMonitorHandler_tim_".concat(FormatDate_yyyyMMdd.format(monitorTime));
		String dataKey = "queryMonitorHandler_data_".concat(FormatDate_yyyyMMdd.format(monitorTime));
		Long tim = (Long) (localObjCache.get(timKey)!=null?localObjCache.get(timKey):-1L);
		HashSet<String> list = (HashSet<String>) (localObjCache.get(dataKey)!=null?localObjCache.get(dataKey):null);
		if (System.currentTimeMillis() - tim > 2 * 60 * 1000) {
			tim = System.currentTimeMillis();
			list = FileDbUtil.queryMonitorHandler(monitorTime);
			localObjCache.put(timKey, tim);
			localObjCache.put(dataKey, list);
		}
		return list;
	}

}
