/**
 * 
 */
package com.dianping.wed.tiger.groovy;

/**
 * @author yuantengkai
 * groovy code 操作接口
 */
public interface IGroovyCodeRepo {
	
	/**
	 * 实现方需要配置的bean名字
	 */
	public static final String BeanName = "groovyCodeRepo";
	
	/**
	 * 根据handlerName获取groovy代码
	 * @return
	 */
	public String loadGroovyCodeByHandlerName(String handlerName);

}
