package com.xxl.controller;


import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xxl.core.model.MonitorRecord;
import com.xxl.core.result.ReturnT;
import com.xxl.service.IMonitorRecordService;

/**
 * monitor center
 * @author xuxueli
 */
@Controller
public class MonitorController {
	private static Logger logger = LoggerFactory.getLogger(MonitorController.class);
	
	@Autowired
	private IMonitorRecordService monitorRecordService;
	
	/**
	 * monitor index
	 * @param model
	 * @param hadleName
	 * @param monitorTime
	 * @return
	 */
	@RequestMapping("")
	public String index(Model model, String hadleName, @DateTimeFormat(pattern="yyyy-MM-dd") Date monitorTime){
		// for param
		monitorTime = monitorTime!=null?monitorTime:new Date();
		model.addAttribute("hadleName", hadleName);
		model.addAttribute("monitorTime", monitorTime);
		
		Map<String, List<MonitorRecord>> map = monitorRecordService.loadMonitorData(hadleName, monitorTime);
		model.addAttribute("map", map);
		
		return "index";
	}
	
	/**
	 * 接收推送数据
	 * @param monitorOrigin
	 * @return
	 */
	@RequestMapping("/pushData")
	@ResponseBody
	public ReturnT<String> pushData(String tm){
		try {
			logger.info("pushData:{}", tm);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ReturnT<String>();
	}
	
}
