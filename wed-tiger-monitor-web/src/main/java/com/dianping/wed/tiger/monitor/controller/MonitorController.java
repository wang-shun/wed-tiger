package com.dianping.wed.tiger.monitor.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
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
 * 
 * @author xuxueli
 */
@Controller
@RequestMapping("")
public class MonitorController {

	private static final SimpleDateFormat FormatDate = new SimpleDateFormat(
			"yyyy-MM-dd");

	@Resource
	private IMonitorService monitorService;
	
	@RequestMapping("")
	public String index(){
		return "forward:/tiger";
//		return "redirect:/tiger";
	}

	/**
	 * monitor index
	 * 
	 * @param model
	 * @param handlerName
	 * @param monitorTime
	 * @return
	 */
	@RequestMapping("/tiger")
	public String tigerIndex(
			Model model,
			String handlerName,
			@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date monitorTimeFrom,
			@DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date monitorTimeTo) {
		// for param
		if (monitorTimeFrom == null) {
			Calendar calendarFrom = Calendar.getInstance();
			calendarFrom.add(Calendar.HOUR_OF_DAY, -2);
			monitorTimeFrom = calendarFrom.getTime();
		}
		if (monitorTimeTo == null) {
			monitorTimeTo = new Date();
		}
		String dateFromStr = FormatDate.format(monitorTimeFrom);
		String dateToStr = FormatDate.format(monitorTimeTo);
		if (!dateFromStr.equals(dateToStr)) {
			Calendar calendarTo = Calendar.getInstance();
			calendarTo.setTime(monitorTimeFrom);
			calendarTo.set(Calendar.HOUR_OF_DAY, 23);
			calendarTo.set(Calendar.MINUTE, 59);
			calendarTo.set(Calendar.SECOND, 59);
			monitorTimeTo = calendarTo.getTime();
		}

		model.addAttribute("handlerName", handlerName);
		model.addAttribute("monitorTimeFrom", monitorTimeFrom);
		model.addAttribute("monitorTimeTo", monitorTimeTo);
		if (!StringUtils.isBlank(handlerName)) {
			Map<String, List<MonitorRecord>> map = monitorService
					.queryMonitorData(handlerName, monitorTimeFrom,
							monitorTimeTo);
			model.addAttribute("map", map);
		}
		
		HashSet<String> handlerNameSet = monitorService.queryMonitorHandler(new Date());
		List<String> handlerNameList = new ArrayList<String>(handlerNameSet);
		Collections.sort(handlerNameList);
//		Collections.sort(handlerNameList, new Comparator<String>(){
//
//			@Override
//			public int compare(String o1, String o2) {
//				if(StringUtils.isBlank(o1) || StringUtils.isBlank(o2)){
//					return 0;
//				}
//				char a = o1.charAt(0);
//				char b = o2.charAt(0);
//				if(a < b){
//					return 1;
//				} else if(a > b){
//					return -1;
//				}
//				return 0;
//			}});
		model.addAttribute("handlerNameList", handlerNameList);

		return "index";
	}
	

	/**
	 * 接收监控数据
	 * 
	 * @param monitorOrigin
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@RequestMapping("/tiger/monitor")
	@ResponseBody
	public ReturnT<String> dealMonitorData(String tm)
			throws UnsupportedEncodingException {
		String decodeTm = URLDecoder.decode(tm, "utf-8");
		monitorService.receiveData(decodeTm);
		return new ReturnT<String>();
	}
	
	public static void main(String[] args) {
		System.out.println(System.currentTimeMillis());
	}

}
