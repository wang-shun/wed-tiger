/**
 * 
 */
package com.dianping.wed.tiger.repository;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yuantengkai 正在执行的任务仓储类
 */
public class EventInConsumerRepository {

	private static final EventInConsumerRepository instance = new EventInConsumerRepository();

	private static ConcurrentHashMap<Long, Long> repository;

	private EventInConsumerRepository() {
		repository = new ConcurrentHashMap<Long, Long>();
	}

	public static EventInConsumerRepository getInstance() {
		return instance;
	}

	/**
	 * 是否存在,存在则返回true;不存在，则返回false,并放入
	 * @param taskId
	 * @return
	 */
	public boolean isContain(Long taskId) {
		Long value = repository.putIfAbsent(taskId, taskId);
		return (value == null) ? false : true;
	}
	
	public void remove(Long taskId){
		repository.remove(taskId);
	}
	
	public void removeAll(){
		repository.clear();
	}
	
	public Long get(Long taskId){
		return repository.get(taskId);
	}

}
