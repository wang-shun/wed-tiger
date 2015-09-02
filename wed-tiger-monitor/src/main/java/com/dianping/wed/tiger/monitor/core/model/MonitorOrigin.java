package com.dianping.wed.tiger.monitor.core.model;

import java.io.Serializable;

/**
 * 原始监控数据 (2s接收一次,初步统计数据)
 * @author xuxueli 2015-8-31 18:25:26
 */
@SuppressWarnings("serial")
public class MonitorOrigin implements Serializable {

	private long timestamp;		// 监控时间
	private String hadleName;	// hadle名称
	private String hostName;	// 服务器host名称
	private int totalNum;		// 调用总次数 
	private int sucNum;			// 成功次数
	private int failNum;		// 失败次数
	private long avgCost;		// 平均耗时
	private long maxCost;		// max耗时
	private long minCost;		// min耗时
	
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public String getHadleName() {
		return hadleName;
	}
	public void setHadleName(String hadleName) {
		this.hadleName = hadleName;
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
	
}
