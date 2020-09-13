package com.qingclass.squirrel.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qingclass.squirrel.domain.cms.SquirrelLevel;
import com.qingclass.squirrel.service.SquirrelLevelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.qingclass.squirrel.entity.SessionSquirrelUser;
import com.qingclass.squirrel.entity.SquirrelRequest;
import com.qingclass.squirrel.entity.SquirrelUser;
import com.qingclass.squirrel.mapper.user.SquirrelUserMapper;
import com.qingclass.squirrel.service.SquirrelUserService;
import com.qingclass.squirrel.utils.SignUtil;
import com.qingclass.squirrel.utils.Tools;

/**
 * 本类为松鼠C端的登陆类，包含了登录等一系列用户账户操作
 * 松鼠采用基于微信登录
 *
 * @author 苏天奇
 * */
@RestController
@RequestMapping("/auth")
public class AuthController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private SquirrelUserMapper squirrelUserMapper = null;
	@Autowired
	private StringRedisTemplate stringRedisTemplate;
	@Autowired
	private SquirrelUserService squirrelUserService = null;
	@Autowired
	private SquirrelLevelService squirrelLevelService = null;

	@PostMapping("/sign-in-with-open-id")
	public Map<String, Object> signInWithOpenId(SquirrelRequest squirrelRequest) {
		HttpServletRequest request = squirrelRequest.getRequest();
		Map<String, Object> params = squirrelRequest.getParams();
		String openId = (String) params.get("openId");
		SquirrelUser squirrelUser = squirrelUserMapper.selectByOpenId(openId);
		
		if (null == squirrelUser) {
			return Tools.f("user not found");
		}
		
		logger.info("user[openId=" + squirrelUser.getOpenId() + "] signed-in successfully!");
		
		return finishSignin(openId, request);
	}
	
	@RequestMapping("/is-signed-in")
	public Map<String, Object> isSignedIn(HttpServletRequest request) {
		HttpSession session = request.getSession();
		SessionSquirrelUser sessionSquirrelUser = (SessionSquirrelUser)session.getAttribute(SessionSquirrelUser.SESSION_SQUIRREL_USER_KEY);
		if (null == sessionSquirrelUser) {
			return Tools.f("you are not signed-in");
		}
		return finishSignin(sessionSquirrelUser.getOpenId(), request);
	}
	
	private Map<String, Object> finishSignin(String openId, HttpServletRequest request) {
		SquirrelUser squirrelUser = squirrelUserMapper.selectByOpenId(openId);
		SessionSquirrelUser sessionSquirrelUser = new SessionSquirrelUser();
		sessionSquirrelUser.setId(squirrelUser.getId());
		sessionSquirrelUser.setOpenId(squirrelUser.getOpenId());
		request.getSession().setAttribute(SessionSquirrelUser.SESSION_SQUIRREL_USER_KEY, sessionSquirrelUser);
		Map<String, Object> map = new HashMap<>();
		map.put("nickName", squirrelUser.getNickName());
		map.put("headImgUrl",squirrelUser.getHeadImgUrl());
		map.put("openId",openId);
		map.put("bgmStatus",squirrelUser.getBgmStatus());
		Map<Integer, String> integerStringMap = squirrelLevelService.skinList();
		map.put("skins", integerStringMap);

		return Tools.s(map); 
	}

	@RequestMapping("/sign-out")
	public Map<String, Object> signOut(HttpServletRequest reqeust) {
		HttpSession session = reqeust.getSession();
		session.removeAttribute(SessionSquirrelUser.SESSION_SQUIRREL_USER_KEY);
		return Tools.s("now you are signed out");
	}

	@RequestMapping("/sign-in-with-code")
	public Map<String, Object> signInWithCode(SquirrelRequest squirrelRequest) {
		Map<String, Object> params = squirrelRequest.getParams();
		HttpServletRequest request = squirrelRequest.getRequest();
		SquirrelUser squirrelUser = null;
		try {
			String code = (String) params.get("code");
			Map<String, Object> userInfoMap = squirrelUserService.getUserInfo(code);
			if(userInfoMap!=null) {
				squirrelUser = squirrelUserService.dealUser(userInfoMap);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return Tools.f();
		}
		return finishSignin(squirrelUser.getOpenId(), request);
	}

	@RequestMapping("/quiet-sign-in-with-code")
	public Map<String, Object> quietSignInWithCode(SquirrelRequest squirrelRequest) {
		Map<String, Object> params = squirrelRequest.getParams();
		try {
			String code = (String) params.get("code");
			Map<String, Object> userInfoMap = squirrelUserService.getUserBase(code);
			return Tools.s(userInfoMap.get("openid"));
		} catch (Exception e) {
			e.printStackTrace();
			return Tools.f();
		}
	}

	/**
	 * 初始化js-sdk
	 * 
	 * @param
	 * @return
	 */
	@PostMapping("/js-sdk-signature")
	public Map<String, Object> generateJsSdkSignature(@RequestParam("noncestr") String noncestr,
			@RequestParam("timestamp") String timestamp, @RequestParam("url") String url) {

		int index = url.indexOf("#");
		if (index >= 0) {
			url = url.substring(0, index);
		}

		String jsapiTicket = stringRedisTemplate.opsForValue().get("msyb_jsapi_ticket");

		String signature = SignUtil.sign(jsapiTicket, url, noncestr, timestamp);

		Map<String, String> result = new HashMap<>();
		result.put("signature", signature);
		return Tools.s(result);
	}

}
