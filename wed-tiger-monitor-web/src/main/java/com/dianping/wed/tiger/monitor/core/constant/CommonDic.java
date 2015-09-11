package com.dianping.wed.tiger.monitor.core.constant;

/**
 * 通用字典类
 * @author xuxueli
 */
public class CommonDic {
	
	/**
	 * Controller,通用视图
	 */
	public class CommonViewName {
		public static final String COMMON_RESULT = "common/common.result";			// 通用返回
		public static final String COMMON_EXCEPTION = "common/common.exception"; 	// 通用错误页
	}
	
	/**
	 * 返回码
	 */
	public enum ReturnCodeEnum {
		SUCCESS("S"),
		FAIL("E");
		private String code;
		private ReturnCodeEnum(String code){
			this.code = code;
		}
		public String code(){
			return this.code;
		}
	}
	
}
