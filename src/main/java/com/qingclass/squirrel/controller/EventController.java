package com.qingclass.squirrel.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.qingclass.squirrel.domain.PaymentTransactions;
import com.qingclass.squirrel.domain.cms.SquirrelLesson;
import com.qingclass.squirrel.domain.cms.SquirrelLevel;
import com.qingclass.squirrel.domain.cms.SquirrelUnit;
import com.qingclass.squirrel.domain.cms.UserLevel;
import com.qingclass.squirrel.entity.MongoUser;
import com.qingclass.squirrel.entity.ScholarshipApplyFor;
import com.qingclass.squirrel.entity.SessionSquirrelUser;
import com.qingclass.squirrel.entity.SquirrelUser;
import com.qingclass.squirrel.mapper.cms.SquirrelLessonMapper;
import com.qingclass.squirrel.mapper.cms.SquirrelLevelMapper;
import com.qingclass.squirrel.mapper.cms.SquirrelUnitMapper;
import com.qingclass.squirrel.mapper.statistic.UserActionMapper;
import com.qingclass.squirrel.mapper.user.PatchVoucherMapper;
import com.qingclass.squirrel.mapper.user.PaymentTransactionMapper;
import com.qingclass.squirrel.mapper.user.ScholarshipApplyForMapper;
import com.qingclass.squirrel.mapper.user.SquirrelUserMapper;
import com.qingclass.squirrel.utils.MongoDataUtil;
import com.qingclass.squirrel.utils.Tools;

/**
 * 事件类
 * 主要是学习过程中的完成事件之类
 *
 * @author 苏天奇
 * */
@RestController
@RequestMapping(value = "/event")
public class EventController {

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private SquirrelUnitMapper squirrelUnitMapper;
    @Autowired
    private SquirrelLessonMapper squirrelLessonMapper;
    @Autowired
    private SquirrelUserMapper squirrelUserMapper;
    @Autowired
    private SquirrelLevelMapper squirrelLevelMapper;
    @Autowired
    private PatchVoucherMapper patchVoucherMapper;
    @Autowired
    private UserActionMapper userActionMapper;
    @Autowired
	private ScholarshipApplyForMapper scholarshipApplyForMapper;
    @Autowired
	PaymentTransactionMapper paymentTransactionMapper;


    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 触发事件,
     * subjectId
     * levelId
     * lessonId
     * eventType {"convent":完成学习}
     * */
    @PostMapping(value = "lesson-finish-event")
    public Map<String,Object> lessonFinishEvent(HttpServletRequest req){
        
    	logger.info("学习完成接口:");
    	
    	String subjectId = req.getParameter("subjectId")+"";
        String levelId = req.getParameter("levelId")+"";
        String lessonId = req.getParameter("lessonId")+"";
        String eventType = req.getParameter("eventType")+"";
        String order = req.getParameter("order")+"";
        String firstUseTime = req.getParameter("firstUseTime")+"";
        String key = "learnHistory.subjects.subject-"+subjectId+".levels.level-"+levelId+".lessons.lesson-"+lessonId;

        logger.info("学习完成接口参数: subjectId:{}, levelId:{}, lessonId:{}, eventType:{}",
        		subjectId, levelId, lessonId, eventType, order, key);
        
        SessionSquirrelUser sessionUserInfo = (SessionSquirrelUser) req.getSession().getAttribute(SessionSquirrelUser.SESSION_SQUIRREL_USER_KEY);
        
        logger.info("sessionUserInfo:{}",sessionUserInfo);
        
        String openId = sessionUserInfo.getOpenId();
        Integer userId = sessionUserInfo.getId();
        
        logger.info("openId:{}, userId:{}", openId, userId);

        logger.info("完成事件....[param:{levelId:"+levelId+",lessonId:"+lessonId+",eventType:"+eventType+",order:"+order+",openId="+openId+"}]");

        DBObject dbObject = new BasicDBObject();
        ((BasicDBObject) dbObject).put("_id",openId);
        DBObject dbFieldObject = new BasicDBObject();
        ((BasicDBObject) dbFieldObject).put(key,true);



        Map<String,Object> map = new HashMap<>();
        //条件查询
        Query q = new BasicQuery(dbObject,dbFieldObject);
        MongoUser one = mongoTemplate.findOne(q, MongoUser.class);
        
        if(one != null && !one.equals("null")){
        	
        	logger.info("one:{}",one);
        	logger.info("LearnHistory:{}",one.getLearnHistory());
        	Map<String, Object> learnHistory = one.getLearnHistory();
            if(learnHistory!=null &&  !learnHistory.isEmpty()){
                Map<String, Object> lessonValueByKeys = MongoDataUtil.getLessonValueByKeys(one, subjectId, levelId, lessonId);
                if(lessonValueByKeys != null){
                	if(lessonValueByKeys.get("isFinish")!= null && (boolean)lessonValueByKeys.get("isFinish")){	
                    	logger.info("在:{},已经完成学习:{},不再做学习记录。", lessonValueByKeys.get("optTime"), (boolean)lessonValueByKeys.get("isFinish"));
                    	return Tools.s();
                    }
                	map.putAll(lessonValueByKeys);
                }
            }
        }else{
        	Query query = new Query(Criteria.where("_id").is(openId));
    		MongoUser mongoUser = mongoTemplate.findOne(query, MongoUser.class);
    		if(mongoUser==null) {
    			//初始化用户
    			mongoUser = new MongoUser();
    			mongoUser.setId(openId);
    			mongoUser.setUserId(userId);
    			logger.info("openId:{}, userId:{} ", openId, userId);
    			mongoTemplate.insert(mongoUser);
    			logger.info("initUserInfo==mongoTemplate.insert success....");
    		}
        }

        //这里判断是否为当天课程
        //如果是，则渲染isShare
        //如果否，则渲染isFinish
        List<SquirrelUser> squirrelUsers = squirrelUserMapper.selectBeginAtByOpenId(openId, Integer.parseInt(levelId));
        String beginAt = squirrelUsers.get(0).getBeginAt();

        //计算应学天数
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date begin;
        try {
            begin = sdf.parse(beginAt);
        } catch (ParseException e) {
            logger.error("类型转换错误",e);
            return Tools.f("查询开课日期失败");
        }
        Date now = new Date();

        long diff = now.getTime() - begin.getTime();
        long days = (diff / (1000 * 60 * 60 * 24)) + 1; // 应学lesson.order

        int dd = 0;
        logger.info("dd:{}",dd);
        if(!order.equals("null") && !order.equals("")){
            dd = Integer.parseInt(order);
        }
        logger.info("dd-1:{}",dd);
        int b = 0;
        logger.info("eventType:{}",eventType);
        if(eventType.equals("convent")){
        	Date todayDate = new Date(System.currentTimeMillis());
    		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    		String today = simpleDateFormat.format(todayDate);//获
        	
        	logger.info("one:{}",one);
        	logger.info("today:{}, dd:{}, days:{}, 'dd == days':{} ", today, dd, days, dd == days);
            if(dd == days){//学的当天课程
                map.put("isFinish",true);
            }else{
            	map.put("isFinish",false);
            }
            map.put("firstUseTime",firstUseTime);
            
            map.put("optTime",today);
            logger.info("map-------------------->:{}",map);
            //做用户数据的该lesson的初始化
            SquirrelLesson lesson = squirrelLessonMapper.selectByPrimaryKey(Integer.parseInt(lessonId));
            b = lesson.getOrder();
        }else{
            return Tools.f("eventType doesn't exist");
        }
        
        logger.info("map:{}",map);
        
        Update update = new Update().update(key+".record",map);

        try {
            mongoTemplate.upsert(q,update, MongoUser.class);
            if(b > 0){
                Update update2 = new Update().update(key+".record.order",""+b);
                mongoTemplate.upsert(q,update2, MongoUser.class);
            }
            logger.info("update success (mongo)");
        }catch (Exception e){
            logger.error(e.toString());
            logger.error("update failed (mongo)");
        }

        return Tools.s();
    }
    
