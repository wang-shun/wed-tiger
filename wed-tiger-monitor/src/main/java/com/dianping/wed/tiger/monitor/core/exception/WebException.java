package com.dianping.wed.tiger.monitor.core.exception;

/**
 * 自定义异常
 * 
 * @author xuxueli
 */

public class WebException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4263535286570348691L;
	public String exceptionKey;
	public String exceptionMsg;

	public WebException() {
	}

	public WebException(String exceptionKey, String exceptionMsg) {
		super(exceptionMsg);
		this.exceptionKey = exceptionKey;
		this.exceptionMsg = exceptionMsg;
	}

	public WebException(String exceptionMsg) {
		super(exceptionMsg);
		this.exceptionMsg = exceptionMsg;
	}

	public String getExceptionMsg() {
		return exceptionMsg;
	}

	public void setExceptionMsg(String exceptionMsg) {
		this.exceptionMsg = exceptionMsg;
	}

	public String getExceptionKey() {
		return exceptionKey;
	}

	public void setExceptionKey(String exceptionKey) {
		this.exceptionKey = exceptionKey;
	}
}
