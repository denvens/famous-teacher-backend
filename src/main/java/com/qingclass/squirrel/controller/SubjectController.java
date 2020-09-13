package com.qingclass.squirrel.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DuplicateKeyException;
import com.qingclass.squirrel.entity.SquirrelUser;
import com.qingclass.squirrel.mapper.user.SquirrelUserMapper;
import com.qingclass.squirrel.service.SquirrelLessonService;
import com.qingclass.squirrel.service.SquirrelLevelService;
import com.qingclass.squirrel.service.SquirrelPicturebookService;
import com.qingclass.squirrel.utils.MongoDataUtil;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.web.bind.annotation.*;

import com.qingclass.squirrel.domain.cms.SquirrelLesson;
import com.qingclass.squirrel.domain.cms.SquirrelUnit;
import com.qingclass.squirrel.entity.MongoUser;
import com.qingclass.squirrel.entity.SessionSquirrelUser;
import com.qingclass.squirrel.mapper.cms.SquirrelLessonMapper;
import com.qingclass.squirrel.mapper.cms.SquirrelUnitMapper;
import com.qingclass.squirrel.utils.DateFormatHelper;
import com.qingclass.squirrel.utils.Tools;

/**
 * 重要类
 * 本类用来和mongo交互（不限于本类），查询用户学习数据等
 *
 * @author 苏天奇
 * */
@RestController
@RequestMapping(value = "/subject")
public class SubjectController {

	@Autowired
	private SquirrelUnitMapper squirrelUnitMapper;
	@Autowired
	private SquirrelLessonMapper squirrelLessonMapper;
	@Autowired
    private SquirrelUserMapper squirrelUserMapper;
	@Autowired
	private SquirrelPicturebookService squirrelPicturebookService;
	@Autowired
	private SquirrelLessonService squirrelLessonService;
	@Autowired
	private SquirrelLevelService squirrelLevelService;
	
	@Autowired
	private MongoTemplate mongoTemplate;
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 *
	 * */
	@PostMapping("/record-study-time")
	public Map<String, Object> recordStudyTime(
			 @RequestBody Map<String,Object> params,HttpServletRequest req
	) {
		SessionSquirrelUser sessionUserInfo = (SessionSquirrelUser) req.getSession().getAttribute(SessionSquirrelUser.SESSION_SQUIRREL_USER_KEY);

		String openId = sessionUserInfo.getOpenId();
		SquirrelUser squirrelUser = squirrelUserMapper.selectByOpenId(openId);


		String subjectId = params.get("subjectId")+"";
		String levelId = params.get("levelId")+"";
		String lessonId = params.get("lessonId")+"";

		Map<String, Object> history = new HashMap<>();
		history.put("startedAt", DateFormatHelper.getNowTimeStr());
		StringBuffer key = new StringBuffer();
		key.append("learnHistory.");
		key.append("subjects.subject-").append(subjectId).append(".");
		key.append("levels.level-").append(levelId).append(".");
		key.append("lessons.lesson-").append(lessonId);


		//条件查询
		DBObject dbObject = new BasicDBObject();
		((BasicDBObject) dbObject).put("_id",openId);
		DBObject dbFieldObject = new BasicDBObject();
		((BasicDBObject) dbFieldObject).put(key.toString(),true);
		Query q = new BasicQuery(dbObject,dbFieldObject);
		MongoUser one = mongoTemplate.findOne(q, MongoUser.class);

		if(one != null && !one.equals("null")){
			if(one.getLearnHistory() != null && !one.getLearnHistory().toString().equals("") && !one.getLearnHistory().toString().equals("null")){
				Map<String, Object> lessonValueByKeys = MongoDataUtil.getLessonValueByKeys(one, subjectId, levelId, lessonId);
				if(lessonValueByKeys != null){
					history.putAll(lessonValueByKeys);
					history.remove("lessonKeyWords");
				}
				try{history.put("lessonKeyWords",squirrelLessonMapper.getLessonWordsCount(Integer.parseInt(lessonId)));}catch (Exception e){
					logger.warn("lesson-words is null (lessonId="+lessonId+")");
				}
			}

		}
		try{history.put("lessonKeyWords",squirrelLessonMapper.getLessonWordsCount(Integer.parseInt(lessonId)));}catch (Exception e){
			logger.warn("lesson-words is null (lessonId="+lessonId+")");
		}

		SquirrelLesson lesson = squirrelLessonMapper.selectByPrimaryKey(Integer.parseInt(lessonId));
		history.put("order",lesson.getOrder());
		Query query = new Query(Criteria.where("_id").is(openId).andOperator(Criteria.where("userId").is(squirrelUser.getId())));
		Update update = new Update().set(key.toString()+".record", history);
		try {
			mongoTemplate.upsert(query, update, MongoUser.class);
		}catch (DuplicateKeyException e){
			logger.error("mongo用户主键冲突，userId="+squirrelUser.getId());
		}
		return Tools.s();
	}

