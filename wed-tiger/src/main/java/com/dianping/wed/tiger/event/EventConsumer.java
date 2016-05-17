/**
 * 
 */
package com.dianping.wed.tiger.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Transaction;
import com.dianping.wed.tiger.ScheduleServer;
import com.dianping.wed.tiger.dispatch.DispatchHandler;
import com.dianping.wed.tiger.dispatch.DispatchParam;
import com.dianping.wed.tiger.dispatch.DispatchResult;
import com.dianping.wed.tiger.dispatch.DispatchResultManager;
import com.dianping.wed.tiger.dispatch.DispatchTaskEntity;
import com.dianping.wed.tiger.monitor.EventMonitor;
import com.dianping.wed.tiger.repository.EventInConsumerRepository;

/**
 * @author yuantengkai 任务消费
 */
public class EventConsumer implements Runnable {

	private static final Logger logger = LoggerFactory
			.getLogger(EventConsumer.class);
	
	private static final String CAT_NAME = "TaskHandler";

	private DispatchTaskEntity task;

	private DispatchHandler dispatchHandler;

	private DispatchResultManager dispatchResultManager;

	private EventInConsumerRepository eventInConsumerRepository;

	private int executorVersinoSnapshot;

	public EventConsumer(DispatchHandler dispatchHandler,
			DispatchResultManager dispatchResultManager,
			DispatchTaskEntity dispatchTaskEntity, int executorVersinoSnapshot) {
		this.dispatchHandler = dispatchHandler;
		this.task = dispatchTaskEntity;
		this.dispatchResultManager = dispatchResultManager;
		this.executorVersinoSnapshot = executorVersinoSnapshot;
		eventInConsumerRepository = EventInConsumerRepository.getInstance();
	}

	@Override
	public void run() {
		// 再次确认检查,任务已被消费或执行版本已发生变化,则立即返回
		ScheduleServer.getInstance().incrRunningTask();
		if (eventInConsumerRepository.get(task.getId()) == null
				|| executorVersinoSnapshot != EventExecutorManager
						.getInstance().getCurrentExecutorVersion()) {
			eventInConsumerRepository.remove(task.getId());
			ScheduleServer.getInstance().decrRunningTask();
			return;
		}
		DispatchResult result = DispatchResult.SUCCESS;
		
		//cat 打点
		String op = task.getHandler();
		Cat.logMetricForCount(CAT_NAME.concat(op));
		Transaction transaction = Cat.getProducer().newTransaction(CAT_NAME, op);
		
		try {
			DispatchParam param = new DispatchParam();
			param.addProperty("id", task.getId());
			param.addProperty("retryTimes", task.getRetryTimes());
			param.addProperty("param", task.getParameter());
			long start = System.currentTimeMillis();
			result = dispatchHandler.invoke(param);
			int duration = (int) (System.currentTimeMillis() - start);
			if(result == DispatchResult.FAIL2RETRY){
				transaction.setStatus(result.name());
			}else{
				transaction.setStatus(Transaction.SUCCESS);
			}
			if(ScheduleServer.getInstance().enableMonitor()){
				EventMonitor.getInstance().record(task.getHandler(), result == DispatchResult.FAIL2RETRY?0:1, duration);
			}
		} catch (Throwable t) {
			logger.error("dispatch invoke exception," + task, t);
			result = DispatchResult.FAIL2RETRY;
			Cat.logError("task dispatch invoke exception:", t);
			transaction.setStatus(t);
			if(ScheduleServer.getInstance().enableMonitor()){
				EventMonitor.getInstance().record(task.getHandler(), 0, 0);
			}
		} finally {
			eventInConsumerRepository.remove(task.getId());
			ScheduleServer.getInstance().decrRunningTask();
			dispatchResultManager.processDispatchResult(result,
					task);
			transaction.complete();
		}

	}

}
