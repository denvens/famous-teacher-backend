## 获取分享页接口
+ 功能说明：获取分享页

### 历史记录

#### 2019-10-10
- 新增

**公共参数:**
+ 测试接口的地址为 https://test-msyb-api.qingclasswelearn.com/no-need-sign-in/get-share-page
+ 线上接口的地址为 https://msyb-api.qingclasswelearn.com/no-need-sign-in/get-share-page
+ 公共参数 说明： 公共参数需要传递到get或post里面，get请求参数都传到参数中，post请求参数都传到form中。

### 接口信息
|接口调用方式 	|	Restful Service						|
|:--------------|:--------------------------------------|
|功能	     	| 获取分享页								|
|接口名称		|/no-need-sign-in/get-share-page		|
|请求方法		|POST					    			|

### 接口公共参数
|参数名		   		|类型	|是否必填	|说明			    					|
|:------------------|:------|:----------|:--------------------------------------|
|levelId			|Integer|是		  	|1000005，子频道ID,四六级ID				|

### 接口返回值
+ 返回值数据类型：json
+ 返回值说明：

**返回值**  

```
{
    "code": 0,
    "data": {
        "id": 1000089,
        "url": "4444",											//	自定义url
        "spaceTitle": "123",									//  微信分享朋友圈标题
        "freTitle": "1",										//  微信分享好友标题
        "content": "1",											//  微信分享好友描述
        "img": "images/uploads/2019-02-27/d28e5961-cd44-45d3-92fd-7236b3b903ad.png",   // 自定义图标
        "pageNo": null,
        "pageTotal": null,
        "pageSize": null
    },
    "success": true,
    "denied": false,
    "message": ""
}
```

**返回值描述**  

```
content:错误描述
```