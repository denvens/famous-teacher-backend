package com.qingclass.squirrel.service;

import com.alibaba.fastjson.JSONObject;
import com.qingclass.squirrel.domain.cms.SquirrelLevel;
import com.qingclass.squirrel.domain.wx.WxChannel;
import com.qingclass.squirrel.domain.wx.WxCustom;
import com.qingclass.squirrel.domain.wx.WxTemplate;
import com.qingclass.squirrel.entity.InvitationRecord;
import com.qingclass.squirrel.entity.SquirrelUser;
import com.qingclass.squirrel.mapper.cms.SquirrelLevelMapper;
import com.qingclass.squirrel.entity.SquirrelUserBehavior;
import com.qingclass.squirrel.mapper.cms.WxChannelMapper;
import com.qingclass.squirrel.mapper.cms.WxPurchaseMapper;
import com.qingclass.squirrel.mapper.user.SquirrelUserMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 处理微信公众消息
 * */
@Service
public class WxService {
    private final String SEND_TEMPLATE = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token="; //推送模版消息
    private final String SEND_CUSTOM = "https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=";
    private final String PURCHASE_NOTICE_TEMPLATE_TYPE = "purchase-notice-template";//购买通知模板
    private final String PURCHASE_NOTICE_CUSTOM_TYPE = "purchase-notice-custom";//购买通知客服
    @Value("${purchase.notice.template.id}")
    private String PURCHASE_NOTICE_TEMPLATE_ID;
    @Value("${invitation.template.id}")
    public String INVITATION_TEMPLATE_ID;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private WxPurchaseMapper wxPurchaseMapper;
    @Autowired
    private WxChannelMapper wxChannelMapper;
    @Autowired
    private SquirrelLevelMapper squirrelLevelMapper;
    @Autowired
    private SquirrelUserMapper squirrelUserMapper;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 购买通知
     * */
    public void purchaseNotice(String openId, SquirrelLevel squirrelLevel, SquirrelUser squirrelUser, String beginAt){
    	
    	logger.info("购买通知:start...openId:{},levelId:{},beginAt:{}",openId,squirrelLevel,squirrelUser);
    	
        if(openId == null || squirrelLevel == null || beginAt == null){
            return;
        }
        //发送模板消息(购买通知模板)
        WxTemplate wxTemplate = wxPurchaseMapper.selectTemplateByType(PURCHASE_NOTICE_TEMPLATE_TYPE);

        logger.info("wxTemplate:{},content:{}",wxTemplate, wxTemplate.getContent());
        
        if(wxTemplate != null){
            String content = wxTemplate.getContent();
            content = content.replace("{levelName}",squirrelLevel.getName());
            content = content.replace("{dateTime}",beginAt);
            content = content.replace("{nickName}",squirrelUser.getNickName());
            Map<String,Object> map = JSONObject.parseObject(content);
            
            logger.info("first, contentHead:{}, contentHeadColor:{}",map.get("contentHead"),map.get("contentHeadColor"));
            
            Map<String,Object> first = new HashMap<>();
            first.put("value",map.get("contentHead"));
            first.put("color",map.get("contentHeadColor"));
            
            Map<String,Object> keyword1 = new HashMap<>();
            keyword1.put("value",squirrelLevel.getName());
            keyword1.put("color",map.get("contentInfoColor"));
            
            Map<String,Object> keyword2 = new HashMap<>();
            keyword2.put("value",beginAt);
            keyword2.put("color",map.get("contentBodyColor"));
            
            Map<String,Object> remark = new HashMap<>();
            remark.put("value",map.get("contentBody"));
            remark.put("color",map.get("contentBodyColor"));
            
            Map<String,Object> data = new HashMap<>();
            data.put("first",first);
            data.put("keyword1",keyword1);
            data.put("keyword2",keyword2);
            data.put("remark",remark);

            Map<String,Object> params = new HashMap<>();
            params.put("touser",openId);
            params.put("template_id",PURCHASE_NOTICE_TEMPLATE_ID);
            params.put("url",wxTemplate.getUrl());
            params.put("data",data);

            String loggerInfo = "send template successful.";
            String loggerErr = "send template failed.";

            sendToWx(params,SEND_TEMPLATE+getAccessToken(),loggerInfo,loggerErr);
        }

        //发送客服消息(购买通知客服)
        WxCustom wxCustom = wxPurchaseMapper.selectCustomByType(PURCHASE_NOTICE_CUSTOM_TYPE);
        if(wxCustom != null){
            String content = wxCustom.getContent();
            content = content.replace("{levelName}",squirrelLevel.getName());
            content = content.replace("{dateTime}",beginAt);
            content = content.replace("{nickName}",squirrelUser.getNickName());
            Map<String,Object> text = new HashMap<>();
            text.put("content",content);
            Map<String,Object> map = new HashMap<>();
            map.put("touser",openId);
            map.put("msgtype","text");
            map.put("text",text);

            String loggerInfo = "send custom successful.";
            String loggerErr = "send custom failed.";
            sendToWx(map,SEND_CUSTOM+getAccessToken(),loggerInfo,loggerErr);
        }
    }

