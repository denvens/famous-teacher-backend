## 已购频道下的课程列表接口
+ 功能说明：已购频道下的课程列表

### 历史记录

#### 2019-09-27
- 新增

**公共参数:**
+ 测试接口的地址为 https://test-msyb-api.qingclasswelearn.com/user/lesson-list
+ 线上接口的地址为 https://msyb-api.qingclasswelearn.com/user/lesson-list
+ 公共参数 说明： 公共参数需要传递到get或post里面，get请求参数都传到参数中，post请求参数都传到form中。

### 接口信息
|接口调用方式 	|	Restful Service			|
|:--------------|:--------------------------|
|功能	     	| 已购频道下的课程列表			|
|接口名称		|/user/lesson-list   		|
|请求方法		|POST					    |

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
    "code":0,
    "data":{
        "nowDate":"2019-09-29",  			// 服务器时间
        "levelUser":{						// 当前用户的购买信息，主要关注beginAt
            "id":1786,
            "openId":null,
            "unionId":null,
            "nickName":null,
            "sex":0,
            "headImgUrl":null,
            "subscribe":null,
            "bgmStatus":null,
            "beginAt":"2019-09-05",
            "vipBeginTime":"2019-09-05",
            "vipEndTime":"2020-09-30",
            "beginAtDate":null,
            "vipBeginDate":null,
            "vipEndDate":null,
            "userLevelId":null,
            "buySite":null,
            "levelName":null,
            "levelId":1000005,
            "sendLessonDays":null
        },
        "lessonList":[						// 当前level的课程信息
            {
                "id":1000050,
                "levelid":1000005,
                "name":"day1",
                "order":1,
                "star":null,
                "lessonkey":"0152f382-599f-4eca-9514-b913ac890c6a",
                "audition":0,
                "isOpen":null,
                "updateDate":null,
                "image":"images/uploads/2019-06-03/31d53b29-ff3c-44ea-8026-dd94caba059c.png",
                "title":"part1",
                "alreadyUnitCount":null,
                "levelName":null,
                "isShow":null,
                "unitList":null
            }
        ],
        "lessonsMap":{
            "lesson-36":{
                "record":{
                    "isFinish":true,
                    "optTime":"2019-11-05 15:26:06",
                    "order":1
                }
            }
        }
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