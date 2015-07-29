/**
 * 
 */
package com.dianping.wed.tiger.dispatch;



/**
 * @author yuantengkai
 *
 */
public interface DispatchHandler {
	
	/**
	 * 任务分发执行入口
	 * @param param
	 * @return
	 * @throws Exception
	 */
	public DispatchResult invoke(DispatchParam param) throws Exception;

}
