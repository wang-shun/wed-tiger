package com.dianping.wed.tiger.monitor.core.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dianping.wed.tiger.monitor.core.model.MonitorRecord;

/**
 * 文件操作
 * 
 * @author xuxueli 2015-9-1 14:50:18
 */
public class FileDbUtil {

	private static Logger logger = LoggerFactory.getLogger(FileDbUtil.class);

	/**
	 * zip压缩触发标识 当前监控时间 201510
	 */
	private static final BlockingQueue<Date> zipTriggerQueue = new LinkedBlockingQueue<Date>();
	
	private static final AtomicBoolean zipInitFlag = new AtomicBoolean(false);
	
	/**
	 * tiger监控页面的数据访问路径
	 */
	private static final File DATA_DIR = new File("/data/appdatas/tiger/");
	
//	private static final File ORIGIN_DIR = new File("/data/tiger/origin/");
	
	private static final String FormatPath_yyyyMM = "yyyyMM";
	
	private static final SimpleDateFormat FormatPath_dd = new SimpleDateFormat(
			"dd");

	static {
		if (!DATA_DIR.exists()) {
			DATA_DIR.mkdirs();
		}
	}

	private static SimpleDateFormat getDateFormat(String format){
		return new SimpleDateFormat(format);
	}
	/**
	 * push data to origin dic :ORIGIN_DIR/DATA_DIR<br/>
	 * 注意001，此处DATA_DIR线上应该为ORIGIN_DIR，守护线程自动同步两个目录下文件<br/>
	 * 写监控数据需要同步，保证线程安全
	 * @param originData
	 */
	public static synchronized void dealReceiveData(String originData) {
		MonitorRecord item = parseLineData(originData);
		if (item != null) {
			initZipThread();
			File dirA = new File(DATA_DIR, getDateFormat(FormatPath_yyyyMM).format(item
					.getMonitorTime())); // ../201509
			if (!dirA.exists()) {
				dirA.mkdirs();
				//每个月启动zip压缩一次，异步实现
				zipTriggerQueue.offer(item.getMonitorTime());
			}
			File dirB = new File(dirA, FormatPath_dd.format(item
					.getMonitorTime())); // ../201509/01
			if (!dirB.exists()) {
				dirB.mkdirs();
			}
			String filePath = item.getHandlerName().concat("_")
					.concat(item.getHostName()).concat(".txt");
			File file = new File(dirB, filePath);// ../201509/01/smsGeneralHandler_tkyuan.local.txt
			if (!file.exists()) {
				try {
					file.createNewFile();
				} catch (IOException e) {
					logger.error("create file exception,path=" + filePath, e);
				}
			}
			FileOutputStream fos = null;
			try {
				fos = new FileOutputStream(file, true);
				fos.write(("\r\n" + originData).getBytes("utf-8"));
				if (logger.isInfoEnabled()) {
					logger.info("deal receive data success:{}", originData);
				}
			} catch (FileNotFoundException e) {
				logger.error(
						"deal receive data FileNotFoundException,originData="
								+ originData, e);
			} catch (IOException e) {
				logger.error("deal receive data IOException,originData="
						+ originData, e);
			} finally {
				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
					}
				}
			}
		}
	}
	
	private static void initZipThread() {
		if(!zipInitFlag.compareAndSet(false, true)){
			return;
		}
		Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(true){
					try{
						Date currentMonitorDate = zipTriggerQueue.take();
						Calendar c = Calendar.getInstance();
						c.setTime(currentMonitorDate);
						c.add(Calendar.MONTH, -2);//提前2个月时间
						final Date zipDate = c.getTime();
						
						
						File[] historyFiles = DATA_DIR.listFiles(new FilenameFilter() {
							
							@Override
							public boolean accept(File dir, String name) {
								if(name.startsWith("20")){ //2100年后的事情暂不考虑
									if(!name.endsWith(".zip")){
										return true;
									}
								}
								return false;
							}
						});
						if(historyFiles != null && historyFiles.length > 0){
							for(File f:historyFiles){ // ../201509 201510 201511
								try{
									String dateName = f.getName(); //201509
									SimpleDateFormat sdf = getDateFormat(FormatPath_yyyyMM);
									Date fileDate = sdf.parse(dateName);
									if(fileDate.after(zipDate)){
										continue;//不足2个月，则不压缩
									}
									AntZipCompressor azc = new AntZipCompressor(f.getAbsolutePath().concat(".zip"));
									azc.compressExe(f.getAbsolutePath());
									//当前文件要删除
									deleteDirectory(f.getAbsolutePath());
								}catch(Exception e){
									logger.error("zip deal error,filePathName="+f.getAbsolutePath());
									continue;
								}

								
							}
						}
						
					}catch(Exception e){
						logger.error("zip deal exception",e);
					}
				}
			}

			/**
			 * 递归删除目录及里面的内容
			 * @param path ../201510
			 */
			private void deleteDirectory(String path) {
				if (!path.endsWith(File.separator)) {  
					path = path + File.separator;  
			    }
				File dirFile = new File(path);  
			    //如果dir对应的文件不存在,或不是个文件目录则退出  
			    if (!dirFile.exists() || !dirFile.isDirectory()) {  
			        return;  
			    }
			    //删除文件夹下的所有文件(包括子目录)  
			    File[] subfiles = dirFile.listFiles();  
			    for (int i = 0; i < subfiles.length; i++) {  
			        //删除子文件 
			    	if(subfiles[i].isDirectory()){
			    		deleteDirectory(subfiles[i].getAbsolutePath());
			    	}else if (subfiles[i].isFile()) {  
			        	subfiles[i].delete();  
			        } 
			    }
			    // The directory is now empty so now it can be smoked
			    dirFile.delete();
			}
			
		});
		t.setDaemon(true);
		t.setName("TigerMonitor-Zip-Thread");
		t.start();
	}

	/**
	 * 查询当天的监控数据
	 * 
	 * @param handlerName 例smsGeneralHandler
	 * @param monitorTime 例201509/02
	 * @return
	 */
	public static Map<String, List<MonitorRecord>> queryMonitorData(
			String handlerName, Date monitorTime) {
		
		Map<String, List<MonitorRecord>> result = null;
		
		// 加载文件列表
		List<File> monitorFiles = queryFiles(handlerName, monitorTime);
		
		if (CollectionUtils.isNotEmpty(monitorFiles)) {
			result = new HashMap<String, List<MonitorRecord>>();
			for (File fileItem : monitorFiles) {
				
				String hostName = fileItem.getName().substring(
						handlerName.length() + 1, fileItem.getName().length() - 4);
				//解析文件
				List<MonitorRecord> list = parseFileItem(fileItem);
				
				if (CollectionUtils.isNotEmpty(list)) {
					Collections.sort(list);
					result.put(hostName, list);
				}
			}
		}
		return result;
	}


	/**
	 * 解析数据
	 * 
	 * @param content
	 * @return
	 */
	public static MonitorRecord parseLineData(String content) {
		try {
			// 解析文件：# 时间 | handlername | hostname | totalNum | sucNum | failNum
			// | avgCost | maxCost | minCost
			if (StringUtils.isNotBlank(content) && content.indexOf("#") == -1) {
				String[] strArray = content.split("\\|");
				if (strArray.length == 9) {
					MonitorRecord item = new MonitorRecord();
					item.setMonitorTime(new Date(Long.valueOf(strArray[0]
							.trim())));
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
			logger.error("parseLineData exception,data:" + content, e);
		}
		return null;
	}
	
	/**
	 * 加载文件list
	 * 
	 * @param handlerName
	 *            specify monitor handle file name
	 * @param monitorTime
	 *            specify monitor date
	 * @return
	 */
	private static List<File> queryFiles(String handlerName, Date monitorTime) {
		File dirA = new File(DATA_DIR, getDateFormat(FormatPath_yyyyMM).format(monitorTime)); // ../201509
		if (dirA.exists() && dirA.isDirectory()) {
			File dirB = new File(dirA, FormatPath_dd.format(monitorTime)); // ../201509/01
			if (dirB.exists() && dirB.isDirectory()) {
				File[] fileArray = dirB.listFiles();
				if (!ArrayUtils.isEmpty(fileArray)) {
					List<File> result = new ArrayList<File>();
					for (File file : fileArray) {
						if (file.getName().startsWith(handlerName+"_")) {
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
	 * 解析文件
	 * 
	 * @param item
	 * @return
	 */
	private static List<MonitorRecord> parseFileItem(File file) {
		List<MonitorRecord> list = new ArrayList<MonitorRecord>();
		InputStream ins = null;
		BufferedReader reader = null;
		try {
			ins = new FileInputStream(file);
			reader = new BufferedReader(new InputStreamReader(ins, "utf-8"));
			if (reader != null) {
				String content = null;
				while ((content = reader.readLine()) != null) {
					MonitorRecord item = parseLineData(content);
					if (item != null) {
						list.add(item);
					}
				}
				return list;
			}
		} catch (FileNotFoundException e) {
			logger.error("解析文件发生异常,fileName="+file.getName(), e);
		} catch (UnsupportedEncodingException e) {
			logger.error("解析文件发生异常,fileName="+file.getName(), e);
		} catch (IOException e) {
			logger.error("解析文件发生异常,fileName="+file.getName(), e);
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
		return null;
	}

	/**
	 * 指定日期下handler列表
	 * @param monitorTime
	 * @return
	 */
	public static HashSet<String> queryMonitorHandler(Date monitorTime) {
		HashSet<String> result = new HashSet<String>();
		File dirA = new File(DATA_DIR, getDateFormat(FormatPath_yyyyMM).format(monitorTime)); // ../201509
		if (dirA.exists() && dirA.isDirectory()) {
			File dirB = new File(dirA, FormatPath_dd.format(monitorTime)); // ../201509/01
			if (dirB.exists() && dirB.isDirectory()) {
				File[] fileArray = dirB.listFiles();
				if (!ArrayUtils.isEmpty(fileArray)) {
					for (File file : fileArray) {
						if (file.getName().indexOf("_") != -1) {
							result.add(file.getName().substring(0, file.getName().indexOf("_")));
						}
					}
					if (CollectionUtils.isNotEmpty(result)) {
						return result;
					}
				}
			}
		}
		return result;
	}

}
