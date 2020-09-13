package com.qingclass.squirrel.controller;

import com.alibaba.fastjson.JSONObject;
import com.qingclass.squirrel.constant.InvitationTypeEnum;
import com.qingclass.squirrel.constant.RefundStatusEnum;
import com.qingclass.squirrel.domain.PaymentTransactions;
import com.qingclass.squirrel.domain.cms.Logistics;
import com.qingclass.squirrel.domain.cms.RequestInfo;
import com.qingclass.squirrel.domain.cms.SquirrelInvitationSetting;
import com.qingclass.squirrel.domain.cms.SquirrelLevel;
import com.qingclass.squirrel.domain.cms.UserLevel;
import com.qingclass.squirrel.entity.EntPayOrder;
import com.qingclass.squirrel.entity.InvitationRecord;
import com.qingclass.squirrel.entity.SquirrelUser;
import com.qingclass.squirrel.entity.Voucher;
import com.qingclass.squirrel.mapper.cms.BigbayMapper;
import com.qingclass.squirrel.mapper.cms.InvitationSettingMapper;
import com.qingclass.squirrel.mapper.cms.SquirrelLevelMapper;
import com.qingclass.squirrel.mapper.user.EntPayOrderMapper;
import com.qingclass.squirrel.mapper.user.PatchVoucherMapper;
import com.qingclass.squirrel.mapper.user.PaymentTransactionMapper;
import com.qingclass.squirrel.mapper.user.SquirrelUserMapper;
import com.qingclass.squirrel.service.LogisticsService;
import com.qingclass.squirrel.service.SmsService;
import com.qingclass.squirrel.service.UserService;
import com.qingclass.squirrel.service.WechatEntPayService;
import com.qingclass.squirrel.service.WxService;
import com.qingclass.squirrel.service.model.request.EntPayCreateOrderRequest;
import com.qingclass.squirrel.utils.MD5Util;
import com.qingclass.squirrel.utils.Tools;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import com.qingclass.squirrel.mapper.cms.PurchaseMapper;
/**
 * ****重要类
 * 本类是海湾的回调类，主要包括支付回调以及退款回调
 *
 * @author 苏天奇
 * */
@RestController
@RequestMapping("/bigbay-payment")
public class BigbayPaymentNotifyController {
	@Autowired
	UserService userService;
	@Autowired
	private WechatEntPayService wechatEntPayService;
	@Autowired
	BigbayMapper bigbayMapper;
	@Autowired
	WxService wxService;
	@Autowired
	SquirrelUserMapper squirrelUserMapper;
	@Autowired
	PatchVoucherMapper patchVoucherMapper;
	@Autowired
	PaymentTransactionMapper paymentTransactionMapper;
	@Autowired
	PurchaseMapper purchaseMapper;
	@Autowired
	MongoTemplate mongoTemplate;
	@Autowired
	private SquirrelLevelMapper squirrelLevelMapper;
	@Autowired
	private InvitationSettingMapper invitationSettingMapper;
	@Autowired
	private EntPayOrderMapper entPayOrderMapper;

	@Autowired
	LogisticsService logisticsService;
	@Autowired
	SmsService smsService;
	
	@Autowired
	private HttpClient httpClient;
	@Value("${logisticsUrl}")
    private String logisticsUrl;
	
	private final static String payTemplateCode = "SMS_178770437";
	
	Logger logger = LoggerFactory.getLogger(this.getClass());


