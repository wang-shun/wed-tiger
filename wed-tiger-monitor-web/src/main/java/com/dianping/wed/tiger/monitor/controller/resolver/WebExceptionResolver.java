package com.dianping.wed.tiger.monitor.controller.resolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import com.dianping.wed.tiger.monitor.core.constant.CommonDic.CommonViewName;
import com.dianping.wed.tiger.monitor.core.exception.WebException;
import com.dianping.wed.tiger.monitor.core.result.ReturnT;
import com.dianping.wed.tiger.monitor.core.result.ReturnT.ReturnCodeEnum;

/**
 * 异常解析器
 * 
 * @author xuxueli
 */
public class WebExceptionResolver implements HandlerExceptionResolver {
	private static transient Logger logger = LoggerFactory.getLogger(WebExceptionResolver.class);

	@Override
	public ModelAndView resolveException(HttpServletRequest request,
			HttpServletResponse response, Object handler, Exception ex) {
		ModelAndView mv = new ModelAndView();
		
		// 异常封装
		ReturnT<String> result = new ReturnT<String>();
		if (ex instanceof WebException) {
			result.setCode(((WebException) ex).getExceptionKey());
			result.setMsg(((WebException) ex).getExceptionMsg());
		} else {
			result.setCode(ReturnCodeEnum.FAIL.code());
			result.setMsg(ex.toString().replaceAll("\n", "<br/>"));
			
			logger.error("==============system异常开始=============");
			logger.error("system catch exception:{}", ex);
			logger.error("==============system异常结束=============");
		}
				
		// 是否JSON返回
		HandlerMethod method = (HandlerMethod)handler;
		ResponseBody responseBody = method.getMethodAnnotation(ResponseBody.class);
		if (responseBody != null) {
			mv.addObject("result", JSONObject.fromObject(result).toString());
			mv.setViewName(CommonViewName.COMMON_RESULT);
		} else {
			mv.addObject("exceptionMsg", result.getMsg());	
			mv.setViewName(CommonViewName.COMMON_EXCEPTION);
		}
		
		return mv;
	}

	
}
