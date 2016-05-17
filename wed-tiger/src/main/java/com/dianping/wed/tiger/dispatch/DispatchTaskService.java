/**
 * 
 */
package com.dianping.wed.tiger.dispatch;

import java.util.Date;

/**
 * @author yuantengkai 任务操作接口,对外暴露
 */
public interface DispatchTaskService {
	
	/**
	 * 添加一条任务
	 * 
	 * @param taskEntity:其中handler,loadbalance,earliestExecuteTime不能为空
	 * @return
	 */
	public long addDispatchTask(DispatchTaskEntity taskEntity);

	/**
	 * 根据任务id更新任务状态
	 * 
	 * @param taskId
	 * @param status
	 *            :see DispatchTaskService.TaskType
	 * @param hostName
	 * @return
	 */
	public boolean updateTaskStatus(long taskId, int status, String hostName);

	/**
	 * 增加重试次数,并设定下次执行时间
	 * 
	 * @param taskId
	 * @param nextExecuteTime
	 * @param hostName
	 * @return
	 */
	public boolean addRetryTimesAndExecuteTime(long taskId,
			Date nextExecuteTime, String hostName);

}
