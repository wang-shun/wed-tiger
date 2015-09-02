/**
 * 
 */
package com.dianping.wed.tiger.event;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import com.dianping.wed.tiger.EventFactory;
import com.dianping.wed.tiger.ScheduleServer;
import com.dianping.wed.tiger.dispatch.DispatchTaskService;
import com.dianping.wed.tiger.repository.EventInConsumerRepository;

/**
 * @author yuantengkai 事件执行管理器
 */
public class EventExecutorManager {

	private static final EventExecutorManager instance = new EventExecutorManager();

	// 执行版本号
	private AtomicInteger executorVersion = new AtomicInteger(0);

	private AtomicBoolean initFlag = new AtomicBoolean(false);

	private final ReentrantLock lock = new ReentrantLock();

	private CopyOnWriteArrayList<EventExecutor> eventExecutors;

	private EventExecutorManager() {
		eventExecutors = new CopyOnWriteArrayList<EventExecutor>();
	}

	public static EventExecutorManager getInstance() {
		return instance;
	}

	public int getCurrentExecutorVersion() {
		return this.executorVersion.get();
	}

	/**
	 * 初始化事件执行器
	 * 
	 * @param eventConfigs
	 */
	public void init(List<EventConfig> eventConfigs) {
		if (eventConfigs == null || eventConfigs.isEmpty()
				|| eventConfigs.get(0).getNodeList().isEmpty()) {
			return;
		}
		lock.lock();
		try {
			eventExecutors.clear();
			executorVersion.set(eventConfigs.get(0).getIdentifyCode());
			if (ScheduleServer.getInstance().getTaskStrategy() == DispatchTaskService.TaskFetchStrategy.Multi
					.getValue()) {
				for (EventConfig config : eventConfigs) {
					EventExecutor ee = EventFactory.createMultiExecutor(config);
					if (ee != null) {
						eventExecutors.add(ee);
					}
				}
			} else {
				EventExecutor ee = EventFactory.createSingleExecutor(eventConfigs.get(0));
				eventExecutors.add(ee);
			}
			initFlag.set(true);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 获取当前事件执行器
	 * 
	 * @return
	 */
	public List<EventExecutor> getEventExecutors() {
		lock.lock();
		try {
			return eventExecutors;
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 清空正在准备执行的任务队列
	 */
	public void clearInReadyRunningQueue() {
		lock.lock();
		try {
			for (EventExecutor e : eventExecutors) {
				e.clearInReadyRunningQueue();
			}
			EventInConsumerRepository.getInstance().removeAll();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 复位执行版本
	 */
	public void resetExecutorVersion() {
		this.executorVersion.set(0);
	}

	/**
	 * 执行器是否已初始化完
	 * 
	 * @return
	 */
	public boolean hasInited() {
		return initFlag.get();
	}

}
