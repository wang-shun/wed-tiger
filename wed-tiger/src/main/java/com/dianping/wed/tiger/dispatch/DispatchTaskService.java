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
	 * 任务捞取策略
	 * @author yuantengkai
	 *
	 */
	public enum TaskFetchStrategy{
		
		Singler(0),Multi(1);
		
		private int value;

		private TaskFetchStrategy(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}

	/**
	 * 任务执行后状态
	 *
	 */
	public enum TaskType {
		NEW(0), SUCCESS(1), FAIL(2);

		private int value;

		private TaskType(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}

	/**
	 * 添加一条任务
	 * 
	 * @param taskEntity
	 * @return
	 */
	public int addDispatchTask(DispatchTaskEntity taskEntity);

	/**
	 * 根据任务id更新任务状态
	 * 
	 * @param taskId
	 * @param status
	 *            :see DispatchTaskService.TaskType
	 * @param hostName
	 * @return
	 */
	public boolean updateTaskStatus(int taskId, int status, String hostName);

	/**
	 * 增加重试次数,并设定下次执行时间
	 * 
	 * @param taskId
	 * @param nextExecuteTime
	 * @param hostName
	 * @return
	 */
	public boolean addRetryTimesAndExecuteTime(int taskId,
			Date nextExecuteTime, String hostName);

}
