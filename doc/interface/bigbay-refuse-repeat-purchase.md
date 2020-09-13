## 重复购买效验接口
+ 功能说明：不可重复购买校验，本接口提供给海湾购买页，通知海湾该用户是否有购买课程的权限

### 历史记录

#### 2019-10-17
- 新增

**公共参数:**
+ 测试接口的地址为 https://test-msyb-api.qingclasswelearn.com/bigbay-payment/refuse-repeat-purchase
+ 线上接口的地址为 https://msyb-api.qingclasswelearn.com/user/bigbay-payment/refuse-repeat-purchase
+ 公共参数 说明： 公共参数需要传递到get或post里面，get请求参数都传到参数中，post请求参数都传到form中。

### 接口信息
|接口调用方式 	|	Restful Service						|
|:--------------|:--------------------------------------|
|功能	     	| 重复购买效验			  				|
|接口名称		|/bigbay-payment/refuse-repeat-purchase	|
|请求方法		|POST					    			|

### 接口公共参数
|参数名		   		|类型	|是否必填	|说明			    						|
|:------------------|:------|:----------|:------------------------------------------|
|openId				|String	|是		  	|openId										|
|levelId			|Integer|是		  	|1000005，子频道ID,四六级ID					|

### 接口返回值
+ 返回值数据类型：json
+ 返回值说明：

**返回值**  

```
{
    "code": 0,
    "data": "",
    "success": true,
    "denied": false,
    "message": ""
}
```

**返回值描述**  
返回值 为 true ，则可以购买<br/>
返回值 为 false，则拒绝购买

```
content:错误描述
```