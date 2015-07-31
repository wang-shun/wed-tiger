/**
 * 
 */
package com.dianping.wed.tiger.dispatch;

import java.util.Date;
import java.util.List;

/**
 * @author yuantengkai
 * 任务操作接口,对外暴露
 */
public interface DispatchTaskService {
	
	public enum TaskType{
		NEW(0),
		SUCCESS(1),
		FAIL(2);
		
		private int value;
		
		private TaskType(int value){
			this.value = value;
		}
		
		public int getValue() {
			return value;
		}
	}
	
	/**
	 * 添加一条任务
	 * @param taskEntity
	 * @return
	 */
	public int addDispatchTask(DispatchTaskEntity taskEntity);
	
	/**
	 * 获取一定数量的任务
	 * @param handler 任务名称
	 * @param nodeList 任务节点
	 * @param limit 任务上限数
	 * @return
	 */
	public List<DispatchTaskEntity> findDispatchTasksWithLimit(
			String handler, List<Integer> nodeList, int limit);
	
	/**
	 * 反压获取一定数量的任务
	 * @param handler
	 * @param nodeList
	 * @param limit
	 * @param taskId
	 * @return
	 */
	public List<DispatchTaskEntity> findDispatchTasksWithLimitByBackFetch(
			String handler, List<Integer> nodeList, int limit,int taskId);
	
	/**
	 * 根据任务id更新任务状态
	 * @param taskId
	 * @param status:see DispatchTaskService.TaskType
	 * @param hostName
	 * @return
	 */
	public boolean updateTaskStatus(int taskId,int status,String hostName);
	
	/**
	 * 增加重试次数,并设定下次执行时间
	 * @param taskId
	 * @param nextExecuteTime
	 * @param hostName
	 * @return
	 */
	public boolean addRetryTimesAndExecuteTime(int taskId,Date nextExecuteTime,String hostName);

}
