## 在线时长提交接口
+ 功能说明：在线时长提交

### 历史记录

#### 2019-11-22
- 新增

**公共参数:**
+ 测试接口的地址为 https://test-msyb-api.qingclasswelearn.com/event/get-once-time
+ 线上接口的地址为 https://msyb-api.qingclasswelearn.com/user/event/get-once-time
+ 公共参数 说明： 公共参数需要传递到get或post里面，get请求参数都传到参数中，post请求参数都传到form中。

### 接口信息
|接口调用方式 	|	Restful Service			|
|:--------------|:--------------------------|
|功能	     	| 在线时长提交				|
|接口名称		|/event/get-once-time		|
|请求方法		|POST					    |

### 接口公共参数
|参数名		   		|类型	|是否必填	|说明			    					|
|:------------------|:------|:----------|:--------------------------------------|
|subjectId			|Integer|是		  	|1000000, 频道ID，固定				    |
|levelId			|Integer|是		  	|1000005, 子频道Id					    |
|lessonId			|Integer|是		  	|1000050, 课程Id							|

### 接口返回值
+ 返回值数据类型：json
+ 返回值说明：

**返回值**  

```
{
    "code": 0,
    "data": {
    	"firstUseTime": "1800",
    	"alreadyOnceTime": "1800"
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