/**
 * 
 */
package com.dianping.wed.tiger.monitor;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author yuantengkai
 *
 */
public class MonitorStatistics {

	private AtomicInteger totalCount = new AtomicInteger(0);
	private AtomicInteger successCount = new AtomicInteger(0);
	private AtomicInteger failCount = new AtomicInteger(0);
	private AtomicInteger avgCost = new AtomicInteger(0);
	private AtomicInteger maxCost = new AtomicInteger(0);
	private AtomicInteger minCost = new AtomicInteger(0);

	public int getTotalCount() {
		return totalCount.intValue();
	}

	public int getSuccessCount() {
		return successCount.intValue();
	}

	public int getFailCount() {
		return failCount.intValue();
	}

	public int getAvgCost() {
		return avgCost.intValue();
	}

	public int getMaxCost() {
		return maxCost.intValue();
	}

	public int getMinCost() {
		return minCost.intValue();
	}

	public void addSuccess(int timeCost) {
		this.totalCount.incrementAndGet();
		this.successCount.incrementAndGet();
		if (maxCost.intValue() < timeCost) {
			maxCost.set(timeCost);
		}
		if (minCost.intValue() == 0 || minCost.intValue() > timeCost) {
			minCost.set(timeCost);
		}
		int avgCosttmp = avgCost.intValue()
				+ (timeCost / successCount.intValue())
				- (avgCost.intValue() / successCount.intValue());

		avgCost.set(avgCosttmp);
	}

	public void addFail() {
		this.totalCount.incrementAndGet();
		this.failCount.incrementAndGet();
	}

}