	/**
	 * 支付回调类
	 * 用户在海湾购买页支付后，海湾平台会通知到此接口，根据一定规则（参考海湾支付接口文档）校验
	 * 校验通过后加入以下逻辑
	 * 1 初始化用户信息到本库
	 * 2 插入购买课程的权限
	 * 3 购买通知推送（公众号推送消息）
	 * 4 判断是否是通过邀请购买
	 * */
	@PostMapping("/notify")
	public String bigbayPaymentNotify(@RequestParam(value = "bigbayAppId",required = false)String bigbayAppId,
									  @RequestParam(value = "content",required = false)String content,
									  @RequestParam(value = "random",required = false)String random,
									  @RequestParam(value = "timestamp",required = false)String timestamp,
									  @RequestParam(value = "signature",required = false)String signature) {

		logger.info("bigbayAppId:{},content:{},random:{},timestamp:{},signature:{}",
				bigbayAppId,content,random,timestamp,signature);
		
		//获取SignKey
		String bigbaySignKey = bigbayMapper.selectKeyByAppId(bigbayAppId);

		//签名校验
		StringBuffer sb = new StringBuffer();
		sb.append("bigbayAppId="+bigbayAppId).
				append("&").
				append("content="+content).
				append("&").
				append("random="+random).
				append("&").
				append("timestamp="+timestamp).
				append("&").
				append("key="+bigbaySignKey);


		String crypt = MD5Util.crypt(sb.toString());
		crypt = crypt.toUpperCase();
		JSONObject jsonObject = new JSONObject();
		if(crypt.equals(signature)){
			logger.info("支付回调[签名校验成功...]");
		}else{
			System.out.println("支付回调[签名校验失败...]");
			jsonObject.put("success",false);
			return jsonObject.toJSONString();
		}

		//初始化用户信息
		userService.initUserInfo(content);

		RequestInfo info = userService.insertPayment(content,timestamp);

		//解析content
		JSONObject con = JSONObject.parseObject(content);
		JSONObject wechatPaymentNotifyParams = JSONObject.parseObject(con.getString("wechatPaymentNotifyParams"));
		String openId = wechatPaymentNotifyParams.getString("openid");
		JSONObject sellPageItemConfigJson = JSONObject.parseObject(con.getString("sellPageItemConfig"));
		Integer levelId = sellPageItemConfigJson.getInteger("levelId");
		String beginAt = sellPageItemConfigJson.getString("beginAt");
		
		SquirrelLevel squirrelLevel = squirrelLevelMapper.selectByPrimaryKey(levelId);
        SquirrelUser squirrelUser = squirrelUserMapper.selectByOpenId(openId);
		
		wxService.purchaseNotice(openId, squirrelLevel, squirrelUser, beginAt);//消息推送
		
		//物流信息**************************************************************************************
		JSONObject userSelections = JSONObject.parseObject(con.getString("userSelections"));
		JSONObject logistics = userSelections.getJSONObject("logistics");
		if(logistics!=null){
			logger.info("logistics:{}",userSelections.getJSONObject("logistics").toString());
			
			Logistics logisticsObj = new Logistics();
			logisticsObj.setOpenId(openId);
			logisticsObj.setLevelId(levelId);
			logisticsObj.setName(logistics.getString("name"));
			logisticsObj.setMobile(logistics.getString("mobile"));
			logisticsObj.setProvince(logistics.getString("province"));
			logisticsObj.setCity(logistics.getString("city"));
			logisticsObj.setArea(logistics.getString("area"));
			logisticsObj.setAddress(logistics.getString("address"));
			logisticsObj.setCreateTime(new Date());
			logisticsObj.setUpdateTime(new Date());
			RequestInfo logisticsInfo = logisticsService.insert(logisticsObj);
			
			String bigbayTranctionId = wechatPaymentNotifyParams.getString("out_trade_no");
			PaymentTransactions paymentTransactions = paymentTransactionMapper.selectTrans(bigbayTranctionId);
			//-------------------------------------------------------------------------------------------------------------------
			String sceneKey = sellPageItemConfigJson.getString("sceneKey");//cs:T514 	ol:L571
			logger.info("物流模板sceneKey:{}",sceneKey);
			String responseBody = logisticsSubmit(logisticsObj, bigbayAppId, paymentTransactions, bigbaySignKey, sceneKey);
			
			logger.info("物流返回：response:{}", responseBody);
			Map<String, Object> responseJson = Tools.jsonToMap(responseBody);
			// 业务端应该按照和海湾约定的格式返回，否则视为响应回调失败，会按一定策略发起重试
			int code = (int) responseJson.get("code");
			if (code==200) {
				// 支付成功后业务处理
				ArrayList<String> dataList = (ArrayList<String>)responseJson.get("data");
				
				Logistics l = (Logistics)logisticsInfo.getDataObject();
				l.setLogisticsCode(dataList.get(0));
				l.setTransactionId(paymentTransactions.getId());
				logger.info("logistics.Id:{},logisticsCode:{}", l.getId(), dataList.get(0));
				logisticsService.updateByPrimaryKey(l);
			}
			
			//发送短信*****************************************************************************************
			logger.info("手机号:{}", logistics.getString("mobile"));
			if(StringUtils.isNotBlank(logistics.getString("mobile"))){
				smsService.SendSms(logistics.getString("mobile"), payTemplateCode, squirrelLevel.getName(), beginAt);
			}
		}
		
		jsonObject.put("success",info.getSuccess());
		return jsonObject.toJSONString();
	}
	