    /**
     * 触发事件,
     * subjectId
     * levelId
     * lessonId
     * eventType {"convent":完成学习}
     * */
    @PostMapping(value = "online-time-event")
    public Map<String,Object> onlineTimeEvent(HttpServletRequest req){
        
    	logger.info("在线时间接口:");
    	
    	String subjectId = req.getParameter("subjectId")+"";
        String levelId = req.getParameter("levelId")+"";
        String lessonId = req.getParameter("lessonId")+"";
        String order = req.getParameter("order")+"";
        
        int onceTime = Integer.parseInt(req.getParameter("onceTime"));
        String onceTimeIsOver = req.getParameter("onceTimeIsOver")+"";
        logger.info("在线时间接口参数: onceTime:{}, onceTimeIsOver:{}",
        		onceTime, onceTimeIsOver);
        
        
        String key = "learnHistory.subjects.subject-"+subjectId+".levels.level-"+levelId+".lessons.lesson-"+lessonId;

        logger.info("在线时间接口参数: subjectId:{}, levelId:{}, lessonId:{}, eventType:{}, onceTimeIsOver:{}",
        		subjectId, levelId, lessonId, order, key);
        
        SessionSquirrelUser sessionUserInfo = (SessionSquirrelUser) req.getSession().getAttribute(SessionSquirrelUser.SESSION_SQUIRREL_USER_KEY);
        
        logger.info("sessionUserInfo:{}",sessionUserInfo);
        
        String openId = sessionUserInfo.getOpenId();
        Integer userId = sessionUserInfo.getId();
        
        logger.info("openId:{}, userId:{}", openId, userId);

        logger.info("在线时间....[param:{levelId:"+levelId+",lessonId:"+lessonId+",order:"+order+",openId="+openId+"}]");

        DBObject dbObject = new BasicDBObject();
        ((BasicDBObject) dbObject).put("_id",openId);
        DBObject dbFieldObject = new BasicDBObject();
        ((BasicDBObject) dbFieldObject).put(key,true);



        Map<String,Object> map = new HashMap<>();
        //条件查询
        Query q = new BasicQuery(dbObject,dbFieldObject);
        MongoUser one = mongoTemplate.findOne(q, MongoUser.class);
        int allTime = 0; 
        int alreadyOnceTime = 0;
        if(one != null && !one.equals("null")){
        	
        	logger.info("one:{}",one);
        	logger.info("LearnHistory:{}",one.getLearnHistory());
        	Map<String, Object> learnHistory = one.getLearnHistory();
            if(learnHistory!=null &&  !learnHistory.isEmpty()){
                Map<String, Object> lessonValueByKeys = MongoDataUtil.getLessonValueByKeys(one, subjectId, levelId, lessonId);
                if(lessonValueByKeys != null){
                	if(lessonValueByKeys.get("allTime")!=null){
                		allTime = Integer.parseInt((String)lessonValueByKeys.get("allTime"));
                	}
                	if(lessonValueByKeys.get("onceTime")!=null){
                		alreadyOnceTime = Integer.parseInt((String)lessonValueByKeys.get("onceTime"));
                	}
                	map.putAll(lessonValueByKeys);
                }
                
            }
        }else{
        	Query query = new Query(Criteria.where("_id").is(openId));
    		MongoUser mongoUser = mongoTemplate.findOne(query, MongoUser.class);
    		if(mongoUser==null) {
    			//初始化用户
    			mongoUser = new MongoUser();
    			mongoUser.setId(openId);
    			mongoUser.setUserId(userId);
    			logger.info("openId:{}, userId:{} ", openId, userId);
    			mongoTemplate.insert(mongoUser);
    			logger.info("initUserInfo==mongoTemplate.insert success....");
    		}
        }

        //这里判断是否为当天课程
        //如果是，则渲染isShare
        //如果否，则渲染isFinish
        List<SquirrelUser> squirrelUsers = squirrelUserMapper.selectBeginAtByOpenId(openId, Integer.parseInt(levelId));
        String beginAt = squirrelUsers.get(0).getBeginAt();

        //计算应学天数
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date begin;
        try {
            begin = sdf.parse(beginAt);
        } catch (ParseException e) {
            logger.error("类型转换错误",e);
            return Tools.f("查询开课日期失败");
        }
        Date now = new Date();

        long diff = now.getTime() - begin.getTime();
        long days = (diff / (1000 * 60 * 60 * 24)) + 1; // 应学lesson.order

        int b = 0;
    	Date todayDate = new Date(System.currentTimeMillis());
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String today = simpleDateFormat.format(todayDate);//获
    	
    	logger.info("one:{}",one);
    	logger.info("today:{}, days:{}", today,  days);
        map.put("onceTime",onceTime);
        if("1".equals(onceTimeIsOver)){
        	map.put("onceTime","0");
        }else{
        	map.put("onceTime",alreadyOnceTime + onceTime +"");
        }
        map.put("allTime",allTime + onceTime + "");
        map.put("optTimeOnceTime",today);
        
        //做用户数据的该lesson的初始化
        SquirrelLesson lesson = squirrelLessonMapper.selectByPrimaryKey(Integer.parseInt(lessonId));
        b = lesson.getOrder();

        Update update = new Update().update(key+".record",map);


        try {
            mongoTemplate.upsert(q,update, MongoUser.class);
            if(b > 0){
                Update update2 = new Update().update(key+".record.order", ""+b);
                mongoTemplate.upsert(q,update2, MongoUser.class);
            }
            logger.info("update success (mongo)");
        }catch (Exception e){
            logger.error(e.toString());
            logger.error("update failed (mongo)");
        }

        return Tools.s();
    }
    
