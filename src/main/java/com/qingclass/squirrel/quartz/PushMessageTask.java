package com.qingclass.squirrel.quartz;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

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

import com.alibaba.fastjson.JSONObject;
import com.qingclass.squirrel.domain.cms.SquirrelLesson;
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

/**
 *
 *
 * @author 苏天奇
 * */
@Service
public class PushMessageTask implements BaseJob {

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
    private String SEND_MESSAGE_LESSON_TEMPLATE_ID = PropertiesLoader.getProperty("send.message.lesson.template.id");
    private String SQUIRREL_FE_URI = PropertiesLoader.getProperty("squirrel.fe.uri");

    Logger logger = LoggerFactory.getLogger(this.getClass());

    private final String SEND_TEMPLATE = "https://api.weixin.qq.com/cgi-bin/message/template/send?access_token="; //推送模版消息
    private final String SEND_CUSTOM = "https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=";

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        wxPurchaseMapper=ApplicationContextHelper.getApplicationContext().getBean(WxPurchaseMapper.class);
        userRemindMapper=ApplicationContextHelper.getApplicationContext().getBean(UserRemindMapper.class);
        squirrelUserMapper=ApplicationContextHelper.getApplicationContext().getBean(SquirrelUserMapper.class);
        squirrelLessonMapper=ApplicationContextHelper.getApplicationContext().getBean(SquirrelLessonMapper.class);
        stringRedisTemplate=ApplicationContextHelper.getApplicationContext().getBean(StringRedisTemplate.class);

        String nowTimeStr = DateFormatHelper.getNowTimeStr("HH:mm");
        List<WxRemind> ols = new ArrayList<>();
        try{
            ols = userRemindMapper.selectLessonRemind(nowTimeStr);//当前时间需推送的消息
            logger.info("课程提醒监控:["+nowTimeStr+"需推送条数"+ols.size()+"]");
        }catch (NullPointerException e){
            e.printStackTrace();
            logger.info("课程提醒监控:["+nowTimeStr+"需推送条数"+ols.size()+"]");
            return;
        }


        WxCustom wxCustom = wxPurchaseMapper.selectCustomByType("push-message-begin-study-custom");
        WxTemplate wxTemplate = wxPurchaseMapper.selectTemplateByType("push-message-begin-study-template");
        String customContent = null;
        String templateContent = null;
        if(wxCustom != null){
            customContent = wxCustom.getContent();
        }
        if(wxTemplate != null){
            templateContent = wxTemplate.getContent();
        }


        String loggerInfo = "send custom successful.";
        String loggerErr = "send custom failed.";

