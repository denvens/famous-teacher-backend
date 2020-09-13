package com.qingclass.squirrel.service;

import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.qingclass.squirrel.domain.statistic.SquirrelKvalueStatistic;
import com.qingclass.squirrel.domain.statistic.UserAction;
import com.qingclass.squirrel.entity.MongoUser;
import com.qingclass.squirrel.entity.SquirrelUser;
import com.qingclass.squirrel.entity.SquirrelUserBehavior;
import com.qingclass.squirrel.mapper.statistic.UserActionMapper;
import com.qingclass.squirrel.mapper.user.SquirrelUserMapper;
import com.qingclass.squirrel.utils.Tools;

@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class, value = "squirrelTransaction")
public class SquirrelUserService {

	private Logger logger = LoggerFactory.getLogger(SquirrelUserService.class);

	public static final String LOGIN_USER = "user";

	@Value("${weixin.appid}")
	private String appId;

	@Value("${weixin.appsecret}")
	private String appSecret;

	@Autowired
	private HttpClient httpClient;
	@Autowired
	private MongoTemplate mongoTemplate;
	@Autowired
	private SquirrelUserMapper squirrelUsersMapper;
	@Autowired
	private UserActionMapper userActionMapper;
	@Autowired
	private WxService wxService;
	/**
	 * 获取用户信息
	 * @param code     
	 * @return Map<String, Object>
	 */
	public Map<String, Object> getUserInfo(String code) {

		String requestUrl = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + appId + "&secret=" + appSecret
				+ "&code=" + code + "&grant_type=authorization_code";
		HttpGet httpGet = new HttpGet(requestUrl);
		HttpResponse response = null;
		String responseBody = null;
		try {
			response = httpClient.execute(httpGet);
			responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		Map<String, Object> ret = Tools.jsonToMap(responseBody);
		requestUrl = "https://api.weixin.qq.com/sns/userinfo?access_token=ACCESS_TOKEN&openid=OPENID&lang=zh_CN";
		requestUrl = requestUrl.replace("ACCESS_TOKEN", (String) ret.get("access_token")).replace("OPENID",
				(String) ret.get("openid"));

		httpGet = new HttpGet(requestUrl);
		try {
			response = httpClient.execute(httpGet);
			responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		ret = Tools.jsonToMap(responseBody);
		logger.info("getUserInfo=====" + ret);
		return ret;
	}


	/**
	 * 获取静默登陆的用户openId
	 * @param code
	 * @return Map<String, Object>
	 */
	public Map<String, Object> getUserBase(String code) {

		String requestUrl = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + appId + "&secret=" + appSecret
				+ "&code=" + code + "&grant_type=authorization_code";
		HttpGet httpGet = new HttpGet(requestUrl);
		HttpResponse response = null;
		String responseBody = null;
		try {
			response = httpClient.execute(httpGet);
			responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}

		Map<String, Object> ret = Tools.jsonToMap(responseBody);
		return ret;
	}

	/**
	 *
	 * */
	public Map<String, Object> getUserInfoByOpenId(String openId) {

		String requestUrl = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + appId + "&secret=" + appSecret;
		HttpGet httpGet = new HttpGet(requestUrl);
		HttpResponse response = null;
		String responseBody = null;
		try {
			response = httpClient.execute(httpGet);
			responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		Map<String, Object> ret = Tools.jsonToMap(responseBody);
		requestUrl = "https://api.weixin.qq.com/cgi-bin/user/info?access_token=ACCESS_TOKEN&openid=OPENID&lang=zh_CN";
		requestUrl = requestUrl.replace("ACCESS_TOKEN", (String) ret.get("access_token")).replace("OPENID",
				openId);

		httpGet = new HttpGet(requestUrl);
		try {
			response = httpClient.execute(httpGet);
			responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
		ret = Tools.jsonToMap(responseBody);
		logger.info("getUserInfo=====" + ret);
		return ret;
	}


	/**
	 * 插入或更新用户信息
	 * @param ret
	 * @return
	 */
	public SquirrelUser dealUser(Map<String, Object> ret) {
		String openid = ret.get("openid") + "";
		SquirrelUser squirrelUser = squirrelUsersMapper.selectByOpenId(openid);
		boolean insert=false;
		if(squirrelUser==null) {
			squirrelUser = new SquirrelUser();
			insert=true;
		}
		squirrelUser.setHeadImgUrl(ret.get("headimgurl") + "");
		squirrelUser.setNickName(ret.get("nickname") + "");
		squirrelUser.setOpenId(ret.get("openid") + "");

		if(ret.get("sex") != null){
			squirrelUser.setSex(Integer.parseInt(ret.get("sex") + ""));
		}

		squirrelUser.setUnionId(ret.get("unionid") + "");
		squirrelUser.setSubscribe(1);
		if (insert) {
			squirrelUsersMapper.insert(squirrelUser);
			logger.info("dealUser==== insertuser id:" + squirrelUser.getId() + "====squirrelUser="
					+ JSON.toJSONString(squirrelUser));
			Query query = new Query(Criteria.where("_id").is(openid));
			MongoUser mongoUser = mongoTemplate.findOne(query, MongoUser.class);
			if (mongoUser == null) {
				logger.info("mongoTemplate.insert userid is " + squirrelUser.getId());
				mongoUser = new MongoUser();
				mongoUser.setId(openid);
				mongoUser.setUserId(squirrelUser.getId());
				mongoTemplate.insert(mongoUser);
			}
		} else {
			squirrelUsersMapper.update(squirrelUser);
			logger.info("dealUser==== update " + squirrelUser.getId() + "====squirrelUser="
					+ JSON.toJSONString(squirrelUser));
		}
		return squirrelUser;
	}

	@Async
	public void recordActionInfo(Map<String, Object> params) {
		UserAction userAction = JSON.parseObject(JSON.toJSONString(params), UserAction.class);
		Map<String, Object> map = userActionMapper.selectUserActionByCondition(params);
		if(map==null) {
			userActionMapper.insert(userAction);
			logger.info("recordActionInfo...success...");
		}
	}

	@Async
	public void recordSellPageActionInfo(Map<String, Object> params) {
		UserAction userAction = JSON.parseObject(JSON.toJSONString(params), UserAction.class);
		Map<String, Object> map = userActionMapper.selectUserActionByCondition(params);
		String flag = (String) params.get("flag");
		if(map==null) {
			logger.info("type::"+userAction.getType());
			//购买过并且试听
			if(SquirrelKvalueStatistic.BUYSUCCESS.equals(userAction.getType())) {
				params.put("type", SquirrelKvalueStatistic.CLICKAUDITION);
				map = userActionMapper.selectUserActionByCondition(params);
				if(map!=null) {
					userAction.setType(SquirrelKvalueStatistic.BUYANDAUDITION);
					userActionMapper.insert(userAction);
					userAction.setType(SquirrelKvalueStatistic.BUYSUCCESS);
				}
			}
			if((flag!=null && flag.equals("goshare"))||SquirrelKvalueStatistic.CLICKAUDITION.equals(userAction.getType())) {//分享打卡
				userActionMapper.insert(userAction);
			}else {
				//扫码关注并购买
				int count = squirrelUsersMapper.selectBehavior(params);
				if(count !=0) {
					userActionMapper.insert(userAction);
				}
				
				//进入购买页面，点击购买按钮，购买完成
				if(SquirrelKvalueStatistic.INIT.equals(userAction.getType())) {
					params.put("type", SquirrelKvalueStatistic.AUDITIONINIT);
					Map<String, Object> map3 = userActionMapper.selectUserActionByCondition(params);
					if(map3==null) {
						userAction.setType(SquirrelKvalueStatistic.AUDITIONINIT);
						userActionMapper.insert(userAction);
					}
					
				}
				if(SquirrelKvalueStatistic.BUY.equals(userAction.getType())) {
					params.put("type", SquirrelKvalueStatistic.AUDITIONCLICK);
					Map<String, Object> map3 = userActionMapper.selectUserActionByCondition(params);
					if(map3==null) {
						userAction.setType(SquirrelKvalueStatistic.AUDITIONCLICK);
						userActionMapper.insert(userAction);
					}
				}
				if(SquirrelKvalueStatistic.BUYSUCCESS.equals(userAction.getType())) {
					params.put("type", SquirrelKvalueStatistic.AUDITIONBUYSUCCESS);
					Map<String, Object> map3 = userActionMapper.selectUserActionByCondition(params);
					if(map3==null) {
						userAction.setType(SquirrelKvalueStatistic.AUDITIONBUYSUCCESS);
						userActionMapper.insert(userAction);
					}
					
				}
				
			}
			logger.info("recordActionInfo...success...");
		}
	}

}
