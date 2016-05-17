/**
 * 
 */
package com.dianping.wed.tiger.dispatch;

import java.util.List;

/**
 * @author yuantengkai
 * 统一捞取任务，再根据handler分发
 */
public interface DispatchSingleService extends DispatchTaskService {

	/**
	 * 获取一定数量的任务
	 * 
	 * @param nodeList
	 *            任务节点
	 * @param limit
	 *            任务上限数
	 * @return
	 */
	public List<DispatchTaskEntity> findDispatchTasksWithLimit(
			List<Integer> nodeList, int limit);

	/**
	 * 反压获取一定数量的任务
	 * 
	 * @param nodeList
	 * @param limit
	 * @param taskId
	 * @return
	 */
	public List<DispatchTaskEntity> findDispatchTasksWithLimitByBackFetch(
			List<Integer> nodeList, int limit, long taskId);

}
