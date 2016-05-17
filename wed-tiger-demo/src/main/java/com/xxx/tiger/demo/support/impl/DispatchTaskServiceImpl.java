/**
 * 
 */
package com.xxx.tiger.demo.support.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import com.alibaba.fastjson.JSON;
import com.dianping.wed.tiger.ScheduleServer;
import com.dianping.wed.tiger.dispatch.DispatchMultiService;
import com.dianping.wed.tiger.dispatch.DispatchTaskEntity;
import com.dianping.wed.tiger.utils.ScheduleConstants;
import com.xxx.tiger.demo.task.dao.DispatchTaskDao;
import com.xxx.tiger.demo.task.dataobject.TigerTaskDo;

/**
 * @author yuantengkai
 * 各个handler捞取各自任务的策略
 */
public class DispatchTaskServiceImpl implements DispatchMultiService{
	
	private static final Logger logger = LoggerFactory.getLogger(DispatchTaskServiceImpl.class);

	@Resource
	private DispatchTaskDao dispatchTaskDao;
	
	@Override
	public long addDispatchTask(DispatchTaskEntity taskEntity) {
		if(taskEntity == null || StringUtils.isBlank(taskEntity.getHandler())){
			return 0;
		}
		TigerTaskDo entity = new TigerTaskDo();
		entity.setHandler(taskEntity.getHandler());
		int loadbanlance = Math.abs(taskEntity.getLoadbalance());
		entity.setNode(loadbanlance % ScheduleServer.getInstance().getNumOfVisualNode());
		entity.setRetryTimes(0);
		entity.setStatus(ScheduleConstants.TaskType.NEW.getValue());
		if(taskEntity.getEarliestExecuteTime() == null){
			entity.setEarliestExecuteTime(new Date());
		}else{
			entity.setEarliestExecuteTime(taskEntity.getEarliestExecuteTime());
		}
		if(StringUtils.isBlank(taskEntity.getParameter())){
			Map<String,String> param = new HashMap<String,String>();
			entity.setParameter(JSON.toJSONString(param));
		}else{
			entity.setParameter(taskEntity.getParameter());
		}
		try {
			long id = dispatchTaskDao.addDispatchTask(entity);
			return id;
		} catch (Exception e) {
			logger.error("dao insert dispatchTask exception,"+entity,e);
		}
		return 0;
	}

	@Override
	public boolean updateTaskStatus(long taskId, int status, String hostName) {
		if(taskId < 1){
			return false;
		}
		return dispatchTaskDao.updateTaskStatus(taskId, status, hostName);
	}

	@Override
	public boolean addRetryTimesAndExecuteTime(long taskId,
			Date nextExecuteTime, String hostName) {
		if(taskId < 1 || nextExecuteTime == null){
			return false;
		}
		return dispatchTaskDao.addRetryTimesAndExecuteTime(taskId, nextExecuteTime, hostName);
	}

	@Override
	public List<DispatchTaskEntity> findDispatchTasksWithLimit(String handler,
			List<Integer> nodeList, int limit) {
		if(StringUtils.isBlank(handler) || nodeList == null || nodeList.size() == 0){
			return null;
		}
		List<TigerTaskDo> doList = dispatchTaskDao.findDispatchTasksWithLimit(handler, nodeList, limit);
		if(doList == null || doList.size() == 0){
			return null;
		}
		List<DispatchTaskEntity> taskList = new ArrayList<DispatchTaskEntity>();
		for(TigerTaskDo tt:doList){
			DispatchTaskEntity task = new DispatchTaskEntity();
			BeanUtils.copyProperties(tt, task);
			taskList.add(task);
		}
		return taskList;
	}

	@Override
	public List<DispatchTaskEntity> findDispatchTasksWithLimitByBackFetch(
			String handler, List<Integer> nodeList, int limit, long taskId) {
		if(StringUtils.isBlank(handler) || nodeList == null 
				|| nodeList.size() == 0 || taskId < 1){
			return null;
		}
		List<TigerTaskDo> doList = dispatchTaskDao.findDispatchTasksWithLimitByBackFetch(handler, nodeList, limit, taskId);
		if(doList == null || doList.size() == 0){
			return null;
		}
		List<DispatchTaskEntity> taskList = new ArrayList<DispatchTaskEntity>();
		for(TigerTaskDo tt:doList){
			DispatchTaskEntity task = new DispatchTaskEntity();
			BeanUtils.copyProperties(tt, task);
			taskList.add(task);
		}
		return taskList;
	}

}