	/**
	 *
	 * */
	@PostMapping("/record-study-info")
	public Map<String, Object> recordStudyInfo(
			 @RequestBody Map<String,Object> params,HttpServletRequest req
	) {
		SessionSquirrelUser sessionUserInfo = (SessionSquirrelUser) req.getSession().getAttribute(SessionSquirrelUser.SESSION_SQUIRREL_USER_KEY);

		String openId = sessionUserInfo.getOpenId();
		logger.info("===recordStudyInfo==");
		String subjectId = params.get("subjectId")+"";
		String levelId = params.get("levelId")+"";
		String lessonId = params.get("lessonId")+"";
		String unitId = params.get("unitId")+"";


		Map<String, Object> history = new HashMap<>();
		StringBuffer key = new StringBuffer();
		key.append("learnHistory.");
		key.append("subjects.subject-").append(subjectId).append(".");
		key.append("levels.level-").append(levelId).append(".");
		key.append("lessons.lesson-").append(lessonId);
		if(StringUtils.isNotEmpty(unitId)) {
			key.append(".");
			key.append("units.unit-").append(unitId);
			SquirrelUnit squirrelUnit = squirrelUnitMapper.selectByPrimaryKey(Integer.parseInt(unitId));
			history.put("name", squirrelUnit.getName());
			history.put("id", squirrelUnit.getId());


		}else {
			SquirrelLesson squirrelLesson = squirrelLessonMapper.selectByPrimaryKey(Integer.parseInt(lessonId));
			history.put("name", squirrelLesson.getName());
			history.put("id", squirrelLesson.getId());
		}

		//条件查询
		DBObject dbObject = new BasicDBObject();
		((BasicDBObject) dbObject).put("_id",openId);
		DBObject dbFieldObject = new BasicDBObject();
		((BasicDBObject) dbFieldObject).put(key.toString(),true);
		Query q = new BasicQuery(dbObject,dbFieldObject);
		MongoUser one = mongoTemplate.findOne(q, MongoUser.class);

		if(one != null && !one.equals("null")){
			if(one.getLearnHistory() != null && !one.getLearnHistory().equals("") && !one.getLearnHistory().equals("null") ){
				Map<String, Object> lessonValueByKeys = MongoDataUtil.getUnitValueByKeys(one, subjectId, levelId, lessonId,unitId);
				if(lessonValueByKeys != null){
					history.putAll(lessonValueByKeys);
				}
			}
	}

		Query query = new Query(Criteria.where("_id").is(openId));


		Update update = new Update().set(key.toString()+".record", history);
		mongoTemplate.updateFirst(query, update, MongoUser.class);
		return Tools.s();
	}

