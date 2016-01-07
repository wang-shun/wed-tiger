/**
 * 
 */
package com.dianping.wed.tiger.groovy;

import groovy.lang.GroovyClassLoader;

import java.lang.reflect.Field;
import java.util.Map.Entry;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.wed.tiger.ScheduleManagerFactory;
import com.dianping.wed.tiger.annotation.AnnotationConstants;
import com.dianping.wed.tiger.annotation.GroovyBeanType;
import com.dianping.wed.tiger.annotation.TService;
import com.dianping.wed.tiger.dispatch.DispatchHandler;

/**
 * @author yuantengkai groovy类工厂(单例、多例)
 */
public class GroovyBeanFactory {

	private static final Logger logger = LoggerFactory
			.getLogger(GroovyBeanFactory.class);

	private final GroovyClassLoader gClassLoader = new GroovyClassLoader();

	private static final GroovyBeanFactory instance = new GroovyBeanFactory();

	private final String GroovyPrefix = "Groovy";

	/**
	 * handler本地缓存map key-handlerName value-handler future
	 */
	private final ConcurrentHashMap<String, Future<DispatchHandler>> handlerCacheMap = new ConcurrentHashMap<String, Future<DispatchHandler>>(64);

	/**
	 * handler clazz本地缓存map key-handlerName value-handler clazz
	 */
	private final ConcurrentHashMap<String, Class<DispatchHandler>> handlerClazzCacheMap = new ConcurrentHashMap<String, Class<DispatchHandler>>(64);

	/**
	 * handler code标示本地缓存map key-handlerName value-code.hashcode
	 */
	private final ConcurrentHashMap<String, Integer> handlerCodeSignCacheMap = new ConcurrentHashMap<String, Integer>(64);
	
	private final BlockingQueue<GroovyCodeEntity> groovyCodeQueue = new LinkedBlockingQueue<GroovyCodeEntity>(5000);
	
	private AtomicBoolean monitorInit = new AtomicBoolean(false);
	
	private GroovyBeanFactory() {

	}

	public static GroovyBeanFactory getInstance() {
		return instance;
	}

	/**
	 * 是否是groovy handler类型
	 * 
	 * @param handlerName
	 * @return
	 */
	public boolean isGroovyHandler(String handlerName) {
		if (StringUtils.isBlank(handlerName)) {
			return false;
		}
		if (handlerName.startsWith(GroovyPrefix)) {
			return true;
		}
		if (handlerClazzCacheMap.containsKey(handlerName)) {
			return true;
		}
		try{
			IGroovyCodeRepo groovyCodeRepo = (IGroovyCodeRepo) ScheduleManagerFactory
					.getBean(IGroovyCodeRepo.BeanName);
			if (groovyCodeRepo == null) {
				return false;
			}
			
			String code = groovyCodeRepo.loadGroovyCodeByHandlerName(handlerName);
			if (StringUtils.isBlank(code)){
				return false;
			}else{
				return true;
			}
		}catch(Throwable t){
			return false;
		}
	}

	/**
	 * 根据handler名字得到任务handler类
	 * 
	 * @param handlerName
	 * @return DispatchHandler
	 */
	public DispatchHandler getHandlerByName(String handlerName) {
		if (StringUtils.isBlank(handlerName)) {
			return null;
		} else if (handlerName.startsWith(GroovyPrefix)) {
			Class<DispatchHandler> clazz = getClazzByHandlerName(handlerName);
			if(clazz == null){
				return null;
			}
			if (clazz.isAnnotationPresent(GroovyBeanType.class)){
				String bType = clazz.getAnnotation(GroovyBeanType.class).value();
				if(AnnotationConstants.BeanType.SINGLE.equalsIgnoreCase(bType)){
					return getHandlerByNameWithSingle(handlerName);
				}
			}
			return getHandlerByNameWithPrototype(handlerName);
		} else {
			return null;
		}
	}

