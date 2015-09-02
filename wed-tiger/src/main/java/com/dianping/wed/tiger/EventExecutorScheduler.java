/**
 * 
 */
package com.dianping.wed.tiger;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dianping.wed.tiger.event.EventExecutor;
import com.dianping.wed.tiger.event.EventExecutorManager;

/**
 * @author yuantengkai 事件执行器的调度者
 */
public class EventExecutorScheduler {

	private static final Logger logger = LoggerFactory
			.getLogger(EventExecutorScheduler.class);

	private static final EventExecutorScheduler instance = new EventExecutorScheduler();

	/**
	 * plugin线程池
	 */
	private static ThreadPoolExecutor handlerThreadPool;

	private EventExecutorScheduler() {
		handlerThreadPool = new ThreadPoolExecutor(10, 15, 10L,
				TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(500),
				new ThreadFactory() {
					AtomicInteger index = new AtomicInteger();

					public Thread newThread(Runnable r) {
						Thread thread = new Thread(r);
						thread.setDaemon(true);
						thread.setName("EventExecutorScheduler#"
								+ (index.incrementAndGet()));
						return thread;
					}
				});
	}

	public static EventExecutorScheduler getInstance() {
		return instance;
	}

	/**
	 * 执行各个事件[plugin]
	 */
	public void execute() {
		List<EventExecutor> eventExcutors = EventExecutorManager.getInstance()
				.getEventExecutors();
		for (final EventExecutor ee : eventExcutors) {
			try {
				handlerThreadPool.execute(new Runnable() {

					@Override
					public void run() {
						ee.doWork();
					}

				});
			} catch (Throwable t) {
				logger.error("eventExcutor[" + ee.getEventConfig().getHandler()
						+ "] exception.", t);
			}
		}
	}

}
