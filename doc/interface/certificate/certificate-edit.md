## 准考证修改接口
+ 功能说明：准考证修改

### 历史记录

#### 2019-11-21 
- 新增

**公共参数:**
+ 测试接口的地址为 https://test-msyb-cms-api.qingclasswelearn.com/certificate/edit
+ 线上接口的地址为 https://msyb-cms-api.qingclasswelearn.com/certificate/edit
+ 公共参数 说明： 公共参数需要传递到get或post里面，get请求参数都传到参数中，post请求参数都传到form中。

### 接口信息
|接口调用方式 	|	Restful Service			|
|:--------------|:--------------------------|
|功能	     	| 准考证修改					|
|接口名称		|/certificate/edit			|
|请求方法		|POST					    |

### 接口公共参数
|参数名		   		|类型	|是否必填	|说明			    					|
|:------------------|:------|:----------|:--------------------------------------|
|id					|Integer|是		  	|准考证ID								|
|classify			|Integer|是		  	|准考证类型:1,大学英语四级考试(CET-4);2,大学英语六级考试(CET-6)	|
|number				|String |是		  	|准考证号								|

### 接口返回值
+ 返回值数据类型：json
+ 返回值说明：

**返回值**  

```
{
    "denied": false,
    "success": true,
    "data": {}
}
```

**返回值描述**  

```
content:错误描述
```