    /**
     * 触发事件,
     * subjectId
     * levelId
     * lessonId
     * eventType {"convent":完成学习}
     * */
    @PostMapping(value = "/get-once-time")
    public String getOnceTime(HttpServletRequest req){
    	logger.info("在线时间接口:");
    	
    	SessionSquirrelUser sessionUserInfo = (SessionSquirrelUser) req.getSession().getAttribute(SessionSquirrelUser.SESSION_SQUIRREL_USER_KEY);
        logger.info("sessionUserInfo:{}",sessionUserInfo);
        String openId = sessionUserInfo.getOpenId();
        Integer userId = sessionUserInfo.getId();
        logger.info("openId:{}, userId:{}", openId, userId);
    	
    	String subjectId = req.getParameter("subjectId")+"";
        String levelId = req.getParameter("levelId")+"";
        String lessonId = req.getParameter("lessonId")+"";
        
        String key = "learnHistory.subjects.subject-"+subjectId+".levels.level-"+levelId+".lessons.lesson-"+lessonId;

        logger.info("在线时间接口参数: subjectId:{}, levelId:{}, lessonId:{}, eventType:{}",
        		subjectId, levelId, lessonId, key);

        logger.info("在线时间....[param:{levelId:"+levelId+",lessonId:"+lessonId+",openId="+openId+"}]");

        DBObject dbObject = new BasicDBObject();
        ((BasicDBObject) dbObject).put("_id",openId);
        DBObject dbFieldObject = new BasicDBObject();
        ((BasicDBObject) dbFieldObject).put(key,true);

        Map<String,Object> map = new HashMap<>();
        //条件查询
        Query q = new BasicQuery(dbObject,dbFieldObject);
        MongoUser one = mongoTemplate.findOne(q, MongoUser.class);
        int firstUseTime = 0;
        int alreadyOnceTime = 0;
        if(one != null && !one.equals("null")){
        	
        	logger.info("one:{}",one);
        	logger.info("LearnHistory:{}",one.getLearnHistory());
        	Map<String, Object> learnHistory = one.getLearnHistory();
            if(learnHistory!=null &&  !learnHistory.isEmpty()){
                Map<String, Object> lessonValueByKeys = MongoDataUtil.getLessonValueByKeys(one, subjectId, levelId, lessonId);
                if(lessonValueByKeys != null){
                	logger.info("firstUseTime:{}",lessonValueByKeys.get("firstUseTime"));
                	if(lessonValueByKeys.get("firstUseTime")!=null){
                		logger.info("firstUseTime:{}",lessonValueByKeys.get("firstUseTime"));
                		firstUseTime = Integer.parseInt((String)lessonValueByKeys.get("firstUseTime"));
                	}
                	logger.info("onceTime:{}",lessonValueByKeys.get("onceTime"));
                	if(lessonValueByKeys.get("onceTime")!=null){
                		logger.info("onceTime:{}",lessonValueByKeys.get("onceTime"));
                		alreadyOnceTime = Integer.parseInt((String)lessonValueByKeys.get("onceTime"));
                	}
                }
                
            }
        }
        
        JSONObject jsonObject1 = new JSONObject();
        jsonObject1.put("firstUseTime", firstUseTime);
        jsonObject1.put("alreadyOnceTime", alreadyOnceTime);
        
        JSONObject json = new JSONObject();
		json.put("denied",false);
		json.put("success",true);
		json.put("data",jsonObject1);
		return json.toJSONString();
    }

