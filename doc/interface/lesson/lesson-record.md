## 学习记录接口
+ 功能说明：学习记录

### 历史记录

#### 2019-10-08
- 新增

**公共参数:**
+ 测试接口的地址为 https://test-msyb-api.qingclasswelearn.com/subject/lesson-record
+ 线上接口的地址为 https://msyb-api.qingclasswelearn.com/subject/lesson-record
+ 公共参数 说明： 公共参数需要传递到get或post里面，get请求参数都传到参数中，post请求参数都传到form中。

### 接口信息
|接口调用方式 	|	Restful Service			|
|:--------------|:--------------------------|
|功能	     	| 学习记录					|
|接口名称		|/subject/lesson-record   	|
|请求方法		|POST					    |
|Content-Type	|application/json			|

### 接口公共参数
|参数名		   		|类型	|是否必填	|说明			    						|
|:------------------|:------|:----------|:------------------------------------------|
|	   				|json	|是		  	|{"subjectId":1000000, "levelId": 1000005}	|

### 接口返回值
+ 返回值数据类型：json
+ 返回值说明：

**返回值**  

```
{
    "code":0,
    "data":{
        "vipEndTime":"2020-09-30",
        "nowDate":"2019-10-08 19:17:29",
        "beginAt":"2019-09-05",
        "vipBeginTime":"2019-09-22",
        "lessonList":[
            {
                "lessonId":"1000137",
                "optTime":"2019-10-08 16:48:30",
                "status":true
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