/**
 * 
 */
package com.xxx.tiger.demo.task.dao;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import com.xxx.tiger.demo.task.dataobject.TigerTaskDo;

/**
 * @author yuantengkai
 *
 */
public interface DispatchTaskDao {
	
	
	public long addDispatchTask(TigerTaskDo entity) throws SQLException;
	
	public boolean updateTaskStatus(long id, int status, String hostName);

	public boolean addRetryTimesAndExecuteTime(long id,Date nextExecuteTime, String hostName);

	public List<TigerTaskDo> findDispatchTasksWithLimit(String handler,List<Integer> nodeList, int limit);

	public List<TigerTaskDo> findDispatchTasksWithLimitByBackFetch(String handler, List<Integer> nodeList, int limit, long id);
}
