"use strict";
define(function(require, exports, module) {
	var jquery = require("jquery");
	var ComAlert = require('semantic-comalert');
	
	$(".delete").on('click', function(){//删除handler
		
		var handlerName = $(this).attr('data');
		var content = '确定删除'+ handlerName +'吗?';
		ComAlert.confirm(content,function(){
			sureDelete(handlerName);
		},function(){
			
		});
		
	});

    function sureDelete(handlerName) {
    	$.ajax({
			type : 'POST',
			url : base_url + 'admin/delete',
			data : {
				'handlerName' : handlerName,
			},
			dataType : "json",
			success : function(data){
				if (data.code == 200) {
					ComAlert.alert('删除成功');
				} else {
					ComAlert.alert(data.msg);
				}
			}
		});
    }
    	
});
