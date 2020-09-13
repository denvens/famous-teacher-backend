## 获取已学习天数接口
+ 功能说明：获取已学习天数

### 历史记录

#### 2019-10-09
- 新增

**公共参数:**
+ 测试接口的地址为 https://test-msyb-api.qingclasswelearn.com/subject/get-alreadyDays
+ 线上接口的地址为 https://msyb-api.qingclasswelearn.com/subject/get-alreadyDays
+ 公共参数 说明： 公共参数需要传递到get或post里面，get请求参数都传到参数中，post请求参数都传到form中。

### 接口信息
|接口调用方式 	|	Restful Service						|
|:--------------|:--------------------------------------|
|功能	     	| 获取已学习天数							|
|接口名称		|/subject/get-alreadyDays		   		|
|请求方法		|POST					    			|

### 接口公共参数
|参数名		   		|类型	|是否必填	|说明			    					|
|:------------------|:------|:----------|:--------------------------------------|
|subjectId			|Integer|是		  	|1000000								|
|levelId			|Integer|是		  	|1000005，子频道ID,四六级ID				|

### 接口返回值
+ 返回值数据类型：json
+ 返回值说明：

**返回值**  

```
{
    "code":0,
    "data":{
        "vipEndTime":"2020-09-30",
        "nowDate":"2019-10-09 14:54:18",
        "alreadyDays":1,
        "beginAt":"2019-09-05",
        "vipBeginTime":"2019-09-22"
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