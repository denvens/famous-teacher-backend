## 截止当前时间课程信息列表接口
+ 功能说明：截止当前时间课程信息列表

### 历史记录

#### 2019-10-08
- 新增

**公共参数:**
+ 测试接口的地址为 https://test-msyb-api.qingclasswelearn.com/user/lesson-send-list
+ 线上接口的地址为 https://msyb-api.qingclasswelearn.com/user/lesson-send-list
+ 公共参数 说明： 公共参数需要传递到get或post里面，get请求参数都传到参数中，post请求参数都传到form中。

### 接口信息
|接口调用方式 	|	Restful Service			|
|:--------------|:--------------------------|
|功能	     	| 截止当前时间课程信息列表   	|
|接口名称		|/user/lesson-send-list   	|
|请求方法		|POST					    |

### 接口公共参数
|参数名		   		|类型	|是否必填	|说明			    						|
|:------------------|:------|:----------|:------------------------------------------|
|levelId	   		|Integer|是		  	|1000005									|
|sendDays	   		|Integer|是		  	|获取几天的课程数据    						    |

### 接口返回值
+ 返回值数据类型：json
+ 返回值说明：

**返回值**  

```
{
    "code":0,
    "data":[
        {
            "lessonId":1000050,
            "lessonKey":"0152f382-599f-4eca-9514-b913ac890c6a",
            "lessonTitle":"Title day1",
            "lessonName":"day1",
            "order":1
        }
    ],
    "success":true,
    "denied":false,
    "message":""
}
```

**返回值描述**  

```
content:错误描述
```