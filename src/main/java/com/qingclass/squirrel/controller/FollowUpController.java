package com.qingclass.squirrel.controller;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.DuplicateKeyException;
import com.qingclass.squirrel.constant.Global;
import com.qingclass.squirrel.domain.statistic.SquirrelFollowUp;
import com.qingclass.squirrel.domain.wx.WxChannel;
import com.qingclass.squirrel.entity.MongoUser;
import com.qingclass.squirrel.entity.SessionSquirrelUser;
import com.qingclass.squirrel.mapper.cms.SquirrelPicturebookMapper;
import com.qingclass.squirrel.mapper.cms.WxChannelMapper;
import com.qingclass.squirrel.mapper.statistic.FollowUpMapper;
import com.qingclass.squirrel.mapper.user.SquirrelUserMapper;
import com.qingclass.squirrel.service.WxService;
import com.qingclass.squirrel.utils.HttpHelper;
import com.qingclass.squirrel.utils.OssUtil;
import com.qingclass.squirrel.utils.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;


/***
 *_______________#########_______________________
 *______________############_____________________
 *______________#############____________________
 *_____________##__###########___________________
 *____________###__######_#####__________________
 *____________###_#######___####_________________
 *___________###__##########_####________________
 *__________####__###########_####_______________
 *________#####___###########__#####_____________
 *_______######___###_########___#####___________
 *_______#####___###___########___######_________
 *______######___###__###########___######_______
 *_____######___####_##############__######______
 *____#######__#####################_#######_____
 *____#######__##############################____
 *___#######__######_#################_#######___
 *___#######__######_######_#########___######___
 *___#######____##__######___######_____######___
 *___#######________######____#####_____#####____
 *____######________#####_____#####_____####_____
 *_____#####________####______#####_____###______
 *______#####______;###________###______#________
 *________##_______####________####______________
 */

/**
 * 该类为 跟读 类，对应趣跟读功能
 *
 * @author 苏天奇
 * */
@RestController
@RequestMapping(value = "/follow-up")
public class FollowUpController {

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private WxService wxService;
    @Autowired
    private Global global;
    @Autowired
    private SquirrelPicturebookMapper squirrelPicturebookMapper;
    @Autowired
    private FollowUpMapper followUpMapper;
    @Autowired
    private SquirrelUserMapper squirrelUserMapper;
    @Autowired
    private WxChannelMapper wxChannelMapper;
    @Value("${follow.up.qr}")
    private String qrCode;


    private Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     *
     * */
    @PostMapping(value = "up")
    public Map<String,Object> up(HttpServletRequest req, @RequestParam(value = "serverId")String serverId,
                                     @RequestParam(value = "levelId")Integer levelId,
                                     @RequestParam(value = "lessonId")Integer lessonId,
                                     @RequestParam(value = "content")String content,
                                    @RequestParam(value = "type")String type,
                                 @RequestParam(value = "audition",required = false)Integer audition) {

        SessionSquirrelUser sessionUserInfo = (SessionSquirrelUser) req.getSession().getAttribute(SessionSquirrelUser.SESSION_SQUIRREL_USER_KEY);
        String openId = sessionUserInfo.getOpenId();

        logger.info("serverId : " + serverId);
        if(serverId == null){
            return Tools.f("serverId is null......");
        }
        HttpHelper httpHelper = new HttpHelper();
        File source = httpHelper.fetchTmpFile(serverId, "video");//取微信临时素材(录音)

        if(source == null){
            return Tools.f("转换失败，请查看微信获取临时素材返回值...");
        }


        UUID uuid = UUID.randomUUID();
        File target = new File(""+ uuid+".mp3");
        it.sauronsoftware.jave.AudioUtils.amrToMp3(source, target);//转码

        OssUtil ossUtil = new OssUtil();
        String fileName = ossUtil.executeUpLoad(global.getOssSoundPath(), target, target.getName());//上传到oss


        //语音评测
        Map<String, Object> a = httpHelper.sendPostYunZ(content, "A", target);


        Map<String,Object> map = new HashMap<>();
        map.put("voice",fileName);
        map.put("Yun",a);

        if(audition == null || audition == 0){
            asyncSave(openId,levelId,lessonId,type,content,fileName,a.get("score").toString());//记录到mongo
        }

        if(source.delete()){
            logger.info("amr delete successful...");
        }else{
            logger.error("amr delete failed...");
        }
        if(target.delete()){
            logger.info("mp3 delete successful...");
        }else{
            logger.error("mp3 delete failed...");
        }

        return Tools.s(map);
    }

