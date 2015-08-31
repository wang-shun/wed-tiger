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

	private static ConcurrentHashMap<Integer, Integer> repository;

	private EventInConsumerRepository() {
		repository = new ConcurrentHashMap<Integer, Integer>();
	}

	public static EventInConsumerRepository getInstance() {
		return instance;
	}

	/**
	 * 是否存在,存在则返回true;不存在，则返回false,并放入
	 * @param taskId
	 * @return
	 */
	public boolean isContain(Integer taskId) {
		Integer value = repository.putIfAbsent(taskId, taskId);
		return (value == null) ? false : true;
	}
	
	public void remove(Integer taskId){
		repository.remove(taskId);
	}
	
	public void removeAll(){
		repository.clear();
	}
	
	public Integer get(Integer taskId){
		return repository.get(taskId);
	}

}
