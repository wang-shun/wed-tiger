/**
 * 
 */
package com.dianping.wed.tiger.event;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * @author yuantengkai 事件导航器：通过一定的巡航策略指示本次事件是否执行
 */
public class EventNavigator {

	private AtomicBoolean allowFlag = new AtomicBoolean(true);
	
	//无任务情况下的命中db次数
	private AtomicInteger sleepLoop = new AtomicInteger(0);

	//无任务次数
	private AtomicInteger sleepCount = new AtomicInteger(0);
	
	private String handler;

	public EventNavigator(String handler) {
		this.handler = handler;
	}

	/**
	 * 停止巡航
	 */
	public void setUnAllowed() {
		this.allowFlag.set(false);
		this.sleepCount.incrementAndGet();
	}

	/**
	 * 需要巡航
	 */
	public void setAllowed() {
		this.allowFlag.set(true);
		this.sleepCount.set(0);
		this.sleepLoop.set(0);
	}

	private void incrLoopAndResetCount() {
		sleepCount.set(0);
		sleepLoop.incrementAndGet();
	}

	public boolean isAllow() {
		if (allowFlag.get() == true) {
			return true;
		}
		// 说明flag是false，进入巡航模式:斐波那契数列算法
		boolean flag = false;
		switch (sleepLoop.get() + 1) {
		case 1:
		case 2:
			if (sleepCount.get() > 1) {
				this.incrLoopAndResetCount();
				flag = true;
			} else {
				this.setUnAllowed();
				flag = false;
			}
			break;
		case 3:
			if (sleepCount.get() > 2) {
				this.incrLoopAndResetCount();
				flag = true;
			} else {
				this.setUnAllowed();
				flag = false;
			}
			break;
		case 4:
			if (sleepCount.get() > 3) {
				this.incrLoopAndResetCount();
				flag = true;
			} else {
				this.setUnAllowed();
				flag = false;
			}
			break;
		case 5:
			if (sleepCount.get() > 5) {
				this.incrLoopAndResetCount();
				flag = true;
			} else {
				this.setUnAllowed();
				flag = false;
			}
			break;
		default:
			this.setAllowed();
			flag = true;
		}
		return flag;
	}

	public String getHandler() {
		return handler;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this,
				ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
