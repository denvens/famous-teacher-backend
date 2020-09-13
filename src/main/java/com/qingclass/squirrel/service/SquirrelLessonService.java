package com.qingclass.squirrel.service;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.qingclass.squirrel.domain.cms.RequestInfo;
import com.qingclass.squirrel.domain.cms.SquirrelLesson;
import com.qingclass.squirrel.entity.MongoUser;
import com.qingclass.squirrel.entity.SquirrelUser;
import com.qingclass.squirrel.mapper.cms.SquirrelLessonMapper;
import com.qingclass.squirrel.mapper.user.SquirrelUserMapper;
import com.qingclass.squirrel.utils.MongoDataUtil;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@Component
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class,value = "squirrelResourceTransaction")
public class SquirrelLessonService {
	
	@Autowired
    private SquirrelLessonMapper squirrelLessonMapper;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private SquirrelUserMapper squirrelUserMapper;

    Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
     * 无条件查询
     * */
    public List<SquirrelLesson> selectAll(){
        return squirrelLessonMapper.selectAll();
    }

    /**
     * 多条件查询
     * */
    public List<SquirrelLesson> selectBy(SquirrelLesson lesson, String openId){
        List<SquirrelLesson> squirrelLessons = squirrelLessonMapper.selectBy(lesson);
        return squirrelLessons;
    }




    /**
     * 主键查询
     * */
    public SquirrelLesson selectByPrimaryKey(Integer id){

        return squirrelLessonMapper.selectByPrimaryKey(id);
    }

    /**
     * 获取lesson下词库
     * */
    public RequestInfo getWords(Integer lessonId){
        RequestInfo info = new RequestInfo();
        info.setDataList(squirrelLessonMapper.getWords(lessonId));
        return info;
    }


}
