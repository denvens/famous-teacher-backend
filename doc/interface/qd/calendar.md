##日历页接口##

请求参数：
{
    "levelId":1 //子频道id  四、六级的id
}

返回值：
{
    denied: false,
    success: true,
    data:{
        courseStartTime:'',//课程开始时间
        serverTime:'',//服务器时间
        courseList:[
            {
                learnType:1, //学习状态： 1:已打卡 2:已学完  
                title:'酒店住宿--早餐', //每日的标题
                ossFileKey:'', //存到oss的文件的key用来请求oss数据
                name:'lesson 10', //课程名字
            },{
                learnType:1, //学习状态： 1:已打卡 2:已学完  
                title:'酒店住宿--早餐', //每日的标题
                ossFileKey:'', //存到oss的文件的key用来请求oss数据
                name:'lesson 10', //课程名字
            },{
                learnType:1, //学习状态： 1:已打卡 2:已学完  
                title:'酒店住宿--早餐', //每日的标题
                ossFileKey:'', //存到oss的文件的key用来请求oss数据
                name:'lesson 10', //课程名字
            }
        ]
        
    }
}