    /**
     * unit完成时点击事件
     * */
    @PostMapping(value = "unit-finish-event")
    public Map<String,Object> unitFinishEvent(HttpServletRequest req){
        String subjectId = req.getParameter("subjectId")+"";
        String levelId = req.getParameter("levelId")+"";
        String lessonId = req.getParameter("lessonId")+"";
        String unitId = req.getParameter("unitId")+"";
        String eventType = req.getParameter("eventType")+"";
        String key = "learnHistory.subjects.subject-"+subjectId+".levels.level-"+levelId+".lessons.lesson-"+lessonId+".units.unit-"+unitId;

        SessionSquirrelUser sessionUserInfo = (SessionSquirrelUser) req.getSession().getAttribute(SessionSquirrelUser.SESSION_SQUIRREL_USER_KEY);
        String openId;
        try{
            openId = sessionUserInfo.getOpenId();
        }catch (NullPointerException e){
            logger.error("openId is null");
            return Tools.f("openId is null");
        }



        DBObject dbObject = new BasicDBObject();
        ((BasicDBObject) dbObject).put("_id",openId);
        DBObject dbFieldObject = new BasicDBObject();
        ((BasicDBObject) dbFieldObject).put(key,true);


        HashMap<String, Object> map = new HashMap<>();
        //条件查询
        Query q = new BasicQuery(dbObject,dbFieldObject);
        MongoUser one = mongoTemplate.findOne(q, MongoUser.class);

        if(one != null && !one.equals("null")){
            if(!one.getLearnHistory().equals("") && !one.getLearnHistory().equals("null") && one.getLearnHistory() != null){
                Map<String, Object> lessonValueByKeys = MongoDataUtil.getUnitValueByKeys(one, subjectId, levelId, lessonId, unitId);
                if(lessonValueByKeys != null){
                    map.putAll(lessonValueByKeys);
                }

            }
        }


        String[] split = eventType.split(",");
        for(int i = 0 ; i < split.length ; i ++){//循环事件类型，push相应value
            String a = split[i];
            if(a.equals("finish")){
                map.put("isFinish",true);
            }else if(a.contains("useTime")){
                map.put("useTime",a.split("-")[1]);
            }else if(a.contains("accuracy")){
                map.put("accuracy",a.split("-")[1]);
            }else if(a.contains("score")){
                map.put("score",a.split("-")[1]);
            }else if(a.contains("star")){
                map.put("star",a.split("-")[1]);
            }else{
                return Tools.f("eventType doesn't exist");
            }
        }
        SquirrelUnit squirrelUnit = squirrelUnitMapper.selectByPrimaryKey(Integer.parseInt(unitId));
        map.put("name", squirrelUnit.getName());
        map.put("id", squirrelUnit.getId());

        Update update = new Update().update(key+".record",map);
        try {
            mongoTemplate.updateFirst(q,update, MongoUser.class);
            logger.info("update success (mongo)");
        }catch (Exception e){
            logger.error(e.toString());
            logger.error("update failed (mongo)");
        }

        return Tools.s();

    }

    @PostMapping(value = "bz-action")
    public Map<String,Object> bzAction(HttpServletRequest req) {
        SessionSquirrelUser sessionUserInfo = (SessionSquirrelUser) req.getSession().getAttribute(SessionSquirrelUser.SESSION_SQUIRREL_USER_KEY);
        String openId = sessionUserInfo.getOpenId();

        int i = userActionMapper.selectBzActionCount(openId);
        if(i > 0){
            return Tools.s();
        }
        userActionMapper.insertBzAction(openId);
        return Tools.s();
    }

    @PostMapping(value = "judge-power-event")
    public Map<String,Object> judgePowerEvent(HttpServletRequest req) {

        String lessonKey = req.getParameter("lessonKey");

        String levelId = squirrelLessonMapper.selectByLessonKey(lessonKey);

        SessionSquirrelUser sessionUserInfo = (SessionSquirrelUser) req.getSession().getAttribute(SessionSquirrelUser.SESSION_SQUIRREL_USER_KEY);

        Integer id = null;
        try{
            id = sessionUserInfo.getId();
        }catch (NullPointerException e){
            logger.error("用户登出");
        }


        if(levelId == null ){
            return Tools.f();
        }

        List<SquirrelUser> squirrelUsers = squirrelUserMapper.selectByLevelIdAndNowDate(Integer.parseInt(levelId), id);

        if(squirrelUsers != null && squirrelUsers.size() > 0){
            return Tools.s();
        }else{
            SquirrelLevel squirrelLevel;
            try{
                squirrelLevel = squirrelLevelMapper.selectByPrimaryKey(Integer.parseInt(levelId));
                return Tools.f(squirrelLevel.getBuySite());
            }catch (NullPointerException e){
                logger.warn("非法登陆，失效的url");
            }
            return Tools.f();
        }
    }


    
    
    
    
    
    
