## 学习完成提交接口
+ 功能说明：学习完成提交

### 历史记录

#### 2019-09-30
- 新增

**公共参数:**
+ 测试接口的地址为 https://test-msyb-api.qingclasswelearn.com/event/lesson-finish-event
+ 线上接口的地址为 https://msyb-api.qingclasswelearn.com/user/event/lesson-finish-event
+ 公共参数 说明： 公共参数需要传递到get或post里面，get请求参数都传到参数中，post请求参数都传到form中。

### 接口信息
|接口调用方式 	|	Restful Service			|
|:--------------|:--------------------------|
|功能	     	| 学习完成提交				|
|接口名称		|/event/lesson-finish-event |
|请求方法		|POST					    |

### 接口公共参数
|参数名		   		|类型	|是否必填	|说明			    					|
|:------------------|:------|:----------|:--------------------------------------|
|subjectId			|Integer|是		  	|1000000, 频道ID，固定				    |
|levelId			|Integer|是		  	|1000005, 子频道Id					    |
|lessonId			|Integer|是		  	|1000050, 课程Id							|
|eventType			|String |是		  	|事件类型。convent：完成学习			    |
|order				|String |是		  	|排序值								    |
|firstUseTime 		|String |是		  	|学习耗时							    |
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