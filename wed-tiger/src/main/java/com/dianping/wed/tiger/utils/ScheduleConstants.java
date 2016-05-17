package com.dianping.wed.tiger.utils;

public interface ScheduleConstants {
	
	/**
	 * 任务捞取策略:0-统一捞取任务，1-各个执行器各自捞取各自的任务
	 * @author yuantengkai
	 *
	 */
	public enum TaskFetchStrategy{
		
		Single(0),Multi(1);
		
		private int value;

		private TaskFetchStrategy(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}
	
	/**
	 * 节点分配方式:0-散列 1-分块
	 * @author yuantengkai
	 *
	 */
	public enum NodeDivideMode{
		
		DIVIDE_SANLIE_MODE(0),DIVIDE_RANGE_MODE(1);
		
		private int value;

		private NodeDivideMode(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}

	/**
	 * 任务状态
	 *
	 */
	public enum TaskType {
		
		/**
		 * 待调度执行
		 */
		NEW(0), 
		
		/**
		 * 执行成功
		 */
		SUCCESS(1), 
		
		/**
		 * 执行失败，不再执行(丢弃)
		 */
		FAIL(2);

		private int value;

		private TaskType(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}
	}
}
