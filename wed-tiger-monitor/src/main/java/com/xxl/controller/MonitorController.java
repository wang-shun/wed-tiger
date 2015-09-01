package com.xxl.controller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.math.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xxl.core.model.MonitorRecord;
import com.xxl.core.result.ReturnT;

/**
 * 监控中心
 * @author xuxueli
 */
@Controller
public class MonitorController {
	private static Logger logger = LoggerFactory.getLogger(MonitorController.class);
	
	/**
	 * 监控
	 * @param model
	 * @param hadleName
	 * @param monitorTime
	 * @return
	 */
	@RequestMapping("")
	public String index(Model model, String hadleName, String monitorTime){
		
		// 原始数据
		Calendar calendar=Calendar.getInstance();
		Map<String, List<MonitorRecord>> map = new HashMap<String, List<MonitorRecord>>();
		List<MonitorRecord> list1 = new ArrayList<MonitorRecord>();
		List<MonitorRecord> list2 = new ArrayList<MonitorRecord>();
		for (int i = 0; i < 10; i++) {
			calendar.add(Calendar.MINUTE, 2*i);
			
			MonitorRecord item1 = new MonitorRecord();
			item1.setMonitorTime(calendar.getTime());
			item1.setTotalNum(RandomUtils.nextInt(10) * i);
			item1.setAvgCost(RandomUtils.nextInt(10) * i);
			list1.add(item1);
			
			if (i > 2) {
				MonitorRecord item2 = new MonitorRecord();
				item2.setMonitorTime(calendar.getTime());
				item2.setTotalNum(RandomUtils.nextInt(100) * i);
				item2.setAvgCost(RandomUtils.nextInt(100) * i);
				list2.add(item2);
			}
		}
		
		Collections.sort(list1);
		Collections.sort(list2);
		
		map.put("list1", list1);
		map.put("list2", list2);
		model.addAttribute("map", map);
		
		model.addAttribute("hadleName", hadleName);
		model.addAttribute("monitorTime", monitorTime!=null?monitorTime:new Date());
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
