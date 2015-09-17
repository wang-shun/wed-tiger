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
	
	// chart local
	Highcharts.setOptions({ global: { useUTC: false } });   // 时区处理
	// 执行次数
    $('#container_count').highcharts({
        title: {
            text: '执行次数监控',
            x: -20 //center
        },
        xAxis: {
        	title: {text:'执行时间'},
        	labels: {
                formatter: function() {
                    return Highcharts.dateFormat('%H:%M:%S', this.value);                  
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
        		
        		var failPanel = this.point.failNum;
        		if (this.point.failNum > 0) {
        			failPanel = '<span style="color:red;">' + this.point.failNum +'</span>';
				}
        		
				return '<b>' + this.series.name + '</b>：<br>' + 
					'监控时间=' + Highcharts.dateFormat('%H:%M:%S',this.x) + ' <br>' +		// %Y-%m-%d %H:%M:%S
					'执行次数=' + this.y + ' <br>' +
					'成功次数=' + this.point.sucNum + ' <br>' +
					'失败次数=' + failPanel + ' <br>';
					/*+
					'平均耗时=' + this.point.avgCost + ' <br>' +
					'max耗时=' + this.point.maxCost + ' <br>' +
					'min耗时=' + this.point.minCost;*/
			}
        },
        series:chartCountData 
    });
    // 执行耗时
    $('#container_cost').highcharts({
        title: {
            text: '执行耗时监控',
            x: -20 //center
        },
        xAxis: {
        	title: {text:'执行时间'},
        	labels: {
                formatter: function() {
                    return Highcharts.dateFormat('%H:%M:%S', this.value);                  
                },
                rotation: -45
            }
        },
        yAxis: {
        	title: {text:'执行耗时/ms'},
            plotLines: [{
                value: 0,
                width: 1,
                color: '#808080'
            }]
        },
        tooltip: {
        	formatter:function(){
        		
        		var failPanel = this.point.failNum;
        		if (this.point.failNum > 0) {
        			failPanel = '<span style="color:red;">' + this.point.failNum +'</span>';
				}
        		
				return '<b>' + this.series.name + '</b>：<br>' + 
					'监控时间=' + Highcharts.dateFormat('%H:%M:%S',this.x) + ' <br>' +		// %Y-%m-%d %H:%M:%S
					/*'执行次数=' + this.y + ' <br>' +
					'成功次数=' + this.point.sucNum + ' <br>' +
					'失败次数=' + failPanel + ' <br>' +*/
					'平均耗时=' + this.point.avgCost + ' <br>' +
					'max耗时=' + this.point.maxCost + ' <br>' +
					'min耗时=' + this.point.minCost;
			}
        },
        series:chartCostData 
    });
    
});

function fillHandler(handlerName){
	$("#handlerName").val(handlerName);
}
