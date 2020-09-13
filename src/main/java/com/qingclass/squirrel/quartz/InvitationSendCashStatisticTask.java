package com.qingclass.squirrel.quartz;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.qingclass.squirrel.domain.cms.SquirrelLevel;
import com.qingclass.squirrel.domain.statistic.InvitationSendCashBuriedPoint;
import com.qingclass.squirrel.domain.statistic.InvitationSendCashStatistic;
import com.qingclass.squirrel.domain.statistic.SquirrelBatchesStatistic;
import com.qingclass.squirrel.entity.BaseJob;
import com.qingclass.squirrel.mapper.cms.SquirrelLevelMapper;
import com.qingclass.squirrel.mapper.statistic.BatchesStatisticMapper;
import com.qingclass.squirrel.mapper.statistic.InvitationSendCashBuriedPointMapper;
import com.qingclass.squirrel.mapper.statistic.InvitationSendCashStatisticMapper;
import com.qingclass.squirrel.utils.ApplicationContextHelper;
import com.qingclass.squirrel.utils.DateUtils;

/**
 *
 *
 * @author 苏天奇
 * */
public class InvitationSendCashStatisticTask implements BaseJob {
	
	InvitationSendCashStatisticMapper cashStatisticMapper =
			ApplicationContextHelper.getApplicationContext().getBean(InvitationSendCashStatisticMapper.class);
	InvitationSendCashBuriedPointMapper sendCashBuriedPoint = 
			ApplicationContextHelper.getApplicationContext().getBean(InvitationSendCashBuriedPointMapper.class);
    SquirrelLevelMapper squirrelLevelMapper = 	
    		ApplicationContextHelper.getApplicationContext().getBean(SquirrelLevelMapper.class);
    BatchesStatisticMapper batchesStatisticMapper = 	
    		ApplicationContextHelper.getApplicationContext().getBean(BatchesStatisticMapper.class);
//    private String FOLLOW_UP_QR = PropertiesLoader.getProperty("follow.up.qr");

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        logger.info("InvitationSendCashStatisticTask start...");

        List<SquirrelLevel> levelList = squirrelLevelMapper.selectSquirrelLevel();
        SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd" );
        for(SquirrelLevel squirrelLevel : levelList){	// level 	
	        
	        
        	//取出所有的开课日期
	        List<SquirrelBatchesStatistic> squirrelBatchesStatistics = sendCashBuriedPoint.selectBeginAtByLevelId(squirrelLevel.getId());

	        for(SquirrelBatchesStatistic sbs : squirrelBatchesStatistics){	//	开课日期	

	        	String format = sdf.format(new Date());
	            if(format.equals(sbs.getBeginDate())){//当天开课不统计
	                continue;
	            }
	            InvitationSendCashBuriedPoint sendCashPoint = new InvitationSendCashBuriedPoint();
		        sendCashPoint.setStartClassDate(sbs.getBeginDate());
		        sendCashPoint.setLevelId(squirrelLevel.getId());
		        
		        List<InvitationSendCashBuriedPoint> sendCashList =  sendCashBuriedPoint.invitationSendCashBuriedPoint(sendCashPoint);
		        logger.info("InvitationSendCashStatisticTask-> levelId:"+squirrelLevel.getId()+" size:"+sendCashList.size());
		        InvitationSendCashStatistic sendCashStatistic = new InvitationSendCashStatistic();
		        if(!sendCashList.isEmpty()){
		        	InvitationSendCashBuriedPoint sendCash = sendCashList.get(0);
			        //插入邀请发送现金统计表
			        
			        sendCashStatistic.setCurrentStatisticDate(sendCash.getCurrentDate());
			        sendCashStatistic.setStartClassDate(sendCash.getStartClassDate());
			        sendCashStatistic.setLevelId(sendCash.getLevelId());
			        sendCashStatistic.setBeginDays(sendCash.getBeginDays());
			        sendCashStatistic.setBeginPeoples(sendCash.getBeginPeoples());
			        sendCashStatistic.setIntoSendCashPageCount(sendCash.getIntoSendCashPageCount());
			        sendCashStatistic.setSendInvitationCount(sendCash.getSendInvitationCount());
			        sendCashStatistic.setGotoBuyPageCount(sendCash.getGotoBuyPageCount());
			        sendCashStatistic.setClickBuyCount(sendCash.getClickBuyCount());
			        sendCashStatistic.setPurchaseCount(sendCash.getPurchaseCount());
			        sendCashStatistic.setCreated(new Date());
			        
		        }else{
		        	InvitationSendCashBuriedPoint x = new InvitationSendCashBuriedPoint();
		        	x.setLevelId(squirrelLevel.getId());
		        	x.setStartClassDate(sbs.getBeginDate());
		        	InvitationSendCashBuriedPoint cashPoint = sendCashBuriedPoint.selectBeginDaysAndPeoples(x);
		        	
			        sendCashStatistic.setCurrentStatisticDate(DateUtils.getYesterday());
			        sendCashStatistic.setStartClassDate(sbs.getBeginDate());
			        sendCashStatistic.setLevelId(squirrelLevel.getId());
			        sendCashStatistic.setBeginDays(cashPoint.getBeginDays());
			        sendCashStatistic.setBeginPeoples(cashPoint.getBeginPeoples());
			        sendCashStatistic.setIntoSendCashPageCount(0);
			        sendCashStatistic.setSendInvitationCount(0);
			        sendCashStatistic.setGotoBuyPageCount(0);
			        sendCashStatistic.setClickBuyCount(0);
			        sendCashStatistic.setPurchaseCount(0);
			        sendCashStatistic.setCreated(new Date());
		        }
		        
		        Integer id = cashStatisticMapper.selectSendCashStatistic(sendCashStatistic);
		        if(id == null){
		        	cashStatisticMapper.insert(sendCashStatistic);
		        }else{
		        	sendCashStatistic.setId(id);
		        	cashStatisticMapper.update(sendCashStatistic);
		        }
		        
        	}
        }
        logger.info("InvitationSendCashStatisticTask end...");
    }
    
}
