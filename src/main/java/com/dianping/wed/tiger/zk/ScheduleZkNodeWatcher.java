package com.dianping.wed.tiger.zk;

import java.util.List;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dianping.wed.tiger.ScheduleServer;

/**
 * 
 * @author yuantengkai zookeeper node watcher
 */
public class ScheduleZkNodeWatcher implements CuratorWatcher {

	private static final Logger logger = LoggerFactory
			.getLogger(ScheduleZkNodeWatcher.class);

	private CuratorFramework zkClient;

	private String path;

	public ScheduleZkNodeWatcher(CuratorFramework client, String path) {
		this.zkClient = client;
		this.path = path;
	}

	@Override
	public void process(WatchedEvent event) throws Exception {
		if (!ScheduleServer.getInstance().enableZookeeper()) {
			logger.warn("node changed,zookeeper switch is not enable,ignore,"
					+ event);
			return;
		}
		logger.warn("node changed," + event);
		if (zkClient.getState() != CuratorFrameworkState.STOPPED) {
			if (event.getType() == EventType.NodeChildrenChanged) {
				List<String> childList = zkClient.getChildren()
						.usingWatcher(this).forPath(path);
				logger.warn("node child changed,"+childList);
				ScheduleServer.getInstance().reset();
			} else if (event.getType() == EventType.None
					&& (event.getState() == KeeperState.Disconnected || event
							.getState() == KeeperState.Expired)) {
				logger.warn("node disconnected or expired, remove handler infos."+event);
				ScheduleServer.getInstance().reset();
			}
		}
	}

}
