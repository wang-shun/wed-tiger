<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<title>Tiger Monitor</title>
	
	<#include "/common/common.style.ftl">
	<#import "/common/common.content.ftl" as netCommon>
	
	<!-- Highcharts -->
	<script type="text/javascript" src="${base_url}static/plugin/Highcharts-4.1.8/highcharts.js"></script>
	<script type="text/javascript" src="${base_url}static/plugin/Highcharts-4.1.8/modules/exporting.js"></script>
	
	<!-- bootstrap-datetimepicker -->
	<link rel="stylesheet" href="${base_url}static/plugin/bootstrap-datetimepicker/css/bootstrap-datetimepicker.min.css" >
	<script type="text/javascript" src="${base_url}static/plugin/bootstrap-datetimepicker/js/bootstrap-datetimepicker.min.js"></script>
	<script type="text/javascript" src="${base_url}static/plugin/bootstrap-datetimepicker/js/locales/bootstrap-datetimepicker.zh-CN.js"></script>
	
	<!-- meme -->
	<script type="text/javascript">
		var chartData = new Array();
		<#if map?exists>
			<#list map?keys as key>
				itemName = '${key}';
				itemData = new Array();
				<#list map[key] as item>
					itemData.push({
						x:new Date(${item.monitorTime?long}), 
						y:${item.totalNum}, 
						sucNum:${item.sucNum},
						failNum:${item.failNum},
						avgCost:${item.avgCost},
						maxCost:${item.maxCost},
						minCost:${item.minCost}
					});
				</#list>
				chartData.push({'name':itemName, 'data':itemData});
			</#list>
		</#if>
	</script>
	<script type="text/javascript" src="${base_url}static/js/index.1.js"></script>
	
</head>
<body>
<@netCommon.header />

<!-- content -->
<div class="container">

	<div class="row">
		<div class="col-xs-3">
			<div class="input-group">
				<span class="input-group-addon">hander</span>
				<input type="text" class="form-control" id="hadleName" value="${hadleName}" placeholder="请输入hander">
			</div>
		</div>
		<div class="col-xs-7">
			<div class="input-group">
				<span class="input-group-addon">日期</span>
				<input type="text" class="form-control monitorTime" id="monitorTimeFrom" value="${monitorTimeFrom?datetime}" placeholder="开始时间">
				<span class="input-group-addon"><span class="glyphicon glyphicon-calendar"></span></span>
				<input type="text" class="form-control monitorTime" id="monitorTimeTo" value="${monitorTimeTo?datetime}" placeholder="结束时间">
			</div>
		</div>
		<div class="col-xs-2">
			<button type="button" class="btn btn-primary pull-right" id="query">查询</button>
		</div>
	</div>
    <hr>
	<div id="container" style="min-width: 310px; height: 400px; margin: 0 auto"></div>
	
	<!--大标题
	<div class="jumbotron"><h4>Hey Girl.</h4></div>
	-->
	
</div>

<@netCommon.footer />
</body>
</html>
