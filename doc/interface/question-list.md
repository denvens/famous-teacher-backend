## 课程的题列表接口
+ 功能说明：课程的题列表

### 历史记录

#### 2019-09-27
- 新增

**公共参数:**
+ 测试接口的地址为 https://famous-teacher-dev.oss-cn-beijing.aliyuncs.com/lessons/lessonkey.json
+ 线上接口的地址为 https://famous-teacher-online.oss-cn-beijing.aliyuncs.com/lessons/lessonkey.json
+ 公共参数 说明： 公共参数需要传递到get或post里面，get请求参数都传到参数中，post请求参数都传到form中。

### 接口信息
|接口调用方式 	|	Restful Service			|
|:--------------|:--------------------------|
|功能	     	| 课程的题列表				|
|接口名称		|/lessons/lessonkey.json   	|
|请求方法		|GET					    |

### 接口公共参数
|参数名		   		|类型	|是否必填	|说明			    					|
|:------------------|:------|:----------|:--------------------------------------|
|lessonkey			|String |是		  	|0152f382-599f-4eca-9514-b913ac890c6a   |

### 接口返回值
+ 返回值数据类型：json
+ 返回值说明：

**返回值**  

```
{
    "data":{
        "audition":0,
        "id":1000256,
        "image":"images/uploads/2019-03-13/e5428d36-e731-4ee2-bc1c-32d4f479132f.png",
        "isOpen":0,
        "levelName":"六级英语",
        "levelid":1000006,
        "name":"test120",
        "order":120,
        "pageNo":0,
        "pageSize":0,
        "part":0,
        "picId":0,
        "questionList":[
            {
                "id":2,
                "lessonId":1000256,
                "order":0,
                "questionData":"{"type":"choice","description":"","options":[{"option":"a"},{"option":"b"},{"option":"c"},{"option":"d"}],"answer":"is","dryType":["图片"],"image":"http://famous-teacher-dev.oss-cn-beijing.aliyuncs.com/images/uploads/2019-09-27/0d6a47b6-3604-4088-bafc-d418117befe2.png","audio":"","video":""}",
                "questionKey":"cd977b3a-bf6b-464b-a4af-5e76c3ea7fbe",
                "questionType":"choice"
            }
        ],
        "relation":0,
        "star":0,
        "title":"hahah",
        "updateDate":1566748800000
    }
}
```

**返回值描述**  

```
content:错误描述
```