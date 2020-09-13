package com.qingclass.squirrel.quartz;

import com.mongodb.*;
import com.qingclass.squirrel.domain.statistic.SquirrelBatchesStatistic;
import com.qingclass.squirrel.entity.BaseJob;
import com.qingclass.squirrel.entity.MongoUser;
import com.qingclass.squirrel.mapper.cms.SquirrelLessonMapper;
import com.qingclass.squirrel.mapper.statistic.BatchesStatisticMapper;
import com.qingclass.squirrel.utils.ApplicationContextHelper;
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
public class BatchesStatisticTask implements BaseJob {


    BatchesStatisticMapper batchesStatisticMapper;
    SquirrelLessonMapper squirrelLessonMapper;
    MongoTemplate mongoTemplate;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        logger.info("batchesStatisticTask start...");

        batchesStatisticMapper = ApplicationContextHelper.getApplicationContext().getBean(BatchesStatisticMapper.class);
        squirrelLessonMapper = ApplicationContextHelper.getApplicationContext().getBean(SquirrelLessonMapper.class);
        mongoTemplate = ApplicationContextHelper.getApplicationContext().getBean(MongoTemplate.class);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        //如果是首次启用本功能，则进行分批次统计的数据初始化

        //取出所有的开课日期
        List<SquirrelBatchesStatistic> squirrelBatchesStatistics = batchesStatisticMapper.selectBeginAtGroup();