	/**
	 *
	 * */
	@PostMapping("/lesson-record")
	public Map<String, Object> lessonRecord(
			@RequestBody Map<String,Object> params, 
			HttpServletRequest req){
		
		String subjectId = params.get("subjectId")+"";
		String levelId = params.get("levelId")+"";
		
		SessionSquirrelUser sessionUserInfo = (SessionSquirrelUser) req.getSession().getAttribute(SessionSquirrelUser.SESSION_SQUIRREL_USER_KEY);
		String openId = sessionUserInfo.getOpenId();
		
		Query query = new Query(Criteria.where("_id").is(openId));
		query.fields().include("learnHistory.subjects.subject-"+subjectId+".levels.level-"+levelId+".lessons");
		MongoUser mongoUser = mongoTemplate.findOne(query, MongoUser.class);

		Map<String, Object> returnMap = new HashMap<>();
		
		Map<String, Object> subjectsMap = new HashMap<>();
		Map<String, Object> levelMap = new HashMap<>();
		Map<String, Object> lessonMap = new HashMap<>();
		try{
			subjectsMap = (Map<String, Object>) mongoUser.getLearnHistory().get("subjects");
			levelMap = (Map<String, Object>) ((Map<String, Object>)subjectsMap.get("subject-"+subjectId)).get("levels");
			lessonMap = (Map<String, Object>) ((Map<String, Object>)levelMap.get("level-"+levelId)).get("lessons");
		}catch(NullPointerException e){
			logger.warn("history is null");
		}
		

		List<Map<String,Object>> lessonList= new ArrayList<>();
		List<Integer> lessonIdsList = new ArrayList<>();
		//
		SimpleDateFormat sdfHms = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		for (Map.Entry<String, Object> entry : lessonMap.entrySet()) {
			Map<String, Object> temp = (Map<String, Object>) entry.getValue();
			if(temp.get("record")!=null){
				temp = (Map<String, Object>)temp.get("record");
				Map<String, Object> lmap = new HashMap<>();

				lmap.put("lessonId",entry.getKey().substring(entry.getKey().lastIndexOf("-")+1));
				if(temp.get("isFinish")!=null){
					lmap.put("status",(boolean)temp.get("isFinish"));
				}
				lmap.put("optTime",temp.get("optTime"));
				lessonList.add(lmap);
				lessonIdsList.add(Integer.parseInt(entry.getKey().substring(entry.getKey().lastIndexOf("-")+1)));
			}
		}
		//字符串转时间戳
		SquirrelUser squirrelUser = squirrelUserMapper.selectBeginAtByOpenId(openId, Integer.parseInt(levelId)).get(0);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date date = null;

		//这部分代码是确保返回lessonIds的数量和推课数量一致
		int days = lessonList.size();
		try {
			date = sdf.parse(squirrelUser.getBeginAt());
			Date dd = new Date();
			if(date.before(dd)){
				int dayss = (int)((dd.getTime() - date.getTime())/(1000 * 60 * 60 * 24));
				if(dayss < days){
					days = dayss;
					days += 1;
				}
			}
		} catch (ParseException e) {
			logger.error("parse error.");
			e.printStackTrace();
		}

		//多余lesson删除
		List<Map<String,Object>> lessonIdss= new ArrayList<>();

		for(int i = 0 ; i < days ; i ++){
			lessonIdss.add(lessonList.get(i));
		}

		lessonList = lessonIdss;

		//合并lessonKey
		returnMap.put("lessonList",lessonList);

		
		returnMap.put("beginAt",squirrelUser.getBeginAt());
		returnMap.put("vipBeginTime",squirrelUser.getVipBeginTime());
		returnMap.put("vipEndTime",squirrelUser.getVipEndTime());
		returnMap.put("nowDate",sdfHms.format(new Date()));

		return Tools.s(returnMap);
	}

	/**
	 *
	 * */
	@PostMapping("/get-alreadyDays")
	public Map<String, Object> getAlreadyDays(
			HttpServletRequest req,
			@RequestParam(name="subjectId",required = false)Integer subjectId,
			@RequestParam(name="levelId",required = false)Integer levelId
			){
		SessionSquirrelUser sessionUserInfo = (SessionSquirrelUser) req.getSession().getAttribute(SessionSquirrelUser.SESSION_SQUIRREL_USER_KEY);
		String openId = sessionUserInfo.getOpenId();
		
		Query query = new Query(Criteria.where("_id").is(openId));
		query.fields().include("learnHistory.subjects.subject-"+subjectId+".levels.level-"+levelId+".lessons");
		MongoUser mongoUser = mongoTemplate.findOne(query, MongoUser.class);

		Map<String, Object> returnMap = new HashMap<>();
		
		Map<String, Object> subjectsMap = new HashMap<>();
		Map<String, Object> levelMap = new HashMap<>();
		Map<String, Object> lessonMap = new HashMap<>();
		try{
			subjectsMap = (Map<String, Object>) mongoUser.getLearnHistory().get("subjects");
			levelMap = (Map<String, Object>) ((Map<String, Object>)subjectsMap.get("subject-"+subjectId)).get("levels");
			lessonMap = (Map<String, Object>) ((Map<String, Object>)levelMap.get("level-"+levelId)).get("lessons");
		}catch(NullPointerException e){
			logger.warn("history is null");
		}
		
		int alreadyDays = 0;
		//
		SimpleDateFormat sdfHms = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		for (Map.Entry<String, Object> entry : lessonMap.entrySet()) {
			Map<String, Object> temp = (Map<String, Object>) entry.getValue();
			if(temp.get("record")!=null){
				temp = (Map<String, Object>)temp.get("record");
				if(temp.get("isFinish")!=null && (boolean)temp.get("isFinish")){
					alreadyDays++;
				}
			}
		}
		//字符串转时间戳
		SquirrelUser squirrelUser = squirrelUserMapper.selectBeginAtByOpenId(openId, levelId).get(0);
		
		returnMap.put("beginAt",squirrelUser.getBeginAt());
		returnMap.put("vipBeginTime",squirrelUser.getVipBeginTime());
		returnMap.put("vipEndTime",squirrelUser.getVipEndTime());
		returnMap.put("nowDate",sdfHms.format(new Date()));
		returnMap.put("alreadyDays",alreadyDays);
		return Tools.s(returnMap);
	}
	
