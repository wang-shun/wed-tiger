# tiger说明

#### 如果阅读完文档后，还有任何疑问，请mail to tengkai.yuan@dianping.com

**tiger**是一种分布式异步调度框架，偏重于执行层面，同一种任务可以由多台机器同时执行，并能保证一条任务不被重复执行。

tiger主要有以下三块组成：

1. zk集群管理：用于管理应用机器的在线情况，进而对机器可执行的任务节点进行自适应分配，保证一个任务同一时间只会被一台机器消费;

2. 事件调度管理：用于每隔一定时间触发一次任务执行，并监听任务执行器的配置情况，一旦发生变化，即停止任务执行，重新设置后再触发任务执行;

3. 任务执行管理：用于管理本机所分配到的执行器节点,进而进行任务节点捞取、任务过滤等,并对任务的执行结果进行处理;

####业务应用场景举例
www.12306.cn上购买火车票的例子：

用户a在12306上提交订单后，会提示请在45分钟内支付，不然就会取消订单。

这样的情形很适合tiger来解决，步骤：

1)  插入一条[订单取消任务]，并设置执行时间45分钟后，addDispatchTask(arg0)

2)  实现任务分发接口DispatchHandler,实现订单取消的业务逻辑（做订单是否已支付的判断）

45分钟后，tiger会自动触发[订单取消任务]。

业务代码逻辑判断：如果此时订单已支付，那么返回任务丢弃；如果订单没支付，那么执行订单取消逻辑，成功后返回。

######总结：tiger适合任何异步任务执行的业务场景.



## ======Quick Start======
### Step一. 依赖

```
<groupId>com.dianping</groupId>
<artifactId>wed-tiger</artifactId>
<version>1.2.4</version>
```

### Step二. 实现任务操作管理接口

#### 任务操作支持两种策略，约定：
***策略Multi***: 各个执行器捞取各自的任务

***策略Single***: 统一捞取任务

***策略Multi***情况下实现接口:

```
配置:
configp.setProperty(ScheduleManagerFactory.ScheduleKeys.taskStrategy.name(),ScheduleConstants.TaskFetchStrategy.Multi.getValue() + "");

则实现各自捞取任务的操作接口
com.dianping.wed.tiger.dispatch.DispatchMultiService

```

***策略Single***情况下实现接口:

```
配置:
configp.setProperty(ScheduleManagerFactory.ScheduleKeys.taskStrategy.name(),ScheduleConstants.TaskFetchStrategy.Single.getValue() + "");

则实现统一捞取任务的操作接口
com.dianping.tiger.dispatch.DispatchSingleService

```
***定义spring bean***

``<bean id="dispatchTaskService" class="你的实现类"/>``
#### Method[必须]实现：
##### 方法1. 添加一条任务
```
public long addDispatchTask(DispatchTaskEntity taskEntity);
```
##### 方法2. 捞取一定数量的任务

***策略Multi***情况下实现:


```
public List<DispatchTaskEntity> findDispatchTasksWithLimit(String handler,List<Integer> nodeList, int limit);
```

***策略Single***情况下实现:

```
public List<DispatchTaskEntity> findDispatchTasksWithLimit(List<Integer> nodeList, int limit);

```

##### 方法3. 更新任务状态
```
public boolean updateTaskStatus(long taskId,int status,String hostName);
```
##### 方法4. 执行不成功，希望下次继续重试
```
public boolean addRetryTimesAndExecuteTime(long taskId,Date nextExecuteTime,String hostName);
```

#### Method[可选]实现:
##### 方法1. 反压获取一定数量的任务，使用前提:
``ScheduleManagerFactory.setBackFetchFlag(true)``

***策略Multi***情况下实现:

```
public List<DispatchTaskEntity> findDispatchTasksWithLimitByBackFetch(String handler, List<Integer> nodeList, int limit,long taskId);
```
***策略Single***情况下实现:


