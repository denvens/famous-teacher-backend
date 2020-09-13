package com.qingclass.squirrel.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;

@Service
public class SmsService {
	static final String regionId = "cn-hangzhou";
	static final String accessKeyId = "LTAI8vN6zomQVKdo";
	static final String accessSecret = "6heV4PsALuWraxzX7QnwupjkkgiF4i";
	static final String SignName = "名师优播";
	
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public void SendSms(String phoneNumbers, String templateCode, String levelName, String beginAt){
		logger.info("开始发短信,手机号:{}", phoneNumbers);
		DefaultProfile profile = DefaultProfile.getProfile(regionId, accessKeyId, accessSecret);
        IAcsClient client = new DefaultAcsClient(profile);

        CommonRequest request = new CommonRequest();
        request.setMethod(MethodType.POST);
        request.setDomain("dysmsapi.aliyuncs.com");
        request.setVersion("2017-05-25");
        request.setAction("SendSms");
        request.putQueryParameter("RegionId", regionId);
        request.putQueryParameter("PhoneNumbers", phoneNumbers);
        request.putQueryParameter("SignName", SignName);
        request.putQueryParameter("TemplateCode", templateCode);
        request.putQueryParameter("TemplateParam", "{\"levelname\":\""+levelName+"\",\"beginAt\":\""+beginAt+"\"}");
        try {
            CommonResponse response = client.getCommonResponse(request);
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }
	}
	
	public static void main(String[] args) {
		new SmsService().SendSms("18612559763","SMS_178760499","四级","2019-12-12");
    }

}