        for(SquirrelBatchesStatistic sbs : squirrelBatchesStatistics){
            int i = 0;
            String format = sdf.format(new Date());
            if(format.equals(sbs.getBeginDate())){//当天开课不统计
                continue;
            }

            for(;;){
                i ++;
                SquirrelBatchesStatistic squirrelBatchesStatistic = new SquirrelBatchesStatistic();

                List<String> openIds = batchesStatisticMapper.selectTempOpenId(sbs.getBeginDate(), sbs.getLevelId());//
                int order = 0;
                if(openIds == null || openIds.size() == 0) {
                    //从user_levels 里取出对应的openId
                    openIds = batchesStatisticMapper.selectBatchesOpenId(sbs.getBeginDate(), sbs.getLevelId());
                    order = 1;
                    squirrelBatchesStatistic.setCurrentDate(sbs.getBeginDate());
                }else{
                    String currentDate = batchesStatisticMapper.selectCurrentDate(sbs.getBeginDate(), sbs.getLevelId());

                    Calendar cal  = Calendar.getInstance();
                    try {
                        cal.setTime(sdf.parse(currentDate));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    cal.add(Calendar.DAY_OF_MONTH, 1);
                    squirrelBatchesStatistic.setCurrentDate(sdf.format(cal.getTime()));

                    long current = 0;
                    long begin = 0;

                    try {
                        current = sdf.parse(sdf.format(cal.getTime())).getTime();
                        begin = sdf.parse(sbs.getBeginDate()).getTime();

                        cal.setTime(new Date());
                        cal.add(Calendar.DAY_OF_MONTH, -1);
                        if(sdf.parse(sdf.format(cal.getTime())).compareTo(sdf.parse(currentDate)) <= 0){//不统计
                            break;
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    order = (int) ((current - begin) / (1000 * 60 * 60 * 24)) + 1;
                }


                Integer lessonId = squirrelLessonMapper.selectIdByOrderAndLevelId(order,sbs.getLevelId());
                if(lessonId == null){
                    logger.info("已结课，跳过本次统计");
                    break;
                }
                StringBuffer key = new StringBuffer();
                key.append("learnHistory.");
                key.append("subjects.subject-").append("1000000").append(".");
                key.append("levels.level-").append(sbs.getLevelId()).append(".");
                key.append("lessons.lesson-").append(lessonId);

                {//计算已学习人数
                    BasicDBList basicDBList = new BasicDBList();
                    DBObject q1 = new BasicDBObject();
                    ((BasicDBObject) q1).put("_id",new BasicDBObject("$in",openIds));
                    basicDBList.add(q1);
                    DBObject obj = new BasicDBObject();
                    obj.put("$and", basicDBList);

                    Query query = new BasicQuery(obj.toString());
                    query.fields()
                            .include(key.toString());
                    Long count = mongoTemplate.count(query, MongoUser.class);
                    squirrelBatchesStatistic.setStudyCount(count.intValue());
                }

                {//计算已打卡人数
                    BasicDBList basicDBList = new BasicDBList();
                    DBObject q1 = new BasicDBObject();
                    ((BasicDBObject) q1).put("_id",new BasicDBObject("$in",openIds));
                    basicDBList.add(q1);
                    basicDBList.add(new BasicDBObject(key.toString()+".record.isShare",true));

                    DBObject obj = new BasicDBObject();
                    obj.put("$and", basicDBList);

                    Query query = new BasicQuery(obj.toString());

                    Long count = mongoTemplate.count(query, MongoUser.class);
                    squirrelBatchesStatistic.setShareCount(count.intValue());
                }

                {//计算已学完人数
                    QueryBuilder queryBuilder = new QueryBuilder();
                    BasicDBList basicDBList = new BasicDBList();
                    DBObject q1 = new BasicDBObject();
                    ((BasicDBObject) q1).put("_id",new BasicDBObject("$in",openIds));
                    basicDBList.add(q1);
                    basicDBList.add(new BasicDBObject(key.toString()+".record.isFinish",true));

                    DBObject obj = new BasicDBObject();
                    obj.put("$and", basicDBList);

                    BasicDBObject fieldsObject=new BasicDBObject();
                    queryBuilder.or(obj);
            //        Query query = new BasicQuery(queryBuilder.get(),fieldsObject);
                    DBCursor users = mongoTemplate.getCollection("users").find(obj);

                    List<MongoUser> mongoUsers = new ArrayList<>();
                    while (users.hasNext()){
                        DBObject object=users.next();
                        MongoUser mongoUser = new MongoUser();
                        mongoUser.setId(object.get("_id").toString());
                        mongoUsers.add(mongoUser);
                    }

             //       List<MongoUser> mongoUsers = mongoTemplate.find(query, MongoUser.class);
                    squirrelBatchesStatistic.setFinishCount(mongoUsers.size());
                    logger.info("统计以学完人数结束..."+mongoUsers.size());
                    //删除openId临时表数据
                    batchesStatisticMapper.deleteTempTableOpenId(sbs.getLevelId(),sbs.getBeginDate());
                    List<SquirrelBatchesStatistic> openList = new ArrayList<>();
                    mongoUsers.forEach(e -> {
                        SquirrelBatchesStatistic sop = new SquirrelBatchesStatistic();
                        sop.setOpenId(e.getId());
                        sop.setLevelId(sbs.getLevelId());
                        sop.setBeginDate(sbs.getBeginDate());
                        sop.setCurrentDate(squirrelBatchesStatistic.getCurrentDate());
                        openList.add(sop);
                    });
                    //插入下次查询的起始openId

                    if(openList.size() > 0){
                        batchesStatisticMapper.insertTempTableOpenId(openList);
                    }else{
                        SquirrelBatchesStatistic sop = new SquirrelBatchesStatistic();
                        sop.setOpenId("1");
                        sop.setLevelId(sbs.getLevelId());
                        sop.setBeginDate(sbs.getBeginDate());
                        sop.setCurrentDate(squirrelBatchesStatistic.getCurrentDate());
                        openList.add(sop);
                        batchesStatisticMapper.insertTempTableOpenId(openList);
                    }

                }

                //插入记录
                squirrelBatchesStatistic.setBeginCount(batchesStatisticMapper.selectCountByBeginAt(sbs.getBeginDate(),sbs.getLevelId()));
                squirrelBatchesStatistic.setBeginDate(sbs.getBeginDate());
                squirrelBatchesStatistic.setBeginDay(order);
                squirrelBatchesStatistic.setLevelId(sbs.getLevelId());
                batchesStatisticMapper.insertBatchesStatistic(squirrelBatchesStatistic);


                //判断跳出条件
                String currentDate = batchesStatisticMapper.selectCurrentDate(sbs.getBeginDate(), sbs.getLevelId());
                Calendar cal = Calendar.getInstance();
                cal.setTime(new Date());
                cal.add(Calendar.DAY_OF_MONTH, -1);

                if(currentDate.equals(sdf.format(cal.getTime()))){
                    break;
                }else{
                    if(i > 10000){
                        logger.error(".........死循环.........强制跳出");
                        return;
                    }
                }
            }
        }

        logger.info("batchesStatisticTask end...");
    }
}
