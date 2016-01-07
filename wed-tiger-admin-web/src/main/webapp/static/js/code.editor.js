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
	var orginCode = editor.getSession().getValue();
	
	// reset
	$("#reset").on('click', function(){
		editor.getSession().setValue("");
	});
	
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
				url : base_url + 'admin/save',
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
							location.href = "/admin/list";
						});
					} else {
						ComAlert.alert(data.msg);
					}
				}
			});
		}
	});
	
	$("#submit1").on('click', function(){//更新handler
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
				url : base_url + 'admin/save',
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
							location.href = "/admin/list";
						});
					} else {
						ComAlert.alert(data.msg);
					}
				}
			});
		}
	});
	
	$("#reset1").on('click', function(){
		editor.setValue(orginCode);
	});
	
	$("#delete").on('click', function(){//删除handler
		
		var handlerName = $(this).attr('data');;
		deleteHandler(handlerName);
		
	});
	
	
	function deleteHandler(handlerName) {
        var content = '<div class="msg">' +
            '你确定要删除' +handlerName+'吗?'
            '</div>' +
            '<div class="btn-box">' +
            '<span class="medi-btn btn"><a href="javascript:;" title="确定" class="btn-txt J_sureDelete">确定</a></span>' +
            '<span class="medi-btn-ash"><a href="javascript:;" title="取消" class="btn-txt J_close-hintbox">取消</a></span>' +
            '</div>';

        var mbox = new Mbox({
            winCls: 'popup-sty pop-recharge',
            contCls: 'con',
            closeable: true,
            title: '删除',
            content: content,
            size: { x: 360}
        }).on('show', function () {
                var self = this;
                $.all('.J_close-hintbox').on('click', function () {
                    self.close();
                });

                $(".J_sureDelete").on('click', function () {
                    self.close();
                    // 确认删除 发送ajax
                    sureDelete(handlerName);
                });
            })
            .open();
    }

    function sureDelete(handlerName) {
        new Ajax({
            url: '/admin/delete',
            data: {
                handlerName: handlerName
            },
            method: "post"
        }).on("success",function (json) {
                var html;
                if (!json) {
                	ComAlert.alert('系统错误');
                }
                if (json.code == 200) {
                	ComAlert.alert('删除成功');
                } else {
                	ComAlert.alert('删除失败');
                }
            }).send();
    }
	
});
