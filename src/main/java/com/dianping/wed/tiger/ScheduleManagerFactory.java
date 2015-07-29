/**
 * 
 */
package com.dianping.wed.tiger;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import com.dianping.wed.tiger.event.EventConfig;
import com.dianping.wed.tiger.event.EventExecutorManager;
import com.dianping.wed.tiger.utils.EventConfigUtil;
import com.dianping.wed.tiger.zk.ScheduleZkManager;

/**
 * @author yuantengkai 执行引擎构造器
 */
public class ScheduleManagerFactory {

	private static final Logger logger = LoggerFactory
			.getLogger(ScheduleManagerFactory.class);

	private static ApplicationContext appCtx;

	private ScheduleZkManager scheduleZkManager = new ScheduleZkManager();

	private int sleepInteverval = 30 * 1000;

	private Thread eventStarterThread;

	private AtomicBoolean initFlag = new AtomicBoolean(false);

	/**
	 * @param sleepTimePerFetch
	 *            每次轮训捞取任务的间隔时间(ms)
	 */
	public ScheduleManagerFactory(int sleepTimeMsPerFetch) {
		if (sleepTimeMsPerFetch > 10) {
			this.sleepInteverval = sleepTimeMsPerFetch;
		}
	}

	public enum keys {
		zkConnectAddress, rootPath, userName, password, zkSessionTimeout, enableNavigate, enableZookeeper,enableBackFetch, handlers, visualNodeNum,divideType;
	}

	/**
	 * 入口:zk调度初始化
	 * 
	 * @param conifg
	 *            :必填：zkConnectString,handlers
	 * @throws IllegalArgumentException
	 * @throws Exception
	 */
	public void initSchedule(Properties conifg) throws IllegalArgumentException,
			Exception {
		if (!initFlag.compareAndSet(false, true)) {
			return;
		}
		String zkAddress = conifg.getProperty(keys.zkConnectAddress.name());
		String handlers = conifg.getProperty(keys.handlers.name());
		if (StringUtils.isBlank(zkAddress) || StringUtils.isBlank(handlers)) {
			throw new IllegalArgumentException(
					"zkAddress or handlers is empty.");
		}
		ScheduleServer.getInstance().setZkAddress(zkAddress);
		String[] handlerArray = handlers.split(",");
		List<Integer> nodeList = ScheduleServer.getInstance().getNodeList();
		List<String> handlerList = new ArrayList<String>();
		for (int i = 0; i < handlerArray.length; i++) {
			ScheduleServer.getInstance().addHandler(handlerArray[i], nodeList);
			handlerList.add(handlerArray[i]);
		}
		ScheduleServer.getInstance().setHandlerIdentifyCode(
				handlerList.hashCode());
		String rootPath = conifg.getProperty(keys.rootPath.name(), "/TIGERZK");
		String visualNode = conifg
				.getProperty(keys.visualNodeNum.name(), "100");
		String divideType = conifg.getProperty(keys.divideType.name(), ScheduleManager.DIVIDE_RNAGE_MODE+"");
		String zkSessionTimeout = conifg.getProperty(
				keys.zkSessionTimeout.name(), "60000");
		String enableZookeeper = conifg.getProperty(
				keys.enableZookeeper.name(), "true");
		String enableNavigate = conifg.getProperty(keys.enableNavigate.name(),
				"true");
		String enableBackFetch = conifg.getProperty(
				keys.enableBackFetch.name(), "false");

		if (!StringUtils.isBlank(rootPath)) {
			ScheduleServer.getInstance().setRootPath(rootPath);
		}
		if (StringUtils.isNumeric(visualNode)) {
			ScheduleServer.getInstance().setNumOfVisualNode(
					Integer.valueOf(visualNode));
		}
		if(StringUtils.isNumeric(divideType)){
			ScheduleServer.getInstance().setDivideType(Integer.valueOf(divideType));
		}
		if (StringUtils.isNumeric(zkSessionTimeout)) {
			ScheduleServer.getInstance().setZkSessionTimeout(
					Integer.valueOf(zkSessionTimeout));
		}
		if (!StringUtils.isBlank(enableZookeeper)) {
			this.setZookeeperFlag("true".equals(enableZookeeper));
		}
		if (!StringUtils.isBlank(enableNavigate)) {
			this.setNavigateFlag("true".equals(enableNavigate));
		}
		if (!StringUtils.isBlank(enableBackFetch)) {
			this.setBackFetchFlag("true".equals(enableBackFetch));
		}

		ScheduleServer.getInstance().initOk();

		startZk();

		startSchedule();

	}
	
	/**
	 * 重新设置调度执行器
	 * @param handlers
	 */
	public void reSchedule(List<String> handlers){
		if(handlers == null || !initFlag.get()){
			return;
		}
		List<Integer> nodeList = ScheduleServer.getInstance().getNodeList();
		ScheduleServer.getInstance().clearAllHandler();
		for (String handler : handlers) {
			ScheduleServer.getInstance().addHandler(handler,
					new ArrayList<Integer>(nodeList));
		}
		ScheduleServer.getInstance().setHandlerIdentifyCode(
				handlers.hashCode());
	}
	
	/**
	 * 设置是否启用巡航模式
	 * @param flag
	 */
	public void setNavigateFlag(boolean flag){
		ScheduleServer.getInstance().setEnableNavigate(flag);
	}
	
	/**
	 * 设置是否启用zk集群服务
	 * @param flag
	 */
	public void setZookeeperFlag(boolean flag){
		ScheduleServer.getInstance().setEnableZookeeper(flag);
	}
	
	/**
	 * 设置是否反压
	 * @param flag
	 */
	public void setBackFetchFlag(boolean flag){
		ScheduleServer.getInstance().setEnableBackFetch(flag);
	}
	
	
	private void startSchedule() {
		eventStarterThread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					if (ScheduleServer.getInstance().canScheduler()) {
						if(!EventExecutorManager.getInstance().hasInited()){
							List<EventConfig> eventConfigs = EventConfigUtil.syncEventConfigs();
							EventExecutorManager.getInstance().init(eventConfigs);
							logger.warn("######" + ScheduleServer.getInstance().getServerName()
									+ " start scheduling,task config:" + eventConfigs);
						}
						EventExecutorScheduler.getInstance().execute();
					}
					try {
						if(EventExecutorManager.getInstance().hasInited()){
							Thread.sleep(sleepInteverval);
						}else{
							Thread.sleep(1000);
						}
						if (EventExecutorManager.getInstance()
								.getCurrentExecutorVersion() != ScheduleServer
								.getInstance().getHandlerIdentifyCode()) {
							Thread.sleep(3 * 1000);// 等待现有任务执行完
							List<EventConfig> eventConfigs = EventConfigUtil
									.syncEventConfigs();
							EventExecutorManager.getInstance().init(
									eventConfigs);
							logger.warn("######"
									+ ScheduleServer.getInstance()
											.getServerName()
									+ " task config changed:" + eventConfigs);
						}
					} catch (InterruptedException e) {
						logger.error("scheduler sleep exception.", e);
					}
				}
			}
		});
		eventStarterThread.setDaemon(true);
		eventStarterThread.setName("Event-Executor-Frame");
		eventStarterThread.start();
	}

	private void startZk() throws Exception {
		scheduleZkManager.start();
	}
	
	
	public void setAppCtx(ApplicationContext applicationContext) {
		appCtx = applicationContext;
	}

	public static Object getBean(String beanName) {
		if (appCtx == null || StringUtils.isBlank(beanName)) {
			return null;
		}
		try{
			return appCtx.getBean(beanName);
		}catch(Exception e){
			return null;
		}
	}

}
