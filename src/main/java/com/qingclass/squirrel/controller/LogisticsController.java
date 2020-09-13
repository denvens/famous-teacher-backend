package com.qingclass.squirrel.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
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
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.qingclass.squirrel.domain.cms.Certificate;
import com.qingclass.squirrel.domain.cms.LevelLogistics;
import com.qingclass.squirrel.domain.cms.Logistics;
import com.qingclass.squirrel.domain.cms.RequestInfo;
import com.qingclass.squirrel.domain.cms.SquirrelLevel;
import com.qingclass.squirrel.domain.cms.UserLogistics;
import com.qingclass.squirrel.entity.SessionSquirrelUser;
import com.qingclass.squirrel.mapper.cms.BigbayMapper;
import com.qingclass.squirrel.mapper.cms.SquirrelLevelMapper;
import com.qingclass.squirrel.service.CertificateService;
import com.qingclass.squirrel.service.LogisticsService;
import com.qingclass.squirrel.service.UserLogisticsService;
import com.qingclass.squirrel.utils.PropertiesLoader;
import com.qingclass.squirrel.utils.Tools;

@RestController
@RequestMapping("/logistics")
public class LogisticsController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	LogisticsService logisticsService;
	
	@Autowired
	UserLogisticsService userLogisticsService;
	
	@Autowired
	BigbayMapper bigbayMapper;
	
	@Autowired
	private HttpClient httpClient;
	
	@Autowired
	private SquirrelLevelMapper squirrelLevelMapper;
	
	private final static String logisticsUrl = "http://bigbay-logistics-flow.qingclasswelearn.com/express-order/query-express";
	
	@ResponseBody
	@RequestMapping(value = "/logistics-List", method = RequestMethod.POST)
	public String logisticsList(HttpServletRequest req){
		SessionSquirrelUser sessionUserInfo = (SessionSquirrelUser) req.getSession().getAttribute(SessionSquirrelUser.SESSION_SQUIRREL_USER_KEY);
		logger.info("sessionUserInfo:{}",sessionUserInfo);
		int id = sessionUserInfo.getId();
		logger.info("id:{}",id);
		List<SquirrelLevel> effectiveLevelList = squirrelLevelMapper.getEffectiveLevelList(id);
		logger.info("effectiveLevelList:{}",effectiveLevelList.size());
		
		ArrayList<LevelLogistics> levelLogisticsList = new ArrayList<LevelLogistics>();
		
		for(int i=0; i<effectiveLevelList.size(); i++){
			
			LevelLogistics levelLogistics = new LevelLogistics();
			
			SquirrelLevel squirrelLevel = effectiveLevelList.get(i);
			
	        String openId = sessionUserInfo.getOpenId();
	        logger.info("************************************************");
	        logger.info("openId:{},levelId:{}",openId,squirrelLevel.getId());
	        Logistics logistics = new Logistics();
	        logistics.setOpenId(openId);
	        logistics.setLevelId(squirrelLevel.getId());
			RequestInfo info = logisticsService.selectBy(logistics);
			List<Logistics> logisticsList = (List<Logistics>)info.getDataList();
			
			if(logisticsList!=null && !logisticsList.isEmpty()){
				logger.info("有物流信息************************************");
				ArrayList<Map<String, Object>> dataArray = null;
				Logistics l = logisticsList.get(0);
				//----------------------------------------------------------------------------------------------
				String responseBody = getLogisticsInfoByCode(l);
				Map<String, Object> responseJson = Tools.jsonToMap(responseBody);
				// 业务端应该按照和海湾约定的格式返回，否则视为响应回调失败，会按一定策略发起重试
				int code = (int) responseJson.get("code");
				if (code==200) {
					// 支付成功后业务处理
					dataArray = (ArrayList<Map<String, Object>>)responseJson.get("data");
					levelLogistics.setData(dataArray);
					//{code=200, data=[{message=未发货, data=null, code=471}], message=ok}
					logger.info("message:{}", dataArray.get(0).get("message"));
//					levelLogistics.setLogisticsInfo(dataArray.get(0).get("message").toString());
				}else{
					logger.info("物流非200返回************************************");
					levelLogistics.setData(null);
				}
				logger.info("物流信息:levelName:{},dataArray:{}",squirrelLevel.getName(),dataArray);
			}else{
				logger.info("有物流信息************************************");
				levelLogistics.setData(null);
			}
				
			levelLogistics.setLevelId(squirrelLevel.getId());
			levelLogistics.setLevelName(squirrelLevel.getName());
			
			levelLogisticsList.add(levelLogistics);
			
		}
		// "logisticsCode"
		JSONObject json = new JSONObject();
		json.put("denied",false);
		json.put("success",true);
		json.put("data",levelLogisticsList);
		return json.toJSONString();
	}
	
	@ResponseBody
	@RequestMapping(value = "/info", method = RequestMethod.POST)
	public String info(HttpServletRequest req,
			@RequestParam(name="levelId",required = false)Integer levelId){
		
		SessionSquirrelUser sessionUserInfo = (SessionSquirrelUser) req.getSession().getAttribute(SessionSquirrelUser.SESSION_SQUIRREL_USER_KEY);
        logger.info("sessionUserInfo:{}",sessionUserInfo);
        String openId = sessionUserInfo.getOpenId();
        logger.info("openId:{}",openId);
        Logistics logistics = new Logistics();
        logistics.setOpenId(openId);
        logistics.setLevelId(levelId);
		RequestInfo info = logisticsService.selectBy(logistics);
		List<Logistics> logisticsList = (List<Logistics>)info.getDataList();
		ArrayList<Map<String, Object>> dataArray = null;
		if(logisticsList!=null && !logisticsList.isEmpty()){
			Logistics l = logisticsList.get(0);
			//----------------------------------------------------------------------------------------------
			String responseBody = getLogisticsInfoByCode(l);
			
			Map<String, Object> responseJson = Tools.jsonToMap(responseBody);
			// 业务端应该按照和海湾约定的格式返回，否则视为响应回调失败，会按一定策略发起重试
			int code = (int) responseJson.get("code");
			if (code==200) {
				// 支付成功后业务处理
				dataArray = (ArrayList<Map<String, Object>>)responseJson.get("data");
				logger.info("message:{}", dataArray.get(0).get("message"));
			}
		}
		
		// "logisticsCode"
		JSONObject json = new JSONObject();
		json.put("denied",info.getDenied());
		json.put("success",info.getSuccess());
		json.put("data",dataArray);
		return json.toJSONString();
	}
	
	public String getLogisticsInfoByCode(Logistics l){
		String logisticsCode = l.getLogisticsCode();
		logger.info("开始物流查询:{},海湾物流单号:{}",	logisticsUrl,	logisticsCode);
		HttpPost postRequest = new HttpPost(logisticsUrl);
		postRequest.setHeader("Content-Type", "application/json;charset=utf-8");
		postRequest.setHeader("Accept", "application/json");
		
		JSONArray jsonArrayObj = new JSONArray();
		jsonArrayObj.add(logisticsCode);
		
		String bigbayAppId = PropertiesLoader.getProperty("bigbayAppId");
		String bigbaySignKey = bigbayMapper.selectKeyByAppId(bigbayAppId);
		
		prepareBigBayRequest(postRequest, jsonArrayObj.toString(), String.valueOf(bigbayAppId), l, bigbaySignKey);
		HttpResponse postResponse = null;
		String responseBody = null;
		try {
			postResponse = httpClient.execute(postRequest);
			responseBody = EntityUtils.toString(postResponse.getEntity(), "utf-8");
		} catch (Exception e) {
			logger.info("exception stackTrace:{}",e.getStackTrace());
		}
		logger.info("物流返回：response:{}", responseBody);
		return responseBody;
	}
	
	public void prepareBigBayRequest(HttpPost request, String content, String bigbayAppId, Logistics logisticsObj, String signKey) {
		String random = Tools.randomString32Chars();
		String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
		String signature = sign(bigbayAppId, content, random, timestamp, signKey);
		
		JSONObject jsonParam = new JSONObject();
		jsonParam.put("bigbayAppId", bigbayAppId);
	    jsonParam.put("content", content);
	    jsonParam.put("random", random);
	    jsonParam.put("timestamp", timestamp);
	    jsonParam.put("signature", signature);
	    StringEntity entity = new StringEntity(jsonParam.toString(),"utf-8");//解决中文乱码问题    
	    entity.setContentEncoding("UTF-8");    
	    entity.setContentType("application/json");    
	    request.setEntity(entity);
	}
	
	public String sign(String bigbayAppId, String content, String random, String timestamp, String key) {
		
		StringBuffer buffer = new StringBuffer()
				.append("bigbayAppId=").append(bigbayAppId).append("&")
				.append("content=").append(content).append("&")
				.append("random=").append(random).append("&")
				.append("timestamp=").append(timestamp).append("&")
				.append("key=").append(key);
		return Tools.md5(buffer.toString()).toUpperCase();
		
	}
	
	//---------------------------------------------------------------------------------------------------
	
	@ResponseBody
	@PostMapping(value = "/create")
	public String create(HttpServletRequest req, 
			@RequestParam(name="levelId",required = false)Integer levelId,
			@RequestParam(name="name",required = false)String name,
			@RequestParam(name="mobile",required = false)String mobile,
			@RequestParam(name="province",required = false)String province,
			@RequestParam(name="city",required = false)String city,
			@RequestParam(name="area",required = false)String area,
			@RequestParam(name="address",required = false)String address){

		SessionSquirrelUser sessionUserInfo = (SessionSquirrelUser) req.getSession().getAttribute(SessionSquirrelUser.SESSION_SQUIRREL_USER_KEY);
        logger.info("sessionUserInfo:{}",sessionUserInfo);
        String openId = sessionUserInfo.getOpenId();
        
        Logistics logistics = new Logistics();
        logistics.setOpenId(openId);
        logistics.setLevelId(levelId);
        logistics.setName(name);
        logistics.setMobile(mobile);
        logistics.setProvince(province);
        logistics.setCity(city);
        logistics.setArea(area);
        logistics.setAddress(address);
        logistics.setCreateTime(new Date());
        logistics.setUpdateTime(new Date());
		RequestInfo info = logisticsService.insert(logistics);
		
		JSONObject json = new JSONObject();
		json.put("denied",info.getDenied());
		json.put("success",info.getSuccess());
		json.put("data",logistics);
		return json.toJSONString();
	}
	
	@ResponseBody
	@RequestMapping(value = "/list", method = RequestMethod.POST)
	public String list(HttpServletRequest req){

//		SessionSquirrelUser sessionUserInfo = (SessionSquirrelUser) req.getSession().getAttribute(SessionSquirrelUser.SESSION_SQUIRREL_USER_KEY);
//        logger.info("sessionUserInfo:{}",sessionUserInfo);
//        String openId = sessionUserInfo.getOpenId();
//		
//		Certificate certificate = new Certificate();
//		certificate.setOpenId(openId);
//		RequestInfo info = certificateService.selectBy(certificate);
//
//		JSONObject jsonObject = new JSONObject();
//		JSONObject jsonObject1 = new JSONObject();
//
//		JSONArray objects = new JSONArray();
//		objects.addAll((List)info.getDataList());
//		jsonObject1.put("certificateList",objects);
//
//		jsonObject.put("denied",info.getDenied());
//		jsonObject.put("success",info.getSuccess());
//		jsonObject.put("data",jsonObject1);
//		return jsonObject.toJSONString();
		return null;
	}

	@ResponseBody
	@RequestMapping(value = "/edit", method = RequestMethod.POST)
	public String edit(HttpServletRequest req, 
			@RequestParam(name="id",required = false)Integer id,
			@RequestParam(name="classify",required = false)Integer classify,
			@RequestParam(name="number",required = false)String number){
//		SessionSquirrelUser sessionUserInfo = (SessionSquirrelUser) req.getSession().getAttribute(SessionSquirrelUser.SESSION_SQUIRREL_USER_KEY);
//        logger.info("sessionUserInfo:{}",sessionUserInfo);
//        String openId = sessionUserInfo.getOpenId();
//        
//		Certificate certificate = new Certificate();
//		certificate.setId(id);
//		certificate.setOpenId(openId);
//		certificate.setClassify(classify);
//		certificate.setNumber(number);
//		certificate.setUpdateDate(new Date());
//		RequestInfo info = certificateService.updateByPrimaryKey(certificate);
//
//		JSONObject json = new JSONObject();
//		json.put("denied",info.getDenied());
//		json.put("success",info.getSuccess());
//		json.put("data",info.getDataObject());
//		return json.toJSONString();
		return null;
	}

}