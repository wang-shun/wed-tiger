<#macro header>
<!-- header导航栏default  -->
<nav class="navbar navbar-inverse navbar-fixed-top" role="navigation">
	<div class="container">
		<!-- 折叠 -->
		<div class="navbar-header">
			<button type="button" class="navbar-toggle" data-toggle="collapse" data-target="#net-navbar-collapse">
				<span class="sr-only"></span>
				<span class="icon-bar"></span>
				<span class="icon-bar"></span>
				<span class="icon-bar"></span>
	      	</button>
			<a class="navbar-brand" href="${base_url}" ><span class="glyphicon glyphicon-home"></span></a>
		</div>
		<!-- 响应式特性 -->
		<div class="collapse navbar-collapse" id="net-navbar-collapse">
			<!-- 左对齐 -->
			<ul class="nav navbar-nav navbar-left active-nav" >
				<li class="nav-click" ><a href="${base_url}" >监控中心</a></li>
			</ul>
	   </div>
	</div>
</nav>
</#macro>

<#macro footer>
<footer class="footer">
	<!-- footerlinks -->
	<section class="footerlinks">
		<div class="container">
			<div class="info">
				<ul>
					<li><b>关于:</b></li>
	          		<li><a href="http://w4c.dp/?p=1040" target="_blank" >关于Tiger</a></li>
			   		<li><a href="http://w4c.dp/?p=1040" target="_blank" >接入文档</a></li>
	        	</ul>
	      	</div>
	    </div>
	</section>
	<!-- copyrightbottom -->
	<section class="copyrightbottom">
		<div class="container">
			<div class="info">“Tiger”是一种分布式异步执行框架，偏重于执行层面，同一种任务可以由多台机器同时执行，并能保证一条任务不被重复执行。。</div>
			<div class="info">Copyright © 2015 dianping</div>
		</div>
	</section>
</footer>

<!-- 通用提示框.模态框Modal -->
<div class="modal fade" id="comAlert" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
	<div class="modal-dialog">
		<div class="modal-content">
			<!--	<div class="modal-header"><h4 class="modal-title"><strong>提示:</strong></h4></div>	-->
         	<div class="modal-body"><div class="alert alert-success"></div></div>
         	<div class="modal-footer">
         		<div class="text-center" >
            		<button type="button" class="btn btn-default" data-dismiss="modal" >确认</button>
            	</div>
         	</div>
		</div>
	</div>
</div>
</#macro>
