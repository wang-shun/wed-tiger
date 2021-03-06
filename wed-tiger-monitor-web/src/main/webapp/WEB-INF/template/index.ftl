<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<title>Tiger Monitor</title>
	
	<#include "/common/common.style.ftl">
	<#import "/common/common.content.ftl" as netCommon>
	
	<!-- Highcharts -->
	<script type="text/javascript" src="/static/plugin/Highcharts-4.1.8/highcharts.js"></script>
	<script type="text/javascript" src="/static/plugin/Highcharts-4.1.8/modules/exporting.js"></script>
	
	<!-- bootstrap-datetimepicker -->
	<link rel="stylesheet" href="/static/plugin/bootstrap-datetimepicker/css/bootstrap-datetimepicker.min.css" >
	<script type="text/javascript" src="/static/plugin/bootstrap-datetimepicker/js/bootstrap-datetimepicker.min.js"></script>
	<script type="text/javascript" src="/static/plugin/bootstrap-datetimepicker/js/locales/bootstrap-datetimepicker.zh-CN.js"></script>
	
	<!-- meme -->
	<script type="text/javascript">
		var chartCountData = new Array();
		var chartCostData = new Array();
		<#if map?exists>
			<#list map?keys as key>
				// 执行次数
				itemName = '${key}';
				itemData = new Array();
				<#list map[key] as item>
					itemData.push({
						x:new Date(${item.monitorTime?long}), 
						y:${item.totalNum}, 
						monitorTime:new Date(${item.monitorTime?long}), 
						totalNum:${item.totalNum},
						sucNum:${item.sucNum},
						failNum:${item.failNum},
						avgCost:${item.avgCost},
						maxCost:${item.maxCost},
						minCost:${item.minCost}
						<#if item.failNum gt 0 >,color:'red'</#if>
					});
				</#list>
				chartCountData.push({'name':itemName, 'data':itemData});
				
				// 执行耗时
				itemName = '${key}';
				itemData = new Array();
				<#list map[key] as item>
					itemData.push({
						x:new Date(${item.monitorTime?long}), 
						y:${item.avgCost}, 
						monitorTime:new Date(${item.monitorTime?long}), 
						totalNum:${item.totalNum},
						sucNum:${item.sucNum},
						failNum:${item.failNum},
						avgCost:${item.avgCost},
						maxCost:${item.maxCost},
						minCost:${item.minCost}
					});
				</#list>
				chartCostData.push({'name':itemName, 'data':itemData});
			</#list>
		</#if>
	</script>
	<script type="text/javascript" src="/static/js/index.1.js"></script>
	
</head>
<body>
<@netCommon.header />

<!-- content -->
<div class="container">

	<div class="row">
		<#if handlerNameList?exists && handlerNameList?size gt 0 >
		<div class="col-xs-1">
			<div class="input-group">
				<div class="dropdown">
				   <button type="button" class="btn dropdown-toggle"  data-toggle="dropdown">handler&nbsp;<span class="caret"></span></button>
				   <ul class="dropdown-menu" role="menu" style="overflow:auto;height:auto !important;max-height:250px;" >
				   		<#list handlerNameList as item>
				      	<li role="presentation"><a role="menuitem" tabindex="-1" href="javascript:fillHandler('${item}');">${item}</a></li>
				      	</#list>
					</ul>
				</div>
			</div>
		</div>
		<div class="col-xs-3 pull-left">
			<div class="input-group">
				<input type="text" class="form-control" id="handlerName" value="${handlerName}" placeholder="请输入handler">
			</div>
		</div>
		<#else>
		<div class="col-xs-4">
			<div class="input-group">
				<span class="input-group-addon">handler</span>
				<input type="text" class="form-control" id="handlerName" value="${handlerName}" placeholder="请输入handler">
			</div>
		</div>
		</#if>
		<div class="col-xs-6">
			<div class="input-group">
				<span class="input-group-addon">日期</span>
				<input type="text" class="form-control monitorTime" id="monitorTimeFrom" value="${monitorTimeFrom?datetime}" placeholder="开始时间">
				<span class="input-group-addon"><span class="glyphicon glyphicon-calendar"></span></span>
				<input type="text" class="form-control monitorTime" id="monitorTimeTo" value="${monitorTimeTo?datetime}" placeholder="结束时间">
			</div>
		</div>
		<div class="col-xs-2 ">
			<button type="button" class="btn btn-primary pull-right" id="query">查询</button>
		</div>
	</div>
    <hr>
    <!-- 执行次数 -->
	<div id="container_count" style="min-width: 250px; height: 280px; margin: 0 auto"></div>
	<hr>
	<!-- 执行耗时 -->
	<div id="container_cost" style="min-width: 250px; height: 280px; margin: 0 auto"></div>
	
</div>

<@netCommon.footer />
</body>
</html>
