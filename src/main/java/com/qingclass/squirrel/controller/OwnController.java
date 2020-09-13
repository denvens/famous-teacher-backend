package com.qingclass.squirrel.controller;

import com.qingclass.squirrel.domain.cms.SquirrelLessonRemind;
import com.qingclass.squirrel.domain.cms.SquirrelLevel;
import com.qingclass.squirrel.domain.wx.WxRemind;
import com.qingclass.squirrel.entity.SessionSquirrelUser;
import com.qingclass.squirrel.entity.SquirrelUser;
import com.qingclass.squirrel.mapper.cms.InvitationSettingMapper;
import com.qingclass.squirrel.mapper.cms.WxPurchaseMapper;
import com.qingclass.squirrel.mapper.user.PatchVoucherMapper;
import com.qingclass.squirrel.mapper.user.SquirrelUserMapper;
import com.qingclass.squirrel.mapper.user.UserRemindMapper;
import com.qingclass.squirrel.utils.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.tools.Tool;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 本类对应 C端 "我的"
 *
 * @author 苏天奇
 * */
@RestController
@RequestMapping("/own")
public class OwnController {

	@Autowired
	UserRemindMapper userRemindMapper;
	@Autowired
	WxPurchaseMapper wxPurchaseMapper;
	@Autowired
	SquirrelUserMapper squirrelUserMapper;
	@Autowired
	InvitationSettingMapper invitationSettingMapper;
	@Autowired
	PatchVoucherMapper patchVoucherMapper;

	Logger logger = LoggerFactory.getLogger(this.getClass());

	@PostMapping("lesson-remind-info")
	public Map<String, Object> lessonRemindInfo(HttpServletRequest req, @RequestParam(value = "levelId",required = false)Integer levelId){
		SessionSquirrelUser sessionUserInfo = (SessionSquirrelUser) req.getSession().getAttribute(SessionSquirrelUser.SESSION_SQUIRREL_USER_KEY);
		String openId = sessionUserInfo.getOpenId();

		List<WxRemind> wxReminds = userRemindMapper.selectUserLessonRemind(openId, levelId);

		if(wxReminds.size() < 2){
			userRemindMapper.deleteUserLessonRemind(openId,levelId);
			//查询默认提醒
			SquirrelLessonRemind remind = wxPurchaseMapper.selectRemindTimeDefault("default");
			if(remind == null || remind.getId() == null
					|| remind.getRemindRate() == null || remind.getFirstRemind() == null
					|| remind.getSecondRemind() == null){
				return Tools.f();
			}
			int userId = sessionUserInfo.getId();
			userRemindMapper.insertRemindRecord(userId,openId,levelId,remind.getFirstRemind(),remind.getRemindRate(),1);
			userRemindMapper.insertRemindRecord(userId,openId,levelId,remind.getSecondRemind(),remind.getRemindRate(),1);
			wxReminds = userRemindMapper.selectUserLessonRemind(openId, levelId);
		}else if(wxReminds.size() > 2){//如果大于2，强制恢复默认
			userRemindMapper.deleteUserLessonRemind(openId,levelId);
			//查询默认提醒
			SquirrelLessonRemind remind = wxPurchaseMapper.selectRemindTimeDefault("default");
			if(remind == null || remind.getId() == null
					|| remind.getRemindRate() == null || remind.getFirstRemind() == null
					|| remind.getSecondRemind() == null){
				return Tools.f();
			}
			int userId = sessionUserInfo.getId();
			userRemindMapper.insertRemindRecord(userId,openId,levelId,remind.getFirstRemind(),remind.getRemindRate(),1);
			userRemindMapper.insertRemindRecord(userId,openId,levelId,remind.getSecondRemind(),remind.getRemindRate(),1);
			wxReminds = userRemindMapper.selectUserLessonRemind(openId, levelId);
		}

		return Tools.s(wxReminds);
	}

