/**
 * 
 */
package com.dianping.wed.tiger.dispatch;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dianping.wed.tiger.ScheduleManagerFactory;
import com.dianping.wed.tiger.ScheduleServer;

/**
 * @author yuantengkai
 *
 */
public class DispatchResultManager {

	private static final Logger logger = LoggerFactory
			.getLogger(DispatchResultManager.class);
	
	private static final DispatchResultManager instance = new DispatchResultManager();

	private static final int MAX_FAIL_TIMES = 60;

	private DispatchTaskService dispatchTaskService;
	
	private DispatchResultManager(){
		
	}
	
	public static DispatchResultManager getInstance() {
		return instance;
	}

	/**
	 * 执行完后续处理，成功－更新状态；失败－增加重试次数；丢弃－状态更新状态
	 * 
	 * @param result
	 * @param task
	 */
	public void processDispatchResult(DispatchResult result,
			DispatchTaskEntity task) {
		if (result == null || task == null) {
			return;
		}
		if (dispatchTaskService == null) {
			dispatchTaskService = (DispatchTaskService) ScheduleManagerFactory
					.getBean("dispatchTaskService");
		}
		if (DispatchResult.SUCCESS.equals(result)) {
			boolean flag = dispatchTaskService.updateTaskStatus(task.getId(),
					DispatchTaskService.TaskType.SUCCESS.getValue(),
					ScheduleServer.getInstance().getServerName());

			if (!flag) {
				logger.error("task execute success, update status failed,"
						+ task);
			}
			return;
		}
		if (DispatchResult.FAIL.equals(result)) {
			if (task.getRetryTimes() < MAX_FAIL_TIMES) {
				Calendar c = Calendar.getInstance();
				c.add(Calendar.MINUTE, task.getRetryTimes() + 1);// 每多重试一次，就多延迟1分钟后执行
				boolean flag = dispatchTaskService.addRetryTimesAndExecuteTime(
						task.getId(), c.getTime(), ScheduleServer.getInstance()
								.getServerName());
				if (!flag) {
					logger.warn("task execute failed, update retryTimes failed,"
							+ task);
				}
				return;
			}
		}
		if (DispatchResult.NEXT.equals(result)) {
			return;
		}
		boolean flag = dispatchTaskService.updateTaskStatus(task.getId(),
				DispatchTaskService.TaskType.FAIL.getValue(), ScheduleServer
						.getInstance().getServerName());
		if (!flag) {
			logger.error("task execute discard, update status failed," + task);
		}

	}

}
