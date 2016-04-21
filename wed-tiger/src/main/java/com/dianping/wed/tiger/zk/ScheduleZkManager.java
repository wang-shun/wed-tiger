/**
 * 
 */
package com.dianping.wed.tiger.zk;

import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.CuratorFrameworkFactory.Builder;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.RetryNTimes;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException.NodeExistsException;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.wed.tiger.ScheduleManager;
import com.dianping.wed.tiger.ScheduleServer;

/**
 * @author yuantengkai zookeeper服务
 */
public class ScheduleZkManager {

	private static final Logger logger = LoggerFactory
			.getLogger(ScheduleZkManager.class);

	private static final String NameSpace = "TIGER";

	private String PATH = "/TIGERZK";

	private static final String defaultZkAddress = "127.0.0.1:2181";

	private CuratorFramework zkClient;

	public void start() throws Exception {
		String addr = ScheduleServer.getInstance().getZkAddress();
		if (StringUtils.isBlank(addr)) {
			addr = defaultZkAddress;
		}
		logger.warn("#########start connecting to zk...");
		int sessionTimeoutMs = ScheduleServer.getInstance().getZkSessionTimeout();
		Builder cBuilder = CuratorFrameworkFactory.builder()
				.connectString(addr).namespace(NameSpace)
				.retryPolicy(new RetryNTimes(10000, 3 * 1000))
				.connectionTimeoutMs(30 * 1000).sessionTimeoutMs(sessionTimeoutMs);

		zkClient = cBuilder.build();
		zkClient.start();
		if(!StringUtils.isBlank(ScheduleServer.getInstance().getRootPath())){
			PATH = ScheduleServer.getInstance().getRootPath();
		}
		final CuratorWatcher watcher = new ScheduleZkNodeWatcher(zkClient, PATH);
		
		createRootNode(watcher);
		zkClient.getChildren().usingWatcher(watcher).forPath(PATH);
		
		zkClient.getConnectionStateListenable().addListener(
				new ConnectionStateListener() {
					@Override
					public void stateChanged(CuratorFramework client,
							ConnectionState newState) {
						if (newState == ConnectionState.LOST
								|| newState == ConnectionState.SUSPENDED) {// session
							// timeout or connection loss
							logger.warn("######zksession timeout or connection loss, begining to reConnectAndRegister..."
									+ newState);
							ScheduleServer.getInstance().reset();
							reConnectAndRegister(PATH, watcher);
						}
					}

				});

		register();
		startScheduleManager();
	}

	/**
	 * 创建根节点
	 * @param watcher
	 * @throws Exception
	 */
	private void createRootNode(CuratorWatcher watcher) throws Exception {
		Stat stat = zkClient.checkExists().usingWatcher(watcher).forPath(PATH);
		if (stat == null) {
			String createPath = zkClient.create()
					.withMode(CreateMode.PERSISTENT)
					.forPath(PATH, "0".getBytes("utf-8"));
			if (!StringUtils.isBlank(createPath)) {
				logger.warn("root zknode created," + PATH);
			} else {
				logger.error("create root zknode failed," + PATH);
				throw new Exception("create root zknode failed," + PATH);
			}
		} else {
			logger.warn("root zknode already exist," + PATH);
		}
	}

	/**
	 * 启动zk调度
	 */
	private void startScheduleManager() {
		ScheduleManager sm = new ScheduleManager(zkClient, PATH);
		sm.start();
	}

	/**
	 * 应用连接zookeeper | 重连
	 * 
	 * @param path
	 * @param watcher
	 */
	private void reConnectAndRegister(final String path,
			final CuratorWatcher watcher) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(2 * 1000);
						logger.warn("start reconnecting to zookeeper...");
						if (zkClient.getZookeeperClient()
								.blockUntilConnectedOrTimedOut()) {
							logger.warn("zookeeper reconnected success.");
							//此时可能rootNode不存在,需要重新创建
							createRootNode(watcher);
							
							zkClient.getChildren().usingWatcher(watcher)
									.forPath(path);
							
							register();
							break;
						}
					} catch (InterruptedException e) {
						logger.error(
								"connect to zookeeper InterruptedException,", e);
						break;
					} catch (Exception e) {
						logger.error("connect to zookeeper unKnow Exception,",
								e);
						try {
							Thread.sleep(30 * 1000);
						} catch (InterruptedException e1) {
						}
					}
				}
			}
		}).start();
	}

	/**
	 * 注册本机可以提供服务的一个临时节点
	 */
	private void register() {
		try {
			String nodePath = PATH + "/"
					+ ScheduleServer.getInstance().getServerName();
			Stat stat = zkClient.checkExists().forPath(nodePath);
			if (stat != null) {
				zkClient.delete().forPath(nodePath);// 先删掉，可能是脏数据，接下来getChildern的时候，为空
			}
			String registerData = ScheduleServer.getInstance().getRegisterVersion();
			String resultPath = zkClient.create()
					.withMode(CreateMode.EPHEMERAL)
					.forPath(nodePath, registerData.getBytes("utf-8"));
			if (!StringUtils.isBlank(resultPath)) {
				logger.warn("scheduleServer node register successed,path:" + resultPath + ",registerData:"
						+ registerData);
			} else {
				logger.warn("scheduleServer node register failed," + nodePath);
			}
		} catch (NodeExistsException e) {
			logger.warn("scheduleServer node already exists,ignore.", e);
		} catch (Exception e) {
			logger.error("scheduleServer node register to zk exception.", e);
			throw new RuntimeException("scheduleServer node register to zk failed.");
		}
	}

}
