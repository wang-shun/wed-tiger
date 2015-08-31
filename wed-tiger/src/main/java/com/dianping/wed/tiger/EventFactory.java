/**
 * 
 */
package com.dianping.wed.tiger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.wed.tiger.dispatch.DispatchHandler;
import com.dianping.wed.tiger.dispatch.DispatchResultManager;
import com.dianping.wed.tiger.dispatch.DispatchTaskEntity;
import com.dianping.wed.tiger.dispatch.DispatchTaskService;
import com.dianping.wed.tiger.event.EventConfig;
import com.dianping.wed.tiger.event.EventConsumer;
import com.dianping.wed.tiger.event.EventExecutor;
import com.dianping.wed.tiger.event.EventFetcher;
import com.dianping.wed.tiger.event.EventFilter;
import com.dianping.wed.tiger.repository.EventInConsumerRepository;

/**
 * @author yuantengkai 事件创建工程
 */
public class EventFactory {

	private static final Logger logger = LoggerFactory
			.getLogger(EventFactory.class);

	/**
	 * 生成任务执行器
	 * 
	 * @param config
	 * @return
	 */
	public static EventExecutor createExecutor(EventConfig config) {
		DispatchTaskService dispatchTaskService = (DispatchTaskService) ScheduleManagerFactory
				.getBean("dispatchTaskService");
		DispatchHandler handler = (DispatchHandler) ScheduleManagerFactory
				.getBean(config.getHandler());
		if (handler == null) {
			logger.warn("there is no handler bean for eventconfig:"
					+ config.getHandler());
			return null;
		}
		EventFetcher eventFetcher = new EventFetcher(dispatchTaskService);
		EventFilter eventFilter = new EventFilter(
				EventInConsumerRepository.getInstance());
		EventExecutor executor = new EventExecutor(config, eventFetcher,
				eventFilter);
		return executor;
	}

	/**
	 * 生成任务消费者
	 * 
	 * @param task
	 * @param handlerName
	 * @return
	 */
	public static EventConsumer createConsumer(DispatchTaskEntity task,
			EventConfig config) {
		DispatchHandler handler = (DispatchHandler) ScheduleManagerFactory
				.getBean(config.getHandler());
		DispatchResultManager resultHandler = DispatchResultManager
				.getInstance();
		if (handler == null) {
			throw new IllegalArgumentException("handler not found,name="
					+ config.getHandler());
		}
		EventConsumer consumer = new EventConsumer(handler, resultHandler,
				task, config.getIdentifyCode());
		return consumer;

	}

}
