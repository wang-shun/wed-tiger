package com.dianping.wed.tiger.dispatch;

/**
 * 
 * @author yuantengkai
 *
 */
public enum DispatchResult {
	/**
	 * 执行成功－更新状态为成功
	 */
	SUCCESS,
	
	/**
	 * 执行失败－下次继续重试|增加重试次数
	 */
	FAIL2RETRY,
	
	/**
	 * 下次执行
	 */
	NEXT,
	
	/**
	 * 执行失败-丢弃,下次不再重试|更新状态为失败
	 */
	FAIL2DISCARD;

}
