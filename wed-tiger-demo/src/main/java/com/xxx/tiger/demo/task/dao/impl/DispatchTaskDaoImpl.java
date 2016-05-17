package com.xxx.tiger.demo.task.dao.impl;

import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.orm.ibatis.support.SqlMapClientDaoSupport;

import com.xxx.tiger.demo.task.dao.DispatchTaskDao;
import com.xxx.tiger.demo.task.dataobject.TigerTaskDo;

/**
 * 任务表操作dao
 * @author yuantengkai
 *
 */
public class DispatchTaskDaoImpl extends SqlMapClientDaoSupport implements DispatchTaskDao{

	@Override
	public long addDispatchTask(TigerTaskDo entity) throws SQLException {
		Long id = (Long) getSqlMapClient().insert("tigerTask.insert", entity);
		if(id == null){
			return 0;
		}
		return id;
	}

	@Override
	public boolean updateTaskStatus(long id, int status, String hostName) {
		Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", id);
        params.put("status", status);
        params.put("hostName", hostName);
        int num = getSqlMapClientTemplate().update("tigerTask.updateTaskStatus", params);
        if(num < 1){
        	return false;
        }
		return true;
	}

	@Override
	public boolean addRetryTimesAndExecuteTime(long id, Date nextExecuteTime,
			String hostName) {
		Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", id);
        params.put("nextExecuteTime", nextExecuteTime);
        params.put("hostName", hostName);
        int num = getSqlMapClientTemplate().update("tigerTask.updateRetryTimesAndExecuteTime", params);
        if(num < 1){
        	return false;
        }
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TigerTaskDo> findDispatchTasksWithLimit(String handler,
			List<Integer> nodeList, int limit) {
		Map<String, Object> param = new HashMap<String, Object>();
        param.put("handler", handler);
        param.put("nodeList", nodeList);
        param.put("limit", limit);
        return getSqlMapClientTemplate().queryForList("tigerTask.queryTasksWithLimit", param);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TigerTaskDo> findDispatchTasksWithLimitByBackFetch(
			String handler, List<Integer> nodeList, int limit, long id) {
		Map<String, Object> param = new HashMap<String, Object>();
        param.put("handler", handler);
        param.put("nodeList", nodeList);
        param.put("limit", limit);
        param.put("id", id);
        return getSqlMapClientTemplate().queryForList("tigerTask.queryTasksWithLimitByBackFetch", param);
	}

}