	public String logisticsSubmit(Logistics logisticsObj, String bigbayAppId, PaymentTransactions paymentTransactions, String bigbaySignKey, String sceneKey){
		logger.info("开始物流,物流地址:{},sceneKey:{}",logisticsUrl,sceneKey);
		HttpPost postRequest = new HttpPost(logisticsUrl);
		postRequest.setHeader("Content-Type", "application/json;charset=utf-8");
		postRequest.setHeader("Accept", "application/json");
		
		JSONObject jsonParamObj = new JSONObject();
		jsonParamObj.put("phone", logisticsObj.getMobile());
		jsonParamObj.put("receiverName", logisticsObj.getName());
		jsonParamObj.put("province", logisticsObj.getProvince());
		jsonParamObj.put("city", logisticsObj.getCity());
		jsonParamObj.put("county", logisticsObj.getArea());
		jsonParamObj.put("detailAddr", logisticsObj.getAddress());
		jsonParamObj.put("bigbayAppId", bigbayAppId);
		jsonParamObj.put("orderId", paymentTransactions.getId());
		jsonParamObj.put("orderName", "名师优播课程");
		jsonParamObj.put("sceneKey", sceneKey);
		jsonParamObj.put("planDeliveryTime", "2019-10-01 14:00:00");
		
		prepareBigBayRequest(postRequest, jsonParamObj.toString(), String.valueOf(bigbayAppId), logisticsObj, bigbaySignKey);
		HttpResponse postResponse = null;
		String responseBody = null;
		try {
			postResponse = httpClient.execute(postRequest);
			responseBody = EntityUtils.toString(postResponse.getEntity(), "utf-8");
		} catch (Exception e) {
			logger.info("StackTrace:{}",e.getStackTrace());
		}
		return responseBody;
	}
	
	public static String sign(String bigbayAppId, String content, String random, String timestamp, String key) {
		
		StringBuffer buffer = new StringBuffer()
				.append("bigbayAppId=").append(bigbayAppId).append("&")
				.append("content=").append(content).append("&")
				.append("random=").append(random).append("&")
				.append("timestamp=").append(timestamp).append("&")
				.append("key=").append(key);
		return Tools.md5(buffer.toString()).toUpperCase();
		
	}

