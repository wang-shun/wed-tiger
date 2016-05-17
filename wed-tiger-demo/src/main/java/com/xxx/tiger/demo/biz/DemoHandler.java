/**
 * 
 */
package com.xxx.tiger.demo.biz;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.dianping.wed.tiger.dispatch.DispatchHandler;
import com.dianping.wed.tiger.dispatch.DispatchParam;
import com.dianping.wed.tiger.dispatch.DispatchResult;

/**
 * @author yuantengkai
 * 业务handler实现demo
 */
public class DemoHandler implements DispatchHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(DemoHandler.class);


	@SuppressWarnings("unchecked")
	@Override
	public DispatchResult invoke(DispatchParam param) throws Exception {
		//实现你的业务逻辑
		logger.warn("start execute DemoHandler...");
		long taskId = param.getTaskId();
		String jsonStr = param.getBizParameter();
		//如果当时是用 Map<String, String> 转化为json string
		Map<String, String> paramMap = (Map<String, String>) JSON.parse(jsonStr);
		logger.warn("end execute,taskId:"+taskId+paramMap);
		return DispatchResult.SUCCESS;
	}

}
