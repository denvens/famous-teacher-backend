package com.qingclass.squirrel.controller;


import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.qingclass.squirrel.domain.cms.TestWord;
import com.qingclass.squirrel.mapper.cms.TestWordMapper;
import com.qingclass.squirrel.service.WechatEntPayService;
import com.qingclass.squirrel.service.model.request.EntPayCreateOrderRequest;
import com.qingclass.squirrel.utils.Tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 测试类，和公司其他项目联合实现了一个判定单词量的小表单，这个类尽量不要动
 *
 * @author 苏天奇
 * */
@RestController
@RequestMapping(value = "/test")
public class TestController {
	
	Logger logger = LoggerFactory.getLogger(this.getClass());
	@Autowired
	private WechatEntPayService wechatEntPayService;
	
	@GetMapping("/who-am-i")
    public Map<String, Object> whoAmI() {
	    logger.info("who-am-i");
	    return Tools.s();
	}
	
	@RequestMapping("test")
    public String test(){
		EntPayCreateOrderRequest orderRequest = new EntPayCreateOrderRequest();
		orderRequest.setAmount(100);
		orderRequest.setOpenid("o5SwA1DleGzjahrWkTA0JFinHqgw");
		orderRequest.setAppId("wx1afbcff2bdd165c3");
		orderRequest.setDescription("微信打款到零钱-测试");
		orderRequest.setSpbillCreateIp("10.200.106.147");
		wechatEntPayService.createOrder(orderRequest);
        return "success";
    }
	
    @Autowired
    TestWordMapper testWordMapper;

    @GetMapping(value = "word-list")
    public Map<String,Object> wordList(){

        List<TestWord> all = testWordMapper.getAll();

        return Tools.s(all);
    }



    @PostMapping(value = "commit")
    public Map<String,Object> commit(@RequestParam("param")String param){


        JSONObject jsonObject = JSONObject.parseObject(param);

        List<TestWord> all = testWordMapper.getAll();


        int count = 0;

        for(Map.Entry<String,Object> entry : jsonObject.entrySet()){
            String value = entry.getValue().toString();
            if(value.equals("a")){//如果答对了
                for(TestWord tw : all){
                    if(tw.getWord().equals(entry.getKey())){
                        if(tw.getLevel() == 1) count += 25;
                        if(tw.getLevel() == 2) count += 35;
                        if(tw.getLevel() == 3) count += 45;
                    }
                }
            }
        }



        return Tools.s(count);
    }
}
