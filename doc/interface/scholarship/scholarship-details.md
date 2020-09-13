## 毕业奖学金详情接口
+ 功能说明：毕业奖学金详情,此接口只有登录用户才有权限访问

### 历史记录

#### 2019-11-25
- 新增

**公共参数:**
+ 测试接口的地址为 https://test-msyb-api.qingclasswelearn.com/user/scholarship-details
+ 线上接口的地址为 https://msyb-api.qingclasswelearn.com/user/scholarship-details

+ 公共参数 说明： 公共参数需要传递到get或post里面，get请求参数都传到参数中，post请求参数都传到form中。

### 接口信息
|接口调用方式 	|	Restful Service							|
|:--------------|:------------------------------------------|
|功能	     	| 毕业奖学金详情								|
|接口名称		|/user/scholarship-details					|
|请求方法		|POST					    				|

### 接口公共参数
|参数名		   		|是否必填	|说明			    			|
|:------------------|:----------|:------------------------------|
|					|		  	|此接口只有登录用户才有权限访问		|

### 接口返回值
+ 返回值数据类型：json
+ 返回值说明：

**返回值**  

```
{
    "code":0,
    "data":{
        "details":[                 				// 奖学金列表
            {
                "name":"Level 2神秘海域",        	//  level名称
                "lessonDay":120,                	//  课程天数
                "returnFeeDay":35,					//  返费天数
            	"beginClassTime": "2019-08-18",		//  开始时间
            	"endClassTime": "2019-08-18",		//  结束时间
                "levelId":"1000006",            	//  level ID
                "subjectId":"1000000",          	//  主题ID
                "alreadyDays":1,                	// 	已学完天数
                "status":3,                  		//  奖学金状态：0:未全勤; 1.已全勤,待申请; 2:已过期; 3:已申请,审核中; 4:审核通过,发放成功; 5:审核拒绝;
                "createdAt":"2019-08-24 18:08:11",  // 	申请时间：2019-08-24 18:08:11
                "updatedAt":"2019-08-24 18:08:11"  	// 	发放时间：2019-08-24 18:08:11
            }
        ]
    },
    "success":true,
    "denied":false,
    "message":""
}
```

**返回值描述**  

```
content:错误描述
```