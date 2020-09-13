## 准考证列表接口
+ 功能说明：准考证列表

### 历史记录

#### 2019-11-22 
- 新增

**公共参数:**
+ 测试接口的地址为 https://test-msyb-cms-api.qingclasswelearn.com/certificate/list
+ 线上接口的地址为 https://msyb-cms-api.qingclasswelearn.com/certificate/list
+ 公共参数 说明： 公共参数需要传递到get或post里面，get请求参数都传到参数中，post请求参数都传到form中。

### 接口信息
|接口调用方式 	|	Restful Service			|
|:--------------|:--------------------------|
|功能	     	| 准考证列表					|
|接口名称		|/certificate/list			|
|请求方法		|POST					    |

### 接口公共参数
|参数名		   		|类型	|是否必填	|说明			    					|
|:------------------|:------|:----------|:--------------------------------------|
|					|		|		  	|										|  

### 接口返回值
+ 返回值数据类型：json
+ 返回值说明：

**返回值**  

```
{
    "pageTotal":25,
    "data":{
        "certificateList":[
            {
                "id":1000137,
                "openId":"o9ghnw_Ojj_7khi1fu_rw8_lw1fc",							//	openId
                "classify":1,	// 准考证类型:1,大学英语四级考试(CET-4);2,大学英语六级考试(CET-6)
                "number":"111234132412341234",
                "createDate":1568217600000				//	创建时间
                "updateDate":1568217600000				// 	更新时间
            }
        ]
    },
    "success":true,
    "denied":false
}
```

**返回值描述**  

```
content:错误描述
```