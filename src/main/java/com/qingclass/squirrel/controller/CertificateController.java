package com.qingclass.squirrel.controller;

import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qingclass.squirrel.domain.cms.Certificate;
import com.qingclass.squirrel.domain.cms.RequestInfo;
import com.qingclass.squirrel.entity.SessionSquirrelUser;
import com.qingclass.squirrel.service.CertificateService;

@RestController
@RequestMapping("/certificate")
public class CertificateController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	CertificateService certificateService;

	@ResponseBody
	@PostMapping(value = "/create")
	public String create(HttpServletRequest req, 
			@RequestParam(name="classify",required = false)Integer classify,
			@RequestParam(name="number",required = false)String number){

		SessionSquirrelUser sessionUserInfo = (SessionSquirrelUser) req.getSession().getAttribute(SessionSquirrelUser.SESSION_SQUIRREL_USER_KEY);
        logger.info("sessionUserInfo:{}",sessionUserInfo);
        String openId = sessionUserInfo.getOpenId();
        
		Certificate certificate = new Certificate();
		certificate.setOpenId(openId);
		certificate.setClassify(classify);
		certificate.setNumber(number);
		certificate.setCreateDate(new Date());
		certificate.setUpdateDate(new Date());
		RequestInfo info = certificateService.insert(certificate);

		JSONObject json = new JSONObject();
		json.put("denied",info.getDenied());
		json.put("success",info.getSuccess());
		json.put("data",certificate);
		return json.toJSONString();
	}
	
	@ResponseBody
	@RequestMapping(value = "/list", method = RequestMethod.POST)
	public String list(HttpServletRequest req){

		SessionSquirrelUser sessionUserInfo = (SessionSquirrelUser) req.getSession().getAttribute(SessionSquirrelUser.SESSION_SQUIRREL_USER_KEY);
        logger.info("sessionUserInfo:{}",sessionUserInfo);
        String openId = sessionUserInfo.getOpenId();
		
		Certificate certificate = new Certificate();
		certificate.setOpenId(openId);
		RequestInfo info = certificateService.selectBy(certificate);

		JSONObject jsonObject = new JSONObject();
		JSONObject jsonObject1 = new JSONObject();

		JSONArray objects = new JSONArray();
		objects.addAll((List)info.getDataList());
		jsonObject1.put("certificateList",objects);

		jsonObject.put("denied",info.getDenied());
		jsonObject.put("success",info.getSuccess());
		jsonObject.put("data",jsonObject1);
		return jsonObject.toJSONString();
	}

	@ResponseBody
	@RequestMapping(value = "/edit", method = RequestMethod.POST)
	public String edit(HttpServletRequest req, 
			@RequestParam(name="id",required = false)Integer id,
			@RequestParam(name="classify",required = false)Integer classify,
			@RequestParam(name="number",required = false)String number){
		SessionSquirrelUser sessionUserInfo = (SessionSquirrelUser) req.getSession().getAttribute(SessionSquirrelUser.SESSION_SQUIRREL_USER_KEY);
        logger.info("sessionUserInfo:{}",sessionUserInfo);
        String openId = sessionUserInfo.getOpenId();
        
		Certificate certificate = new Certificate();
		certificate.setId(id);
		certificate.setOpenId(openId);
		certificate.setClassify(classify);
		certificate.setNumber(number);
		certificate.setUpdateDate(new Date());
		RequestInfo info = certificateService.updateByPrimaryKey(certificate);

		JSONObject json = new JSONObject();
		json.put("denied",info.getDenied());
		json.put("success",info.getSuccess());
		json.put("data",info.getDataObject());
		return json.toJSONString();
	}

}