	/**
	 *
	 * */
	@SuppressWarnings("unchecked")
	@PostMapping("/study-record")
	public Map<String, Object> studyRecord(
			 @RequestBody Map<String,Object> params,HttpServletRequest req
	){
//		Map<String, Object> params = squirrelRequest.getParams();
		SessionSquirrelUser sessionUserInfo = (SessionSquirrelUser) req.getSession().getAttribute(SessionSquirrelUser.SESSION_SQUIRREL_USER_KEY);
		String openId = sessionUserInfo.getOpenId();
		String subjectId = params.get("subjectId")+"";
		String levelId = params.get("levelId")+"";
		String lessonId = params.get("lessonId")+"";
		Query query = new Query(Criteria.where("_id").is(openId));
		query.fields()
			.include("learnHistory.subjects.subject-"+subjectId+".levels.level-"+levelId);
			//.include("learnHistory.subjects.subject-1054.lessons.lesson-6052.units.unit-7752.duration");
		MongoUser mongoUser = mongoTemplate.findOne(query, MongoUser.class);


		Map<String, Object> map;
		Map<String, Object> map1;
		Map<String, Object> map2 = new HashMap<>();
		Map<String, Object> map3 = new HashMap<>();
		Map<String, Object> map4 = new HashMap<>();
		Map<String,Object> lessonsMap = null;
		if(mongoUser != null && mongoUser.getLearnHistory() != null){
			map =  (Map<String, Object>) mongoUser.getLearnHistory().get("subjects");
			if(map != null){
				map1 = (Map<String, Object>) ((Map<String, Object>)map.get("subject-"+subjectId)).get("levels");
				if(map1 != null){
					map2 = ((Map<String, Object>)map1.get("level-"+levelId));
					if(map2 != null){
						lessonsMap = (Map<String,Object>)map2.get("lessons");
						map3 = ((Map<String, Object>)lessonsMap.get("lesson-"+lessonId));
						if(map3 != null){
							Map<String,Object> unitMap = (Map<String,Object>)map3.get("units");
							map4 = (Map<String, Object>)map3.get("record");
						}
					}
				}
			}
		}




		//
		int alreadyDays = 0;
		int alreadyWords = 0;
		List<Integer> lessonIds = new ArrayList<>();
		if(lessonsMap != null){
			for (Map.Entry<String, Object> entry : lessonsMap.entrySet()) {
				Map<String, Object> temp = (Map<String, Object>) entry.getValue();
				if(temp.get("record")!=null){
					temp = (Map<String, Object>)temp.get("record");

					if(temp.get("isShare")!=null && (boolean)temp.get("isShare")){  //分享
						alreadyDays++;
						lessonIds.add(Integer.parseInt(entry.getKey().substring(entry.getKey().lastIndexOf("-")+1)));
						if(temp.get("lessonKeyWords")!=null && !temp.get("lessonKeyWords").equals("")){//计算已学单词
							alreadyWords+=Integer.parseInt(temp.get("lessonKeyWords").toString());
						}
					}else if(temp.get("isFinish")!=null && (boolean)temp.get("isFinish")){  //已完成学习
						alreadyDays++;
						lessonIds.add(Integer.parseInt(entry.getKey().substring(entry.getKey().lastIndexOf("-")+1)));
						if(temp.get("lessonKeyWords")!=null && !temp.get("lessonKeyWords").equals("")){//计算已学单词
							alreadyWords+=Integer.parseInt(temp.get("lessonKeyWords").toString());
						}
					}
				}
			}
		}
		int alreadyStudyPicturebook = 0;
		if(lessonIds.size() > 0){
			alreadyStudyPicturebook = squirrelPicturebookService.userAlreadyStudyCount(lessonIds);
		}

		boolean isTodayLesson = false;
		//查询该lesson是否是当天课程
		try{
			if(!lessonId.equals("null")){
				SquirrelLesson lesson = squirrelLessonService.selectByPrimaryKey(Integer.parseInt(lessonId));
				isTodayLesson = squirrelLevelService.selectEffectiveRecord(sessionUserInfo.getId(), levelId, lesson.getOrder());
			}
		}catch (NumberFormatException e){
			logger.error("For input string: 'null'");
		}




		if(map3 != null) {
			if (map4 != null) {
				map3.putAll(map4);
			}
			map3.remove("alreadyDays");
			map3.remove("alreadyWords");
			map3.put("alreadyDays", alreadyDays);
			map3.put("alreadyWords", alreadyWords);
			map3.put("alreadyStudyPicturebook", alreadyStudyPicturebook);
			map3.put("isTodayLesson",isTodayLesson);
		}
		if(lessonId != null && !lessonId.equals("") && !lessonId.equals("none") && !lessonId.equals("null")){
			try{map3.put("lessonName",squirrelLessonMapper.selectByPrimaryKey(Integer.parseInt(lessonId)).getName());}catch (NullPointerException e){}
		}
		return Tools.s(map3);
	}

}