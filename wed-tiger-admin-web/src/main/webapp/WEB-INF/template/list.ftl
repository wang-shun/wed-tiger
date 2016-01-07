<#import "/common/common.macro.ftl" as netCommon>
<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<title>Tiger admin</title>
	<@netCommon.commonStyle />
	<#include "/common/common.style.ftl">
	<#include "/common/common.pageNavigation.ftl">
</head>
<body>

<!-- content -->
<div class="container" style="padding-left:15px;padding-right:15px;margin-left:auto;margin-right:auto;width:1170px">
	<h3 style="width:100%;text-align:center;padding-top:17px">Handler列表页</h3>
    <a href="/admin/add" class="addBtn">添加新的handler</a>
	<table border="0" cellspacing="0" cellpadding="0"><#t>
    <tbody><#t>
    <tr><#t>
    	<th class="tit" style="width: 30%">handlerName</th><#t>
    	<th class="tit" style="width: 50%">描述</th><#t>
        <th class="tit" style="width: 20%">操作</th><#t>
    </tr><#t>
    <#list handlerList as handler>
    <tr><#t>
    	<td class="tit">${handler.handlerName}</td><#t>
    	<td class="tit">${handler.desc}</td>
        <td class="opt">
            <a href="/admin/modify?handlerName=${handler.handlerName}">修改</a>
            <em class="line">|</em>
            <a href="javascript:;" class="delete" data=${handler.handlerName}>删除</a>
        </td>
    </tr><#t>
    </#list>
    </tbody><#t>
	</table><#t>
	<div class="pages-num"><#t><#-- 分页控件 -->
		<@pageNavigation pageModel/>
	</div><#t>
</div>
<script src="/static/plugin/requirejs/requirejs.2.1.22.min.js" data-main="/static/js/requirejs.config" ></script>
<script>
var base_url = '/';
require(['list']);
</script>

</body>
</html>
