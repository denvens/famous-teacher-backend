## 支付成功回调开课接口
+ 功能说明：支付成功回调开课

### 历史记录

#### 2019-10-08
- 新增

**公共参数:**
+ 测试接口的地址为 https://test-msyb-api.qingclasswelearn.com/bigbay-payment/notify
+ 线上接口的地址为 https://msyb-api.qingclasswelearn.com/bigbay-payment/notify
+ 公共参数 说明： 公共参数需要传递到get或post里面，get请求参数都传到参数中，post请求参数都传到form中。

### 接口信息
|接口调用方式 	|	Restful Service			|
|:--------------|:--------------------------|
|功能	     	| 支付成功回调开课		  	|
|接口名称		|/bigbay-payment/notify		|
|请求方法		|POST					    |

### 接口公共参数
|参数名		   		|类型	|是否必填	|说明			    						|
|:------------------|:------|:----------|:------------------------------------------|
|bigbayAppId	   	|String	|是		  	|业务端的海湾id								|
|content	   		|String	|是		  	|回调请求的具体内容,json字符串    				|
|random		   		|String	|是		  	|随机字符串,12-32位    						    |
|timestamp	   		|String	|是		  	|请求发出时间,10位，精确到秒			        |
|signature	   		|String	|是		  	|请求数据签名,32位大写字母    					|

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

```
content:错误描述
```