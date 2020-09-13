package com.qingclass.squirrel.controller;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import com.qingclass.squirrel.domain.statistic.SquirrelBatchesStatistic;
import com.qingclass.squirrel.entity.MongoUser;
import com.qingclass.squirrel.mapper.cms.SquirrelLessonMapper;
import com.qingclass.squirrel.mapper.statistic.BatchesStatisticMapper;
import com.qingclass.squirrel.utils.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 本类为临时类，下面的功能已经没啥用了，前端并没有调用
 *
 * @author 苏天奇
 * */
@RestController
@RequestMapping(value = "/temp")
public class TempController {

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    BatchesStatisticMapper batchesStatisticMapper;
    @Autowired
    SquirrelLessonMapper squirrelLessonMapper;
    
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     *
     * */
    @PostMapping(value = "finish-lesson")
    public Map<String,Object> finishLesson(@RequestParam("beginDate")String beginDate, @RequestParam("levelId")Integer levelId){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        int i = 0;
        String format = sdf.format(new Date());
        if(format.equals(beginDate)){//当天开课不统计
            return null;
        }
        for(;;){
            i ++;
            logger.info("for times : "+ i);
            SquirrelBatchesStatistic squirrelBatchesStatistic = new SquirrelBatchesStatistic();

            List<String> openIds = batchesStatisticMapper.selectTempOpenId(beginDate, levelId);//
            int order = 0;
            if(openIds == null || openIds.size() == 0) {
                //从user_levels 里取出对应的openId
                openIds = batchesStatisticMapper.selectBatchesOpenId(beginDate, levelId);
                order = 1;
                squirrelBatchesStatistic.setCurrentDate(beginDate);
            }else{
                String currentDate = batchesStatisticMapper.selectCurrentDate(beginDate, levelId);

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
                    begin = sdf.parse(beginDate).getTime();

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


            Integer lessonId = squirrelLessonMapper.selectIdByOrderAndLevelId(order,levelId);
            if(lessonId == null){
                logger.info("已结课，跳过本次统计");
                break;
            }
            StringBuffer key = new StringBuffer();
            key.append("learnHistory.");
            key.append("subjects.subject-").append("1000000").append(".");
            key.append("levels.level-").append(levelId).append(".");
            key.append("lessons.lesson-").append(lessonId);

            {//计算已学习人数
                logger.info("开始统计以学习人数...");
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
                logger.info("统计以学习人数结束..."+count);
            }

            {//计算已打卡人数
                logger.info("开始统计以打卡人数...");
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
                logger.info("统计以打卡人数结束..."+count);
            }

            {//计算已学完人数
                logger.info("开始统计以学完人数...");
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
                Query query = new BasicQuery(queryBuilder.get(),fieldsObject);

                List<MongoUser> mongoUsers = mongoTemplate.find(query, MongoUser.class);
                squirrelBatchesStatistic.setFinishCount(mongoUsers.size());
                logger.info("统计以学完人数结束..."+mongoUsers.size());
                //删除openId临时表数据
                batchesStatisticMapper.deleteTempTableOpenId(levelId,beginDate);
                List<SquirrelBatchesStatistic> openList = new ArrayList<>();

                for(int j = 0 ; j < mongoUsers.size() ; j ++){
                    SquirrelBatchesStatistic sop = new SquirrelBatchesStatistic();
                    sop.setOpenId(mongoUsers.get(j).getId());
                    sop.setLevelId(levelId);
                    sop.setBeginDate(beginDate);
                    sop.setCurrentDate(squirrelBatchesStatistic.getCurrentDate());
                    openList.add(sop);
                }
                logger.info("循环结束...");
                //插入下次查询的起始openId

                if(openList.size() > 0){
                    batchesStatisticMapper.insertTempTableOpenId(openList);
                }else{
                    SquirrelBatchesStatistic sop = new SquirrelBatchesStatistic();
                    sop.setOpenId("1");
                    sop.setLevelId(levelId);
                    sop.setBeginDate(beginDate);
                    sop.setCurrentDate(squirrelBatchesStatistic.getCurrentDate());
                    openList.add(sop);
                    batchesStatisticMapper.insertTempTableOpenId(openList);
                }

            }

            //插入记录
            squirrelBatchesStatistic.setBeginCount(batchesStatisticMapper.selectCountByBeginAt(beginDate,levelId));
            squirrelBatchesStatistic.setBeginDate(beginDate);
            squirrelBatchesStatistic.setBeginDay(order);
            squirrelBatchesStatistic.setLevelId(levelId);
            batchesStatisticMapper.insertBatchesStatistic(squirrelBatchesStatistic);

            //判断跳出条件
            String currentDate = batchesStatisticMapper.selectCurrentDate(beginDate, levelId);
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.DAY_OF_MONTH, -1);

            if(currentDate.equals(sdf.format(cal.getTime()))){
                break;
            }else{
                if(i > 1000){
                    logger.error(".........死循环.........强制跳出");
                    return null;
                }
            }
        }

        return Tools.s();
    }

}
