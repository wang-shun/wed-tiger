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
	 * 执行失败－增加重试次数|下次继续调度重试
	 */
	FAIL,
	
	/**
	 * 下次执行
	 */
	NEXT,
	
	/**
	 * 丢弃－更新状态为失败|下次不再调度
	 */
	DISCARD;

}
