package com.qingclass.squirrel.controller;


import com.qingclass.squirrel.entity.ConversionPush;
import com.qingclass.squirrel.entity.SquirrelUser;
import com.qingclass.squirrel.entity.SquirrelUserBehavior;
import com.qingclass.squirrel.mapper.user.SquirrelUserMapper;
import com.qingclass.squirrel.service.SquirrelUserService;
import com.qingclass.squirrel.service.WxService;
import com.qingclass.squirrel.utils.FormatUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 重要类
 * 用于作为微信公众号的通知接口
 *
 * @author 苏天奇
 * */
@RestController
@RequestMapping(value = "")
public class UserEventController {

    @Autowired
    SquirrelUserMapper squirrelUserMapper;
    @Autowired
    WxService wxService;
    @Autowired
    SquirrelUserService squirrelUserService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    private String tooken = "123a";

    /**
     * 更换第一次用
     * @param request
     * @param response
     * @throws IOException
     */
//    @ResponseBody
//    @GetMapping(value = "/wechat-notify")
    public void userRecordListChangeUrl(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String signature = request.getParameter("signature");

        String timestamp = request.getParameter("timestamp");

        String nonce = request.getParameter("nonce");

        String echostr = request.getParameter("echostr");

        PrintWriter out = response.getWriter();

        if (checkSignature(signature, timestamp, nonce)) {

            // 如果校验成功，将得到的随机字符串原路返回

            out.print(echostr);

        }

    }
    /**
     * 本接口已经注册在了 松鼠绘本馆公众号，
     * 用户的关注取关等 微信定义事件将会推送到本接口
     * */
    @ResponseBody
    @RequestMapping(value = "/wechat-notify")
    public String userRecordList(HttpServletRequest request){
        Map<String, String> map = FormatUtil.xmlToMap(request);
        // 发送方帐号
        String fromUserName = map.get("FromUserName");
        // 开发者微信号
        String toUserName = map.get("ToUserName");
        // 消息类型
        String msgType = map.get("MsgType");
        //事件类型类型
        String event = map.get("Event");
        //事件场景值
        String eventKey = map.get("EventKey");
        // 默认回复一个"success"
        String responseMessage = "success";


        SquirrelUser squirrelUser = squirrelUserMapper.selectByOpenId(fromUserName);
        if(squirrelUser == null){
            //初始化用户信息
            Map<String, Object> ret = squirrelUserService.getUserInfoByOpenId(fromUserName);
            System.out.println("ret : " + ret);
            if(ret != null){
                squirrelUserService.dealUser(ret);
            }
        }

        // 对消息进行处理
        if(msgType.equals("event")){
            switch (event){
                case "subscribe":{//订阅公众号
                    if(eventKey != null){//是否通过渠道进入
                        wxService.channelReturn(fromUserName,eventKey,event);
                    }

                    squirrelUserMapper.updateSubscribeByOpenId(fromUserName,1);
                    logger.info("user subscription successful.(openId="+fromUserName+")");

                    //推送转化表插入数据
                    ConversionPush conversionPush = new ConversionPush();
                    conversionPush.setOpenId(fromUserName);
                    List<ConversionPush> conversionPushes = squirrelUserMapper.selectConversionByOpenId(conversionPush);
                    if(conversionPushes == null || conversionPushes.size() < 1){
                        conversionPush.setFlag(1);
                        conversionPush.setIsSend(0);
                        conversionPush.setIsPurchase(0);
                        squirrelUserMapper.insertConversionPush(conversionPush);
                    }
                    break;
                }
                case "unsubscribe":{//取消订阅公众号
                    squirrelUserMapper.updateSubscribeByOpenId(fromUserName,0);

                    List<SquirrelUserBehavior> squirrelUserBehaviors = squirrelUserMapper.selectUserBehaviorByOpenIdAndSubscribe(fromUserName);
                    for(int i = 0 ; i < squirrelUserBehaviors.size() ; i ++){
                        if(squirrelUserBehaviors.get(i) != null){
                            squirrelUserMapper.updateUserBehavior(squirrelUserBehaviors.get(i).getId(),null,0,null);
                        }
                    }

                    //从推送转化表汇总删除
                    ConversionPush conversionPush = new ConversionPush();
                    conversionPush.setOpenId(fromUserName);
                    List<ConversionPush> conversionPushes = squirrelUserMapper.selectConversionByOpenId(conversionPush);
                    conversionPushes.forEach(e -> {
                        if(e.getIsPurchase() != 1){
                            squirrelUserMapper.deleteConversionPush(e.getId());
                        }
                    });


                    logger.info("user unsubscribed successful.(openId="+fromUserName+")");
                    break;
                }
                case "SCAN":{//已关注

                    wxService.channelReturn(fromUserName,eventKey,event);
                    break;
                }
            }
        }


        return responseMessage;
    }




    public boolean checkSignature(String signature,String timestamp,String nonce){

//1.定义数组存放tooken，timestamp,nonce

        String[] arr = {tooken,timestamp,nonce};

//2.对数组进行排序

        Arrays.sort(arr);

//3.生成字符串

        StringBuffer sb = new StringBuffer();

        for(String s : arr){

            sb.append(s);

        }

//4.sha1加密,网上均有现成代码

        String temp = getSha(sb.toString());

//5.将加密后的字符串，与微信传来的加密签名比较，返回结果

        return temp.equals(signature);

    }



    public String getSha(String str) {

        if (str == null || str.length() == 0) {

            return null;

        }

        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',

                'a', 'b', 'c', 'd', 'e', 'f'};

        try {

            MessageDigest mdTemp = MessageDigest.getInstance("SHA1");

            mdTemp.update(str.getBytes("UTF-8"));


            byte[] md = mdTemp.digest();

            int j = md.length;

            char buf[] = new char[j * 2];

            int k = 0;

            for (int i = 0; i < j; i++) {

                byte byte0 = md[i];

                buf[k++] = hexDigits[byte0 >>> 4 & 0xf];

                buf[k++] = hexDigits[byte0 & 0xf];

            }

            return new String(buf);

        } catch (Exception e) {

            // TODO: handle exception

            return null;

        }

    }
}