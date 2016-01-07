package com.dianping.wed.tiger.admin.controller;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.dianping.core.type.PageModel;
import com.dianping.wed.platform.common.result.WpsResult;
import com.dianping.wed.platform.groovy.api.WedHandlerService;
import com.dianping.wed.platform.groovy.dto.HandlerCodeDTO;
import com.dianping.wed.tiger.core.result.ReturnT;

/**
 * handler管理
 * @author yinxiaoran
 *
 */

@Controller
@RequestMapping("")
public class AdminController {
	
	@Resource
	private WedHandlerService wedHandlerService;
	
	private final int PAGE_SIZE = 10;
	
	@RequestMapping("")
	public String index(){
		return "forward:/list";
	}
	
	@RequestMapping("/list")
	public String list(
			Model model,
			Integer pageIndex
			) {
		if(pageIndex == null){
			pageIndex = 1;
		}
		PageModel pageModel = wedHandlerService.paginateFindWedHandler(pageIndex, PAGE_SIZE);
		if(pageModel != null && !CollectionUtils.isEmpty(pageModel.getRecords())){
			model.addAttribute("handlerList", (List<HandlerCodeDTO>)pageModel.getRecords());
			model.addAttribute("page", pageIndex);
			model.addAttribute("pageModel", pageModel);
		}
		return "list";
	}
	
	@RequestMapping("/modify")
	public String modify(Model model,String handlerName){
		HandlerCodeDTO handlerCodeDto = wedHandlerService.loadHandlerCodeByHandlerName(handlerName);
		if(handlerCodeDto == null){
			return "500";
		}
		model.addAttribute("handlerCode", handlerCodeDto);
		return "modify";
	}
	
	@RequestMapping("/add")
	public String add(){
		return "add";
	}
	
	@RequestMapping("/save")
	@ResponseBody
	public ReturnT<String> save(HandlerCodeDTO handlerCodeDto,String actionType){
		if(StringUtils.equalsIgnoreCase(actionType, "add")){
			WpsResult<Integer> result = wedHandlerService.addHandlerCode(handlerCodeDto);
			if(result.isSuccess()){
				return ReturnT.SUCCESS;
			}else{
				return new ReturnT<String>(500,result.getErrorMsg());
			}
		}else if(StringUtils.equalsIgnoreCase(actionType, "update")){
			wedHandlerService.updateHanderCode(handlerCodeDto);
			return ReturnT.SUCCESS;
		}
		
		return ReturnT.FAIL;
	}
		
	@RequestMapping("/delete")
	@ResponseBody
	public ReturnT<String> delete(Model model,String handlerName){
		int i = wedHandlerService.deleteHandlerCodeByHandlerName(handlerName);
		if(i > 0){
			return ReturnT.SUCCESS;
		}else{
			return ReturnT.FAIL;
		}
	}
	
}
