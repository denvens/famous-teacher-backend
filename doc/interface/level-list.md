## 子频道列表接口
+ 功能说明：子频道列表

### 历史记录

#### 2019-09-29
- 新增

**公共参数:**
+ 测试接口的地址为 https://test-msyb-api.qingclasswelearn.com/user/level-list
+ 线上接口的地址为 https://msyb-api.qingclasswelearn.com/user/level-list
+ 公共参数 说明： 公共参数需要传递到get或post里面，get请求参数都传到参数中，post请求参数都传到form中。

### 接口信息
|接口调用方式 	|	Restful Service			|
|:--------------|:--------------------------|
|功能	     	| 子频道列表					|
|接口名称		|/user/level-list   		|
|请求方法		|POST					    |

### 接口公共参数
|参数名		   		|类型	|是否必填	|说明			    					|
|:------------------|:------|:----------|:--------------------------------------|
|subjectId			|Integer|是		  	|1000000，频道ID，固定					|

### 接口返回值
+ 返回值数据类型：json
+ 返回值说明：

**返回值**  

```
{
    "code":0,
    "data":{
        "allList":[									// 所有的level集合
            {
                "id":1000005,
                "subjectId":1000000,
                "name":"四级英语",
                "order":null,
                "minWord":200,
                "maxWord":400,
                "image":"images/uploads/2019-05-08/a505eaa1-b0b7-463d-8c92-f13cb02ca2f8.png",
                "introduction":"简介",
                "isOpen":null,
                "buySite":"http://bigbay.qingclasswelearn.com/mall?pageKey=Xs4ovVvH",
                "skin":"grass",
                "isShow":null,
                "levelId":null,
                "beginAt":null,
                "beginDate":null,
                "vipBeginTime":null,
                "vipEndTime":null,
                "vipBeginDate":null,
                "vipEndDate":null,
                "sendLessonDays":null,
                "validDays":null,
                "vouchersCount":null,
                "squirrelUserId":null,
                "switchLevel":null
            }
        ],
        "powerList":[							  // 当前用户已购买的level集合
            {
                "id":1000005,
                "subjectId":1000000,
                "name":"四级英语",
                "order":null,
                "minWord":200,
                "maxWord":400,
                "image":"images/uploads/2019-05-08/a505eaa1-b0b7-463d-8c92-f13cb02ca2f8.png",
                "introduction":"简介",
                "isOpen":null,
                "buySite":"http://bigbay.qingclasswelearn.com/mall?pageKey=Xs4ovVvH",
                "skin":null,
                "isShow":null,
                "levelId":null,
                "beginAt":"2019-09-05",
                "beginDate":1567612800000,
                "vipBeginTime":"2019-09-05",
                "vipEndTime":"2020-09-30",
                "vipBeginDate":1567612800000,
                "vipEndDate":1601395200000,
                "sendLessonDays":null,
                "validDays":null,
                "vouchersCount":null,
                "squirrelUserId":null,
                "switchLevel":false
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