    @Async
    protected void asyncSave(String openId, Integer levelId, Integer lessonId, String type, String text, String fileName, String score){
        text = text.replaceAll("\\.","&dot;");

        StringBuffer key = new StringBuffer();
        key.append("learnHistory.");
        key.append("subjects.subject-").append(1000000).append(".");
        key.append("levels.level-").append(levelId).append(".");
        key.append("lessons.lesson-").append(lessonId);

        if(type.equals("word")){
            key.append(".follow.word.");
            key.append(text);
        }else if(type.equals("picbook")){
            key.append(".follow.picbook.");
            key.append(text);
        }else {
            return;
        }

        Map<String,Object> map = new HashMap<>();
        map.put("site",fileName);
        map.put("score",score);

        Query query = new Query(Criteria.where("_id").is(openId));
        Update update = new Update().set(key.toString(), map);
        try {
            mongoTemplate.upsert(query, update, MongoUser.class);
        }catch (DuplicateKeyException e){
            logger.error("mongo用户主键冲突，_id="+openId);
        }
    }

    @PostMapping(value = "record")
    public Map<String,Object> record(HttpServletRequest req,
                                 @RequestParam(value = "levelId")Integer levelId,
                                 @RequestParam(value = "lessonId")Integer lessonId,
                                 @RequestParam(value = "type")String type) {

        SessionSquirrelUser sessionUserInfo = (SessionSquirrelUser) req.getSession().getAttribute(SessionSquirrelUser.SESSION_SQUIRREL_USER_KEY);
        String openId = sessionUserInfo.getOpenId();
        int subjectId = 1000000;


        DBObject dbObject = new BasicDBObject();
        ((BasicDBObject) dbObject).put("_id",openId);
        DBObject dbFieldObject = new BasicDBObject();


        if(type.equals("word")){
            ((BasicDBObject) dbFieldObject).put(("learnHistory.subjects.subject-"+subjectId+".levels.level-"+levelId+".lessons.lesson-"+lessonId+".follow.word"),1);
        }else if(type.equals("picbook")){
            ((BasicDBObject) dbFieldObject).put(("learnHistory.subjects.subject-"+subjectId+".levels.level-"+levelId+".lessons.lesson-"+lessonId+".follow.picbook"),1);
        }else {
            Tools.f();
        }

        Query query = new BasicQuery(dbObject,dbFieldObject);
        MongoUser mongoUser = mongoTemplate.findOne(query, MongoUser.class);

        Map<String, Object> map = new HashMap<>();
        if(type.equals("word")){
            Map m1 = (Map<String,Object>)mongoUser.getLearnHistory().get("subjects");  // 原本的value
            Map m2 = (Map<String,Object>)m1.get("subject-" + subjectId);
            Map m3 = (Map<String,Object>)m2.get("levels");
            Map m4 = (Map<String,Object>)m3.get("level-" + levelId);
            if(m4 != null){
                Map m5 = (Map<String,Object>)m4.get("lessons");
                if(m5 !=null){
                    Map<String,Object> o = (Map<String,Object>)m5.get("lesson-" + lessonId);
                    if(o != null){
                        Map<String,Object> follow = (Map<String,Object>)o.get("follow");
                        if(follow != null){
                            map = (Map<String,Object>)follow.get("word");

                        }
                    }
                }
            }

        }else if(type.equals("picbook")){
            Map m1 = (Map<String,Object>)mongoUser.getLearnHistory().get("subjects");  // 原本的value
            Map m2 = (Map<String,Object>)m1.get("subject-" + subjectId);
            Map m3 = (Map<String,Object>)m2.get("levels");
            Map m4 = (Map<String,Object>)m3.get("level-" + levelId);
            if(m4 != null){
                Map m5 = (Map<String,Object>)m4.get("lessons");
                if(m5 !=null){
                    Map<String,Object> o = (Map<String,Object>)m5.get("lesson-" + lessonId);
                    if(o != null){
                        Map<String,Object> follow = (Map<String,Object>)o.get("follow");
                        if(follow != null){
                            map = (Map<String,Object>)follow.get("picbook");

                        }
                    }
                }
            }

        }

        return Tools.s(map);
    }

