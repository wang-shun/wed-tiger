package com.dianping.wed.tiger.monitor.core.model;

import java.io.Serializable;
import java.util.Date;

/**
 * 监控结果记录
 * @author xuxueli 2015-8-31 18:24:51
 */
public class MonitorRecord implements Serializable, Comparable<MonitorRecord> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2105834915834622366L;
	private Date monitorTime;	// 监控时间
	private String handlerName;	// hadle名称
	private String hostName;	// 服务器host名称
	private int totalNum;	// 监控total数
	private int sucNum;		// 监控success数
	private int failNum;	// 监控fail数
	private long avgCost;		// avg耗时
	private long maxCost;		// MAX耗时
	private long minCost;		// MIN耗时
	
	public Date getMonitorTime() {
		return monitorTime;
	}

	public void setMonitorTime(Date monitorTime) {
		this.monitorTime = monitorTime;
	}

	public String getHandlerName() {
		return handlerName;
	}

	public void setHandlerName(String handlerName) {
		this.handlerName = handlerName;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public int getTotalNum() {
		return totalNum;
	}

	public void setTotalNum(int totalNum) {
		this.totalNum = totalNum;
	}

	public int getSucNum() {
		return sucNum;
	}

	public void setSucNum(int sucNum) {
		this.sucNum = sucNum;
	}

	public int getFailNum() {
		return failNum;
	}

	public void setFailNum(int failNum) {
		this.failNum = failNum;
	}

	public long getAvgCost() {
		return avgCost;
	}

	public void setAvgCost(long avgCost) {
		this.avgCost = avgCost;
	}

	public long getMaxCost() {
		return maxCost;
	}

	public void setMaxCost(long maxCost) {
		this.maxCost = maxCost;
	}

	public long getMinCost() {
		return minCost;
	}

	public void setMinCost(long minCost) {
		this.minCost = minCost;
	}
	
	@Override
	public int compareTo(MonitorRecord o) {
		if (this.monitorTime != null && o.monitorTime != null) {
			if (this.monitorTime.before(o.monitorTime)) {
				return 1;
			} else {
				return -1;
			}
		}
		return 0;
	}
}
