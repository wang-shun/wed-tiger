# tiger说明

#### 如果阅读完文档后，还有任何疑问，请mail to tengkai.yuan@dianping.com

**tiger**是一种分布式异步执行框架，偏重于执行层面，同一种任务可以由多台机器同时执行，并能保证一条任务不被重复执行。

tiger主要有以下三块组成：

1. zk集群管理：用于管理应用机器的在线情况，进而对机器可执行的任务节点进行自适应分配，保证一个任务同一时间只会被一台机器消费;

2. 事件调度管理：用于每隔一定时间触发一次任务执行，并监听任务执行器的配置情况，一旦发生变化，即停止任务执行，重新设置后再触发任务执行;

3. 任务执行管理：用于管理本机所分配到的执行器节点,进而进行任务节点捞取、任务过滤等,并对任务的执行结果进行处理;

## 使用步骤
### Step一. 依赖

```
<groupId>com.dianping</groupId>
<artifactId>wed-tiger</artifactId>
<version>1.0.1</version>
```

### Step二. 实现任务操作管理接口

``com.dianping.wed.tiger.dispatch.DispatchTaskService``
#### 必须实现：
##### 方法1. 添加一条任务
```
public int addWedDispatchTask(DispatchTaskEntity taskEntity);
```
##### 方法2. 捞取一定数量的任务
```
public List<DispatchTaskEntity> findDispatchTasksWithLimit(String handler,List<Integer> nodeList, int limit);
```
##### 方法3. 更新任务状态
```
public boolean updateTaskStatus(int taskId,int status,String hostName);
```
##### 方法4. 执行不成功，希望下次继续重试
```
public boolean addRetryTimesAndExecuteTime(int taskId,Date nextExecuteTime,String hostName);
```

#### 可选实现:
##### 方法1. 反压获取一定数量的任务，使用前提ScheduleManagerFactory.setBackFetchFlag(true)
```
public List<DispatchTaskEntity> findDispatchTasksWithLimitByBackFetch(String handler, List<Integer> nodeList, int limit,int taskId);
```
### Step三. 实现任务分发接口
``com.dianping.wed.tiger.dispatch.DispatchHandler``

这里用于实现 ***业务逻辑***;

任务分发支持并行、串行两种执行策略。 默认是并行执行策略，如果需要串行执行策略（同一个任务有先后执行顺序的情况下）,在实现的类里增加一个注解,如：

```
@ExecuteType(AnnotationConstants.Executor.CHAIN)
public class ChainTestHandler implements DispatchHandler {
    @Override
    public DispatchResult invoke(DispatchParam param) throws Exception {
        int taskId = (Integer) param.getProperty("id");
        String jsonStr = (String) param.getProperty("param");
        Map<String, String> paramMap = (Map<String, String>) JSON.parse(jsonStr);
        ...
    }
}
```
### Step四. 应用启动唤起
``com.dianping.wed.tiger.ScheduleManagerFactory``
***example***:

```
===========声明 ScheduleManagerFactory=======
ScheduleManagerFactory smf = new ScheduleManagerFactory(30*1000);

smf.setAppCtx(applicationcontext);

===========初始化配置==============
Properties configp = new Properties();

zk地址，必须
configp.setProperty(ScheduleManagerFactory.keys.zkConnectAddress.name(),"127.0.0.1:2181,127.0.1.1:2181");

执行器名称，必须
configp.setProperty(ScheduleManagerFactory.keys.handlers.name(),"handler1,hander2,hangdler3");

zk节点rootpath,必须
configp.setProperty(ScheduleManagerFactory.keys.rootPath.name(),"/DPWED");

虚拟节点数，最好大于20，默认100,可选
configp.setProperty(ScheduleManagerFactory.keys.visualNodeNum.name(),"30");

zk虚拟节点分配策略,1-散列模式,2－分块模式,默认分块模式,建议用2,可选
configp.setProperty(ScheduleManagerFactory.keys.divideType.name(), "2");

总调度开关,默认true,可选
configp.setProperty(ScheduleManagerFactory.keys.scheduleFlag.name(),"true");

启用巡航模式，默认true,可选
configp.setProperty(ScheduleManagerFactory.keys.enableNavigate.name(),"true");

启用反压模式，默认false,可选
configp.setProperty(ScheduleManagerFactory.keys.enableBackFetch.name(),"false");

===========初始化启用==========
smf.initSchedule(configp);
```
***完成以上4步，启动你的应用就可以使用了.***

**注意点:**


1) ScheduleManagerFactory.keys.handlers.name()的名字需要和DispatchHandler接口实现类的 **bean名字**一样,执行器handler之间用,分隔;

2) DispatchHandler接口实现类的spring bean配置默认是 **单例**，所以在实现类里最好 **不用成员变量**，而要用局部变量， **成员变量是有状态的，会有线程安全问题;**


### Step五. 运行中改变
  
  初始化需要的配置外，tiger支持运行中的 ***配置改变***，目前支持以下几种:
  
  1) 运行过程中执行器配置改变
  
  ``ScheduleManagerFactory.reSchedule(List<String> handlers);``
  
  2) 运行中调度总开关控制
  
  ``ScheduleManagerFactory.setScheduleFlag(boolean flag);``
  
  3) 运行中巡航开关控制
  
  ``ScheduleManagerFactory.setNavigateFlag(boolean flag);``
  
  4) 运行中反压措施开关控制
  
  ``ScheduleManagerFactory.setBackFetchFlag(boolean flag);``
  

**Thanks**

