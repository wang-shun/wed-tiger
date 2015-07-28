/**
 * 
 */
package com.dianping.wed.tiger.event;

import java.util.List;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * @author yuantengkai
 * 事件执行器配置项,来自lion
 */
public class EventConfig {
	
	/**
	 * 执行器名称
	 */
	private String handler;
	
	/**
	 * 执行器节点
	 */
	private List<Integer> nodeList;
	
	private int identifyCode;

	public String getHandler() {
		return handler;
	}

	public void setHandler(String handler) {
		this.handler = handler;
	}

	public List<Integer> getNodeList() {
		return nodeList;
	}

	public void setNodeList(List<Integer> nodeList) {
		this.nodeList = nodeList;
	}

	public int getIdentifyCode() {
		return identifyCode;
	}

	public void setIdentifyCode(int identifyCode) {
		this.identifyCode = identifyCode;
	}
	
	public String toString() {
        return ToStringBuilder.reflectionToString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
