package com.qingclass.squirrel.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.qingclass.squirrel.constant.InvitationTypeEnum;
import com.qingclass.squirrel.constant.ScholarshipApplyForStatusEnum;
import com.qingclass.squirrel.constant.ScholarshipTypeEnum;
import com.qingclass.squirrel.domain.PaymentTransactions;
import com.qingclass.squirrel.domain.cms.SquirrelInvitationSetting;
import com.qingclass.squirrel.domain.wx.WxShare;
import com.qingclass.squirrel.entity.*;
import com.qingclass.squirrel.mapper.cms.InvitationSettingMapper;
import com.qingclass.squirrel.mapper.cms.ScholarshipSettingMapper;
import com.qingclass.squirrel.mapper.cms.SquirrelLevelMapper;
import com.qingclass.squirrel.mapper.cms.WxPurchaseMapper;
import com.qingclass.squirrel.mapper.user.EntPayOrderMapper;
import com.qingclass.squirrel.mapper.user.EntPayOrderScholarshipMapper;
import com.qingclass.squirrel.mapper.user.PatchVoucherMapper;
import com.qingclass.squirrel.mapper.user.ScholarshipApplyForMapper;

import org.apache.ibatis.annotations.Param;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import com.qingclass.squirrel.domain.cms.ScholarshipApplyForResult;
import com.qingclass.squirrel.domain.cms.ScholarshipSetting;
import com.qingclass.squirrel.domain.cms.ScholarshipSettingDetails;
import com.qingclass.squirrel.domain.cms.SquirrelLesson;
import com.qingclass.squirrel.domain.cms.SquirrelLevel;
import com.qingclass.squirrel.domain.cms.UserLevel;
import com.qingclass.squirrel.mapper.cms.SquirrelLessonMapper;
import com.qingclass.squirrel.mapper.user.SquirrelUserMapper;
import com.qingclass.squirrel.service.SquirrelLessonService;
import com.qingclass.squirrel.service.SquirrelLevelService;
import com.qingclass.squirrel.service.SquirrelUserService;
import com.qingclass.squirrel.service.WechatEntPayScholarshipService;
import com.qingclass.squirrel.service.model.request.EntPayCreateOrderRequest;
import com.qingclass.squirrel.service.model.request.EntPayCreateOrderScholarshipRequest;
import com.qingclass.squirrel.utils.MongoDataUtil;
import com.qingclass.squirrel.utils.Tools;
import com.qingclass.squirrel.mapper.user.PaymentTransactionMapper;

/**
 * 重要类
 * 本类接口涵盖 从主页到日历等一系列接口
 *
 * @author 苏天奇
 * */
@RestController
@RequestMapping("/user")
public class SignedInUserController {
	
	@Autowired
	private SquirrelUserMapper squirrelUserMapper = null;
	@Autowired
	private SquirrelLevelService squirrelLevelService = null;
	@Autowired
	private SquirrelLessonService squirrelLessonService = null;
	@Autowired
	private SquirrelLessonMapper squirrelLessonMapper = null;
	@Autowired
	private SquirrelUserService squirrelUserService = null;
	@Autowired
	private PatchVoucherMapper patchVoucherMapper = null;
	@Autowired
	private InvitationSettingMapper invitationSettingMapper = null;
	@Autowired
	private WxPurchaseMapper wxPurchaseMapper = null;
	@Autowired
	private SquirrelLevelMapper squirrelLevelMapper = null;
	@Autowired
	private ScholarshipApplyForMapper scholarshipApplyForMapper;
	@Autowired
	private ScholarshipSettingMapper scholarshipMapper;
	@Autowired
    private EntPayOrderMapper entPayOrderMapper;
	@Autowired
	WechatEntPayScholarshipService scholarshipService;
	@Autowired
	MongoTemplate mongoTemplate;
	
	@Autowired
	PaymentTransactionMapper paymentTransactionMapper;

	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private final static String level1 = "https://component.qingclass.com/api/group_collection/17800569/address";
	
	private final static String level2 = "https://component.qingclass.com/api/group_collection/17800569/address";

