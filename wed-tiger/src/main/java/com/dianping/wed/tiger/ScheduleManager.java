/**
 * 
 */
package com.dianping.wed.tiger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.wed.tiger.utils.ScheduleConstants;

/**
 * @author yuantengkai 任务调度分配管理器:<br/>
 *         1)轮询zk集群的变化.<br/>
 *         2)等待本机调度任务完成.<br/>
 *         3)等待集群注册版本全部更新完，自己变更注册版本.<br/>
 *         4)自己重新分配执行节点
 */
public class ScheduleManager {

	private static final Logger logger = LoggerFactory
			.getLogger(ScheduleManager.class);

	private CuratorFramework zkClient;

	private String path;

	public ScheduleManager(CuratorFramework zkClient, String path) {
		this.zkClient = zkClient;
		this.path = path;
	}

	public void start() {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {
						if (!ScheduleServer.getInstance().isInitOk()) {
							logger.warn("scheduleServer is not already inited.");
							continue;
						}
						List<String> serverList = zkClient.getChildren()
								.forPath(path);
						if (serverList == null || serverList.size() == 0) {
							logger.warn("scheduleServer init ok, but serverList is empty.path="+path);
							continue;
						}
						StringBuilder sb = new StringBuilder();
						for (String s : serverList) {
							sb.append(s).append("_");
						}
						String currentRegisterVersion = DigestUtils.md5Hex(sb
								.substring(0, sb.length() - 1));
						if (!StringUtils.equals(currentRegisterVersion,
								ScheduleServer.getInstance()
										.getRegisterVersion())) {// 集群注册版本发生变化
							ScheduleServer.getInstance().reset();
							if (ScheduleServer.getInstance().isInRunning()) {
								Thread.sleep(1 * 1000);
								if (ScheduleServer.getInstance().isInRunning()) {// 等待1s后，还在执行，则等下次
									logger.warn("########registerversion changed,but local scheduleServer already in running,scheduleFlag="
											+ ScheduleServer.getInstance()
													.canScheduler()
											+ ",serverList:" + serverList);
									continue;
								}
							}
							String selfNodePath = path
									+ "/"
									+ ScheduleServer.getInstance()
											.getServerName();
							logger.warn("########registerversion changed,start registering new version:"
									+ currentRegisterVersion
									+ ",serverList:"
									+ serverList);
							String registerDate = currentRegisterVersion;
							zkClient.setData().forPath(selfNodePath,
									registerDate.getBytes("utf-8"));
							// 检查集群机器的注册版本是否更新完，已完成的话，更新本机注册版本
							boolean hasReady = true;
							for (String s : serverList) {
								String remoteNodePath = path + "/" + s;
								if (StringUtils.equals(remoteNodePath,
										selfNodePath)) {
									continue;
								}
								String remoteRegisterDate = new String(zkClient
										.getData().forPath(remoteNodePath),
										"utf-8");
								if (!StringUtils.equals(remoteRegisterDate,
										registerDate)) {
									hasReady = false;
									break;
								}
							}
							if (hasReady) {
								logger.warn("registerversion changed, all server register ok,serverList:"
										+ serverList);
								List<String> handlers = new ArrayList<String>();
								Set<String> handlerSet = ScheduleServer
										.getInstance().getHandlers();
								if (handlerSet.size() == 0) {
									logger.warn("handler config is empty, ignore,"
											+ serverList);
									continue;
								}
								handlers.addAll(handlerSet);
								ScheduleServer.getInstance()
										.setRegisterVersion(
												currentRegisterVersion);
								// 自我分配节点
								List<Integer> newNodeList = getNodeList(serverList);
								if (newNodeList.size() == 0) {
									logger.warn("registerversion changed, new nodeList is empty,hostName="
											+ ScheduleServer.getInstance()
													.getServerName()
											+ ",serverList:" + serverList);
									continue;
								}
								ScheduleServer.getInstance().setNodeList(
										newNodeList);
								ScheduleServer.getInstance().clearAllHandler();
								for (String handler : handlers) {
									ScheduleServer.getInstance()
											.addHandler(
													handler,
													new ArrayList<Integer>(
															newNodeList));
								}
								ScheduleServer.getInstance()
										.setHandlerIdentifyCode(
												handlers.hashCode());
								logger.warn("########registerversion changed,"
										+ "handler constructed with new nodelist:"
										+ newNodeList);
								ScheduleServer.getInstance().startScheduler();
							}
						}
					} catch (Exception e) {
						logger.error("scheduleManager exception", e);
					} finally {
						try {
							Thread.sleep(3 * 1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}// end while
			}
		});
		t.setName("ScheduleManager-Thread");
		t.setPriority(Thread.MAX_PRIORITY);
		t.start();
		logger.warn("scheduleManager start...");
	}

	/**
	 * 自适应分配本机该处理的节点
	 * 
	 * @param serverList
	 * @return
	 */
	private List<Integer> getNodeList(List<String> serverList) {
		Collections.sort(serverList);
		List<Integer> nodelist = new ArrayList<Integer>();
		int index = serverList.indexOf(ScheduleServer.getInstance()
				.getServerName());
		if (index == -1) {
			return nodelist;
		}
		int divideType = ScheduleServer.getInstance().getDivideType();
		int numOfVisualNode = ScheduleServer.getInstance().getNumOfVisualNode();
		if (divideType == ScheduleConstants.NodeDivideMode.DIVIDE_SANLIE_MODE.getValue()) {// 散列模式
			for (int i = index; i < numOfVisualNode; i += serverList.size()) {
				nodelist.add(i);
			}
		} else {// 默认分块模式
			int range = numOfVisualNode / serverList.size();
			int remainder = numOfVisualNode % serverList.size();
			int first = index * range;
			int max = (index + 1) * range;
			for (int i = first; i < max; i++) {
				if (i >= numOfVisualNode) {
					break;
				}
				nodelist.add(i);
			}
			if (remainder - index > 0) {
				nodelist.add(numOfVisualNode - 1 - index);
			}
		}
		return nodelist;
	}

}
