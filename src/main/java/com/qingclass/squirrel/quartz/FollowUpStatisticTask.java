package com.qingclass.squirrel.quartz;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.qingclass.squirrel.domain.statistic.SquirrelBatchesStatistic;
import com.qingclass.squirrel.domain.statistic.SquirrelFollowUp;
import com.qingclass.squirrel.entity.BaseJob;
import com.qingclass.squirrel.entity.MongoUser;
import com.qingclass.squirrel.entity.SquirrelUserBehavior;
import com.qingclass.squirrel.mapper.cms.SquirrelLessonMapper;
import com.qingclass.squirrel.mapper.statistic.BatchesStatisticMapper;
import com.qingclass.squirrel.mapper.statistic.FollowUpMapper;
import com.qingclass.squirrel.mapper.user.SquirrelUserMapper;
import com.qingclass.squirrel.utils.ApplicationContextHelper;
import com.qingclass.squirrel.utils.PropertiesLoader;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Query;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 *
 *
 * @author 苏天奇
 * */
public class FollowUpStatisticTask implements BaseJob {


    FollowUpMapper followUpMapper;
    SquirrelUserMapper squirrelUserMapper;

    private String FOLLOW_UP_QR = PropertiesLoader.getProperty("follow.up.qr");

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        logger.info("FollowUpStatisticTask start...");

        followUpMapper = ApplicationContextHelper.getApplicationContext().getBean(FollowUpMapper.class);
        squirrelUserMapper = ApplicationContextHelper.getApplicationContext().getBean(SquirrelUserMapper.class);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DAY_OF_MONTH,-1);

        List<Integer> levelIds = followUpMapper.selectLevelId(sdf.format(cal.getTime()));

        if(levelIds.size() == 0){
            return;
        }



        for(Integer levelId : levelIds){

            int clickWordUp = 0;
            int clickPicbookUp = 0;
            int finishWordUp = 0;
            int finishPicbookUp = 0;
            int shareWordUp = 0;
            int sharePicbookUp = 0;
            int finishAllUp = 0;
            int purchaseCount = 0;
            SquirrelFollowUp sfu = new SquirrelFollowUp();

            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_MONTH,-1);
            String currentDate = sdf.format(cal.getTime());
            List<SquirrelFollowUp> squirrelFollowUps = followUpMapper.selectByLevelId(levelId,currentDate);

            for(SquirrelFollowUp squirrelFollowUp : squirrelFollowUps){

                if(squirrelFollowUp.getClickWordUp() == 1) clickWordUp ++;
                if(squirrelFollowUp.getClickPicbookUp() == 1) clickPicbookUp ++;
                if(squirrelFollowUp.getFinishWordUp() == 1) finishWordUp ++;
                if(squirrelFollowUp.getFinishPicbookUp() == 1) finishPicbookUp ++;
                if(squirrelFollowUp.getShareWordUp() == 1) shareWordUp ++;
                if(squirrelFollowUp.getSharePicbookUp() == 1) sharePicbookUp ++;
                if(squirrelFollowUp.getFinishWordUp() == 1 && squirrelFollowUp.getFinishPicbookUp() == 1) finishAllUp ++;

            }

            //计算购买

            List<String> openIds = squirrelUserMapper.selectUserBehaviorByDateAndSubscribeAndCode(FOLLOW_UP_QR, sdf.format(cal.getTime()));
            if(openIds.size() > 0){
                purchaseCount = squirrelUserMapper.selectPurchaseCountByDate(openIds, sdf.format(cal.getTime()), levelId);
            }



            sfu.setDate(currentDate);
            sfu.setClickWordUp(clickWordUp);
            sfu.setClickPicbookUp(clickPicbookUp);
            sfu.setFinishWordUp(finishWordUp);
            sfu.setFinishPicbookUp(finishPicbookUp);
            sfu.setShareWordUp(shareWordUp);
            sfu.setSharePicbookUp(sharePicbookUp);
            sfu.setFinishAllUp(finishAllUp);
            sfu.setPurchaseCount(purchaseCount);

            Integer integer = followUpMapper.followUpStatisticExist(currentDate, levelId);

            if(integer == null){
                sfu.setLevelId(levelId);
                sfu.setOnRead(squirrelUserMapper.selectOnReadCountByLevelId(levelId));
                followUpMapper.followUpStatisticInsert(sfu);
                followUpMapper.followUpStatisticUpdate(sfu);
            }else{
                sfu.setId(integer);
                sfu.setLevelId(levelId);
                sfu.setOnRead(squirrelUserMapper.selectOnReadCountByLevelId(levelId));
                followUpMapper.followUpStatisticUpdate(sfu);
            }
            followUpMapper.deleteAction(levelId,currentDate);
        }


        logger.info("FollowUpStatisticTask end...");
    }
}
