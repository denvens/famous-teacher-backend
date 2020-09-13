package com.qingclass.squirrel.quartz;

import com.qingclass.squirrel.entity.BaseJob;
import com.qingclass.squirrel.entity.ConversionPush;
import com.qingclass.squirrel.entity.SquirrelUser;
import com.qingclass.squirrel.mapper.cms.ConversionPushMapper;
import com.qingclass.squirrel.mapper.user.SquirrelUserMapper;
import com.qingclass.squirrel.utils.ApplicationContextHelper;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 *
 * @author 苏天奇
 * */
public class ConversionPushTask implements BaseJob {

    @Autowired
    SquirrelUserMapper squirrelUserMapper;
    @Autowired
    ConversionPushMapper conversionPushMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private final String SEND_CUSTOM = "https://api.weixin.qq.com/cgi-bin/message/custom/send?access_token=";

    Logger logger = LoggerFactory.getLogger(this.getClass());
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        //注入相应的bean
        squirrelUserMapper= ApplicationContextHelper.getApplicationContext().getBean(SquirrelUserMapper.class);
        conversionPushMapper=ApplicationContextHelper.getApplicationContext().getBean(ConversionPushMapper.class);
        stringRedisTemplate=ApplicationContextHelper.getApplicationContext().getBean(StringRedisTemplate.class);

        //每分钟循环一次本任务
        //每次执行任务取出购买转化的所有记录
        //循环购买转化记录
        //根据循环记录延时推送时间以及level和scope查询出对应需要推送的用户进行推送客服消息


        logger.info("conversionPushTask start...");
        String loggerInfo = "send custom successful.";
        String loggerErr = "send custom failed.";

        List<ConversionPush> conversionPushes = conversionPushMapper.selectAll();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date now = new Date();
        for(ConversionPush cp : conversionPushes){
            Integer levelId = cp.getLevelId();
            Integer scope = cp.getScope();
            String customContent = cp.getCustomContent();
            String pushTime = cp.getPushTime();


            //计算应推送的时间 当前时间减去pushTime
            String[] split = pushTime.split(":");
            long time = (Integer.parseInt(split[0])*60*60*1000)+(Integer.parseInt(split[1])*60*1000)+(Integer.parseInt(split[2])*1000);
            String format = sdf.format(new Date(now.getTime() - time));

            List<ConversionPush> openIds = squirrelUserMapper.selectConversionByLevelIdAndUpdatedAtAndFlag(levelId, scope, format);
            List<Integer> ids = new ArrayList<>();


            for(ConversionPush conversionPush : openIds){//循环推送
                if(customContent != null){//发送客服消息
                    SquirrelUser squirrelUser = squirrelUserMapper.selectByOpenId(conversionPush.getOpenId());
                    ids.add(conversionPush.getId());

                    String cc = customContent;

                    cc = cc.replace("{nickName}",squirrelUser.getNickName());

                    Map<String,Object> text = new HashMap<>();
                    text.put("content",cc);
                    Map<String,Object> map = new HashMap<>();
                    map.put("touser",conversionPush.getOpenId());
                    map.put("msgtype","text");
                    map.put("text",text);

                    String accessToken = getAccessToken();
                    sendToWx(map,SEND_CUSTOM+accessToken,loggerInfo,loggerErr);
                }
            }

            //设置为已推送
            if(ids.size() > 0){
                squirrelUserMapper.updateConversionPushIsSend(ids);
            }

        }
        logger.info("conversionPushTask end...");

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
