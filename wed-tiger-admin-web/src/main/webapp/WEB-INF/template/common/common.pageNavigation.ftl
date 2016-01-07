<#macro pageNavigation pageModel=null>
	<#--
		TODO:第一页不需要传递参数pg，后台处理的时候pg默认为1
		输出分页导航 
			1.引用的页面需要能获取到  pageModel:PageModel 对象
				@see com.dianping.avatar.dao.PageModel
			2.分页导航中始终需要输出第一页和最后一页
		@param pageModel.pageCount : 总的页数
		@param pageModel.page ： 当前页
		@author mingxing.ma
		@create 2010-10-29
	-->
	<#if pageModel?exists && (pageModel.pageCount > 1)> 
		<#assign curPage = pageModel.page>
		<#assign pageCount = pageModel.pageCount>
		
		<#--
			开始页 : 求最大值(当前页和4的差值 , 1) 
		-->
		<#if ((curPage - 4) > 1)>
			<#assign startPage = (curPage - 4)>
		<#else>
			<#assign startPage = 1>
		</#if>
		
		<#--
			结束页 : 求最小值(开始页+8, 总页数)
		-->
		<#if ((startPage + 8) < pageCount)>
			<#assign endPage = (startPage + 8)>
		<#else>
			<#assign endPage = pageCount>
		</#if>

		<div class="Pages">
			<#--如果当前页大于第一页，输出上一页导航-->
			<#if (curPage > 1) >
				<a href="/admin/list?pageIndex=${curPage-1}" class="page-prev"><i class="p-prev"></i>上一页</a>
			</#if>
			
			<#--开始输出页码导航-->
			<#--
				如果开始页大于1 (表示当前页和4的差值大于1)
					先输出"第一页的link"和"..."
				否则跳过
					然后由遍历的过程输出第一页的链接
			-->
			<#if (startPage > 1)>
				<a href="/admin/list?pageIndex=1">1</a>
				<#if (startPage != 2)>
					<span class="ellipsis">...</span>
				</#if>
			</#if>
			
			<#--
				遍历输出开始页到结束页的链接
					如果是当前页，页码没有链接并且有自己的样式
			-->
			<#if (startPage <= endPage)>
				<#list startPage..endPage as page>
					<#if curPage == page>
						<span class="cur">${page}</span>
					<#else>
						<a href="/admin/list?pageIndex=${page}">${page}</a>
					</#if>
				</#list>
			</#if>
			
			<#--
				如果endPage < pageCount 	(表示结束页是startPage + 8，否则endPage = pageCount)
					1.先判断是否小于最后一页的前一页,如果是先输出"..."，否则跳过
					2.单独输出最后一页
				否则跳过
					实质上在上面的遍历的过程中已经输出了最后一页的链接
			-->
			<#if (endPage < pageCount)>
				<#if (endPage < pageCount - 1)>
					<span class="ellipsis">...</span>
				</#if>
				<a href="javascript:;">${pageCount}</a>
			</#if>
							
			<#--如果当前页小于总页数，输出下一页导航-->
			<#if (curPage < pageCount)>
				<a href="/admin/list?pageIndex=${curPage+1}" class="page-next">下一页<i class="p-next"></i></a>
			</#if>
			<#--结束输出页码导航-->
		</div>
	</#if>
</#macro>