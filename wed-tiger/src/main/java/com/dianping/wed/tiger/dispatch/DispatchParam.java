/**
 * 
 */
package com.dianping.wed.tiger.dispatch;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yuantengkai
 * 任务分发参数类
 */
public class DispatchParam {

	private Map<String, Object> properties = new HashMap<String, Object>();

	public void addProperty(String key, Object value) {
		properties.put(key, value);
	}

	public Object getProperty(String key) {
		return properties.get(key);
	}

}
