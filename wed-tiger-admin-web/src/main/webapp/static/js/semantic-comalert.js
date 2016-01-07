'use strict';
define(['jquery', 'semantic', 'semantic-ui-modal'], function() {
	// 闭包 ： 可访问上一层函数作用域里变量的函数，即便上一层函数已经关闭。例如 “计数器困境”
	return {
		alert : function(msg, callback){
			if ($('#ComAlert').length == 0){
				var comAlertHtml = '<div class="ui modal" id="ComAlert" >' +
				  	'<i class="close icon"></i>' +
				  	'<div class="header">提示</div>' +
					'<div class="content">' +
					    '<div class="description">...</div>' +
				  	'</div>' +
				  	'<div class="actions">' +
					    '<div class="ui button green ok">确定</div>' +
				  	'</div>' +
				'</div>';
				$('body').append(comAlertHtml);
			}
			
			$('#ComAlert .description').html(msg);
			$('#ComAlert').modal({
				closable  : false,
				onApprove : function() {
					if(typeof callback == 'function') {
						callback();
						callback = null;	// 闭包js会缓存入参callback函数，蛋疼
					}
			    }
			}).modal('show');
		},
		confirm : function(msg, callback_ok, callback_cancel){
			if ($('#ComConfirm').length == 0){
				var comAlertHtml = '<div class="ui modal" id="ComConfirm" >' +
				  	'<i class="close icon"></i>' +
				  	'<div class="header">提示</div>' +
				  	'<div class="actions">' +
					    '<div class="ui button green ok">确定</div>' +
					    '<div class="ui button red  cancel">取消</div>' +
				  	'</div>' +
				'</div>';
				$('body').append(comAlertHtml);
			}
			
			$('#ComConfirm .header').html(msg);
			console.log(callback_ok);
			console.log(callback_cancel);
			$('#ComConfirm').modal({
				closable  : false,
				onApprove : function(){
					if(typeof callback_ok == 'function') {
						callback_ok();
					}
			    },
			    onDeny : function() {
			    	if(typeof callback_cancel == 'function') {
			    		callback_cancel();
					}
			    }
			}).modal('show');
		}
	}
	
});