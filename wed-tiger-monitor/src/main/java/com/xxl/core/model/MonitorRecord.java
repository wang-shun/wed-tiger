package com.xxl.core.model;

import java.io.Serializable;
import java.util.Date;

/**
 * 监控结果记录
 * @author xuxueli 2015-8-31 18:24:51
 */
@SuppressWarnings("serial")
public class MonitorRecord implements Serializable, Comparable<MonitorRecord> {

	private Date monitorTime;	// 监控时间
	private String hadleName;	// hadle名称
	private String hostName;	// 服务器host名称
	private Integer totalNum;	// 监控total数
	private Integer sucNum;		// 监控success数
	private Integer failNum;	// 监控fail数
	private long avgCost;		// avg耗时
	private long maxCost;		// MAX耗时
	private long minCode;		// MIN耗时
	
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
	
	public Date getMonitorTime() {
		return monitorTime;
	}
	public void setMonitorTime(Date monitorTime) {
		this.monitorTime = monitorTime;
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
	public Integer getTotalNum() {
		return totalNum;
	}
	public void setTotalNum(Integer totalNum) {
		this.totalNum = totalNum;
	}
	public Integer getSucNum() {
		return sucNum;
	}
	public void setSucNum(Integer sucNum) {
		this.sucNum = sucNum;
	}
	public Integer getFailNum() {
		return failNum;
	}
	public void setFailNum(Integer failNum) {
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
	public long getMinCode() {
		return minCode;
	}
	public void setMinCode(long minCode) {
		this.minCode = minCode;
	}
	
}