	@InitBinder
	public void initBinder(WebDataBinder binder, WebRequest request) {
		//转换日期
		DateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));// CustomDateEditor为自定义日期编辑器
	}
	
	/**
	 *
	 * */
	@PostMapping("/who-am-i")
	public Map<String, Object> whoAmI(SessionSquirrelUser sessionSquirrelUser) {
		int userId = sessionSquirrelUser.getId();
		SquirrelUser squirrelUser = squirrelUserMapper.selectById(userId);
		Map<String, String> userInfo = new HashMap<>();
		userInfo.put("openId", squirrelUser.getOpenId());
		userInfo.put("nickName", squirrelUser.getNickName());
		return Tools.s(userInfo);
	}

	/**
	 *
	 * */
	@PostMapping("/update-nick-name")
	public Map<String, Object> updateNickName(
			SessionSquirrelUser sessionSquirrelUser,
			SquirrelRequest squirrelRequest
	) {
		Map<String, Object> params = squirrelRequest.getParams();
		String nickName = params.get("nickName") + "";
		
		int userId = sessionSquirrelUser.getId();
		SquirrelUser squirrelUser = squirrelUserMapper.selectById(userId);
		squirrelUser.setNickName(nickName);
		squirrelUserMapper.update(squirrelUser);
		
		return Tools.s("nickName updated");
	}

	/**
	 *
	 * */
	@PostMapping("/level-list")
	public Map<String, Object> levelList(
			SquirrelRequest squirrelRequest,
			SessionSquirrelUser sessionSquirrelUser){
		Map<String, Object> params = squirrelRequest.getParams();
		String subjectId = params.get("subjectId")+"";


		SquirrelLevel squirrelLevel = new SquirrelLevel();

		try{
			squirrelLevel.setSubjectId(Integer.parseInt(subjectId));
		}catch (Exception e){
			logger.error(""+e);
		}

		List<SquirrelLevel> list = squirrelLevelService.list(squirrelLevel);



		SquirrelUser sessionSquirrelUser1 = squirrelUserMapper.selectByOpenId(sessionSquirrelUser.getOpenId());
		List<SquirrelLevel> powerList = squirrelLevelService.powerList(sessionSquirrelUser1.getId());

		//计算时候可以换课
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		for(SquirrelLevel e : powerList){
			Date date;
			Date dd = new Date();//换课最后期限期限
			boolean before = false;
			try {
				date = sdf.parse(e.getBeginAt()+" 22:00:00");

				before = dd.before(date);
			} catch (ParseException e1) {
				e1.printStackTrace();
			}
			e.setSwitchLevel(before);
		}


		HashMap<String, Object> map = new HashMap<>();
		map.put("allList",list);
		map.put("powerList",powerList);


//		Query query = new Query(Criteria.where("userId").is(sessionSquirrelUser1.getId()));
//		MongoUser mongoUsers = mongoTemplate.findOne(query, MongoUser.class);//userList的对应学习记录
//		Map<String, Integer> stringIntegerMap = MongoDataUtil.alreadyFinishDayAndWordsAll(mongoUsers);
//
//		if(stringIntegerMap != null){
//			map.putAll(stringIntegerMap);
//		}

		return Tools.s(map);
	}

	/**
	 *
	 * */
	@PostMapping("/lesson-list")
	public Map<String, Object> lessonList(
			HttpServletRequest req
	) {
		HashMap<String, Object> map = new HashMap<>();
		
		SessionSquirrelUser sessionUserInfo = (SessionSquirrelUser) req.getSession().getAttribute(SessionSquirrelUser.SESSION_SQUIRREL_USER_KEY);

		String levelId = req.getParameter("levelId");
		SquirrelLesson squirrelLesson = new SquirrelLesson();
		squirrelLesson.setLevelid(Integer.parseInt(levelId));

		List<SquirrelLesson> squirrelLessons = squirrelLessonService.selectBy(squirrelLesson, sessionUserInfo.getOpenId());
		
		SquirrelUser squirrelUser = squirrelLevelService.selectEffectiveLevelUser(sessionUserInfo.getId(), levelId);
		
//		boolean equals = false;
//		if(squirrelUser!=null){
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//
//            String beginAt = squirrelUser.getBeginAt();
//            Date parse = null;
//            try {
//                parse = sdf.parse(beginAt);
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
//            String begin = sdf.format(beginAt);
//            String now = sdf.format(new Date());
//            equals = begin.equals(now);
//        }else{
//        	equals = false;
//        }
		//----------------------------------------------------------------------------------------------------------------------------
    	String subjectId = "1000000";
        String key = "learnHistory.subjects.subject-"+subjectId+".levels.level-"+levelId+".lessons";

        logger.info("提取学习记录参数: subjectId:{}, levelId:{}, lessonId:{}, eventType:{}",
        		subjectId, levelId, key);
        
        String openId = sessionUserInfo.getOpenId();
        Integer userId = sessionUserInfo.getId();
        
        logger.info("openId:{}, userId:{}", openId, userId);

        logger.info("提取学习记录[param:{levelId:"+levelId+",openId="+openId+"}]");

        DBObject dbObject = new BasicDBObject();
        ((BasicDBObject) dbObject).put("_id",openId);
        DBObject dbFieldObject = new BasicDBObject();
        ((BasicDBObject) dbFieldObject).put(key,true);



        Map<String,Object> lessonsMap = new HashMap<>();
        //条件查询
        Query q = new BasicQuery(dbObject,dbFieldObject);
        MongoUser one = mongoTemplate.findOne(q, MongoUser.class);
        
        if(one != null && !one.equals("null")){
        	
        	logger.info("one:{}",one);
        	logger.info("LearnHistory:{}",one.getLearnHistory());
        	Map<String, Object> learnHistory = one.getLearnHistory();
            if(learnHistory!=null &&  !one.getLearnHistory().isEmpty()){
                Map<String, Object> lessonValueByKeys = MongoDataUtil.getLevelValueByKeys(one, subjectId, levelId);
                if(lessonValueByKeys != null){
                	lessonsMap.putAll(lessonValueByKeys);
                }
                
            }
        }
        //----------------------------------------------------------------------------------------------------------------------------
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String nowDate = sdf.format(new Date());
		map.put("lessonList", squirrelLessons);
		map.put("levelUser", squirrelUser);
		map.put("lessonsMap", lessonsMap);
		map.put("nowDate", nowDate);
		return Tools.s(map);
	}

	/**
	 *
	 * */
	@PostMapping("lesson-key")
	public Map<String, Object> lessonKey(SquirrelRequest squirrelRequest){
		Map<String, Object> params = squirrelRequest.getParams();
		String lessonId = params.get("lessonId")+"";

		SquirrelLesson squirrelLesson;
		if(lessonId.equals("none")){
			return Tools.f();
		}
		squirrelLesson = squirrelLessonService.selectByPrimaryKey(Integer.parseInt(lessonId));

		return Tools.s(squirrelLesson.getLessonkey());
	}

	/**
	 *
	 * */
	@PostMapping("/lesson-send-list")
	public Map<String, Object> lessonSendList(
			HttpServletRequest req ){
		SessionSquirrelUser sessionUserInfo = (SessionSquirrelUser) req.getSession().getAttribute(SessionSquirrelUser.SESSION_SQUIRREL_USER_KEY);
//		int userId = sessionUserInfo.getId();
		String levelId = req.getParameter("levelId");
		int sendDays = Integer.parseInt(req.getParameter("sendDays"));
		
		List<SquirrelLesson> squirrelLessons = squirrelLessonMapper.selectSendLessonByDays(Integer.parseInt(levelId),sendDays);

		List<Map<String,Object>> idList = new ArrayList<>();
		squirrelLessons.forEach(e -> {
			Map<String,Object> map = new HashMap<>();
			map.put("lessonId",e.getId());
			map.put("lessonKey",e.getLessonkey());
			map.put("lessonName",e.getName());
			map.put("lessonTitle","稍后+  "+e.getName());
			map.put("order", e.getOrder());
			idList.add(map);
		});

		return Tools.s(idList);
	}

	/**
	 *
	 * */
	@PostMapping("user-purchase-record")
	public Map<String, Object> userPurchaseRecord(HttpServletRequest req){

		SessionSquirrelUser sessionUserInfo = (SessionSquirrelUser) req.getSession().getAttribute(SessionSquirrelUser.SESSION_SQUIRREL_USER_KEY);
		int id = 0;
		try{
			id = sessionUserInfo.getId();
		}catch (NullPointerException e){
			logger.error("登陆状态无效");
			return Tools.f("登陆状态无效");
		}


		List<SquirrelUser> squirrelUsers = squirrelUserMapper.selectPurchaseRecordByUserId(id);

		return Tools.s(squirrelUsers);
	}

	@PostMapping("/record-sellPage-action-info")
	@ResponseBody
	public Map<String, Object> recordSellPageActionInfo(SquirrelRequest squirrelRequest,SessionSquirrelUser sessionSquirrelUser) {
		
		Map<String, Object> params = squirrelRequest.getParams();
		squirrelUserService.recordSellPageActionInfo(params);
		return Tools.s("");
	}

	@PostMapping("/record-action-info")
	@ResponseBody
	public Map<String, Object> recordActionInfo(SquirrelRequest squirrelRequest,SessionSquirrelUser sessionSquirrelUser) {
		
		Map<String, Object> params = squirrelRequest.getParams();
		squirrelUserService.recordActionInfo(params);
		return Tools.s("");
	}

	/**
	 * 查看邀请记录
	 * */
	@PostMapping("/invitation-record")
	@ResponseBody
	public Map<String,Object> invitationRecord(HttpServletRequest req,
			@RequestParam(value = "levelId",required = true)Integer levelId,
			@RequestParam(value = "invitationType",required = true)Integer invitationType){

		SessionSquirrelUser sessionUserInfo = (SessionSquirrelUser) req.getSession().getAttribute(SessionSquirrelUser.SESSION_SQUIRREL_USER_KEY);
		int id = sessionUserInfo.getId();
		
		InvitationRecord invitationRecord = new InvitationRecord();
		invitationRecord.setInvitationUserId(id);
		invitationRecord.setLevelId(levelId);
		invitationRecord.setInvitationType(invitationType);
		List<InvitationRecord> invitationRecords = patchVoucherMapper.selectInvitationRecords(invitationRecord);

		return Tools.s(invitationRecords);
	}

	/**
	 * 查看邀请规则
	 * */
	@PostMapping("/get-invitation-rule")
	@ResponseBody
	public Map<String,Object> getInvitationRule(HttpServletRequest req,
			@RequestParam(value = "levelId",required = true)Integer levelId,
			@RequestParam(value = "invitationType",required = true)Integer invitationType){
		SessionSquirrelUser sessionUserInfo = (SessionSquirrelUser) req.getSession().getAttribute(SessionSquirrelUser.SESSION_SQUIRREL_USER_KEY);
		int id = sessionUserInfo.getId();
		
		InvitationRecord invitationRecord = new InvitationRecord();
		invitationRecord.setLevelId(levelId);
		invitationRecord.setInvitationType(invitationType);
		
		SquirrelInvitationSetting squirrelInvitationSetting = invitationSettingMapper.selectByLevelId(invitationRecord);
		
		EntPayOrder entPayOrder = new EntPayOrder();
		entPayOrder.setLevelId(levelId);
		entPayOrder.setInvitationUserId(id);
		BigDecimal bigDecimal = entPayOrderMapper.getInvitationCashSum(entPayOrder);
		Integer cashSum = 0;
		if(bigDecimal!=null){
			cashSum = bigDecimal.divide(new BigDecimal(100)).intValue();//乘以100(单位：分);
		}
		squirrelInvitationSetting.setCashSum(cashSum);
		
		return Tools.s(squirrelInvitationSetting);
	}

	/**
	 * 获取分享页内容
	 * */
	@PostMapping("/get-invitation-share-page")
	@ResponseBody
	public Map<String,Object> getInvitationSharePage(
			HttpServletRequest req,
			@RequestParam(value = "levelId",required = true)Integer levelId,
			@RequestParam(value = "invitationType",required = true)Integer invitationType){

		SessionSquirrelUser sessionUserInfo = (SessionSquirrelUser) req.getSession().getAttribute(SessionSquirrelUser.SESSION_SQUIRREL_USER_KEY);
		int id = sessionUserInfo.getId();
		int squirrelUserId = id * 9 - 9;
		
		InvitationRecord invitationRecord = new InvitationRecord();
		invitationRecord.setLevelId(levelId);
		invitationRecord.setInvitationType(invitationType);
		WxShare wxShare = wxPurchaseMapper.selectSharePageBylevelId(invitationRecord);
		SquirrelLevel squirrelLevel = squirrelLevelService.selectByPrimaryKey(levelId);
		
		wxShare.setSquirrelUserId(squirrelUserId);
		wxShare.setUrl(squirrelLevel.getBuySite());
		return Tools.s(wxShare);
	}

	@PostMapping("/get-effective-level-list")
	@ResponseBody
	public Map<String,Object> getEffectiveLevelList(HttpServletRequest req){
		SessionSquirrelUser sessionUserInfo = (SessionSquirrelUser) req.getSession().getAttribute(SessionSquirrelUser.SESSION_SQUIRREL_USER_KEY);
		int id = sessionUserInfo.getId();
		logger.info("id:{}",id);
		List<SquirrelLevel> effectiveLevelList = squirrelLevelMapper.getEffectiveLevelList(id);
		logger.info("effectiveLevelList:{}",effectiveLevelList.size());
		return Tools.s(effectiveLevelList);
	}


	@PostMapping("change-level")
	@ResponseBody
	public Map<String,Object> changeLevel(
			HttpServletRequest req,
			@RequestParam("sourceLevelId")Integer sourceLevelId){
		SessionSquirrelUser sessionUserInfo = (SessionSquirrelUser) req.getSession().getAttribute(SessionSquirrelUser.SESSION_SQUIRREL_USER_KEY);
		int id = sessionUserInfo.getId();

		List<SquirrelLevel> squirrelLevels = squirrelLevelMapper.selectBySquirrelUserId(id);

		if(squirrelLevels.size() != 1){
			return Tools.f("转换失败，code : 1");// 请检查用户level数量是否正确
		}else if(!squirrelLevels.get(0).getId().equals(sourceLevelId)){
			return Tools.f("转换失败，code : 2");//源id不匹配
		}else{

			List<SquirrelUser> squirrelUsers = squirrelUserMapper.selectByLevelIdAndUserId(sourceLevelId, id);
			Date date;
			Date dd = new Date();//换课最后期限期限
			boolean before = false;
			try {
				//计算时候可以换课
				if(squirrelUsers.size() != 1){
					return Tools.f("转换失败，code : 3");//有相同level，请联系管理员
				}
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				date = sdf.parse(squirrelUsers.get(0).getBeginAt()+" 22:00:00");

				before = dd.before(date);
			} catch (ParseException e1) {
				e1.printStackTrace();
			}

			if(!before){
				return Tools.f("已超时，切换课程无效");
			}
		}
		Query query = Query.query(Criteria.where("userId").is(id));
		Update update = new Update();
		if(sourceLevelId.equals(1000005)){
			update.unset("learnHistory.subjects.subject-1000000.levels.level-"+1000005);
			mongoTemplate.updateFirst(query,update,"users");
			squirrelUserMapper.changeLevelId(id,sourceLevelId,1000006);


		}else if(sourceLevelId.equals(1000006)){
			update.unset("learnHistory.subjects.subject-1000000.levels.level-"+1000006);
			mongoTemplate.updateFirst(query,update,"users");
			squirrelUserMapper.changeLevelId(id,sourceLevelId,1000005);
		}



		return Tools.s();
	}
	
	@PostMapping("/scholarship-enter-port")
	public String scholarshipEnterPort(HttpServletRequest request) {
		
		SessionSquirrelUser sessionUserInfo = (SessionSquirrelUser) request.getSession().getAttribute(SessionSquirrelUser.SESSION_SQUIRREL_USER_KEY);
		int userId = sessionUserInfo.getId();
		
		List<ScholarshipSetting> scholarshipList = scholarshipMapper.getScholarshipByUserId(userId);
		
		if(scholarshipList.isEmpty()){
			return "{\"success\":false}";
		}else{
			return "{\"success\":true}";
		}
	}
	
	@PostMapping("/scholarship-details")
	public Map<String,Object> scholarshipDetails(HttpServletRequest request) {
		
		SessionSquirrelUser sessionUserInfo = (SessionSquirrelUser) request.getSession().getAttribute(SessionSquirrelUser.SESSION_SQUIRREL_USER_KEY);
		int userId = sessionUserInfo.getId();
		String openId = sessionUserInfo.getOpenId();
		List<ScholarshipSettingDetails> scholarshipList = scholarshipMapper.getScholarshipDetailsByUserId(userId);
		logger.info("userId:{},openId:{}", userId, openId);
		Map<String,Object> map = new HashMap<>();
		if(!scholarshipList.isEmpty()){
			for(int i=0; i<scholarshipList.size(); i++){
				ScholarshipSettingDetails scholarship = scholarshipList.get(i);
				int alreadyDays = this.getAlreadyDays(openId, scholarship.getSubjectId(), scholarship.getLevelId());
				
				logger.info("alreadyDays:{}, scholarship:{}", alreadyDays, scholarship);
				
				scholarship.setAlreadyDays(alreadyDays);
				if(alreadyDays>=scholarship.getReturnFeeDay().intValue()){
					ScholarshipApplyFor scholarshipApplyFor = new ScholarshipApplyFor();
					scholarshipApplyFor.setLevelId(scholarship.getLevelId());
					scholarshipApplyFor.setBeginAt(scholarship.getBeginAt());
					scholarshipApplyFor.setScholarshipOpenId(openId);
					List<ScholarshipApplyFor> applyForList = scholarshipApplyForMapper.selectApplyForByOpenId(scholarshipApplyFor);
					
					if(!applyForList.isEmpty()){
						ScholarshipApplyFor applyFor = applyForList.get(0);
						scholarship.setOperationStatus(applyFor.getOperationStatus());
						if(applyFor.getStatus().intValue()==ScholarshipApplyForStatusEnum.Pass.getKey()){
							scholarship.setStatus(ScholarshipApplyForStatusEnum.Pass.getKey());
						}else if(applyFor.getStatus().intValue()==ScholarshipApplyForStatusEnum.Refuse.getKey()){
							scholarship.setStatus(ScholarshipApplyForStatusEnum.Refuse.getKey());
						}else if(applyFor.getStatus().intValue()==ScholarshipApplyForStatusEnum.ApplyFor.getKey()){
							scholarship.setStatus(ScholarshipApplyForStatusEnum.ApplyFor.getKey());
						}else{
							scholarship.setStatus(ScholarshipApplyForStatusEnum.Other.getKey());
						}
					}else{
						DateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd");
						Calendar c = Calendar.getInstance();
						String todayTime = dateFormat.format(c.getTime());
						long days = daysOfTwo(scholarship.getEndClassTime(), todayTime);
						
						if(days>30){
							scholarship.setStatus(ScholarshipApplyForStatusEnum.Expired.getKey());
						}else{
							scholarship.setStatus(ScholarshipApplyForStatusEnum.WaitApplyFor.getKey());
						}
					}
				}else{
					scholarship.setStatus(ScholarshipApplyForStatusEnum.NotFinished.getKey());
				}
			}
		}
		
		map.put("details",scholarshipList);
		return Tools.s(map);
	}
	
	public int getAlreadyDays(String openId, Integer subjectId, Integer levelId){
		Query query = new Query(Criteria.where("_id").is(openId));
		query.fields().include("learnHistory.subjects.subject-"+subjectId+".levels.level-"+levelId);
		MongoUser mongoUser = mongoTemplate.findOne(query, MongoUser.class);

		Map<String, Object> subjectsMap;
		Map<String, Object> levelsMap;
		Map<String, Object> levelMap;
		Map<String,Object> lessonsMap = null;
		if(mongoUser != null && mongoUser.getLearnHistory() != null){
			subjectsMap =  (Map<String, Object>) mongoUser.getLearnHistory().get("subjects");
			if(subjectsMap != null){
				levelsMap = (Map<String, Object>) ((Map<String, Object>)subjectsMap.get("subject-"+subjectId)).get("levels");
				if(levelsMap != null){
					levelMap = ((Map<String, Object>)levelsMap.get("level-"+levelId));
					if(levelMap != null){
						lessonsMap = (Map<String,Object>)levelMap.get("lessons");
					}
				}
			}
		}

		int alreadyDays = 0;
		if(lessonsMap != null){
			for (Map.Entry<String, Object> entry : lessonsMap.entrySet()) {
				Map<String, Object> temp = (Map<String, Object>) entry.getValue();
				if(temp.get("record")!=null){
					temp = (Map<String, Object>)temp.get("record");
					if(temp.get("isFinish")!=null && (boolean)temp.get("isFinish")){  //已完成学习
						alreadyDays++;
					} 
					
				}
			}
		}
		return alreadyDays;
	}
	
	public int getMakeUpLearnDays(String openId, Integer subjectId, Integer levelId){
		Query query = new Query(Criteria.where("_id").is(openId));
		query.fields().include("learnHistory.subjects.subject-"+subjectId+".levels.level-"+levelId);
		MongoUser mongoUser = mongoTemplate.findOne(query, MongoUser.class);

		Map<String, Object> subjectsMap;
		Map<String, Object> levelsMap;
		Map<String, Object> levelMap;
		Map<String,Object> lessonsMap = null;
		if(mongoUser != null && mongoUser.getLearnHistory() != null){
			subjectsMap =  (Map<String, Object>) mongoUser.getLearnHistory().get("subjects");
			if(subjectsMap != null){
				levelsMap = (Map<String, Object>) ((Map<String, Object>)subjectsMap.get("subject-"+subjectId)).get("levels");
				if(levelsMap != null){
					levelMap = ((Map<String, Object>)levelsMap.get("level-"+levelId));
					if(levelMap != null){
						lessonsMap = (Map<String,Object>)levelMap.get("lessons");
					}
				}
			}
		}

		int makeUpLearnDays = 0;
		if(lessonsMap != null){
			for (Map.Entry<String, Object> entry : lessonsMap.entrySet()) {
				Map<String, Object> temp = (Map<String, Object>) entry.getValue();
				if(temp.get("record")!=null){
					temp = (Map<String, Object>)temp.get("record");
					if(temp.get("isFinish")!=null && !(boolean)temp.get("isFinish")){  //已完成学习
						makeUpLearnDays++;
					} 
					
				}
			}
		}
		return makeUpLearnDays;
	}
	
	@PostMapping("/scholarship-apply-for")
	public Map<String,Object> scholarshipApplyFor(
			HttpServletRequest request,
			@RequestParam(value = "levelId",required = true)Integer levelId) {
		
		SessionSquirrelUser sessionUserInfo = (SessionSquirrelUser) request.getSession().getAttribute(SessionSquirrelUser.SESSION_SQUIRREL_USER_KEY);
		int userId = sessionUserInfo.getId();
		String openId = sessionUserInfo.getOpenId();
		
		ScholarshipApplyForResult result = new ScholarshipApplyForResult();

		logger.info("userId:{}, levelId:{}", userId, levelId);
		
		List<UserLevel> userLevelList = squirrelLevelMapper.getUserLevelsByLevelIdAndUserId(levelId, userId);
		if(userLevelList.isEmpty()){
			logger.info("userId:{}, levelId:{},没有购买记录,不可以申请奖学金", userId, levelId);
			result.setMessage("没有购买记录,不可以申请奖学金");
			result.setStatus(ScholarshipApplyForStatusEnum.Other.getKey());
			return Tools.f(result);
		}else{
			for(int i=0; i<userLevelList.size(); i++){
				UserLevel userLevel = userLevelList.get(i);
				int alreadyDays = this.getAlreadyDays(openId, userLevel.getSubjectId(), userLevel.getLevelId());
				userLevel.setAlreadyDays(alreadyDays);
				
				logger.info("userId:{}, levelId:{}, alreadyDays:{}, LessonDay:{} ", userId, levelId, alreadyDays, userLevel.getReturnFeeDay().intValue());
				if(alreadyDays>=userLevel.getReturnFeeDay().intValue()){// 已全勤  
					logger.info("userId:{}, levelId:{},达到全勤,申请奖学金。", userId, levelId);
					ScholarshipApplyFor applyFor = new ScholarshipApplyFor();
		        	applyFor.setBeginAt(userLevel.getBeginAt());
		        	applyFor.setScholarshipOpenId(openId);
		        	applyFor.setLevelId(userLevel.getLevelId());
		    		List<ScholarshipApplyFor> applyForList = scholarshipApplyForMapper.selectApplyForByOpenId(applyFor);
		    		
		    		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		    		
		    		if(!applyForList.isEmpty()){	//	已经成功申请奖学金,不可以再申请奖学金！
		        		logger.info("申请人:" + openId +
		        				"LevelId："+ userLevel.getLevelId() +
		        				"BeginAt："+ userLevel.getBeginAt() +
		        				"已经成功过申请奖学金,不可以再次申请奖学金！");
		        		ScholarshipApplyFor scholarshipApplyFor = applyForList.get(0);
		        		result.setCreatedAt(sdf.format(scholarshipApplyFor.getCreatedAt()));
		    			result.setUpdatedAt(sdf.format(scholarshipApplyFor.getUpdatedAt()));
		        		if(scholarshipApplyFor.getStatus().intValue()==ScholarshipApplyForStatusEnum.Pass.getKey()){
			    			result.setMessage("已经成功申请，并领取过奖学金，不可以再发奖学金！");
			    			result.setStatus(ScholarshipApplyForStatusEnum.Pass.getKey());
			    			return Tools.f(result);
						}else if(scholarshipApplyFor.getStatus().intValue()==ScholarshipApplyForStatusEnum.Refuse.getKey()){
			    			result.setMessage("已经成功申请奖学金，审核拒绝，请联系管理员！");
			    			result.setStatus(ScholarshipApplyForStatusEnum.Refuse.getKey());
			    			return Tools.f(result);
						}else if(scholarshipApplyFor.getStatus().intValue()==ScholarshipApplyForStatusEnum.ApplyFor.getKey()){
							result.setMessage("已申请,审核中！");
			    			result.setStatus(ScholarshipApplyForStatusEnum.ApplyFor.getKey());
			    			return Tools.f(result);
						}else {
			    			result.setMessage("已经成功领取过奖学金，不可以再发奖学金！");
			    			result.setStatus(ScholarshipApplyForStatusEnum.Other.getKey());
			    			return Tools.f(result);
		        		}
		        	}else{// 申请
		        		PaymentTransactions p = paymentTransactionMapper.selectTotalFeeByUserIdAndLevelId(userLevel);
		        		
		        		int makeUpLearnDay = this.getMakeUpLearnDays(openId, userLevel.getSubjectId(), userLevel.getLevelId());
		        		
		        		applyFor.setStatus(ScholarshipApplyForStatusEnum.ApplyFor.getKey());
		        		applyFor.setAmount(p.getTotalFee()); 
		        		applyFor.setCreatedAt(new Date());
		        		applyFor.setLearnDay(alreadyDays);
		        		applyFor.setLearnMakeUpDay(makeUpLearnDay);
		        		scholarshipApplyForMapper.insert(applyFor);
		    			result.setMessage(ScholarshipApplyForStatusEnum.ApplyFor.getValue());
		    			result.setStatus(ScholarshipApplyForStatusEnum.ApplyFor.getKey());
		    			result.setCreatedAt(sdf.format(applyFor.getCreatedAt()));
		    			return Tools.s(result);
		        	}
				}else{
					logger.info("userId:{}, levelId:{},没有达到全勤,不可申请奖学金。", userId, levelId);
					result.setMessage(ScholarshipApplyForStatusEnum.NotFinished.getValue());
					result.setStatus(ScholarshipApplyForStatusEnum.NotFinished.getKey());
					return Tools.f(result);
				}
			}
			result.setMessage(ScholarshipApplyForStatusEnum.Other.getValue());
			result.setStatus(ScholarshipApplyForStatusEnum.Other.getKey());
			return Tools.f(result);
		}
	}
	
	
	public static String getStringDateShort(Date dateTime) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String dateString = formatter.format(dateTime);
		return dateString;
	}
	
	public static Date strToDate(String strDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		ParsePosition pos = new ParsePosition(0);
		Date strtodate = formatter.parse(strDate, pos);
		return strtodate;
	}
	
	public static long daysOfTwo(String datef, String dateo) {
		try {
			SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
	        //跨年不会出现问题
	        //如果时间为：2016-03-18 11:59:59 和 2016-03-19 00:00:01的话差值为 0
			Date fDate = sdf.parse(datef);
			Date oDate=sdf.parse(dateo);
	        long days=(oDate.getTime()-fDate.getTime())/(1000*3600*24);
	        System.out.println(days);
	        return days;
		} catch (ParseException e) {
			e.printStackTrace();
			return 0;
		}
    }
	
	public static void main(String[] args){
//		Date currentTime = new Date();
//		Date date = strToDate("2019-08-24");
//		System.out.println(date);
//		System.out.println(getStringDateShort(date));
//		
//		daysOfTwo( "2015-12-31", "2016-01-01");
//		daysOfTwo( "2016-01-01", "2015-12-31");
//		
		BigDecimal a = new BigDecimal(110);
		BigDecimal fenAmount = a.divide(new BigDecimal(100), 2, BigDecimal.ROUND_UNNECESSARY);//分转元  RoundingMode.HALF_UP
		System.out.println(fenAmount);
	}
}
