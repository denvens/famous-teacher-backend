package com.qingclass.squirrel.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.qingclass.squirrel.domain.statistic.SquirrelKvalueStatistic;
import com.qingclass.squirrel.domain.statistic.UserAction;
import com.qingclass.squirrel.mapper.statistic.SquirrelKvalueStatisticMapper;
import com.qingclass.squirrel.mapper.statistic.UserActionMapper;
import com.qingclass.squirrel.mapper.user.SquirrelUserMapper;
import com.qingclass.squirrel.utils.DateFormatHelper;

@Service
public class SquirrelKvalueStatisticService {
	@Autowired
	private SquirrelKvalueStatisticMapper squirrelKvalueStatisticMapper;
	@Autowired
	private UserActionMapper userActionMapper;

	@Transactional(value="statisticTransactionManager",propagation = Propagation.REQUIRED,rollbackFor=Exception.class)
	public void dataStatistic(Integer levelId, List<UserAction> userActions, int statisticCount,Integer careCount, Integer onReadCount) {
		try {
			//1.判断是否需要统计
			if(statisticCount>0) return;
			//2.插入统计数据
			Map<String,Integer> map =new HashMap<>();
			for (UserAction userAction : userActions) {
				if (map.get(userAction.getType()) == null) {
					map.put(userAction.getType(), 1);
				} else {
					map.put(userAction.getType(), map.get(userAction.getType())+1);
				}
			}
			Calendar calendar =Calendar.getInstance();
			calendar.setTime(new Date());
			calendar.add(Calendar.DATE, -1);
			SquirrelKvalueStatistic record = new SquirrelKvalueStatistic();
			record.setBuy(map.get(SquirrelKvalueStatistic.BUY)==null ?0 : map.get(SquirrelKvalueStatistic.BUY));
			record.setBuySuccess(map.get(SquirrelKvalueStatistic.BUYSUCCESS)==null ?0 : map.get(SquirrelKvalueStatistic.BUYSUCCESS));
			record.setCare(careCount==null ? 0 :careCount);
			record.setDate(calendar.getTime());
			record.setGoShare(map.get(SquirrelKvalueStatistic.GOSHARE)==null ?0 : map.get(SquirrelKvalueStatistic.GOSHARE));
			record.setInit(map.get(SquirrelKvalueStatistic.INIT)==null ?0 : map.get(SquirrelKvalueStatistic.INIT));
			record.setOnRead(onReadCount==null ? 0 :onReadCount);
			record.setShare(map.get(SquirrelKvalueStatistic.SHARE)==null ?0 : map.get(SquirrelKvalueStatistic.SHARE));
			record.setLevelId(levelId);
			record.setClickAudition(map.get(SquirrelKvalueStatistic.CLICKAUDITION)==null ?0 : map.get(SquirrelKvalueStatistic.CLICKAUDITION));
			record.setLearnAudition(map.get(SquirrelKvalueStatistic.LEARNAUDITION)==null ?0 : map.get(SquirrelKvalueStatistic.LEARNAUDITION));
			record.setClickBuyAtAudition(map.get(SquirrelKvalueStatistic.CLICKBUYATAUDITION)==null ?0 : map.get(SquirrelKvalueStatistic.CLICKBUYATAUDITION));
			record.setLearnFinish(map.get(SquirrelKvalueStatistic.LEARNFINISH)==null ?0 : map.get(SquirrelKvalueStatistic.LEARNFINISH));
			record.setLearnAndBuy(map.get(SquirrelKvalueStatistic.BUYANDAUDITION)==null ?0 : map.get(SquirrelKvalueStatistic.BUYANDAUDITION));
			record.setAuditionInit(map.get(SquirrelKvalueStatistic.AUDITIONINIT)==null ?0 : map.get(SquirrelKvalueStatistic.AUDITIONINIT));
			record.setAuditionClick(map.get(SquirrelKvalueStatistic.AUDITIONCLICK)==null ?0 : map.get(SquirrelKvalueStatistic.AUDITIONCLICK));
			record.setAuditionBuySuccess(map.get(SquirrelKvalueStatistic.AUDITIONBUYSUCCESS)==null ?0 : map.get(SquirrelKvalueStatistic.AUDITIONBUYSUCCESS));
			squirrelKvalueStatisticMapper.insert(record);
			
			//3.备份删除历史埋点数据
			List<UserAction> userActionList = new ArrayList<>();
			for (UserAction userAction : userActions) {
				userActionList.add(userAction);
				if(userActionList.size()==200) {
					userActionMapper.batchInsert(userActionList);
					userActionList.clear();
				}
			}
			if(!CollectionUtils.isEmpty(userActionList))
				userActionMapper.batchInsert(userActionList);
			//4.删除历史埋点数据
			List<Integer> ids = new ArrayList<>();
			for (UserAction userAction : userActions) {
				ids.add(userAction.getId());
				if(ids.size()==200) {
					userActionMapper.batchDelete(ids);
					ids.clear();
				}
			}
			if(!CollectionUtils.isEmpty(ids))
				userActionMapper.batchDelete(ids);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
}