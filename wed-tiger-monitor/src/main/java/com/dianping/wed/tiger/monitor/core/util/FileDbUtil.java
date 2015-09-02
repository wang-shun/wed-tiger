package com.dianping.wed.tiger.monitor.core.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.wed.tiger.monitor.core.model.MonitorRecord;

/**
 * file db util
 * @author xuxueli 2015-9-1 14:50:18
 */
public class FileDbUtil {
	protected static Logger logger = LoggerFactory.getLogger(FileDbUtil.class);

	private static final File DATA_DIR = new File("/data/tiger/data/");
	private static final File ORIGIN_DIR = new File("/data/tiger/origin/");
	private static final SimpleDateFormat formatPathA = new SimpleDateFormat("yyyyMM");
	private static final SimpleDateFormat formatPathB = new SimpleDateFormat("dd");
	
	/**
	 * 加载文件list
	 * @param handlerName		specify monitor handle file name
	 * @param monitorTime	specify monitor date
	 * @return
	 */
	private static List<File> loadFiles(String handlerName, Date monitorTime){
		if (!DATA_DIR.exists()) {
			DATA_DIR.mkdirs();
		}
		File dirA = new File(DATA_DIR, formatPathA.format(monitorTime));	// ../201509
		if (dirA.exists() && dirA.isDirectory()) {
			File dirB = new File(dirA, formatPathB.format(monitorTime));	// ../201509/01
			if (dirB.exists()  && dirB.isDirectory()) {
				File[] fileArr = dirB.listFiles();
				if (!ArrayUtils.isEmpty(fileArr)) {
					List<File> result = new ArrayList<File>();
					for (File file : fileArr) {
						if (file.getName().startsWith(handlerName)) {
							result.add(file);
						}
					}
					if (CollectionUtils.isNotEmpty(result)) {
						return result;
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * line data check
	 * @param content
	 * @return
	 */
	public static MonitorRecord parseLineData(String content){
		try {
			// 解析文件：# 时间 | handlername | hostname | totalNum | sucNum | failNum | avgCost | maxCost | minCost
			if (StringUtils.isNotBlank(content)	&& content.indexOf("#") == -1) {
				String[] strArray = content.split("\\|");
				if (strArray.length == 9) {
					MonitorRecord item = new MonitorRecord();
					item.setMonitorTime(new Date(Long.valueOf(strArray[0].trim())));
					item.setHandlerName(strArray[1].trim());
					item.setHostName(strArray[2].trim());
					item.setTotalNum(Integer.valueOf(strArray[3].trim()));
					item.setSucNum(Integer.valueOf(strArray[4].trim()));
					item.setFailNum(Integer.valueOf(strArray[5].trim()));
					item.setAvgCost(Long.valueOf(strArray[6].trim()));
					item.setMaxCost(Long.valueOf(strArray[7].trim()));
					item.setMinCost(Long.valueOf(strArray[8].trim()));
					return item;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 解析文件
	 * @param item
	 * @return
	 */
	private static void parseFile(File file, List<MonitorRecord> list) {
		InputStream ins = null;
		BufferedReader reader = null;
		try {
			ins = new FileInputStream(file);
			reader = new BufferedReader(new InputStreamReader(ins, "UTF-8"));
			String content = null;
			if (reader != null) {
				while ((content = reader.readLine()) != null) {
					MonitorRecord item = parseLineData(content);
					if (item!=null) {
						list.add(item);
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (ins != null) {
				try {
					ins.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * 检索数据
	 * @param handlerName
	 * @param monitorTime
	 * @return
	 */
	public static Map<String, List<MonitorRecord>> loadMonitorData(String handlerName, Date monitorTime){
		Map<String, List<MonitorRecord>> result = null;
		// 加载文件列表
		List<File> monotorFiles = loadFiles(handlerName, monitorTime);
		if (CollectionUtils.isNotEmpty(monotorFiles)) {
			result = new HashMap<String, List<MonitorRecord>>();
			for (File item : monotorFiles) {
				String hostName = item.getName().substring(handlerName.length() + 1, item.getName().length() - 4);
				List<MonitorRecord> list = new ArrayList<MonitorRecord>();
				parseFile(item, list);
				if (CollectionUtils.isNotEmpty(list)) {
					Collections.sort(list);
					result.put(hostName, list);
				}
			}
		}
		return result;
	}

	public static void main(String[] args) {
		/*// load data
		Map<String, List<MonitorRecord>> map = loadMonitorData("demohandler", new Date());
		if (MapUtils.isNotEmpty(map)) {
			for (Entry<String, List<MonitorRecord>> item : map.entrySet()) {
				logger.info("----------------------------:" + item.getKey());
				for (MonitorRecord monitor : item.getValue()) {
					logger.info(JSONObject.fromObject(monitor).toString());
				}
			}
		}*/
	}

	/**
	 * push data to origin dic	:ORIGIN_DIR/DATA_DIR	// 注意001，此处DATA_DIR线上应该为ORIGIN_DIR，守护线程自动同步两个目录下文件
	 * @param originData
	 */
	public static void pushData(String originData) {
		if (!DATA_DIR.exists()) {
			DATA_DIR.mkdirs();
		}
		MonitorRecord item = parseLineData(originData);
		if (item != null) {
			File dirA = new File(DATA_DIR, formatPathA.format(item.getMonitorTime()));	// ../201509
			if (!dirA.exists()) {
				dirA.mkdirs();
			}
			File dirB = new File(dirA, formatPathB.format(item.getMonitorTime()));	// ../201509/01
			if (!dirB.exists()) {
				dirB.mkdirs();
			}
			File file = new File(dirB, item.getHandlerName().concat("_").concat(item.getHostName()).concat(".txt"));
			if (!file.exists()) {
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			FileOutputStream fos = null;
			try {
				fos =new FileOutputStream(file, true);
				fos.write(("\r\n" + originData).getBytes());
				logger.info("push data success:{}", originData);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	

}
