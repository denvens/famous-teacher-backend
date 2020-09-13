package com.qingclass.squirrel.service;


import com.qingclass.squirrel.domain.cms.SquirrelLessonRemind;
import com.qingclass.squirrel.domain.statistic.SquirrelBatchesStatistic;
import com.qingclass.squirrel.domain.wx.WxCustom;
import com.qingclass.squirrel.entity.ConversionPush;
import com.qingclass.squirrel.mapper.cms.WxChannelMapper;
import com.qingclass.squirrel.mapper.cms.WxPurchaseMapper;
import com.qingclass.squirrel.mapper.user.UserRemindMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.qingclass.squirrel.domain.PaymentTransactions;
import com.qingclass.squirrel.domain.cms.RequestInfo;
import com.qingclass.squirrel.entity.MongoUser;
import com.qingclass.squirrel.entity.SessionSquirrelUser;
import com.qingclass.squirrel.entity.SquirrelUser;
import com.qingclass.squirrel.mapper.user.PaymentTransactionMapper;
import com.qingclass.squirrel.mapper.user.SquirrelUserMapper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@Transactional(value = "squirrelTransaction", propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
public class UserService {
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	 
	@Autowired
    SquirrelUserMapper userMapper;
	@Autowired
	private MongoTemplate mongoTemplate;
	@Autowired
    private WxPurchaseMapper wxPurchaseMapper;
	@Autowired
    private UserRemindMapper userRemindMapper;

	@Autowired
    PaymentTransactionMapper paymentTransactionMapper;
	@Autowired
    SquirrelUserMapper squirrelUserMapper;

	public RequestInfo insertPayment(String content, String timestamp){
	    //user-level参数
        Integer levelId;
        String openId;
        String beginAt;

        String wechatTrannsactionId;
        String bigbayTranction;
        String merchantionId;
        String appId;
        String totalFee;
        String tradeType;
        String bankType;
        String feeType;
        String clientIp;
        String sellPageItemConfig;
        String timeEnd = timestamp;
        String vipBeginTime;
        String vipEndTime;


        //解析content
        JSONObject con = JSONObject.parseObject(content);

        //取levelId
        JSONObject sellPageItemConfigJson = JSONObject.parseObject(con.getString("sellPageItemConfig"));
        levelId = sellPageItemConfigJson.getInteger("levelId");
        beginAt = sellPageItemConfigJson.getString("beginAt");
        vipBeginTime = sellPageItemConfigJson.getString("vipBeginTime");
        vipEndTime = sellPageItemConfigJson.getString("vipEndTime");

        sellPageItemConfig = con.getString("sellPageItemConfig");

        //
        JSONObject orderInfo = JSONObject.parseObject(con.getString("orderInfo"));
        totalFee = orderInfo.getString("totalFee");
        clientIp = orderInfo.getString("clientIp");

        //取openId
        JSONObject wechatPaymentNotifyParams = JSONObject.parseObject(con.getString("wechatPaymentNotifyParams"));

        openId = wechatPaymentNotifyParams.getString("openid");
        wechatTrannsactionId = wechatPaymentNotifyParams.getString("transaction_id");
        bankType = wechatPaymentNotifyParams.getString("bank_type");
        feeType = wechatPaymentNotifyParams.getString("fee_type");
        merchantionId = wechatPaymentNotifyParams.getString("mch_id");
        bigbayTranction = wechatPaymentNotifyParams.getString("out_trade_no");
        tradeType = wechatPaymentNotifyParams.getString("trade_type");
        appId = wechatPaymentNotifyParams.getString("appid");

        PaymentTransactions paymentTransactions = new PaymentTransactions();
        paymentTransactions.setSellPageItemConfig(sellPageItemConfig);
        paymentTransactions.setTotalFee(totalFee);
        paymentTransactions.setClientIp(clientIp);
        paymentTransactions.setWechatTransactionId(wechatTrannsactionId);
        paymentTransactions.setBankType(bankType);
        paymentTransactions.setFeeType(feeType);
        paymentTransactions.setMerchantionId(merchantionId);
        paymentTransactions.setBigbayTranctionnId(bigbayTranction);
        paymentTransactions.setTradeType(tradeType);
        paymentTransactions.setAppId(appId);
        paymentTransactions.setTimeEnd(timeEnd);
        paymentTransactions.setOpenId(openId);



        RequestInfo info;
        info = new RequestInfo();
        paymentTransactionMapper.insert(paymentTransactions);
        logger.info("支付回调[insert paymentTransaction successful...]");

        //取userId
        SquirrelUser sessionSquirrelUser = userMapper.selectByOpenId(openId);
        int userId = sessionSquirrelUser.getId();

        List<SquirrelUser> squirrelUsers = userMapper.selectByLevelIdAndNowDateAfter(levelId, userId);

        if(squirrelUsers.size() >= 1){//如果该level会员没过期，则增加对应时常
            logger.info("支付回调[该level已购买...执行延期操作...openId="+openId+"]");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            SquirrelUser squirrelUser = squirrelUsers.get(0);
            String vet = squirrelUser.getVipEndTime();
            Date end = null;
            long from = 0;
            long to = 0;
            try {
                from = sdf.parse(vipBeginTime).getTime();
                to = sdf.parse(vipEndTime).getTime();
                end = sdf.parse(vet);
            } catch (ParseException e) {
                logger.error("date parse error.");
            }

            int days = (int) ((to - from)/(1000 * 60 * 60 * 24));


            Calendar cal = Calendar.getInstance();
            if(end != null){
                cal.setTime(end);
            }
            cal.add(Calendar.DATE, days);
            String format = sdf.format(cal.getTime());
            //更新
            userMapper.updateUserLevel(null,null,format,squirrelUser.getId());
            logger.info("支付回调[延期操作成功...]");
        }else{
            //查询是否通过渠道关注
            String code = userMapper.selectCodeInBehavior(openId);

            if(code == null){
                //插入
                userMapper.insertUserLevel(userId, levelId, paymentTransactions.getId(), beginAt, vipBeginTime, vipEndTime);
                logger.info("支付回调[成功插入课程权限...openId="+openId+"]");
                insertLessonRemind(userId,openId,levelId);
            }else{
                //插入
                userMapper.insertUserLevelIncludeCode(userId, levelId, paymentTransactions.getId(), beginAt, vipBeginTime, vipEndTime,code);
                logger.info("支付回调[成功插入课程权限...openId="+openId+"]");
                insertLessonRemind(userId,openId,levelId);
            }
        }

        return info;
    }

    //插入课程提醒通知
    public void insertLessonRemind(Integer userId,String openId,Integer levelId){
	    //查询默认提醒
        SquirrelLessonRemind remind = wxPurchaseMapper.selectRemindTimeDefault("default");
        if(remind == null || remind.getId() == null
                || remind.getRemindRate() == null || remind.getFirstRemind() == null
                || remind.getSecondRemind() == null){
            return;
        }

        userRemindMapper.insertRemindRecord(userId,openId,levelId,remind.getFirstRemind(),remind.getRemindRate(),1);
        userRemindMapper.insertRemindRecord(userId,openId,levelId,remind.getSecondRemind(),remind.getRemindRate(),1);

        //转化推送消息设置成已购买
        ConversionPush conversionPush = new ConversionPush();
        conversionPush.setOpenId(openId);
        conversionPush.setLevelId(levelId);
        conversionPush.setIsPurchase(1);
        squirrelUserMapper.updateConversionPushFlag(conversionPush);
    }

	/**
	 * 初始化用户
	 * @param content
	 */
	public void initUserInfo(String content) {
		//解析content
        JSONObject con = JSONObject.parseObject(content);
		
		 //取openId
        JSONObject wechatPaymentNotifyParams = JSONObject.parseObject(con.getString("wechatPaymentNotifyParams"));
        JSONObject userInfo = JSONObject.parseObject(con.getString("userInfo"));

        String openId = wechatPaymentNotifyParams.getString("openid");
        logger.info("initUserInfo get openId is ...===="+openId);
        //取userid
      	SquirrelUser squirrelUser = userMapper.selectByOpenId(openId);
      	if(squirrelUser==null) {
      		squirrelUser =new SquirrelUser();
      		squirrelUser.setHeadImgUrl(userInfo.getString("headimgurl"));
      		squirrelUser.setNickName(userInfo.getString("nickname"));
      		squirrelUser.setSex(userInfo.getInteger("sex"));
      		squirrelUser.setUnionId(userInfo.getString("unionid"));
      		squirrelUser.setOpenId(openId);
      		squirrelUser.setSubscribe(1);
      		userMapper.insert(squirrelUser);
      		logger.info("initUserInfo==userMapper.insert success....");
      	}
      	
		Query query = new Query(Criteria.where("_id").is(openId));
		MongoUser mongoUser = mongoTemplate.findOne(query, MongoUser.class);
		if(mongoUser==null) {
			//初始化用户
			mongoUser = new MongoUser();
			mongoUser.setId(openId);
			mongoUser.setUserId(squirrelUser.getId());
			mongoTemplate.insert(mongoUser);
			logger.info("initUserInfo==mongoTemplate.insert success....");
		}
	}

}