    @PostMapping(value = "lesson-finish-event-Repair-Data")
    public Map<String,Object> lessonFinishEventRepairData(){
        
    	
    	List<SquirrelUser> userList = squirrelUserMapper.selectByBeginAt("2019-11-04");
    	logger.info("共有userList:{}",userList.size());
    	for(int i=0; i<userList.size(); i++){
    		logger.info("共{}条, 当前第{}条开始------>",userList.size(), i);
    		SquirrelUser squirrelUser = userList.get(i);
    	
	    	logger.info("学习完成接口:");
	    	
	    	String subjectId = "1000000";
	        String levelId = squirrelUser.getLevelId()+"";
	        String lessonId = "";
	        if(levelId.equals("1000047")){
	        	lessonId = "1000547";
	        }
	        if(levelId.equals("1000048")){
	        	lessonId = "1000580";
	        }
	        
	        
	        String eventType = "convent";
	        String order = "1";
	        
	        String key = "learnHistory.subjects.subject-"+subjectId+".levels.level-"+levelId+".lessons.lesson-"+lessonId;
	
	        logger.info("学习完成接口参数: subjectId:{}, levelId:{}, lessonId:{}, eventType:{}",
	        		subjectId, levelId, lessonId, eventType, order, key);
	        
//	        SessionSquirrelUser sessionUserInfo = (SessionSquirrelUser) req.getSession().getAttribute(SessionSquirrelUser.SESSION_SQUIRREL_USER_KEY);
	        
//	        logger.info("sessionUserInfo:{}",sessionUserInfo);
	        
	        String openId = squirrelUser.getOpenId();
	        Integer userId = squirrelUser.getUserId();
	        
	        logger.info("openId:{}, userId:{}", openId, userId);
	
	        logger.info("完成事件....[param:{levelId:"+levelId+",lessonId:"+lessonId+",eventType:"+eventType+",order:"+order+",openId="+openId+"}]");
	
	        DBObject dbObject = new BasicDBObject();
	        ((BasicDBObject) dbObject).put("_id",openId);
	        DBObject dbFieldObject = new BasicDBObject();
	        ((BasicDBObject) dbFieldObject).put(key,true);
	
	
	
	        Map<String,Object> map = new HashMap<>();
	        //条件查询
	        Query q = new BasicQuery(dbObject,dbFieldObject);
	        MongoUser one = mongoTemplate.findOne(q, MongoUser.class);
	        
	        if(one != null && !one.equals("null")){
	        	
	        	logger.info("one:{}",one);
	        	logger.info("LearnHistory:{}",one.getLearnHistory());
	        	Map<String, Object> learnHistory = one.getLearnHistory();
	            if(learnHistory!=null &&  !one.getLearnHistory().isEmpty()){
	                Map<String, Object> lessonValueByKeys = MongoDataUtil.getLessonValueByKeys(one, subjectId, levelId, lessonId);
	                if(lessonValueByKeys != null){
	                    map.putAll(lessonValueByKeys);
	                }
	            }
	        }else{
	        	Query query = new Query(Criteria.where("_id").is(openId));
	    		MongoUser mongoUser = mongoTemplate.findOne(query, MongoUser.class);
	    		if(mongoUser==null) {
	    			//初始化用户
	    			mongoUser = new MongoUser();
	    			mongoUser.setId(openId);
	    			mongoUser.setUserId(userId);
	    			logger.info("openId:{}, userId:{} ", openId, userId);
	    			mongoTemplate.insert(mongoUser);
	    			logger.info("initUserInfo==mongoTemplate.insert success....");
	    		}
	        }
	
	        //这里判断是否为当天课程
	        //如果是，则渲染isShare
	        //如果否，则渲染isFinish
	        List<SquirrelUser> squirrelUsers = squirrelUserMapper.selectBeginAtByOpenId(openId, Integer.parseInt(levelId));
	        String beginAt = squirrelUsers.get(0).getBeginAt();
	
	        //计算应学天数
	        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	        Date begin;
	        try {
	            begin = sdf.parse(beginAt);
	        } catch (ParseException e) {
	            logger.error("类型转换错误",e);
	            return Tools.f("查询开课日期失败");
	        }
	        Date now = new Date();
	
	        long diff = now.getTime() - begin.getTime();
	        long days = (diff / (1000 * 60 * 60 * 24)) + 1; // 应学lesson.order
	
	        int dd = 0;
	        logger.info("dd:{}",dd);
	        if(!order.equals("null") && !order.equals("")){
	            dd = Integer.parseInt(order);
	        }
	        logger.info("dd-1:{}",dd);
	        int b = 0;
	        logger.info("eventType:{}",eventType);
	        if(eventType.equals("convent")){
	        	
	        	Date yesterdayDate = new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24);
	    		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    		String yesterday = simpleDateFormat.format(yesterdayDate);//获
	        	
	        	logger.info("one:{}",one);
	        	logger.info("dd:{}, days:{}, 'dd == days':{} ", dd, days, dd == days);
	        	map.put("isFinish",true);
                map.put("optTime",yesterday);
//	            if(dd == days){//学的当天课程
//	                map.put("isFinish",true);
//	                map.put("optTime",new Date());
//	            }else{
//	            	map.put("isFinish",false);
//	            	map.put("optTime",new Date());
//	            }
	        }else{
	            return Tools.f("eventType doesn't exist");
	        }
	
	        Update update = new Update().update(key+".record",map);
	
	
	        try {
	            mongoTemplate.upsert(q,update, MongoUser.class);
	            if(b > 0){
	                Update update2 = new Update().update(key+".record.order",b);
	                mongoTemplate.upsert(q,update2, MongoUser.class);
	            }
	            logger.info("update success (mongo)");
	        }catch (Exception e){
	            logger.error(e.toString());
	            logger.error("update failed (mongo)");
	        }
	        logger.info("共{}条, 当前第{}条结束----->",userList.size(), i);
    	}

