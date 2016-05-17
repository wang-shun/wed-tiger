#
# Tiger seems to work best with the mysql-5.5.49 href:http://dev.mysql.com/downloads/mysql/5.5.html#downloads
#
# In Tiger-Demo, you will need to create following table

# DROP TABLE IF EXISTS Tiger_Task;

CREATE TABLE Tiger_Task (
	`id` bigint(12) NOT NULL AUTO_INCREMENT,
	`addTime` datetime NOT NULL,
	`updateTime` datetime NOT NULL,
	`handler` varchar(64) NOT NULL,
	`node` int(11) NOT NULL,
	`retryTimes` int(11) NOT NULL,
	`status` tinyint(2) NOT NULL,
	`earliestExecuteTime` datetime NOT NULL,
	`parameter` varchar(1024) DEFAULT NULL,
	`host` varchar(64) DEFAULT NULL,
	 PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

commit;
