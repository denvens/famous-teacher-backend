package com.qingclass.squirrel.service;

import com.github.binarywang.wxpay.bean.entpay.EntPayRequest;
import com.github.binarywang.wxpay.bean.entpay.EntPayResult;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.service.impl.BaseWxPayServiceImpl;
import com.github.binarywang.wxpay.service.impl.WxPayServiceApacheHttpImpl;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;
import com.github.binarywang.wxpay.service.impl.WxPayServiceJoddHttpImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import com.qingclass.mango.pay.service.common.Result;
//import com.qingclass.mango.pay.service.domain.EntPayOrder;
//import com.qingclass.mango.pay.service.model.request.EntPayCreateOrderRequest;
//import com.qingclass.mango.pay.service.model.response.EntPayCreateOrderResponse;
//import com.qingclass.mango.pay.service.repository.EntPayOrderRepository;
//import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.github.binarywang.wxpay.service.WxPayService;
import com.qingclass.squirrel.constant.InvitationTypeEnum;
import com.qingclass.squirrel.domain.cms.UserLevel;
import com.qingclass.squirrel.entity.EntPayOrder;
import com.qingclass.squirrel.entity.InvitationRecord;
import com.qingclass.squirrel.mapper.cms.PurchaseMapper;
import com.qingclass.squirrel.mapper.cms.SquirrelLevelMapper;
import com.qingclass.squirrel.mapper.cms.SquirrelUnitMapper;
import com.qingclass.squirrel.mapper.user.EntPayOrderMapper;
import com.qingclass.squirrel.service.model.request.EntPayCreateOrderRequest;
//@Slf4j
@Service
public class WechatEntPayService {
	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
    private SquirrelLevelMapper squirrelLevelMapper; 
    
    private WxPayService wxService;//new WxPayServiceImpl()

    @Autowired
    private EntPayOrderMapper entPayOrderMapper;
    
    @Autowired
    private PurchaseMapper purchaseMapper;
    
    @Autowired
    public WechatEntPayService(com.github.binarywang.wxpay.service.WxPayService wxService) {
        this.wxService = wxService;
    }

    @Autowired
	private static  Environment environment;
	