        return Tools.s();
    }
    
    
    public static void main(String[] args){
    	Date today = new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24);

		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String yesterday = simpleDateFormat.format(today);//获
		System.out.println(yesterday);
		
    }
    
    
    /**
     * levelId:
     * lessonId:
     * order:
     * openId:
     * date:
     * 
     * @param req
     * @return
     */
    @PostMapping(value = "repair-learning-record")
    public Map<String,Object> repairLearningRecord(HttpServletRequest req){
        
    	logger.info("learning-record:...");
    	
    	String levelId = req.getParameter("levelId")+"";
        String lessonId = req.getParameter("lessonId")+"";
        String order = req.getParameter("order")+"";
        String openId = req.getParameter("openId")+"";
        String date = req.getParameter("date")+"";
    	
    	String subjectId = "1000000";
    	String eventType = "convent";
        
        String key = "learnHistory.subjects.subject-"+subjectId+".levels.level-"+levelId+".lessons.lesson-"+lessonId;

        logger.info("学习完成接口参数: subjectId:{}, levelId:{}, lessonId:{}, eventType:{}",
        		subjectId, levelId, lessonId, eventType, order, key);
        
        
        SquirrelUser squirrelUser = squirrelUserMapper.selectByOpenId(openId);
        Integer userId = squirrelUser.getId();
        
        logger.info("openId:{}, userId:{}", openId, userId);

        logger.info("完成事件....[param:{levelId:"+levelId+",lessonId:"+lessonId+",eventType:"+eventType+",order:"+order+",openId="+openId+"}]");

        DBObject dbObject = new BasicDBObject();
        ((BasicDBObject) dbObject).put("_id",openId);
        DBObject dbFieldObject = new BasicDBObject();
        ((BasicDBObject) dbFieldObject).put(key,true);



        Map<String,Object> map = new HashMap<>();
        //条件查询
        Query q = new BasicQuery(dbObject,dbFieldObject);
        MongoUser one = mongoTemplate.findOne(q, MongoUser.class);
        
        if(one != null && !one.equals("null")){
        	
        	logger.info("one:{}",one);
        	logger.info("LearnHistory:{}",one.getLearnHistory());
        	Map<String, Object> learnHistory = one.getLearnHistory();
            if(learnHistory!=null &&  !one.getLearnHistory().isEmpty()){
                Map<String, Object> lessonValueByKeys = MongoDataUtil.getLessonValueByKeys(one, subjectId, levelId, lessonId);
                if(lessonValueByKeys != null){
                	if(lessonValueByKeys.get("isFinish")!= null && (boolean)lessonValueByKeys.get("isFinish")){	
                    	logger.info("在:{},已经完成学习:{},不再做学习记录。", lessonValueByKeys.get("optTime"), (boolean)lessonValueByKeys.get("isFinish"));
                    	return Tools.s();
                    }
                	map.putAll(lessonValueByKeys);
                }
                
            }
        }else{
        	Query query = new Query(Criteria.where("_id").is(openId));
    		MongoUser mongoUser = mongoTemplate.findOne(query, MongoUser.class);
    		if(mongoUser==null) {
    			//初始化用户
    			mongoUser = new MongoUser();
    			mongoUser.setId(openId);
    			mongoUser.setUserId(userId);
    			logger.info("openId:{}, userId:{} ", openId, userId);
    			mongoTemplate.insert(mongoUser);
    			logger.info("initUserInfo==mongoTemplate.insert success....");
    		}
        }

        //这里判断是否为当天课程
        //如果是，则渲染isShare
        //如果否，则渲染isFinish
        List<SquirrelUser> squirrelUsers = squirrelUserMapper.selectBeginAtByOpenId(openId, Integer.parseInt(levelId));
        String beginAt = squirrelUsers.get(0).getBeginAt();

        //计算应学天数
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date begin;
        try {
            begin = sdf.parse(beginAt);
        } catch (ParseException e) {
            logger.error("类型转换错误",e);
            return Tools.f("查询开课日期失败");
        }
        Date now = new Date();

        long diff = now.getTime() - begin.getTime();
        long days = (diff / (1000 * 60 * 60 * 24)) + 1; // 应学lesson.order

        int dd = 0;
        logger.info("dd:{}",dd);
        if(!order.equals("null") && !order.equals("")){
            dd = Integer.parseInt(order);
        }
        logger.info("dd-1:{}",dd);
        int b = 0;
        logger.info("eventType:{}",eventType);
        if(eventType.equals("convent")){
//        	Date todayDate = new Date(System.currentTimeMillis());
//    		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//    		String today = simpleDateFormat.format(todayDate);//获
        	
        	logger.info("one:{}",one);
//        	logger.info("today:{}, dd:{}, days:{}, 'dd == days':{} ", today, dd, days, dd == days);
//            if(dd == days){//学的当天课程
//                map.put("isFinish",true);
//                map.put("optTime",today);
//            }else{
//            	map.put("isFinish",false);
//            	map.put("optTime",today);
//            }
        	
        	map.put("isFinish",true);
            map.put("optTime",date);
            //做用户数据的该lesson的初始化
            SquirrelLesson lesson = squirrelLessonMapper.selectByPrimaryKey(Integer.parseInt(lessonId));
            b = lesson.getOrder();
        }else{
            return Tools.f("eventType doesn't exist");
        }

        Update update = new Update().update(key+".record",map);


        try {
            mongoTemplate.upsert(q,update, MongoUser.class);
            if(b > 0){
                Update update2 = new Update().update(key+".record.order",b);
                mongoTemplate.upsert(q,update2, MongoUser.class);
            }
            logger.info("update success (mongo)");
        }catch (Exception e){
            logger.error(e.toString());
            logger.error("update failed (mongo)");
        }
        logger.info("learning-record:end");
        return Tools.s();
    }
    
    @PostMapping(value = "lesson-finish-rate")
    public Map<String,Object> lessonFinishRate(){
    	Map<String, Object> returnMap = new HashMap<>();
    	List<SquirrelLevel> levelList = squirrelLevelMapper.selectSquirrelLevel();
    	for(SquirrelLevel squirrelLevel : levelList){	// level---------------------------------------------------------------------------- 	
    		//取出该level下所有的用户
    		List<SquirrelUser> squirrelUserList = squirrelUserMapper.selectByLevelId(squirrelLevel.getId());
    		logger.info("level:{},共有:{} 用户",squirrelLevel.getName(),squirrelUserList.size());
    		int allCount = squirrelUserList.size();
    		int studyCount = 0;
    		String subjectId = squirrelLevel.getSubjectId()+"";
	        String levelId = squirrelLevel.getId()+"";
    		
	        SquirrelLesson lesson = new SquirrelLesson();
	        lesson.setLevelid(squirrelLevel.getId());

	        List<SquirrelLesson> lessonList = squirrelLessonMapper.selectBy(lesson);
	        Map<Integer,SquirrelLesson> lessonMap = getLessonMap(lessonList);
	        
    		for(int i=0; i<squirrelUserList.size(); i++){ // user----------------------------------------------------------------------------
    			
    			boolean isAllStudy = false;
    			
    			
    			SquirrelUser squirrelUser = squirrelUserList.get(i);
    			
    			logger.info("当前第{}条开始------>",squirrelUserList.size(), i+1);
    			
    	        String key = "learnHistory.subjects.subject-"+subjectId+".levels.level-"+levelId+".lessons";
    	
    	        logger.info("subjectId:{}, levelId:{}, eventType:{}",
    	        		subjectId, levelId, key);
    	        
    	        
    	        String openId = squirrelUser.getOpenId();
    	        Integer userId = squirrelUser.getUserId();
    	        
    	        logger.info("openId:{}, userId:{}", openId, userId);
    	        
    	        logger.info("当前第{}位, 用户昵称:{}, levelId:{}, openId:{}", i+1, squirrelUser.getNickName(), levelId, openId );
    	        DBObject dbObject = new BasicDBObject();
    	        ((BasicDBObject) dbObject).put("_id",openId);
    	        DBObject dbFieldObject = new BasicDBObject();
    	        ((BasicDBObject) dbFieldObject).put(key,true);
    	
    	        //条件查询
    	        Query q = new BasicQuery(dbObject,dbFieldObject);
    	        MongoUser one = mongoTemplate.findOne(q, MongoUser.class);
    	        
    	        if(one != null && !one.equals("null")){
    	        	
    	        	logger.info("one:{}",one);
    	        	logger.info("学习记录：LearnHistory:{}",one.getLearnHistory());
    	        	Map<String, Object> learnHistory = one.getLearnHistory();
    	            if(learnHistory!=null &&  !learnHistory.isEmpty()){
    	            	
    	                Map<String, Object> lessons = MongoDataUtil.getLevelValueByKeys(one, subjectId, levelId);
    	                if(lessons !=null){
		                    //计算应学天数
		                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		                    Date begin;
		                    int days = 0;
		                    int studyDays = 0;
		                    try {
		                    	String beginAt = squirrelUser.getBeginAt();
		                        begin = sdf.parse(beginAt);
		                        Date now = new Date();
		                        long diff = now.getTime() - begin.getTime();
	    	                    long longDays = (diff / (1000 * 60 * 60 * 24)) + 1; // 应学lesson.order
	    	                    days = Integer.parseInt(String.valueOf(longDays));
		                    } catch (ParseException e) {
		                        logger.error("类型转换错误",e);
		                    }
		                    
		                    for(int y=1; y<=days; y++){
		                    	SquirrelLesson squirrelLesson = lessonMap.get(y);
	                        	Map<String, Object> lessonValueByKeys = MongoDataUtil.getLessonValueByKeys(one, subjectId, levelId, ""+squirrelLesson.getId());
	                        	if(lessonValueByKeys != null){
	                        		logger.info("order---> levelId:{}, lessonId:{}, order:{}", lessonValueByKeys.get("isFinish"), squirrelLesson.getId(), lessonValueByKeys.get("order"));
	                            	if(lessonValueByKeys.get("isFinish")!= null && (boolean)lessonValueByKeys.get("isFinish")){	
	                            		studyDays = studyDays + 1;
	                                }else{
	                                	break;
	                                }
	                            	
	                            }
		                    }
		                    logger.info("openId:{},应学:{}天, 实际学:{}", openId, days, studyDays);
		                    if(studyDays==days){
		                    	isAllStudy = true;
		                    }else{
		                    	isAllStudy = false;
		                    }
    	                }else{
    	                	isAllStudy = false;
    	                	logger.info("level:{},用户:{},openId:{},lessons为空！",
                	        		squirrelLevel.getName(), squirrelUser.getNickName(),squirrelUser.getOpenId());
    	                }
    	            }else{
    	            	isAllStudy = false;
    	            	logger.info("level:{},用户:{},openId:{},无学习记录！",
            	        		squirrelLevel.getName(), squirrelUser.getNickName(),squirrelUser.getOpenId());
    	            }
    	        }else{
    	        	isAllStudy = false;
    	        	logger.info("level:{},用户:{},openId:{},无mongo记录！",
        	        		squirrelLevel.getName(), squirrelUser.getNickName(),squirrelUser.getOpenId());
    	        }
    	        logger.info("level:{},用户:{},openId:{},是否全勤:{}",
    	        		squirrelLevel.getName(), squirrelUser.getNickName(),squirrelUser.getOpenId(),isAllStudy);
    	        //判断该用户是否全勤
    	        if(isAllStudy){
    	        	studyCount = studyCount + 1;
    	        }
    		
    		}    
    		//得到该level下的全勤用户数
    		logger.info("结果：level:{},共有用户:{},全勤用户:{}",squirrelLevel.getName(),allCount,studyCount);
    		returnMap.put(squirrelLevel.getName(), "共有用户:"+allCount+ ", 全勤用户:" +studyCount);
    	}
        return Tools.s(returnMap);
    }
    
    public static Map<Integer,SquirrelLesson> getLessonMap(List<SquirrelLesson> userlist){
    	Map<Integer,SquirrelLesson> userMap = new HashMap<Integer,SquirrelLesson>();
    	for(int i=0; i<userlist.size(); i++){
    		userMap.put(userlist.get(i).getOrder(), userlist.get(i));
    	}
    	return userMap;
    }
    
    
    //=============================================================================================================
    @PostMapping(value = "repair-lesson-order")
    public Map<String,Object> repairLessonOrder(){
    	Map<String, Object> returnMap = new HashMap<>();
    	List<SquirrelLevel> levelList = squirrelLevelMapper.selectSquirrelLevel();
    	for(SquirrelLevel squirrelLevel : levelList){	// level---------------------------------------------------------------------------- 	
    		//取出该level下所有的用户
    		List<SquirrelUser> squirrelUserList = squirrelUserMapper.selectByLevelId(squirrelLevel.getId());
    		
//    		List<SquirrelUser> squirrelUserList = squirrelUserMapper.selectByLevelIdAndUserId(1000048, 100684);
    		
    		logger.info("level:{},共有:{} 用户",squirrelLevel.getName(),squirrelUserList.size());
    		int allCount = squirrelUserList.size();
    		int studyCount = 0;
    		String subjectId = squirrelLevel.getSubjectId()+"";
	        String levelId = squirrelLevel.getId()+"";
    		
	        SquirrelLesson lesson = new SquirrelLesson();
	        lesson.setLevelid(squirrelLevel.getId());

	        List<SquirrelLesson> lessonList = squirrelLessonMapper.selectBy(lesson);
	        Map<String,SquirrelLesson> lessonMap = getLessonMapOrder(lessonList);
	        
    		for(int i=0; i<squirrelUserList.size(); i++){ // user----------------------------------------------------------------------------
    			
    			boolean isAllStudy = false;
    			
    			
    			SquirrelUser squirrelUser = squirrelUserList.get(i);
    			
    			logger.info("当前第{}条开始------>",squirrelUserList.size(), i+1);
    			
    	        String key = "learnHistory.subjects.subject-"+subjectId+".levels.level-"+levelId+".lessons";
    	
    	        logger.info("subjectId:{}, levelId:{}, eventType:{}",
    	        		subjectId, levelId, key);
    	        
    	        
    	        String openId = squirrelUser.getOpenId();
    	        Integer userId = squirrelUser.getUserId();
    	        
    	        logger.info("openId:{}, userId:{}", openId, userId);
    	        
    	        logger.info("当前第{}位, 用户昵称:{}, levelId:{}, openId:{}", i+1, squirrelUser.getNickName(), levelId, openId );
    	        DBObject dbObject = new BasicDBObject();
    	        ((BasicDBObject) dbObject).put("_id",openId);
    	        DBObject dbFieldObject = new BasicDBObject();
    	        ((BasicDBObject) dbFieldObject).put(key,true);
    	
    	        //条件查询
    	        Query q = new BasicQuery(dbObject,dbFieldObject);
    	        MongoUser one = mongoTemplate.findOne(q, MongoUser.class);
    	        
    	        if(one != null && !one.equals("null")){
    	        	
    	        	logger.info("one:{}",one);
    	        	logger.info("学习记录：LearnHistory:{}",one.getLearnHistory());
    	        	Map<String, Object> learnHistory = one.getLearnHistory();
    	            if(learnHistory!=null &&  !learnHistory.isEmpty()){
    	            	
    	                Map<String, Object> lessons = MongoDataUtil.getLevelValueByKeysForOrder(one, subjectId, levelId);
    	                if(lessons !=null){
		                    //计算应学天数
		                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		                    Date begin;
		                    int days = 0;
		                    int studyDays = 0;
		                    try {
		                    	String beginAt = squirrelUser.getBeginAt();
		                        begin = sdf.parse(beginAt);
		                        Date now = new Date();
		                        long diff = now.getTime() - begin.getTime();
	    	                    long longDays = (diff / (1000 * 60 * 60 * 24)) + 1; // 应学lesson.order
	    	                    days = Integer.parseInt(String.valueOf(longDays));
		                    } catch (ParseException e) {
		                        logger.error("类型转换错误",e);
		                    }
		                    
		                    for(int y=1; y<=days; y++){
		                    	logger.info("用户:{}, levelId:{},第{}天", openId, levelId, y );
		                    	SquirrelLesson squirrelLesson = lessonMap.get(y+"");
		                    	if(squirrelLesson!=null){
		                        	Map<String, Object> lessonValueByKeys = MongoDataUtil.getLessonValueByKeys(one, subjectId, levelId, ""+squirrelLesson.getId());
		                        	if(lessonValueByKeys != null){
		                        		
		                        		logger.info("order---> levelId:{}, lessonId:{}, order:{}", lessonValueByKeys.get("isFinish"), squirrelLesson.getId(), lessonValueByKeys.get("order"));
	//	                            	if(lessonValueByKeys.get("isFinish")!= null && (boolean)lessonValueByKeys.get("isFinish")){	
	//	                            		studyDays = studyDays + 1;
	//	                                }else{
	//	                                	break;
	//	                                }
		                        		
		                        		SquirrelLesson lessonOrder = squirrelLessonMapper.selectByPrimaryKey(squirrelLesson.getId());
		                        		String order = lessonOrder.getOrder().toString();
		                        		logger.info("order:{}, key:{}", order, key+".lesson-"+squirrelLesson.getId()+".record.order");
		                                try {
	                                        Update update2 = new Update().update(key+".lesson-"+squirrelLesson.getId()+".record.order",order);
	                                        mongoTemplate.upsert(q,update2, MongoUser.class);
		                                    logger.info("update success (mongo)");
		                                }catch (Exception e){
		                                    logger.error(e.toString());
		                                    logger.error("update failed (mongo)");
		                                }
		                                logger.info("order:end");
		                                
		                            }
		                    	}
		                    }
		                    logger.info("openId:{},应学:{}天, 实际学:{}", openId, days, studyDays);
		                    if(studyDays==days){
		                    	isAllStudy = true;
		                    }else{
		                    	isAllStudy = false;
		                    }
    	                }else{
    	                	isAllStudy = false;
    	                	logger.info("level:{},用户:{},openId:{},lessons为空！",
                	        		squirrelLevel.getName(), squirrelUser.getNickName(),squirrelUser.getOpenId());
    	                }
    	            }else{
    	            	isAllStudy = false;
    	            	logger.info("level:{},用户:{},openId:{},无学习记录！",
            	        		squirrelLevel.getName(), squirrelUser.getNickName(),squirrelUser.getOpenId());
    	            }
    	        }else{
    	        	isAllStudy = false;
    	        	logger.info("level:{},用户:{},openId:{},无mongo记录！",
        	        		squirrelLevel.getName(), squirrelUser.getNickName(),squirrelUser.getOpenId());
    	        }
    	        logger.info("level:{},用户:{},openId:{},是否全勤:{}",
    	        		squirrelLevel.getName(), squirrelUser.getNickName(),squirrelUser.getOpenId(),isAllStudy);
    	        //判断该用户是否全勤
    	        if(isAllStudy){
    	        	studyCount = studyCount + 1;
    	        }
    		
    		}    
    		//得到该level下的全勤用户数
    		logger.info("结果：level:{},共有用户:{},全勤用户:{}",squirrelLevel.getName(),allCount,studyCount);
    		returnMap.put(squirrelLevel.getName(), "共有用户:"+allCount+ ", 全勤用户:" +studyCount);
    	}
        return Tools.s(returnMap);
    }
    
    public static Map<String,SquirrelLesson> getLessonMapOrder(List<SquirrelLesson> userlist){
    	Map<String,SquirrelLesson> userMap = new HashMap<String,SquirrelLesson>();
    	for(int i=0; i<userlist.size(); i++){
    		userMap.put(userlist.get(i).getOrder()+"", userlist.get(i));
    	}
    	return userMap;
    }
    
    
    @PostMapping("/repair-amount")
	public Map<String,Object> scholarshipApplyFor(){
    	List<ScholarshipApplyFor> applyForList = scholarshipApplyForMapper.selectApplyForList();
    	for(int i=0; i<applyForList.size(); i++){
    		ScholarshipApplyFor scholarship = applyForList.get(i);
    		String amount = scholarship.getAmount();
    		
    		SquirrelUser su = squirrelUserMapper.selectByOpenId(scholarship.getScholarshipOpenId());
    		
    		UserLevel userLevel = new UserLevel();
    		userLevel.setSquirrelUserId(su.getId());
    		userLevel.setLevelId(scholarship.getLevelId());
    		PaymentTransactions p = paymentTransactionMapper.selectTotalFeeByUserIdAndLevelId(userLevel);
    		if(p!=null && !"0".equals(p.getTotalFee())){
    			ScholarshipApplyFor s = new ScholarshipApplyFor();
    			s.setId(scholarship.getId());
    			s.setAmount(p.getTotalFee());
    			s.setBigbayTranctionId(p.getBigbayTranctionnId());
        		scholarshipApplyForMapper.updateScholarshipApplyFor(s);
        		logger.info("共有申请用户:{}, 当前第 {} 用户:{}, 奖学金由:{},修改为:{}", 
        				applyForList.size(), i, scholarship.getScholarshipOpenId(), amount, p.getTotalFee());
    		}
    		
    	}
		return null;
	}
    
}