    @PostMapping(value = "entrance")
    public Map<String,Object> entrance(HttpServletRequest req,
                                     @RequestParam(value = "levelId")Integer levelId,
                                     @RequestParam(value = "lessonId")Integer lessonId) {

        SessionSquirrelUser sessionUserInfo = (SessionSquirrelUser) req.getSession().getAttribute(SessionSquirrelUser.SESSION_SQUIRREL_USER_KEY);
        String openId = sessionUserInfo.getOpenId();
        int subjectId = 1000000;


        DBObject dbObject = new BasicDBObject();
        ((BasicDBObject) dbObject).put("_id",openId);
        DBObject dbFieldObject = new BasicDBObject();
        ((BasicDBObject) dbFieldObject).put(("learnHistory.subjects.subject-"+subjectId+".levels.level-"+levelId+".lessons.lesson-"+lessonId+".follow"),1);

        Query query = new BasicQuery(dbObject,dbFieldObject);
        MongoUser mongoUser = mongoTemplate.findOne(query, MongoUser.class);

        Map<String, Object> map = new HashMap<>();

        Map m1 = (Map<String,Object>)mongoUser.getLearnHistory().get("subjects");  // 原本的value
        Map m2 = (Map<String,Object>)m1.get("subject-" + subjectId);
        Map m3 = (Map<String,Object>)m2.get("levels");
        Map m4 = (Map<String,Object>)m3.get("level-" + levelId);
        if(m4 != null){
            Map m5 = (Map<String,Object>)m4.get("lessons");
            if(m5 !=null){
                Map<String,Object> o = (Map<String,Object>)m5.get("lesson-" + lessonId);
                if(o != null){
                    Map<String,Object> follow = (Map<String,Object>)o.get("follow");
                    if(follow != null){
                        Map<String,Object> wordMap = (Map<String,Object>)follow.get("word");
                        Map<String,Object> picbookMap = (Map<String,Object>)follow.get("picbook");

                        map.put("wordMap",wordMap);
                        map.put("picbookMap",picbookMap);
                    }
                }
            }
        }
        return Tools.s(map);
    }


