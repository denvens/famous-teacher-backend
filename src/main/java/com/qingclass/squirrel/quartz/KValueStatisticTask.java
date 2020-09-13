package com.qingclass.squirrel.quartz;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.qingclass.squirrel.domain.statistic.UserAction;
import com.qingclass.squirrel.entity.BaseJob;
import com.qingclass.squirrel.entity.SquirrelUserBehavior;
import com.qingclass.squirrel.mapper.statistic.SquirrelKvalueStatisticMapper;
import com.qingclass.squirrel.mapper.statistic.UserActionMapper;
import com.qingclass.squirrel.mapper.user.SquirrelUserMapper;
import com.qingclass.squirrel.service.SquirrelKvalueStatisticService;
import com.qingclass.squirrel.utils.ApplicationContextHelper;

/**
 *
 *
 * @author 苏天奇
 * */
@Service
public class KValueStatisticTask implements BaseJob {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
		logger.info("KValueStatisticTask...start...");
		SquirrelKvalueStatisticMapper squirrelKvalueStatisticMapper = ApplicationContextHelper.getApplicationContext().getBean(SquirrelKvalueStatisticMapper.class);
		SquirrelUserMapper squirrelUserMapper = ApplicationContextHelper.getApplicationContext().getBean(SquirrelUserMapper.class);
		UserActionMapper userActionMapper = ApplicationContextHelper.getApplicationContext().getBean(UserActionMapper.class);
		SquirrelKvalueStatisticService squirrelKvalueStatisticService = ApplicationContextHelper.getApplicationContext().getBean(SquirrelKvalueStatisticService.class);
		int statisticCount = squirrelKvalueStatisticMapper.selectStatistic();
		List<UserAction> userActions = userActionMapper.selectUserAction();
		Map<Integer, List<UserAction>> map = new HashMap<>();
		for (UserAction userAction : userActions) {
			if (map.get(userAction.getLevelId()) == null) {
				List<UserAction> list = new ArrayList<>();
				list.add(userAction);
				map.put(userAction.getLevelId(), list);
			} else {
				List<UserAction> list = map.get(userAction.getLevelId());
				list.add(userAction);
				map.put(userAction.getLevelId(), list);
			}
		}
		Map<Integer, Integer> userBehaviorMap = new HashMap<>();
		List<SquirrelUserBehavior> squirrelUserBehaviors = squirrelUserMapper.selectUserBehavior();
		for (SquirrelUserBehavior squirrelUserBehavior : squirrelUserBehaviors) {
			if (userBehaviorMap.get(squirrelUserBehavior.getLevelId()) == null) {
				userBehaviorMap.put(squirrelUserBehavior.getLevelId(), 1);
			} else {
				userBehaviorMap.put(squirrelUserBehavior.getLevelId(),
						userBehaviorMap.get(squirrelUserBehavior.getLevelId()) + 1);
			}
		}
		Map<Integer,Integer> onReadMap = new HashMap<>();
		List<Map<String,Integer>> lists =squirrelUserMapper.selectOnReadCount();
		for (Map<String, Integer> map2 : lists) {
			onReadMap.put(Integer.valueOf(map2.get("levelId")+""), Integer.valueOf(map2.get("onRead")+""));
		}
		
		for (Entry<Integer, List<UserAction>> entry : map.entrySet()) {
			squirrelKvalueStatisticService.dataStatistic(entry.getKey(), entry.getValue(), statisticCount,
					userBehaviorMap.get(entry.getKey()),onReadMap.get(entry.getKey()));
		}
		logger.info("KValueStatisticTask...end...");
	}
}