    /**
     * 渠道二维码回复客服消息
     * */
    public void channelReturn(String openId, String eventKey, String event){

        JSONObject sceneJson;
        WxChannel wxChannel;

        //对各类型事件是否为本系统提供二维码进行判断
        if(event.equals("SCAN")){
            //解析eventKey
            sceneJson =JSONObject.parseObject(eventKey);
            Object k = sceneJson.get("k");
            if(k == null){
                return;
            }
        }else if(event.equals("subscribe")){
            if(eventKey.length() < 8 || !eventKey.startsWith("qrscene_")){
                return;
            }else{
                //解析eventKey
                sceneJson = JSONObject.parseObject(eventKey.substring(8));
            }
        }else{
            return;
        }

        try{
            wxChannel = wxChannelMapper.getChannelByCode(sceneJson.get("k").toString());//根据唯一码查询channel
        }catch(NullPointerException e){
            logger.warn("qr don't have key.");
            return;
        }

        if(!event.equals("SCAN")){//如果用户已关注，就不记录本次用户行为
            //先记录本次用户行为
            userBehaviorRecord(openId,eventKey,sceneJson.get("k").toString(),1,"subscribe",null);
        }


        if(wxChannel.getMessages() != null){
            SquirrelUser squirrelUser = squirrelUserMapper.selectByOpenId(openId);
            String[] messages = wxChannel.getMessages().split("&&&&");
            for(String message : messages){

                message = message.replace("{nickName}",squirrelUser.getNickName());

                Map<String,Object> text = new HashMap<>();
                text.put("content",message);
                Map<String,Object> map = new HashMap<>();
                map.put("touser",openId);
                map.put("msgtype","text");
                map.put("text",text);

                String loggerInfo = "send custom successful.";
                String loggerErr = "send custom failed.";
                sendToWx(map,SEND_CUSTOM+getAccessToken(),loggerInfo,loggerErr);
            }
        }
    }


