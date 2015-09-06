/**
 * 
 */
package com.dianping.wed.tiger.event;

import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yuantengkai 执行器队列,用于串行执行策略(需要严格保证顺序的情况)
 */
public class EventQueue {

	private static final Logger logger = LoggerFactory
			.getLogger(EventQueue.class);

	private static final EventQueue instance = new EventQueue();

	/**
	 * 串行阻塞执行器map:k-handler v-queue
	 * 
	 */
	private ConcurrentHashMap<String, BlockingQueue<EventConsumer>> handlerQueueMap;

	private EventQueue() {
		handlerQueueMap = new ConcurrentHashMap<String, BlockingQueue<EventConsumer>>();
	}

	public static EventQueue getInstance() {
		return instance;
	}

	/**
	 * 入队串行执行
	 * 
	 * @param handler
	 * @param consumer
	 * @return
	 */
	public boolean put2ChainDeal(String handler, EventConsumer consumer) {
		BlockingQueue<EventConsumer> queue = handlerQueueMap.get(handler);
		boolean needNewThreadFlag = false;
		if (queue == null) {
			queue = new LinkedBlockingQueue<EventConsumer>(10000);
			BlockingQueue<EventConsumer> exists = handlerQueueMap.putIfAbsent(
					handler, queue);
			if (exists == null) {
				needNewThreadFlag = true;// 说明是第一次进入队列，需要新启一个线程进行消费
			} else {
				queue = exists;
			}
		}
		boolean success = queue.offer(consumer);
		if (!success) {
			return false;
		}
		if (needNewThreadFlag) {
			startThread2ConsumeQueue(handler);
		}
		return true;
	}
	
	/**
	 * 清空阻塞队列中的任务 
	 */
	public void clearTaskInQueue(){
		for(Entry<String, BlockingQueue<EventConsumer>> e : handlerQueueMap.entrySet()){
			e.getValue().clear();
		}
	}

	/**
	 * 启动一个线程对handler对应的queue进行消费
	 * 
	 * @param handler
	 */
	private void startThread2ConsumeQueue(final String handler) {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					BlockingQueue<EventConsumer> queue = handlerQueueMap
							.get(handler);
					try {
						EventConsumer c = queue.take();
						c.run();
					} catch (InterruptedException e) {
						logger.error(
								"chainExecute InterruptedException,handler="
										+ handler, e);
					}
				}
			}

		});
		t.setName("Event-ChainConsumer-" + handler);
		t.setDaemon(true);
		t.start();
	}

}
