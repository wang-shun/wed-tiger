/**
 * 
 */
package com.dianping.wed.tiger.event;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.wed.tiger.EventFactory;
import com.dianping.wed.tiger.ScheduleManagerFactory;
import com.dianping.wed.tiger.ScheduleServer;
import com.dianping.wed.tiger.annotation.AnnotationConstants;
import com.dianping.wed.tiger.annotation.ExecuteType;
import com.dianping.wed.tiger.dispatch.DispatchHandler;
import com.dianping.wed.tiger.dispatch.DispatchTaskEntity;
import com.dianping.wed.tiger.dispatch.DispatchTaskService;
import com.dianping.wed.tiger.repository.EventInConsumerRepository;

/**
 * @author yuantengkai 事件执行器
 */
public class EventExecutor {

	private static final Logger logger = LoggerFactory
			.getLogger(EventExecutor.class);

	private EventConfig eventConfig;

	private EventFetcher eventFetcher;

	private EventFilter eventFilter;

	private EventNavigator eventNavigator;

	private ThreadPoolExecutor eventThreadPool;

	private final ReentrantLock lock = new ReentrantLock();

	public EventExecutor(final EventConfig eventConfig,
			EventFetcher eventFetcher, EventFilter eventFilter) {
		this.eventConfig = eventConfig;
		this.eventFetcher = eventFetcher;
		this.eventFilter = eventFilter;
		this.eventNavigator = new EventNavigator(eventConfig.getHandler());
		int coreSize = ScheduleServer.getInstance().getHandlerCoreSize();
		int maxSize = ScheduleServer.getInstance().getHandlerMaxSize();
		String handlerName = eventConfig.getHandler();
		if (ScheduleServer.getInstance().getTaskStrategy() == DispatchTaskService.TaskFetchStrategy.Multi
				.getValue()) {
			Class<DispatchHandler> clazz = ScheduleManagerFactory
					.getHandlerClazz(handlerName);
			if (clazz.isAnnotationPresent(ExecuteType.class)) {
				String dType = clazz.getAnnotation(ExecuteType.class).value();
				if (AnnotationConstants.Executor.CHAIN.equalsIgnoreCase(dType)) {// 说明是串行
					coreSize = 1;
					maxSize = 1;
				}
			}
		} else {
			handlerName = "singleTigerHandler";
		}
		final String handlerNameTmp = handlerName;
		this.eventThreadPool = new ThreadPoolExecutor(coreSize, maxSize, 10L,
				TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<Runnable>(10000), new ThreadFactory() {
					AtomicInteger index = new AtomicInteger();

					public Thread newThread(Runnable r) {
						Thread thread = new Thread(r);
						thread.setDaemon(true);
						thread.setName(handlerNameTmp + "#"
								+ (index.incrementAndGet()));
						return thread;
					}
				});
	}

	/**
	 * 事件执行器工作入口
	 */
	public void doWork() {
		// 配置发生变更则结束返回
		if (eventConfig.getIdentifyCode() != ScheduleServer.getInstance()
				.getHandlerIdentifyCode()) {
			return;
		}
		// 巡航调度模式，是否可以执行
		if (ScheduleServer.getInstance().enableNavigate()) {
			if (!eventNavigator.isAllow()) {
				if (logger.isInfoEnabled()) {
					logger.info("the navigator strategy is not allow,"
							+ eventNavigator);
				}
				return;
			}
		}
		lock.lock();
		try {
			if (!ScheduleServer.getInstance().canScheduler()) {
				logger.warn("scheduleServer disable schedule.");
				return;
			}
			List<DispatchTaskEntity> tasks = eventFetcher.getTasks(
					eventConfig.getHandler(), eventConfig.getNodeList());
			if (tasks.isEmpty()) {
				eventNavigator.setUnAllowed();
				if (logger.isInfoEnabled()) {
					logger.info("there is no tasks for handler="
							+ eventConfig.getHandler() + ",nodes="
							+ eventConfig.getNodeList() + ",taskStrategy="
							+ ScheduleServer.getInstance().getTaskStrategy());
				}
				return;
			}
			eventNavigator.setAllowed();
			dispatchTasks(tasks);
			if (tasks.size() == EventFetcher.TASK_NUM
					&& ScheduleServer.getInstance().enableBackFetch()) {// 支持反压的话
				int lastTaskId = tasks.get(tasks.size() - 1).getId();
				List<DispatchTaskEntity> backFetchTasks = eventFetcher
						.getTasksByBackFetch(eventConfig.getHandler(),
								eventConfig.getNodeList(), lastTaskId);
				if (!backFetchTasks.isEmpty()) {
					dispatchTasks(backFetchTasks);
				}
			}
		} catch (Exception e) {
			logger.error("EventExecutor happens exception," + eventConfig, e);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 任务分发，丢进线程池处理
	 * 
	 * @param tasks
	 */
	private void dispatchTasks(List<DispatchTaskEntity> tasks) {
		for (DispatchTaskEntity task : tasks) {
			try {
				// 任务过滤,并加入仓储
				if (!eventFilter.isAccept(task.getId())) {
					continue;
				}
				EventConsumer consumer = EventFactory.createConsumer(task,
						eventConfig);
				// ======统一任务捞取策略下 并行 or 串行=======
				if (ScheduleServer.getInstance().getTaskStrategy() != DispatchTaskService.TaskFetchStrategy.Multi
						.getValue()) {
					if (!ScheduleServer.getInstance().getHandlers()
							.contains(task.getHandler())) {
						logger.warn("taskFetch single strategy,the handler is not in whitelist,"
								+ task);
						continue;
					}
					Class<DispatchHandler> handlerClazz = ScheduleManagerFactory
							.getHandlerClazz(task.getHandler());
					if (handlerClazz.isAnnotationPresent(ExecuteType.class)) {
						String dType = handlerClazz.getAnnotation(
								ExecuteType.class).value();
						if (AnnotationConstants.Executor.CHAIN
								.equalsIgnoreCase(dType)) {// 说明是串行
							boolean succ = EventQueue.getInstance()
									.put2ChainDeal(task.getHandler(), consumer);
							if (!succ) {
								logger.warn("taskFetch single strategy,chain execute mode,put into queue failed,"
										+ task);
								return;
							}
							continue;
						}
					}
				}
				// =========end=============
				eventThreadPool.execute(consumer);
			} catch (RejectedExecutionException e) {
				logger.error("task execute rejected exception," + task, e);
				EventInConsumerRepository.getInstance().remove(task.getId());
				break;
			} catch (Throwable t) {
				logger.error("task execute exception," + task, t);
				EventInConsumerRepository.getInstance().remove(task.getId());
			}
		}
	}

	public EventConfig getEventConfig() {
		return eventConfig;
	}

	/**
	 * 清理当前事件执行器中的任务队列
	 */
	public void clearInReadyRunningQueue() {
		lock.lock();
		try {
			this.eventThreadPool.getQueue().clear();
			if (ScheduleServer.getInstance().getTaskStrategy() != DispatchTaskService.TaskFetchStrategy.Multi
					.getValue()) {
				EventQueue.getInstance().clearTaskInQueue();
			}
		} finally {
			lock.unlock();
		}
	}

}