    public Object createOrder(EntPayCreateOrderRequest entPayorderRequest){
        String orderId = UUID.randomUUID().toString();
        String partnerTradeNo = orderId.replaceAll("-", "");

        EntPayRequest entPayRequest = new EntPayRequest();
        entPayRequest.setDeviceInfo("WEB");
        entPayRequest.setAmount(entPayorderRequest.getAmount());
        entPayRequest.setPartnerTradeNo(partnerTradeNo);
        entPayRequest.setOpenid(entPayorderRequest.getOpenid());
        entPayRequest.setDescription(entPayorderRequest.getDescription());
        entPayRequest.setSpbillCreateIp(entPayorderRequest.getSpbillCreateIp());
        entPayRequest.setAppid(entPayorderRequest.getAppId());
        entPayRequest.setCheckName("NO_CHECK");

//        EntPayCreateOrderResponse entPayCreateOrderResponse = new EntPayCreateOrderResponse();
//        entPayCreateOrderResponse.setOrderId(orderId);

        try {
//        	EntPayOrder queryEntPayOrder = new EntPayOrder();
//        	queryEntPayOrder.setInvitationOpenId(entPayorderRequest.getInvitationOpenId());
//        	queryEntPayOrder.setPurchaseOpenId(entPayorderRequest.getPurchaseOpenId());
//        	queryEntPayOrder.setLevelId(entPayorderRequest.getLevelId());
//        	queryEntPayOrder.setStatus("SUCCESS");
//        	List<EntPayOrder> successList = entPayOrderMapper.selectEntPayOrderd(queryEntPayOrder);
        	
        	
        	EntPayOrder invitationOrder = new EntPayOrder();
        	invitationOrder.setInvitationOpenId(entPayorderRequest.getInvitationOpenId());
        	invitationOrder.setLevelId(entPayorderRequest.getLevelId());
        	invitationOrder.setStatus("SUCCESS");
        	
        	InvitationRecord invitationRecord = new InvitationRecord();
    		invitationRecord.setInvitationUserId(entPayorderRequest.getInvitationUserId());
    		invitationRecord.setLevelId(entPayorderRequest.getLevelId());
    		invitationRecord.setInvitationType(InvitationTypeEnum.InvitationCash.getKey());
        	List<InvitationRecord> purchaseRecordList = purchaseMapper.selectPurchaseRecord(invitationRecord);
        	
//        	List<EntPayOrder> invitationList = entPayOrderMapper.selectEntPayOrderd(invitationOrder);
        		
//        	if(!successList.isEmpty()  ){// 1.被邀请人支付成功以后,邀请人只能收到一次返现; 
//        		log.info("微信打款到零钱（邀请人:" + entPayorderRequest.getInvitationOpenId() +
//        				"被邀请人："+ entPayorderRequest.getPurchaseOpenId() +
//        				"LevelId："+ entPayorderRequest.getLevelId() +
//        				"已经发过零钱返现）,不可以再返现！");
//        		return null;
//        	}
        	List<UserLevel> userLevels = squirrelLevelMapper.getUserLevelsByLevelIdAndUserId(entPayorderRequest.getLevelId(), entPayorderRequest.getInvitationUserId());
        	if(userLevels.isEmpty()){
        		log.info("微信打款到零钱（邀请人:" + entPayorderRequest.getInvitationOpenId() +
        				"被邀请人："+ entPayorderRequest.getPurchaseOpenId() +
        				"LevelId："+ entPayorderRequest.getLevelId() +
        				"邀请人没有购买）,不可以返现！");
        		return null;
        	}
        	
        	if(purchaseRecordList.size()>10){// 2.第11人不再返现
        		log.info("微信打款到零钱（邀请人:" + entPayorderRequest.getInvitationOpenId() +
        				"被邀请人："+ entPayorderRequest.getPurchaseOpenId() +
        				"LevelId："+ entPayorderRequest.getLevelId() +
        				"已超过10人）,不可以再返现！");
        		return null;
        	}
            EntPayResult entPayResult = this.wxService.getEntPayService().entPay(entPayRequest);

            EntPayOrder entPayOrder = new EntPayOrder();
            
            entPayOrder.setInvitationOpenId(entPayorderRequest.getInvitationOpenId());
            entPayOrder.setPurchaseOpenId(entPayorderRequest.getPurchaseOpenId());
            entPayOrder.setLevelId(entPayorderRequest.getLevelId());
            entPayOrder.setOrderId(orderId);
            entPayOrder.setAmount(entPayorderRequest.getAmount());
            entPayOrder.setOpenId(entPayorderRequest.getOpenid());

            entPayOrder.setSpbillCreateIp(entPayorderRequest.getSpbillCreateIp());
            entPayOrder.setCreatedAt(new Date());
            entPayOrder.setUpdatedAt(new Date());

            if(entPayResult.getReturnCode().equals(WxPayConstants.ResultCode.FAIL)) {
            	entPayOrder.setStatus("FAIL");
                entPayOrder.setPartnerTradeNo(partnerTradeNo);
                entPayOrder.setPaymentNo("FAIL");
                entPayOrder.setPaymentTime(new Date());
            	entPayOrderMapper.insert(entPayOrder);
                log.error("微信打款到零钱（orderId = " + orderId + "）失败：接口请求失败");

//                entPayCreateOrderResponse.setSuccess(false);
//                return Result.success(entPayCreateOrderResponse);
            }
            if("SUCCESS".equals(entPayResult.getResultCode())) {
            	entPayOrder.setStatus("SUCCESS");
                entPayOrder.setPartnerTradeNo(entPayResult.getPartnerTradeNo());
                entPayOrder.setPaymentNo(entPayResult.getPaymentNo());
                
            	
				try {
					String strReqDelTime = entPayResult.getPaymentTime();
					Date date = new SimpleDateFormat("YYYY-MM-DD hh:mm:ss").parse(strReqDelTime);
					entPayOrder.setPaymentTime(date);
				} catch (ParseException e) {
					log.error("微信打款到零钱,PaymentTime格式错误,（orderId = " + orderId + "）失败：接口请求失败");
				}
                
				entPayOrderMapper.insert(entPayOrder);
//            	log.error("微信打款到零钱（orderId = " + orderId + "）成功");
//                entPayCreateOrderResponse.setSuccess(true);
//                return Result.success(entPayCreateOrderResponse);
            }else {
                entPayOrder.setStatus("FAIL");
                entPayOrder.setPartnerTradeNo(partnerTradeNo);
                entPayOrder.setPaymentNo("FAIL");
                entPayOrder.setPaymentTime(new Date());
            	entPayOrderMapper.insert(entPayOrder);
                log.error("微信打款到零钱（orderId = " + orderId + "）失败：resultCode=" + entPayResult.getResultCode());

//                entPayCreateOrderResponse.setSuccess(false);
//                return Result.success(entPayCreateOrderResponse);
            }
        } catch (WxPayException e) {
            log.error("微信打款到零钱（orderId = " + orderId +"）失败：" + e.getReturnMsg());

//            entPayCreateOrderResponse.setSuccess(false);
//            return Result.success(entPayCreateOrderResponse);
        }
        return null;
    }
    
    
    public static void main(String[] args){
//    	String env = environment.getProperty("spring.profiles.active");
//    	WxPayService a = new WxPayServiceApacheHttpImpl();
//    	new WechatEntPayService(a).createOrder();
    }

}