    /**
     * 邀请成功向邀请人推送消息
     * */
    public void invitationSuccessNotice(InvitationRecord invitationRecord){
        Integer levelId = invitationRecord.getLevelId();
        
    	invitationRecord.setLevelId(levelId);
    	invitationRecord.setInvitationType(invitationRecord.getInvitationType());
    	
        WxTemplate wxTemplate = wxPurchaseMapper.selectInvitationTemplate(invitationRecord);
        WxCustom wxCustom = wxPurchaseMapper.selectInvitationCustom(invitationRecord);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        SquirrelUser squirrelUser = squirrelUserMapper.selectById(invitationRecord.getPurchaseUserId());
        //发送模板消息
        if(wxTemplate != null){
            String content = wxTemplate.getContent();
            Map<String,Object> map = JSONObject.parseObject(content);

            Map<String,Object> first = new HashMap<>();
            first.put("value",map.get("title"));
            first.put("color",map.get("titleColor"));
            Map<String,Object> keyword1 = new HashMap<>();
            keyword1.put("value",squirrelUser.getNickName());
            keyword1.put("color",map.get("remarkColor"));
            Map<String,Object> keyword2 = new HashMap<>();
            keyword2.put("value",sdf.format(new Date()));
            keyword2.put("color",map.get("remarkColor"));
            Map<String,Object> remark = new HashMap<>();
            remark.put("value",map.get("remark"));
            remark.put("color",map.get("remarkColor"));
            Map<String,Object> data = new HashMap<>();
            data.put("first",first);
            data.put("keyword1",keyword1);
            data.put("keyword2",keyword2);
            data.put("remark",remark);

            Map<String,Object> params = new HashMap<>();
            params.put("touser",squirrelUserMapper.selectById(invitationRecord.getInvitationUserId()).getOpenId());
            params.put("template_id",INVITATION_TEMPLATE_ID);
            params.put("url",wxTemplate.getUrl());
            params.put("data",data);

            String loggerInfo = "send template successful.";
            String loggerErr = "send template failed.";

            sendToWx(params,SEND_TEMPLATE+getAccessToken(),loggerInfo,loggerErr);
        }

        //发送客服消息
        if(wxCustom != null){
            String content = wxCustom.getContent();
            content = content.replace("{nickName}",squirrelUser.getNickName());
            content = content.replace("{dateTime}",sdf.format(new Date()));

            Map<String,Object> text = new HashMap<>();
            text.put("content",content);
            Map<String,Object> map = new HashMap<>();
            map.put("touser",squirrelUserMapper.selectById(invitationRecord.getInvitationUserId()).getOpenId());
            map.put("msgtype","text");
            map.put("text",text);

            String loggerInfo = "send custom successful.";
            String loggerErr = "send custom failed.";
            sendToWx(map,SEND_CUSTOM+getAccessToken(),loggerInfo,loggerErr);
        }



    }

    /**
     * 取token
     * */
    public String getAccessToken(){
        return stringRedisTemplate.opsForValue().get("msyb_access_token")+"";
    }


    /**
     * 调用微信接口
     * */
    private Map<String,Object> sendToWx(Map<String,Object> paramsMap, String url,String loggerInfo,String loggerErr){
        RestTemplate rest = new RestTemplate();
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        HttpEntity requestEntity = new HttpEntity(paramsMap, headers);
        Map result = null;
        try {
            ResponseEntity<Map> entity = rest.exchange(url, HttpMethod.POST, requestEntity,Map.class, new Object[0]);
            result = (Map) entity.getBody();
            logger.info(loggerInfo+"result:"+result);
        } catch (Exception e) {
            logger.error(loggerErr+"result:");
        }
        if(result != null){
            return result;
        }
        return null;
    }

    /**
     * 用户行为记录
     * */
    public void userBehaviorRecord(String openId, String eventKey, String channelCode, int behaviorRecordNumber, String type, String note){
        SquirrelUserBehavior squirrelUserBehavior = squirrelUserMapper.selectUserBehaviorByOpenIdAndChannelCode(openId,channelCode);
        List<Integer> levelIdByChannelCode = wxChannelMapper.getLevelIdByChannelCode(channelCode);
        Integer levelId = null;
        if(levelIdByChannelCode.size() > 0){
            levelId = levelIdByChannelCode.get(0);
        }

        if(squirrelUserBehavior != null){
            squirrelUserMapper.updateUserBehavior(squirrelUserBehavior.getId(),channelCode,behaviorRecordNumber,levelId);
        }else{
            squirrelUserMapper.insertUserBehavior(openId,channelCode,behaviorRecordNumber,type,note,levelId);
        }
    }

}
