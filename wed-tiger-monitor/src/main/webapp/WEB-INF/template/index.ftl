<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<title>Tiger Monitor</title>
	
	<#include "/common/common.style.ftl">
	<#import "/common/common.content.ftl" as netCommon>
	
	<script type="text/javascript" src="${base_url}static/plugin/Highcharts-4.1.8/highcharts.js"></script>
	<script type="text/javascript" src="${base_url}static/plugin/Highcharts-4.1.8/modules/exporting.js"></script>
	<script type="text/javascript">
		var chartData = new Array();
		<#if map?exists>
			<#list map?keys as key>
				itemName = '${key}';
				itemData = new Array();
				<#list map[key] as item>
					itemData.push({x:new Date(${item.monitorTime?long}), y:'${item.totalNum}', avgCost:'${item.avgCost}'});
				</#list>
				chartData.push({'name':itemName, 'data':itemData});
			</#list>
		</#if>
		
		chartData = [{
            name: 'host1',
            data: [{x:new Date(2015, 9, 01, 8, 59, 59), y:10, avgCost:'04'}, 	// yyyy,mth(月份区间0-11),dd,hh,mm,ss
                   {x:new Date(2015, 9, 02, 9, 59, 59), y:20, avgCost:'78'},
                   {x:new Date(2015, 9, 04, 10, 59, 59), y:30, avgCost:'278'},
                   {x:new Date(2015, 9, 06, 11, 59, 59), y:40, avgCost:'20'},
                   {x:new Date(2015, 9, 08, 12, 59, 59), y:50, avgCost:'21'},]
        	}];
        	
    	chartData = new Array();
    	asd = new Array();
    	asd.push({x:new Date(2015, 9, 01, 9, 59, 59), y:20, avgCost:'78'});
    	asd.push({x:new Date(2015, 9, 02, 9, 59, 59), y:20, avgCost:'78'});
    	chartData.push({'name':'asdas', 'data':asd});
    	
	</script>
	<script type="text/javascript" src="${base_url}static/js/index.1.js"></script>
	
</head>
<body>
<@netCommon.header />

<!-- content -->
<div class="container">

	<div class="row">
		<div class="col-xs-5">
			<div class="input-group">
				<span class="input-group-addon">hander</span>
				<input type="text" class="form-control" id="hadleName" value="${hadleName}" placeholder="请输入hander">
			</div>
		</div>
		<div class="col-xs-5">
			<div class="input-group">
				<span class="input-group-addon">日期</span>
				<input type="text" class="form-control" id="monitorTime" value="${monitorTime}" placeholder="请输入日期(默认查询当日)">
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