	@PostMapping("lesson-remind-open-or-close")
	public Map<String, Object> lessonRemindOpenOrClose(HttpServletRequest req, @RequestParam(value = "levelId",required = false)Integer levelId,
													   @RequestParam(value = "isOpen",required = false)Integer isOpen){
		SessionSquirrelUser sessionUserInfo = (SessionSquirrelUser) req.getSession().getAttribute(SessionSquirrelUser.SESSION_SQUIRREL_USER_KEY);
		String openId = sessionUserInfo.getOpenId();

		int i = userRemindMapper.updateOpenStatus(openId, levelId, isOpen);

		return Tools.s(i);
	}

	@PostMapping("lesson-remind-edit")
	public Map<String, Object> lessonRemindEdit(HttpServletRequest req, @RequestParam(value = "levelId",required = false)Integer levelId,
												@RequestParam(value = "remindTime",required = false)String remindTime,@RequestParam(value = "id",required = false)Integer id,
												@RequestParam(value = "isOpen",required = false)Integer isOpen){


		if(id != null){
			userRemindMapper.updateUserLessonRemind(id, levelId, remindTime);
		}else{
			SessionSquirrelUser sessionUserInfo = (SessionSquirrelUser) req.getSession().getAttribute(SessionSquirrelUser.SESSION_SQUIRREL_USER_KEY);
			String openId = sessionUserInfo.getOpenId();
			int userId = sessionUserInfo.getId();
			userRemindMapper.insertRemindRecord(userId,openId,levelId,remindTime,"每天",isOpen);
		}
		return Tools.s();
	}

	/**
	 * 背景音乐开关
	 * */
	@PostMapping("get-bgm-status")
	public Map<String, Object> bgmStatus(HttpServletRequest req){

		SessionSquirrelUser sessionUserInfo = (SessionSquirrelUser) req.getSession().getAttribute(SessionSquirrelUser.SESSION_SQUIRREL_USER_KEY);
		String openId = sessionUserInfo.getOpenId();
		SquirrelUser squirrelUser = squirrelUserMapper.selectByOpenId(openId);

		return Tools.s(squirrelUser);
	}

	/**
	 * 背景音乐开关
	 * */
	@PostMapping("edit-bgm-status")
	public Map<String, Object> editStatus(@RequestParam("openId")String openId,@RequestParam("bgmStatus")Integer bgmStatus){

		squirrelUserMapper.updateBgmStatusByOpenId(openId,bgmStatus);

		return Tools.s();
	}



	/**
	 * 我的-补卡劵
	 * */
	@PostMapping("vouchers-list")
	public Map<String,Object> vouchersList(HttpServletRequest req){
		logger.info("entry vouchers-list");
		//取用户id
		SessionSquirrelUser sessionUserInfo = (SessionSquirrelUser) req.getSession().getAttribute(SessionSquirrelUser.SESSION_SQUIRREL_USER_KEY);
		int id = sessionUserInfo.getId();

		//查询该id下的所有level并关联补卡劵有效期
		List<SquirrelLevel> squirrelLevels = invitationSettingMapper.selectBySquirrelUserId(id);

		for(SquirrelLevel sl : squirrelLevels){
			sl.setVouchersCount(patchVoucherMapper.selectVoucherCount(id,sl.getId()));
		}

		return Tools.s(squirrelLevels);
	}


	@PostMapping("vouchers-prohibit")
	public Map<String,Object> vouchersProhibit(HttpServletRequest req,@RequestParam("levelId")Integer levelId){
		SessionSquirrelUser sessionUserInfo = (SessionSquirrelUser) req.getSession().getAttribute(SessionSquirrelUser.SESSION_SQUIRREL_USER_KEY);
		int id = sessionUserInfo.getId();

		int i = patchVoucherMapper.selectProhibit(id, levelId);
		patchVoucherMapper.deleteProhibit(id, levelId);

		Map<String, Object> map = new HashMap<>();

		map.put("count",i);
		if(i == 0){
			map.put("remind",false);
		}else{
			map.put("remind",true);
		}

		return Tools.s(map);
	}
}
