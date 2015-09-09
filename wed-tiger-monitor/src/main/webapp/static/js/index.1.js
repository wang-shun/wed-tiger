$(function () {
	// datetimepicker
	$('.monitorTime').datetimepicker({
        weekStart: 1,
        todayBtn:  1,
		autoclose: 1,
		todayHighlight: 1,
		startView: 2,
		forceParse: 1,
        showMeridian: 0,
        language:'zh-CN',
        format:'yyyy-mm-dd hh:mm:ss',
        minView: 1,
        minuteStep:1
    });
	
	// query
	$("#query").click(function(){
		var handlerName = $("#handlerName").val();
		var monitorTimeFrom = $("#monitorTimeFrom").val();
		var monitorTimeTo = $("#monitorTimeTo").val();
		if (!handlerName) {
			ComAlert.show(0, "请输入handlerName");
			return;
		}
		
		window.location.href = "/tiger?handlerName=" + handlerName 
			+ "&monitorTimeFrom=" + monitorTimeFrom 
			+ "&monitorTimeTo=" + monitorTimeTo;
		
	});
	
	// chart
	Highcharts.setOptions({ global: { useUTC: false } });   // 时区处理
    $('#container').highcharts({
        title: {
            text: '监控中心',
            x: -20 //center
        },
        xAxis: {
        	title: {text:'执行时间'},
        	labels: {
                formatter: function() {
                    return Highcharts.dateFormat('%Y-%m-%d %H:%M:%S', this.value);                  
                },
                rotation: -45
            }
        },
        yAxis: {
        	title: {text:'执行次数'},
            plotLines: [{
                value: 0,
                width: 1,
                color: '#808080'
            }]
        },
        tooltip: {
        	formatter:function(){
				return '<b>' + this.series.name + '</b>：<br>' + 
					'监控时间=' + Highcharts.dateFormat('%Y-%m-%d %H:%M:%S',this.x) + ' <br>' +
					'执行次数=' + this.y + ' <br>' +
					'成功次数=' + this.point.sucNum + ' <br>' +
					'失败次数=' + this.point.failNum + ' <br>' +
					'平均耗时=' + this.point.avgCost + ' <br>' +
					'max耗时=' + this.point.maxCost + ' <br>' +
					'min耗时=' + this.point.minCost;
			}
        },
        series:chartData 
        /*
        	[{
            name: 'host1',
            data: [{x:new Date(2015, 9, 01, 8, 59, 59), y:10, avgCost:'04'}, 	// yyyy,mth(月份区间0-11),dd,hh,mm,ss
                   {x:new Date(2015, 9, 02, 9, 59, 59), y:20, avgCost:'78'},
                   {x:new Date(2015, 9, 04, 10, 59, 59), y:30, avgCost:'278'},
                   {x:new Date(2015, 9, 06, 11, 59, 59), y:40, avgCost:'20'},
                   {x:new Date(2015, 9, 08, 12, 59, 59), y:50, avgCost:'21'},]
        	}]
        */
    });
});
