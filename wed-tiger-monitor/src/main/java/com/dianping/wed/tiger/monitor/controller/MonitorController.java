package com.dianping.wed.tiger.monitor.controller;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dianping.wed.tiger.monitor.core.model.MonitorRecord;
import com.dianping.wed.tiger.monitor.core.result.ReturnT;
import com.dianping.wed.tiger.monitor.service.IMonitorService;

/**
 * monitor center
 * @author xuxueli
 */
@Controller
public class MonitorController {
	private static final SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd");
	
	@Autowired
	private IMonitorService monitorService;
	
	/**
	 * monitor index
	 * @param model
	 * @param hadleName
	 * @param monitorTime
	 * @return
	 */
	@RequestMapping("")
	public String index(Model model, String hadleName, 
			@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date monitorTimeFrom, 
			@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date monitorTimeTo){
		// for param
		if (monitorTimeFrom == null) {
			Calendar calendarFrom = Calendar.getInstance();
			calendarFrom.set(Calendar.HOUR_OF_DAY, 0);
			calendarFrom.set(Calendar.MINUTE, 0);
			calendarFrom.set(Calendar.SECOND, 0);
			monitorTimeFrom = calendarFrom.getTime();
		} 
		if(monitorTimeTo == null) {
			Calendar calendarTo = Calendar.getInstance();
			calendarTo.set(Calendar.HOUR_OF_DAY, 23);
			calendarTo.set(Calendar.MINUTE, 59);
			calendarTo.set(Calendar.SECOND, 59);
			monitorTimeTo = calendarTo.getTime();
		} 
		String dateFromStr = formatDate.format(monitorTimeFrom);
		String dateToStr = formatDate.format(monitorTimeTo);
		if (!dateFromStr.equals(dateToStr)) {
			Calendar calendarTo = Calendar.getInstance();
			calendarTo.setTime(monitorTimeFrom);
			calendarTo.set(Calendar.HOUR_OF_DAY, 23);
			calendarTo.set(Calendar.MINUTE, 59);
			calendarTo.set(Calendar.SECOND, 59);
			monitorTimeTo = calendarTo.getTime();
		}
		
		model.addAttribute("hadleName", hadleName);
		model.addAttribute("monitorTimeFrom", monitorTimeFrom);
		model.addAttribute("monitorTimeTo", monitorTimeTo);
		
		Map<String, List<MonitorRecord>> map = monitorService.loadMonitorData(hadleName, monitorTimeFrom, monitorTimeTo);
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
			monitorService.pushData(tm);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ReturnT<String>();
	}
	
}
