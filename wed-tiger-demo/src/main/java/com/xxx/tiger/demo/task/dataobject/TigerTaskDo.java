/**
 * 
 */
package com.xxx.tiger.demo.task.dataobject;

import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * @author yuantengkai
 * 任务表do
 */
public class TigerTaskDo {
	
	/**
	 * 任务id
	 */
	private Long id;
	
	/**
	 * 任务添加时间
	 */
	private Date addTime;
	
	/**
	 * 任务更新时间
	 */
	private Date updateTime;
	
	/**
	 * handler名字
	 */
	private String handler;
	
	/**
	 * 虚拟节点
	 */
	private Integer node;
	
	/**
	 * 重试次数
	 */
	private Integer retryTimes;
	
	/**
	 * 任务执行状态,see DispatchTaskService.TaskType
	 */
	private Integer status;
	
	/**
	 * 最早执行时间, 非空
	 */
	private Date earliestExecuteTime;
	
	/**
	 * 业务参数,json格式,com.alibaba.fastjson.JSON.toJSONString()
	 */
	private String parameter;
	
	/**
	 * 真实的执行机器
	 */
	private String host;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getAddTime() {
		return addTime;
	}

	public void setAddTime(Date addTime) {
		this.addTime = addTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getHandler() {
		return handler;
	}

	public void setHandler(String handler) {
		this.handler = handler;
	}

	public Integer getNode() {
		return node;
	}

	public void setNode(Integer node) {
		this.node = node;
	}

	public Integer getRetryTimes() {
		return retryTimes;
	}

	public void setRetryTimes(Integer retryTimes) {
		this.retryTimes = retryTimes;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Date getEarliestExecuteTime() {
		return earliestExecuteTime;
	}

	public void setEarliestExecuteTime(Date earliestExecuteTime) {
		this.earliestExecuteTime = earliestExecuteTime;
	}

	public String getParameter() {
		return parameter;
	}

	public void setParameter(String parameter) {
		this.parameter = parameter;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String toString() {
        return ToStringBuilder.reflectionToString(this,ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
