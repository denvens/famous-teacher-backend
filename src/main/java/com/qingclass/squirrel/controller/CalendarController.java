package com.qingclass.squirrel.controller;

import com.qingclass.squirrel.entity.MongoUser;
import com.qingclass.squirrel.entity.SessionSquirrelUser;
import com.qingclass.squirrel.utils.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
/**
 *
 *
 * @author 苏天奇
 * */
@RestController
@RequestMapping(value = "/calendar")
public class CalendarController {

    @Autowired
    private MongoTemplate mongoTemplate;
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     *
     * */
    @PostMapping(value = "record")
    public Map<String,Object> record(HttpServletRequest req){


        String subjectId = req.getParameter("subjectId")+"";
        String levelId = req.getParameter("levelId")+"";
        String lessonId = req.getParameter("lessonId")+"";

        SessionSquirrelUser sessionUserInfo = (SessionSquirrelUser) req.getSession().getAttribute(SessionSquirrelUser.SESSION_SQUIRREL_USER_KEY);
        String openId = sessionUserInfo.getOpenId();

        //条件查询
        Criteria criteria = new Criteria();
        criteria.orOperator(Criteria.where("_id").is(openId));
        Query q = new Query(criteria);
        q.fields().include("learnHistory.subjects.subject-"+subjectId+".levels.level-"+levelId+".lessons.lesson-"+lessonId);
        List<MongoUser> mongoUsers = mongoTemplate.find(q, MongoUser.class);

        return Tools.s(mongoUsers);
    }

}
