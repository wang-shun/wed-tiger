package com.xxl.controller;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.xxl.core.model.MonitorOrigin;
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
			
			MonitorRecord item = new MonitorRecord();
			item.setMonitorTime(calendar.getTime());
			
			item.setTotalNum(i * 5);
			item.setAvgCost(i * 10);
			list1.add(item);
			
			if (i > 2) {
				item.setTotalNum(i * 5 + 50);
				item.setAvgCost(i * 10 + 50);
				list2.add(item);
			}
		}
		
		Collections.sort(list1);
		Collections.sort(list2);
		
		map.put("list1", list1);
		map.put("list2", list2);
		model.addAttribute("map", map);
		model.addAttribute("hadleName", hadleName);
		model.addAttribute("monitorTime", monitorTime);
		return "index";
	}
	
	/**
	 * 接收推送数据
	 * @param monitorOrigin
	 * @return
	 */
	@RequestMapping("/pushData")
	public ReturnT<String> pushData(MonitorOrigin monitorOrigin){
		try {
			logger.info("pushData:{}", JSONObject.fromObject(monitorOrigin)
					.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ReturnT<String>();
	}
	
}
