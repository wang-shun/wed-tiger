$(function(){
	// 导航栏,选中样式处理
	$(".nav-click").each(function(){
		if( window.location.href.indexOf( $(this).find("a").attr("href") ) > -1){
			$(this).siblings().removeClass("active");  
			if (!$(this).hasClass("active")) {
				$(this).addClass("active");
				return;
			}
		}
	});
	
});

//通用提示
var ComAlert = {
	show:function(type, msg){
		// 弹框初始
		if (type == 1) {
			$('#comAlert .alert').attr('class', 'alert alert-success');
		} else {
			$('#comAlert .alert').attr('class', 'alert alert-warning');
		}
		$('#comAlert .alert').html(msg);
		$('#comAlert').modal('show');
		// 监听关闭
		$("#comAlert").on('hide.bs.modal', function () {
			ComAlert.callback();
		});
	},
	hide:function(){
		$('#comAlert').modal('hide');
	},
	callback:function(){
		// TODO
	}
};
