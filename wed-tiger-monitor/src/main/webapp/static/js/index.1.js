$(function () {
	
	// query
	$("#query").click(function(){
		var hadleName = $("#hadleName").val();
		var monitorTime = $("#monitorTime").val();
		if (!hadleName) {
			ComAlert.show(0, "hadleName");
			return;
		}
		
		window.location.href = base_url + "?hadleName=" + hadleName + "&monitorTime=" + monitorTime;
		
	});
	
	// chart
    $('#container').highcharts({
        title: {
            text: 'hander = demoHander; 日期 = 2015-08-31',
            x: -20 //center
        },
        xAxis: {
        	title: {text:'执行时间'},
        	labels: {
                formatter: function() {
                    return Highcharts.dateFormat('%Y-%m-%d %H:%M:%S', this.value);                  
                }
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
				'执行时间=' + Highcharts.dateFormat('%Y-%m-%d %H:%M:%S',this.x) + ', <br>' +
				'执行次数=' + this.y + ', <br>' +
				'平均耗时=' + this.point.avgCost;
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
