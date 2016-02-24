"use strict";
define(function(require, exports, module) {
	var jquery = require("jquery");
	var ComAlert = require('semantic-comalert');
	
	// init ace-editor
	//require("source-code-pro");
	var ace = require("ace/ace");
	var editor = ace.edit("ace-editor");
	editor.setTheme("ace/theme/idle_fingers");
	editor.getSession().setMode("ace/mode/groovy");
	var orginCode = $("#orginCode").val();
	//console.log(orginCode);
	
	//添加handler
	$("#submit").on('click', function(){
		var code = editor.getSession().getValue();
		
		var handlerName = $("#handlerName").val();
		var desc = $("#desc").val();
		if(!handlerName || !desc){
			ComAlert.alert('handlerName及描述不能为空');
		}else{
			$.ajax({
				type : 'POST',
				url : '/save',
				data : {
					'handlerName' : handlerName,
					'desc' : desc,
					'code' : code,
					'actionType':"add"
				},
				dataType : "json",
				success : function(data){
					if (data.code == 200) {
						ComAlert.alert('添加成功',function(){
							location.href = "/list";
						});
					} else {
						ComAlert.alert(data.msg);
					}
				}
			});
		}
	});
	
	//修改handler
	$("#submit1").on('click', function(){
		var code = editor.getSession().getValue();
		// or session.getValue
		console.log(code);
		var handlerName = $("#handlerName").html();
		var desc = $("#desc").val();
		
		if(!desc){
			ComAlert.alert("描述不能为空");
		}else{
		
			$.ajax({
				type : 'POST',
				url : '/save',
				data : {
					'handlerName' : handlerName,
					'code' : code,
					'desc' : desc,
					'actionType':"update"
				},
				dataType : "json",
				success : function(data){
					if (data.code == 200) {
						ComAlert.alert('更新成功',function(){
							location.href = "/list";
						});
					} else {
						ComAlert.alert(data.msg);
					}
				}
			});
		}
	});
	
	$("#reset").on('click', function(){
		editor.setValue(orginCode);
	});
	$("#reset").click();
	
});