	/**
	 * 得到handler clazz
	 * 
	 * @param handlerName
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Class<DispatchHandler> getClazzByHandlerName(String handlerName) {
		try{
			if (handlerClazzCacheMap.containsKey(handlerName)) {
				return handlerClazzCacheMap.get(handlerName);
			}
			IGroovyCodeRepo groovyCodeRepo = (IGroovyCodeRepo) ScheduleManagerFactory
					.getBean(IGroovyCodeRepo.BeanName);
			if (groovyCodeRepo == null) {
				throw new RuntimeException("groovyCodeRepo is null.");
			}
			String code = groovyCodeRepo.loadGroovyCodeByHandlerName(handlerName);
			if (StringUtils.isBlank(code)) {
				throw new RuntimeException("groovyCode is blank.");
			}
			if (!isClassTypeHandler(code)) {
				throw new RuntimeException(
						"groovyCode is not DispatchHandler type.");
			}
			Class<DispatchHandler> clazz = gClassLoader.parseClass(code);// 可能刚开始
																			// 同时有多个线程parse同一块code,但不影响业务
			handlerClazzCacheMap.putIfAbsent(handlerName, clazz);
			//监控groovy handler更新情况
			putIntoGroovyCodeMonitor(new GroovyCodeEntity(handlerName,code));
			return handlerClazzCacheMap.get(handlerName);
		}catch(Throwable t){
			logger.error("getHandlerClazz exeption,handlerName="+handlerName, t);
			return null;
		}
	}
	
	/**
	 * groovy代码变化,对外暴露，供应用方及时刷新本地groovy handler缓存<br/>
	 * 及时应用方不刷新缓存，本地也会有轮询线程监控code的变化
	 * @param groovyHandlerName
	 */
	public void onChange(String groovyHandlerName){
		this.clearHandlerCache(groovyHandlerName);
	}

	/**
	 * 代码监控，异步处理
	 * @param groovyCodeEntity
	 */
	private void putIntoGroovyCodeMonitor(GroovyCodeEntity codeEntity) {
		if(groovyCodeQueue.offer(codeEntity)){
			if(monitorInit.compareAndSet(false, true)){
				startThread2Monitor();
			}
		}else{
			this.clearHandlerCache(codeEntity.getHandlerName());
		}
	}

