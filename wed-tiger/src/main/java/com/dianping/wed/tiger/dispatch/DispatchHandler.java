/**
 * 
 */
package com.dianping.wed.tiger.dispatch;



/**
 * @author yuantengkai
 *  任务分发接口:<br/>
 *  1. java类实现，需要配置spring bean<br/>
 *  2. groovy代码实现，约定Groovy字母开头，并通过GroovyBeanType来制定handler的多例、单例
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
