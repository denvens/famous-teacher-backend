package com.qingclass.squirrel.controller;

import com.qingclass.squirrel.constant.InvitationTypeEnum;
import com.qingclass.squirrel.domain.cms.UserLevel;
import com.qingclass.squirrel.domain.statistic.InvitationSendCashBuriedPoint;
import com.qingclass.squirrel.domain.statistic.SquirrelFollowUp;
import com.qingclass.squirrel.domain.wx.WxShare;
import com.qingclass.squirrel.entity.ConversionPush;
import com.qingclass.squirrel.entity.InvitationRecord;
import com.qingclass.squirrel.entity.SessionSquirrelUser;
import com.qingclass.squirrel.entity.SquirrelUser;
import com.qingclass.squirrel.mapper.cms.PurchaseMapper;
import com.qingclass.squirrel.mapper.cms.SquirrelLevelMapper;
import com.qingclass.squirrel.mapper.statistic.FollowUpMapper;
import com.qingclass.squirrel.mapper.statistic.InvitationSendCashBuriedPointMapper;
import com.qingclass.squirrel.mapper.user.SquirrelUserMapper;
import com.qingclass.squirrel.utils.Tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;

/**
 * 本类无需登录权限即可访问
 *
 * @author 苏天奇
 * */
@RestController
@RequestMapping(value = "/no-need-sign-in")
public class NoNeedSignInController {

    @Autowired
    private SquirrelLevelMapper squirrelLevelMapper = null;
    @Autowired
    private SquirrelUserMapper squirrelUserMapper = null;
    @Autowired
    private PurchaseMapper purchaseMapper;
    @Autowired
    FollowUpMapper followUpMapper;
    @Autowired
    InvitationSendCashBuriedPointMapper invitationSendCashMapper;
    
    Logger logger = LoggerFactory.getLogger(this.getClass());
    /**
     *
     * */
    @PostMapping("/get-share-page")
    public Map<String, Object> getSharePage(
            HttpServletRequest req) {

        String levelId = req.getParameter("levelId");

        WxShare shareByLevelId = squirrelLevelMapper.getShareByLevelId(Integer.parseInt(levelId));

        return Tools.s(shareByLevelId);
    }

    @PostMapping("/get-share-template-page")
    public Map<String, Object> getShareTemplatePage(
            HttpServletRequest req
    ) {

        String levelId = req.getParameter("levelId");

        WxShare shareByLevelId = squirrelLevelMapper.getShareTemplateByLevelId(Integer.parseInt(levelId));

        return Tools.s(shareByLevelId);
    }


    /**
     * 购买转化推送埋点
     * */
    @PostMapping(value = "conversion-push-point")
    public Map<String, Object> conversionPushPoint(@RequestParam(value = "openId")String openId,
                                                   @RequestParam(value = "levelId")Integer levelId,
                                                   @RequestParam(value = "flag")Integer flag){

        logger.info("转化埋点:[start...param{openId:"+openId+",levelId:"+levelId+",flag:"+flag+"}]");
        ConversionPush conversionPush = new ConversionPush();
        conversionPush.setOpenId(openId);
        List<ConversionPush> conversionPushes = squirrelUserMapper.selectConversionByOpenId(conversionPush);


        int oper = 0;

        for(int i = 0 ; i < conversionPushes.size() ; i ++){
            ConversionPush con = conversionPushes.get(i);
            if(con.getLevelId() != null){//
                if((int)con.getLevelId() == levelId){
                    if(con.getFlag() <= flag){
                        conversionPush.setLevelId(levelId);
                        conversionPush.setFlag(flag);
                        conversionPush.setIsSend(0);
                        squirrelUserMapper.updateConversionPushFlag(conversionPush);
                    }
                    oper = 1;
                }
            }else{
                conversionPush.setLevelId(levelId);
                conversionPush.setFlag(flag);
                squirrelUserMapper.updateConversionPush(conversionPush);
                oper = 1;
            }
        }

        if(oper == 0){
            conversionPush.setLevelId(levelId);
            conversionPush.setFlag(flag);
            conversionPush.setIsSend(0);
            conversionPush.setIsPurchase(0);
            squirrelUserMapper.insertConversionPush(conversionPush);
        }

        return Tools.s();
    }
    
    @PostMapping("/invitationVerify")
	public String invitationVerify(
			@RequestParam(name = "levelId", required = true)Integer levelId,
			@RequestParam(name = "userId", required = true)Integer userId,
			HttpServletRequest request) {
    	userId = (userId+9)/9;
		List<UserLevel> userLevels = squirrelLevelMapper.getUserLevelsByLevelIdAndUserId(levelId, userId);
		
		InvitationRecord invitationRecord = new InvitationRecord();
		invitationRecord.setInvitationUserId(userId);
		invitationRecord.setLevelId(levelId);
		invitationRecord.setInvitationType(InvitationTypeEnum.InvitationCash.getKey());
		
		List<InvitationRecord> purchaseRecordList = purchaseMapper.selectPurchaseRecord(invitationRecord);
		if(!userLevels.isEmpty() && purchaseRecordList.size()<10){
			return "{\"success\":true}";
		}else{
			return "{\"success\":false}";
		}
	}
    
    @PostMapping(value = "invitation-send-cash")
    public Map<String,Object> action(
    		HttpServletRequest req, 
    		@RequestParam(value = "levelId", required = true) Integer levelId,
    		@RequestParam(value = "invitationUserId", required = true) Integer invitationUserId,
    		@RequestParam(value = "purchaseOpenId", required = false) String purchaseOpenId,
    		@RequestParam(value = "type", required = true) Integer type){
        
    	//解密
    	invitationUserId = (invitationUserId+9)/9;
    	
        InvitationSendCashBuriedPoint sendCashPoint = new InvitationSendCashBuriedPoint();
        sendCashPoint.setLevelId(levelId);
        
        SquirrelUser invitationUser = squirrelUserMapper.selectById(invitationUserId);
        String startClassDate = "2015-01-01";
        String invitationOpenId = "0";
        if(invitationUser!=null){
        	List<UserLevel> userLevelList = squirrelLevelMapper.getUserLevelsByLevelIdAndUserId(levelId, invitationUserId);
        	startClassDate = userLevelList.get(0).getBeginAt();
        	invitationOpenId = invitationUser.getOpenId();
        }
        sendCashPoint.setStartClassDate(startClassDate);
        sendCashPoint.setInvitationUserId(invitationUserId.toString());
        if(invitationOpenId==null){
        	invitationOpenId="0";
        }
        sendCashPoint.setPurchaseOpenId(purchaseOpenId);
        switch (type){
            case 1 : sendCashPoint.setIntoSendCashPageCount(1);break;
            case 2 : sendCashPoint.setSendInvitationCount(1);break;
            case 3 : 
            	if(purchaseOpenId.equals(invitationOpenId)){
            		sendCashPoint.setGotoBuyPageCount(0);
            	}else{
            		sendCashPoint.setGotoBuyPageCount(1);
            	}
            	break;
            case 4 : sendCashPoint.setClickBuyCount(1);break;
            case 5 : sendCashPoint.setPurchaseCount(1);break;
            default: break;
        }
       
        Integer id = invitationSendCashMapper.selectSendCashPoint(sendCashPoint);
        if(id == null){
        	invitationSendCashMapper.insert(sendCashPoint);
        }else{
        	sendCashPoint.setId(id);
            invitationSendCashMapper.update(sendCashPoint);
        }
        return Tools.s();
    }
    
}