    @PostMapping(value = "on-read-count")
    public Map<String,Object> onReadCount(HttpServletRequest req, @RequestParam(value = "type")String type){
        SessionSquirrelUser sessionUserInfo = (SessionSquirrelUser) req.getSession().getAttribute(SessionSquirrelUser.SESSION_SQUIRREL_USER_KEY);
        String openId = sessionUserInfo.getOpenId();

        Query query = new Query(Criteria.where("_id").is(openId));
        MongoUser mongoUser = mongoTemplate.findOne(query, MongoUser.class);

        Map<String,Object> m1 = (Map<String,Object>)mongoUser.getLearnHistory().get("subjects");  // 原本的value
        Map<String,Object> m2 = (Map<String,Object>)m1.get("subject-" + 1000000);
        Map<String,Object> m3 = (Map<String,Object>)m2.get("levels");

        int alWords = 0;
        List<Integer> les = new ArrayList<>();

        for(Map.Entry<String, Object> entry : m3.entrySet()){
            Map<String,Object> m4 = (Map<String,Object>)entry.getValue();//levels
            if(m4 != null){
                for(Map.Entry<String, Object> en : m4.entrySet()){
                    Map<String,Object> lessons = (Map<String,Object>)en.getValue();
                    if(lessons != null){
                        for(Map.Entry<String, Object> entry2 : lessons.entrySet()){
                            Map<String,Object> m5 = (Map<String,Object>)entry2.getValue();
                            if(m5 != null){
                                Map<String,Object> m6 = (Map<String,Object>)m5.get("follow");
                                if(m6 != null){
                                    if(type.equals("word")){
                                        Map<String,Object> m7 = (Map<String,Object>)m6.get("word");
                                        if(m7 != null){
                                            for(Map.Entry<String, Object> entry3 : m7.entrySet()){
                                                alWords ++;
                                            }
                                        }
                                    }else if(type.equals("picbook")){
                                        Map<String,Object> m7 = (Map<String,Object>)m6.get("picbook");
                                        if(m7 != null){
                                            les.add(Integer.parseInt(entry2.getKey().split("-")[1]));
                                        }
                                    }

                                }
                            }
                        }
                    }
                }
            }
        }
        Map<String, Object> map = new HashMap<>();
        if(type.equals("word")){
            map.put("alCount",alWords);
        }else if(type.equals("picbook")){
            int alPicbook = 0;

            if(les.size() > 0){
                alPicbook = squirrelPicturebookMapper.selectGroupCountByLessonIds(les);
            }
            map.put("alCount",alPicbook);
        }else{
            return Tools.f();
        }

        map.put("channel",wxChannelMapper.getChannelSiteByCode(qrCode));

        return Tools.s(map);
    }


    @PostMapping(value = "action")
    public Map<String,Object> action(HttpServletRequest req, @RequestParam(value = "type")Integer type,
                                     @RequestParam(value = "levelId")Integer levelId){
        SessionSquirrelUser sessionUserInfo = (SessionSquirrelUser) req.getSession().getAttribute(SessionSquirrelUser.SESSION_SQUIRREL_USER_KEY);
        String openId = sessionUserInfo.getOpenId();
        SquirrelFollowUp squirrelFollowUp = new SquirrelFollowUp();

        squirrelFollowUp.setOpenId(openId);
        squirrelFollowUp.setLevelId(levelId);
        switch (type){
            case 1 : squirrelFollowUp.setClickWordUp(1);break;
            case 2 : squirrelFollowUp.setClickPicbookUp(1);break;
            case 3 : squirrelFollowUp.setFinishWordUp(1);break;
            case 4 : squirrelFollowUp.setFinishPicbookUp(1);break;
            case 5 : squirrelFollowUp.setShareWordUp(1);break;
            case 6 : squirrelFollowUp.setSharePicbookUp(1);break;
        }

        Integer id = followUpMapper.selectByOpenIdAndLevelId(openId, levelId);
        if(id == null){
            followUpMapper.insert(squirrelFollowUp);
        }else{
            squirrelFollowUp.setId(id);
        }

        followUpMapper.update(squirrelFollowUp);

        return Tools.s();
    }

    @PostMapping(value = "action-entry")
    public Map<String,Object> actionEntry(@RequestParam(value = "levelId")Integer levelId){

        SquirrelFollowUp squirrelFollowUp = new SquirrelFollowUp();

        squirrelFollowUp.setLevelId(levelId);

        //查询今天应kai课的人数
        squirrelFollowUp.setOnRead(squirrelUserMapper.selectOnReadCountByLevelId(levelId));

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        squirrelFollowUp.setDate(sdf.format(new Date()));

        Integer id = followUpMapper.followUpStatisticExist(sdf.format(new Date()),levelId);
        if(id == null){
            followUpMapper.followUpStatisticInsert(squirrelFollowUp);
            followUpMapper.followUpStatisticUpdate(squirrelFollowUp);
        }else {
            squirrelFollowUp.setId(id);
            squirrelFollowUp.setEntryShare(1);
            followUpMapper.followUpStatisticUpdate(squirrelFollowUp);
        }


        return Tools.s();
    }


}
