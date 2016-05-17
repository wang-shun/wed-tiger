/**
 * 
 */
package com.xxx.tiger.demo.support;

import java.util.Properties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import com.dianping.wed.tiger.ScheduleManagerFactory;
import com.dianping.wed.tiger.utils.ScheduleConstants;

/**
 * @author yuantengkai
 * 等server启动后，启动tiger
 */
public class TigerConfigManager implements InitializingBean,ApplicationContextAware{
	
	//10s轮询一次任务
	private	ScheduleManagerFactory smf = new ScheduleManagerFactory(10*1000);
	
	private ApplicationContext applicationcontext;

	@Override
	public void afterPropertiesSet() throws Exception {
		
		//===========初始化配置==============
		Properties configp = new Properties();

		//zk地址，必须
		configp.setProperty(ScheduleManagerFactory.ZookeeperKeys.zkConnectAddress.name(),"127.0.0.1:2181");

		//执行器名称，必须
		configp.setProperty(ScheduleManagerFactory.ScheduleKeys.handlers.name(),"demoHandler");

		//zk节点rootpath,必须
		configp.setProperty(ScheduleManagerFactory.ZookeeperKeys.rootPath.name(),"/TigerDemo");

		//虚拟节点数，最好大于20，默认100,可选
		configp.setProperty(ScheduleManagerFactory.ScheduleKeys.virtualNodeNum.name(),"20");

		//zk虚拟节点分配策略,0-散列模式,1－分块模式,默认分块模式,建议用1,可选
		configp.setProperty(ScheduleManagerFactory.ScheduleKeys.divideType.name(), ScheduleConstants.NodeDivideMode.DIVIDE_RANGE_MODE.getValue()+"");

		//执行器策略，可选，默认为策略a:各个执行器各自捞取各自的任务
		configp.setProperty(ScheduleManagerFactory.ScheduleKeys.taskStrategy.name(),ScheduleConstants.TaskFetchStrategy.Multi.getValue()+"");

		//总调度开关,默认true,可选
		configp.setProperty(ScheduleManagerFactory.ScheduleKeys.scheduleFlag.name(),"true");

		//启用巡航模式，默认true,可选
		configp.setProperty(ScheduleManagerFactory.ScheduleKeys.enableNavigate.name(),"true");

		//启用反压模式，默认false,可选
		configp.setProperty(ScheduleManagerFactory.ScheduleKeys.enableBackFetch.name(),"false");

		smf.setAppCtx(applicationcontext);
		
		//===========初始化启用==========
		smf.initSchedule(configp);
		
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		this.applicationcontext = applicationContext;
	}

}
