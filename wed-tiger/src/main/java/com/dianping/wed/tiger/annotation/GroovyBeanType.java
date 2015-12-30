/**
 * 
 */
package com.dianping.wed.tiger.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author yuantengkai
 * groovy bean类型（单例，多例,href:AnnotationConstants.BeanType,默认为多例）
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface GroovyBeanType {
	String value();
}
