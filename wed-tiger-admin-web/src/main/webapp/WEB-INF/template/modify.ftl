<#import "/common/common.macro.ftl" as netCommon>
<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<title>Tiger Modify</title>
	<@netCommon.commonStyle />
	<style>
    html {height: 100%;width: 100%;overflow: hidden;}
	body {overflow: hidden;margin: 0;padding: 0;height: 100%;width: 100%;font-size: 12px;font-family: Arial, Helvetica, sans-serif, Tahoma, Verdana, sans-serif;/*background: rgb(14, 98, 165);color: white;*/}
	#ace-editor {position: absolute;top: 50px;left: 10px;bottom: 10px;right: 0px;/*background: white;*/}
    </style>
</head>
<body>

<!-- tools -->
<div class="ui fixed inverted menu">
	<div class="ui container">
	    <a href="javascript:;" class="item">HandlerName:<span id="handlerName">${handlerCode.handlerName}</span></a>
	    <a href="javascript:;" class="item">描述:</a><input type="text" id="desc" value="${handlerCode.desc}" style="width:400px;height:30px;margin-top:6px;margin-bottom:6px;border:0px;border-radius:2px;">
	    <a href="javascript:;" class="item right" id="reset" >重置</a>
	    <a href="javascript:;" class="item" id="submit1" >提交</a>
	    <a href="/list" class="item">返回列表页</a>
    </div>
</div>

<!-- editor -->
<div id="ace-editor"></div>
<textarea id="orginCode" style="display:none;" >${handlerCode.code}</textarea>

<script src="/static/plugin/requirejs/requirejs.2.1.22.min.js" data-main="/static/js/requirejs.config" ></script>
<script>
require(['code.editor']);
</script>


</body>
</html>
