package com.qingclass.squirrel.quartz;

import com.alibaba.fastjson.JSONObject;
import com.qingclass.squirrel.domain.cms.SquirrelLesson;
import com.qingclass.squirrel.domain.cms.SquirrelLessonRemind;
import com.qingclass.squirrel.domain.wx.WxCustom;
import com.qingclass.squirrel.domain.wx.WxRemind;
import com.qingclass.squirrel.domain.wx.WxTemplate;
import com.qingclass.squirrel.entity.BaseJob;
import com.qingclass.squirrel.entity.SquirrelUser;
import com.qingclass.squirrel.mapper.cms.SquirrelLessonMapper;
import com.qingclass.squirrel.mapper.cms.WxPurchaseMapper;
import com.qingclass.squirrel.mapper.user.SquirrelUserMapper;
import com.qingclass.squirrel.mapper.user.UserRemindMapper;
import com.qingclass.squirrel.utils.ApplicationContextHelper;
import com.qingclass.squirrel.utils.DateFormatHelper;
import com.qingclass.squirrel.utils.PropertiesLoader;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 *
 * @author 苏天奇
 * */
@Service
public class ClassBeginsTask implements BaseJob {

    @Autowired
    UserRemindMapper userRemindMapper;
    @Autowired
    WxPurchaseMapper wxPurchaseMapper;
    @Autowired
    SquirrelUserMapper squirrelUserMapper;
    @Autowired
    SquirrelLessonMapper squirrelLessonMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
 //   @Value("{send.message.lesson.template.id}")
    private String CLASS_BEGINS_TEMPLATE_ID = PropertiesLoader.getProperty("class.begins.template.id");

    Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String SEND_TEMPLATE = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token="; //推送模版消息
    private final String SEND_CUSTOM = "https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=";

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        logger.info("开课通知[定时任务开始执行:扫表...]");
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        wxPurchaseMapper=ApplicationContextHelper.getApplicationContext().getBean(WxPurchaseMapper.class);
        userRemindMapper=ApplicationContextHelper.getApplicationContext().getBean(UserRemindMapper.class);
        squirrelUserMapper=ApplicationContextHelper.getApplicationContext().getBean(SquirrelUserMapper.class);
        squirrelLessonMapper=ApplicationContextHelper.getApplicationContext().getBean(SquirrelLessonMapper.class);
        stringRedisTemplate=ApplicationContextHelper.getApplicationContext().getBean(StringRedisTemplate.class);

        SquirrelLessonRemind squirrelLessonRemind = wxPurchaseMapper.selectRemindTimeDefault("class-begins");
        WxTemplate wxTemplate  = wxPurchaseMapper.selectTemplateByType("class-begins-template");
        WxCustom wxCustom = wxPurchaseMapper.selectCustomByType("class-begins-custom");


        String loggerInfo = "send message successful.";
        String loggerErr = "send message failed.";

        String remindRate = squirrelLessonRemind.getRemindRate();
        if(remindRate == null){
            return;
        }
        String[] split = remindRate.split(",");
        Calendar rightNow = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        Map<Integer,String> sendDate = new HashMap<>();
        for(String re : split){
            int i = Integer.parseInt(re);
            rightNow.setTime(new Date());
            rightNow.add(Calendar.DAY_OF_YEAR,i);
            Date dt1=rightNow.getTime();
            sendDate.put(i,sdf.format(dt1));
        }

        List<SquirrelUser> sus = new ArrayList<>();
        for(Map.Entry<Integer,String> entry: sendDate.entrySet()){
            String value = entry.getValue();
            logger.info("开课通知[定时任务执行时间:" + value);
            List<SquirrelUser> squirrelUsers = squirrelUserMapper.selectByBeginAt(value);
            sus.addAll(squirrelUsers);
        }

        String customContent = null;
        String templateContent = null;
        if(wxCustom != null){
            customContent = wxCustom.getContent();
        }
        if(wxTemplate != null){
            templateContent = wxTemplate.getContent();
        }
        //开始推送了
        for(SquirrelUser su : sus){
            if(templateContent != null){
                logger.info("开课通知:模版消息[param:{openId="+su.getOpenId()+"}]");
                Map<String,Object> map = JSONObject.parseObject(templateContent);

                Map<String,Object> userName = new HashMap<>();
                userName.put("value",su.getNickName());
                userName.put("color",map.get("remarkColor"));
                Map<String,Object> courseName = new HashMap<>();
                courseName.put("value",su.getLevelName());
                courseName.put("color",map.get("remarkColor"));
                Map<String,Object> date = new HashMap<>();
                date.put("value",su.getBeginAt());
                date.put("color",map.get("remarkColor"));
                Map<String,Object> remark = new HashMap<>();
                remark.put("value",map.get("remarkCon"));
                remark.put("color",map.get("remarkColor"));
                Map<String,Object> data = new HashMap<>();
                data.put("userName",userName);
                data.put("courseName",courseName);
                data.put("date",date);
                data.put("remark",remark);

                Map<String,Object> params = new HashMap<>();
                params.put("touser",su.getOpenId());
                params.put("template_id",CLASS_BEGINS_TEMPLATE_ID);
                params.put("url",wxTemplate.getUrl());
                params.put("data",data);

                String accessToken = getAccessToken();
                sendToWx(params,SEND_TEMPLATE+accessToken,loggerInfo,loggerErr);
            }
            if(customContent != null){
                String cc =  customContent;

                cc = cc.replace("{nickName}",su.getNickName());
                cc = cc.replace("{levelName}",su.getLevelName());
                cc = cc.replace("{dateTime}",su.getBeginAt());

                logger.info("开课通知:客服消息[param:{openId="+su.getOpenId()+"}]");
                Map<String,Object> text = new HashMap<>();
                text.put("content",cc);
                Map<String,Object> map = new HashMap<>();
                map.put("touser",su.getOpenId());
                map.put("msgtype","text");
                map.put("text",text);

                String accessToken = getAccessToken();
                sendToWx(map,SEND_CUSTOM+accessToken,loggerInfo,loggerErr);
            }
        }

    }


    //---------------------以下代码在WxService中有重复代码，为了方便日后移植，粘贴过来了

    /**
     * 取token
     * */
    private String getAccessToken(){
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
            logger.info(loggerInfo+"..."+result.toString());
        } catch (Exception e) {
            logger.info(loggerErr+"..."+result.toString());
        }
        if(result != null){
            return result;
        }
        return null;
    }
}