	public static void prepareBigBayRequest(HttpPost request, String content, String bigbayAppId, Logistics logisticsObj, String signKey) {
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

	/**
	 * 不可重复购买校验，本接口提供给海湾购买页，通知海湾该用户是否有购买课程的权限
	 * */
	@PostMapping("/refuse-repeat-purchase")
	public Map<String,Object> refuseRepeatPurchase(
			@Param(value = "levelId")Integer levelId, 
			@Param(value = "openId")String openId){

		if(openId == null){
			return Tools.f();
		}

		if(levelId == null){
			logger.error("购买权限校验[levelId is null...]");
			return Tools.f();
		}

		List<SquirrelUser> squirrelUsers = squirrelUserMapper.selectPurchaseRecordByOpenIdAndLevelId(openId, levelId);

		if(squirrelUsers.size() == 0){
			return Tools.s();
		}else{
			return Tools.f();
		}
	}


	/**
	 * 邀请购买记录
	 * */
	public Map<String,Object> invitationPurchaseSuccessRecord(
			@Param(value = "levelId")Integer levelId, 
			@Param(value = "invitationUserId")Integer invitationUserId,
			@Param(value = "purchaseOpenId")String purchaseOpenId,
			@Param(value = "invitationType")Integer invitationType){
		
		//插入邀请记录
		InvitationRecord invitationRecord = new InvitationRecord();
		invitationRecord.setLevelId(levelId);
		invitationRecord.setInvitationType(invitationType);

		//解密
		invitationUserId = (invitationUserId+9)/9;
		invitationRecord.setInvitationUserId(invitationUserId);

		SquirrelUser squirrelUser = squirrelUserMapper.selectByOpenId(purchaseOpenId);
		invitationRecord.setPurchaseUserId(squirrelUser.getId());

		List<UserLevel> userLevels = squirrelLevelMapper.getUserLevelsByLevelIdAndUserId(levelId, invitationUserId);
    	if(userLevels.isEmpty()){
    		logger.info("没有购买记录,不产生邀请记录！");
    	}else{
    		patchVoucherMapper.insertInvitationRecord(invitationRecord);
    	}
		
		logger.info("邀请：invitationType：{}",invitationType);
		if(InvitationTypeEnum.InvitationCoupon.getKey().intValue()==invitationType.intValue()){
			logger.info("邀请：invitationType：券{}",invitationType);
			//插入补卡劵
			Voucher voucher = new Voucher();
			voucher.setSquirrelUserId(invitationUserId);
			voucher.setLevelId(levelId);
			voucher.setIsOpen(1);
			voucher.setStatus(0);
			patchVoucherMapper.insertVoucher(voucher);
		}else if(InvitationTypeEnum.InvitationCash.getKey().intValue()==invitationType.intValue()){
			logger.info("邀请：invitationType：现金{}",invitationType);
			SquirrelUser invitationUser = squirrelUserMapper.selectById(invitationUserId);
			
			EntPayCreateOrderRequest orderRequest = new EntPayCreateOrderRequest();
			SquirrelInvitationSetting squirrelInvitationSetting = invitationSettingMapper.selectByLevelId(invitationRecord);
			BigDecimal bonusAmount=squirrelInvitationSetting.getBonusAmount().multiply(new BigDecimal(100));//乘以100(单位：分)
			orderRequest.setAmount(bonusAmount.intValue());
			// my o5SwA1DleGzjahrWkTA0JFinHqgw   gq  o5SwA1NGyHeS9WqaymsnlPGj5WB8
			orderRequest.setOpenid(invitationUser.getOpenId());//invitationUser.getOpenId()
			orderRequest.setAppId("wx1afbcff2bdd165c3");
			orderRequest.setDescription("松鼠绘本馆-邀请送现金奖励入账");
			
			try {
				String localIp = InetAddress.getLocalHost().getHostAddress();
				orderRequest.setSpbillCreateIp(localIp);
			} catch (UnknownHostException e) {
				logger.info("邀请送现金IP地址解析错误");
			}
			
			orderRequest.setInvitationOpenId(invitationUser.getOpenId());
			orderRequest.setPurchaseOpenId(purchaseOpenId);
			orderRequest.setLevelId(levelId);
			orderRequest.setInvitationUserId(invitationUser.getId());
			wechatEntPayService.createOrder(orderRequest);
		}
		

		//推送消息
		wxService.invitationSuccessNotice(invitationRecord);
		return Tools.s();
	}

	@PostMapping("/invitation-success-record")
	public Map<String,Object> invitationSucceessRecord(@Param(value = "levelId")Integer levelId, @Param(value = "invitationUserId")Integer invitationUserId,
													   @Param(value = "purchaseOpenId")String purchaseOpenId){

		return Tools.f("该接口已弃用");
	}

	/**
	 * 海湾退款回调（未完成）
	 *
	 * */
	@PostMapping("/refund")
	public String refund(@RequestParam(value = "bigbayAppId",required = false)String bigbayAppId,
									 @RequestParam(value = "content",required = false)String content,
									 @RequestParam(value = "random",required = false)String random,
									 @RequestParam(value = "timestamp",required = false)String timestamp,
									 @RequestParam(value = "signature",required = false)String signature){

		
		//获取SignKey
		String bigbaySignKey = bigbayMapper.selectKeyByAppId(bigbayAppId);

		//签名校验
		StringBuffer sb = new StringBuffer();
		sb.append("bigbayAppId="+bigbayAppId).
				append("&").
				append("content="+content).
				append("&").
				append("random="+random).
				append("&").
				append("timestamp="+timestamp).
				append("&").
				append("key="+bigbaySignKey);


		String crypt = MD5Util.crypt(sb.toString());
		crypt = crypt.toUpperCase();
		JSONObject jsonObject = new JSONObject();
		if(crypt.equals(signature)){
			logger.info("退款回调[签名校验成功...]");
		}else{
			System.out.println("支付回调[签名校验失败...]");
			jsonObject.put("success",false);
			return jsonObject.toJSONString();
		}

		//解析content
		JSONObject con = JSONObject.parseObject(content);

		String refundFee = con.getString("refundFee");//退款金额
		String totalFee = con.getString("totalFee");//订单金额
		String refundReason = con.getString("refundReason");//退款原因
		String outTradeNo = con.getString("outTradeNo");//商户单号
		String refundMode = con.getString("refundMode");//退款形式，1代表"仅退款"，2代表"退款且退课"
		String wechatTransactionId = con.getString("wechatTransactionId");//微信交易单号


		if(refundMode.equals("1")){
			purchaseMapper.updatePurchaseRecordStatus(wechatTransactionId, RefundStatusEnum.Refund.getKey());
			paymentTransactionMapper.insertRefund(refundFee,totalFee,refundReason,outTradeNo,refundMode,wechatTransactionId);
			return "{\"success\":true}";
		}

		logger.info("outTradeNo:{}",outTradeNo);
		//查询订单表的id
		PaymentTransactions paymentTransactions = paymentTransactionMapper.selectTrans(outTradeNo);
		logger.info("paymentTransactions-1:{}",paymentTransactions);
		logger.info("paymentTransactionId-2:{}",paymentTransactions.getId());
		//根据订单id删除对应购买记录和学习记录
		SquirrelLevel squirrelLevel = squirrelUserMapper.selectByTransactionId(paymentTransactions.getId());
		logger.info("squirrelLevel:{}",squirrelLevel);
		Integer levelId = squirrelLevel.getLevelId();
		logger.info("levelId:{}",levelId);
		Integer squirrelUserId = squirrelLevel.getSquirrelUserId();


		squirrelUserMapper.deleteUserLevel(paymentTransactions.getId());

		try{
			Query query = Query.query(Criteria.where("userId").is(squirrelUserId));
			Update update = new Update();
			update.unset("learnHistory.subjects.subject-1000000.levels.level-"+levelId);
			mongoTemplate.updateFirst(query,update,"users");
		}catch (Exception e){
			return "{\"success\":false}";
		}

		//插入退款记录
		purchaseMapper.updatePurchaseRecordStatus(wechatTransactionId, RefundStatusEnum.Refund.getKey());
		paymentTransactionMapper.insertRefund(refundFee,totalFee,refundReason,outTradeNo,refundMode,wechatTransactionId);
		
		return "{\"success\":true}";
	}
	
}