        for(WxRemind remind : ols){//循环推送消息
            String templateCon = templateContent;

            String openId = remind.getOpenId();
            Integer levelId = remind.getLevelId();
            SquirrelUser squirrelUser = squirrelUserMapper.selectByOpenId(openId);
            String url = "";
            if(wxTemplate != null){//发送模板消息
                //占位替换
                List<SquirrelUser> squirrelUsers = squirrelUserMapper.selectPurchaseRecordByOpenIdAndLevelIdAtVip(openId, levelId);
                if(squirrelUsers.size() < 1){//不在上课时间内，不推送
                    logger.info("课程提醒监控：unwanted send,return...");
                    continue;
                }else{
                    Integer maxOrder = squirrelLessonMapper.selectMaxOrder(levelId);
                    Date beginAtDate = squirrelUsers.get(0).getBeginAtDate();
                    Date date = new Date();
                    long from1 = beginAtDate.getTime();
                    long to1 = date.getTime();
                    int days = (int) ((to1 - from1) / (1000 * 60 * 60 * 24));
                    if(maxOrder < days + 1){
                        logger.info("maxOrd < days + 1 , return...");
                        continue;
                    }
                    List<SquirrelLesson> lessonAndLevelByLevelIdAndOrder = squirrelLessonMapper.getLessonAndLevelByLevelIdAndOrder(days + 1, levelId);
                    SquirrelLesson lesson = lessonAndLevelByLevelIdAndOrder.get(0);
                    logger.info("课程提醒监控：[ sqlParams:{days:"+(days+1)+",levelId:"+levelId+"} ... " +
                            "userInfo:{openId:"+openId+"} ... result:{lessonName:"+lesson.getName()+",lessonOrder:"+lesson.getOrder()+"}]");
                    templateCon = templateCon.replace("{nickName}",squirrelUser.getNickName());
                    templateCon = templateCon.replace("{levelName}",lesson.getLevelName());
                    templateCon = templateCon.replace("{lessonName}",lesson.getName());
                    templateCon = templateCon.replace("{lessonProgress}","Day"+lesson.getOrder());

                    if(wxTemplate.getUrl().equals("{lessonUrl}")){
                        url = SQUIRREL_FE_URI +"course/"+lesson.getLessonkey();
                    }else {
                        url = wxTemplate.getUrl();
                    }
                }



                Map<String,Object> map = JSONObject.parseObject(templateCon);

                Map<String,Object> first = new HashMap<>();
                first.put("value",map.get("lessonHead"));
                first.put("color",map.get("lessonHeadColor"));
                Map<String,Object> keyword1 = new HashMap<>();
                keyword1.put("value",map.get("lessonName"));
                keyword1.put("color",map.get("lessonNameColor"));
                Map<String,Object> keyword2 = new HashMap<>();
                keyword2.put("value",map.get("lessonState"));
                keyword2.put("color",map.get("lessonStateColor"));
                Map<String,Object> keyword3 = new HashMap<>();
                keyword3.put("value",map.get("lessonProgress"));
                keyword3.put("color",map.get("lessonProgressColor"));

                Map<String,Object> remark = new HashMap<>();
                remark.put("value",map.get("lessonContent"));
                remark.put("color",map.get("lessonContentColor"));
                Map<String,Object> data = new HashMap<>();
                data.put("first",first);
                data.put("keyword1",keyword1);
                data.put("keyword2",keyword2);
                data.put("keyword3",keyword3);
                data.put("remark",remark);

                Map<String,Object> params = new HashMap<>();
                params.put("touser",openId);
                params.put("template_id",SEND_MESSAGE_LESSON_TEMPLATE_ID);
                params.put("url",url);
                params.put("data",data);

                String accessToken = getAccessToken();
                sendToWx(params,SEND_TEMPLATE+accessToken,loggerInfo,loggerErr);
            }

            if(wxCustom != null){//发送客服消息
                List<SquirrelUser> squirrelUsers = squirrelUserMapper.selectPurchaseRecordByOpenIdAndLevelIdAtVip(openId, levelId);
                String cc = customContent;
                if(squirrelUsers.size() < 1){//不在上课时间内，不推送
                    logger.info("课程提醒监控：unwanted send,return...");
                    continue;
                }else{
                    Integer maxOrder = squirrelLessonMapper.selectMaxOrder(levelId);
                    Date beginAtDate = squirrelUsers.get(0).getBeginAtDate();
                    Date date = new Date();
                    long from1 = beginAtDate.getTime();
                    long to1 = date.getTime();
                    int days = (int) ((to1 - from1) / (1000 * 60 * 60 * 24));
                    if(maxOrder < days + 1){
                        logger.info("maxOrd < days + 1 , return...");
                        continue;
                    }
                    List<SquirrelLesson> lessonAndLevelByLevelIdAndOrder = squirrelLessonMapper.getLessonAndLevelByLevelIdAndOrder(days + 1, levelId);
                    SquirrelLesson lesson = lessonAndLevelByLevelIdAndOrder.get(0);
                    logger.info("课程提醒监控：[ sqlParams:{days:"+(days+1)+",levelId:"+levelId+"} ... " +
                            "userInfo:{openId:"+openId+"} ... result:{lessonName:"+lesson.getName()+",lessonOrder:"+lesson.getOrder()+"}]");


                    cc = cc.replace("{nickName}",squirrelUser.getNickName());
                    cc = cc.replace("{levelName}",lesson.getLevelName());
                    cc = cc.replace("{lessonName}",lesson.getName());
                    cc = cc.replace("{lessonProgress}","Day"+lesson.getOrder());
                    cc = cc.replace("{lessonUrl}",SQUIRREL_FE_URI +"course/"+lesson.getLessonkey());
                }

                Map<String,Object> text = new HashMap<>();
                text.put("content",cc);
                Map<String,Object> map = new HashMap<>();
                map.put("touser",openId);
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
            logger.error(loggerErr+"..."+result.toString());
        }
        if(result != null){
            return result;
        }
        return null;
    }
}
