package com.dianping.wed.tiger.monitor.controller;


import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.annotation.Resource;
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
@RequestMapping("/tiger")
public class MonitorController {
	private static final SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd");
	
	@Resource
	private IMonitorService monitorService;
	
	/**
	 * monitor index
	 * @param model
	 * @param handlerName
	 * @param monitorTime
	 * @return
	 */
	@RequestMapping("")
	public String index(Model model, String handlerName, 
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
		
		model.addAttribute("handlerName", handlerName);
		model.addAttribute("monitorTimeFrom", monitorTimeFrom);
		model.addAttribute("monitorTimeTo", monitorTimeTo);
		
		Map<String, List<MonitorRecord>> map = monitorService.loadMonitorData(handlerName, monitorTimeFrom, monitorTimeTo);
		model.addAttribute("map", map);
		
		return "index";
	}
	
	/**
	 * 接收监控数据
	 * @param monitorOrigin
	 * @return
	 * @throws UnsupportedEncodingException 
	 */
	@RequestMapping("/monitor")
	@ResponseBody
	public ReturnT<String> dealMonitorData(String tm) throws UnsupportedEncodingException{
		String decodeTm = URLDecoder.decode(tm, "utf-8");
		monitorService.pushData(decodeTm);
		return new ReturnT<String>();
	}
	
}