```
public List<DispatchTaskEntity> findDispatchTasksWithLimitByBackFetch(List<Integer> nodeList, int limit, long taskId);
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
        Long taskId =  param.getTaskId();
        String jsonStr = param.getBizParameter();
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

设置30s轮询一次任务
ScheduleManagerFactory smf = new ScheduleManagerFactory(30*1000);

smf.setAppCtx(applicationcontext);

===========初始化配置==============
Properties configp = new Properties();

zk地址，必须
configp.setProperty(ScheduleManagerFactory.ZookeeperKeys.zkConnectAddress.name(),"127.0.0.1:2181,127.0.1.1:2181");

执行器名称，必须
configp.setProperty(ScheduleManagerFactory.ScheduleKeys.handlers.name(),"handler1,hander2,hangdler3");

zk节点rootpath,必须
configp.setProperty(ScheduleManagerFactory.ZookeeperKeys.rootPath.name(),"/XXXX");

虚拟节点数，最好大于20，默认100,可选
configp.setProperty(ScheduleManagerFactory.ScheduleKeys.virtualNodeNum.name(),"20");

zk虚拟节点分配策略,0-散列模式,1－分块模式,默认分块模式,建议用1,可选

configp.setProperty(ScheduleManagerFactory.ScheduleKeys.divideType.name(),ScheduleConstants.NodeDivideMode.DIVIDE_RANGE_MODE.getValue()+"");

执行器策略，可选，默认为策略Multi(多执行器各自捞取任务策略)
configp.setProperty(ScheduleManagerFactory.ScheduleKeys.taskStrategy.name(),ScheduleConstants.TaskFetchStrategy.Multi.getValue()+"");

总调度开关,默认true,可选
configp.setProperty(ScheduleManagerFactory.ScheduleKeys.scheduleFlag.name(),"true");

启用巡航模式，默认true,可选
configp.setProperty(ScheduleManagerFactory.ScheduleKeys.enableNavigate.name(),"true");

启用反压模式，默认false,可选
configp.setProperty(ScheduleManagerFactory.ScheduleKeys.enableBackFetch.name(),"false");

===========初始化启用==========
smf.initSchedule(configp);
```
***完成以上4步，启动你的应用就可以使用了.（应用启动前要部署启动zookeeper服务）***

配置tiger日志：

```
<appender name="TIGER" class="org.apache.log4j.DailyRollingFileAppender">
        <param name="file" value="/data/applogs/tiger-demo/logs/tiger.log"/>
        <param name="append" value="true"/>
        <param name="encoding" value="UTF-8"/>
        <layout class="org.apache.log4j.PatternLayout">
            <param name="ConversionPattern" value="%-d{yyyy-MM-dd HH:mm:ss} [%c]-[%t]-[%M]-[%L]-[%p] %m%n"/>
        </layout>
    </appender>
    
<logger name="com.dianping.wed.tiger" additivity="false">
      <level value="INFO"/>
      <appender-ref ref="TIGER"/>
</logger>
```
应用启动后，查看tiger启动日志，看到红线标注部分(start success)，代表启动成功，如图：

![image](http://code.dianpingoa.com/shop-business/wed-tiger/blob/master/wed-tiger-demo/src/main/resources/img/startlog.png)

**注意点:**


1) ScheduleManagerFactory.keys.handlers.name()的名字需要和DispatchHandler接口实现类的 **bean名字**一样,执行器handler之间用,分隔;

2) DispatchHandler接口实现类的spring bean配置默认是 **单例**，所以在实现类里最好 **不用成员变量**，而要用局部变量， **成员变量是有状态的，会有线程安全问题;**

##### 为了能快速基于tiger搭建分布式异步调度平台，可以直接下载tiger-demo进行修改部署。

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
  

## ======Tiger任务动态加载Groovy======
自 ***1.2.0*** 版本起，tiger支持任务代码的动态修改，通过groovy来实现.

### groovy动态加载接入说明
1 配置启用groovy动态加载开关:

```
configp.setProperty(ScheduleManagerFactory.ScheduleKeys.enableGroovyCode.name(),"true");

```
2 实现groovy code操作接口

```
com.dianping.wed.tiger.groovy.IGroovyCodeRepo
```

3 接下来实现任务分发接口（同quick start step3）

**groovy特别说明**

1) groovy代码中service注入方式

```
import com.dianping.wed.tiger.annotation.TService;

@TService
private WedSmsSendService wpsWedSmsSendService;
```
2) groovy代码支持单例和多例两种方式，默认为多例，若需要使用单例，则采用如下注解的形式

```
import com.dianping.wed.tiger.annotation.AnnotationConstants;
import com.dianping.wed.tiger.annotation.GroovyBeanType;

@GroovyBeanType(AnnotationConstants.BeanType.SINGLE)
class GroovyTest implements DispatchHandler {
}
```

## ======Tiger监控======
tiger应用运行期间，支持任务监控，部署tiger-monitor
并且在tiger应用中增加如下配置:

```
监控服务地址，必须
configp.setProperty(ScheduleManagerFactory.MonitorKeys.monitorIP.name(),"http://127.0.0.1:8080");

监控开关，默认关闭，必须
configp.setProperty(ScheduleManagerFactory.MonitorKeys.enableMonitor.name(),"true");

同时支持监控运行中开关控制：
scheduleManagerFactory.setMonitorFlag(boolean flag);

```
**注意点:**
tiger监控用的是文件存储方式，需要对/data/appdatas/tiger/目录有读写权限

tiger监控截图：

![image](http://code.dianpingoa.com/shop-business/wed-tiger/blob/master/wed-tiger-demo/src/main/resources/img/startlog.png)


**Thanks**