	private void startThread2Monitor() {
		Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(true){
					try {
						GroovyCodeEntity entity = groovyCodeQueue.poll(5000, TimeUnit.MILLISECONDS);
						if(entity == null){//5s一次轮询groovy code
							IGroovyCodeRepo groovyCodeRepo = (IGroovyCodeRepo) ScheduleManagerFactory
									.getBean(IGroovyCodeRepo.BeanName);
							if(!handlerCodeSignCacheMap.isEmpty()){
								for(Entry<String, Integer> e:handlerCodeSignCacheMap.entrySet()){
									String code = groovyCodeRepo.loadGroovyCodeByHandlerName(e.getKey());
									if(StringUtils.isBlank(code) || e.getValue() != code.hashCode()){
										clearHandlerCache(e.getKey());//如果发现代码有更新，则清空handler缓存，等待下一次调度
									}
								}
							}
						}else{
							Integer codeSign = handlerCodeSignCacheMap.get(entity.getHandlerName());
							if(codeSign == null){
								handlerCodeSignCacheMap.put(entity.getHandlerName(), entity.getCode().hashCode());
							}else if(codeSign != entity.getCode().hashCode()){//代码有变化
								//清空本地缓存
								clearHandlerCache(entity.getHandlerName());
							}
						}
					} catch (InterruptedException e) {
						logger.error("GroovyCode-Monitor happens InterruptedException.", e);
					} catch(Throwable t){//只捕获异常打错误日志
						logger.error("GroovyCode-Monitor happens exception.", t);
					}
				}
			}
		});
		t.setName("GroovyCode-Monitor-Thread");
		t.setDaemon(true);
		t.start();
		
	}

	private void clearHandlerCache(String handlerName) {
		this.handlerClazzCacheMap.remove(handlerName);
		this.handlerCacheMap.remove(handlerName);
		this.handlerCodeSignCacheMap.remove(handlerName);
		
	}

	private boolean isClassTypeHandler(String code) {
		if (code.indexOf("tiger.dispatch.DispatchHandler") >= 0) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 得到[单例]的任务handler类
	 * 
	 * @param handlerName
	 * @return DispatchHandler
	 */
	private DispatchHandler getHandlerByNameWithSingle(String handlerName) {
		// 构造DispatchHandler会比较耗时，为了提高性能、保证单例，这里通过future的方式
		Future<DispatchHandler> fdh = handlerCacheMap.get(handlerName);
		if (fdh == null) {
			FutureTask<DispatchHandler> fTask = new FutureTask<DispatchHandler>(
					new HandlerConstruction(handlerName));
			fdh = handlerCacheMap.putIfAbsent(handlerName, fTask);
			if (fdh == null) {
				fdh = fTask;
				fTask.run();// 只有第一个handlerName的线程去构造DispatchHandler
			}
		}
		DispatchHandler dh = null;
		try {
			dh = fdh.get(200, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			handlerCacheMap.remove(handlerName);
			throw new RuntimeException(e.getCause());
		} catch (ExecutionException e) {
			handlerCacheMap.remove(handlerName);
			throw new RuntimeException(e.getCause());
		} catch (TimeoutException e) {
			handlerCacheMap.remove(handlerName);
			throw new RuntimeException(
					"construct groovyhandler timeoutException,handlerName="
							+ handlerName, e.getCause());
		} catch (Throwable e) {
			handlerCacheMap.remove(handlerName);
			throw new RuntimeException(
					"construct groovyhandler unKnowException,handlerName="
							+ handlerName, e.getCause());
		}
		if (dh instanceof DefaultErrorHandler) {
			handlerCacheMap.remove(handlerName);
		}
		return dh;
	}

	/**
	 * 得到[多例]的任务handler类
	 * 
	 * @param handlerName
	 * @return DispatchHandler
	 */
	private DispatchHandler getHandlerByNameWithPrototype(String handlerName) {
		try {
			Class<DispatchHandler> dhClazz = getClazzByHandlerName(handlerName);
			if(dhClazz == null){
				return new DefaultErrorHandler();
			}
			Field[] fields = dhClazz.getDeclaredFields();
			DispatchHandler handler = dhClazz.newInstance();// 每次都new 一个实例
			for (Field field : fields) {
				TService serviceAnnotation = field
						.getAnnotation(TService.class);
				if (serviceAnnotation != null) {
					Object fieldValue = ScheduleManagerFactory.getBean(field
							.getName());
					if (fieldValue == null) {
						throw new RuntimeException(
								"TService fildValue is null,field="
										+ field.getName());
					}
					field.setAccessible(true);
					field.set(handler, fieldValue);
				}
			}
			return handler;
		} catch (Exception e) {
			logger.error(
					"construct Prototype groovyhandler happens exception,handlerName="
							+ handlerName, e);
			return new DefaultErrorHandler();
		}
	}

	/**
	 * handler构造器
	 * 
	 * @author yuantengkai
	 *
	 */
	private class HandlerConstruction implements Callable<DispatchHandler> {

		private String handlerName;

		public HandlerConstruction(String handlerName) {
			this.handlerName = handlerName;
		}

		@Override
		public DispatchHandler call() throws Exception {
			try {
				Class<DispatchHandler> clazz = getClazzByHandlerName(handlerName);
				if(clazz == null){
					return new DefaultErrorHandler();
				}
				Field[] fields = clazz.getDeclaredFields();
				DispatchHandler handler = clazz.newInstance();
				for (Field field : fields) {
					TService serviceAnnotation = field
							.getAnnotation(TService.class);
					if (serviceAnnotation != null) {
						Object fieldValue = ScheduleManagerFactory
								.getBean(field.getName());
						if (fieldValue == null) {
							throw new RuntimeException(
									"TService fildValue is null,field="
											+ field.getName()+",handlerName="+handlerName);
						}
						field.setAccessible(true);
						field.set(handler, fieldValue);
					}
				}
				return handler;
			} catch (Throwable t) {
				logger.error(
						"construct groovyhandler happens exception,handlerName="
								+ handlerName, t);
				return new DefaultErrorHandler();
			}
		}

	}
	
	/**
	 * groovy code内部类
	 * @author yuantengkai
	 *
	 */
	public class GroovyCodeEntity{
		
		private String handlerName;
		
		private String code;

		public GroovyCodeEntity(String handlerName,String code){
			this.handlerName = handlerName;
			this.code = code;
		}
		
		public String getHandlerName() {
			return handlerName;
		}

		public void setHandlerName(String handlerName) {
			this.handlerName = handlerName;
		}

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}
	}
	

}
