package com.qingclass.squirrel.utils;

import com.qingclass.squirrel.entity.MongoUser;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoDataUtil {

    private static final int SUBJECT_ID = 1000000;//subjectId

    public static Map<String, Object> getLessonValueByKeys(MongoUser one,String subjectId,String levelId,String lessonId){
        Map m1 = (Map<String,Object>)one.getLearnHistory().get("subjects");  // 原本的value
        Map m2 = (Map<String,Object>)m1.get("subject-" + subjectId);
        Map m3 = (Map<String,Object>)m2.get("levels");
        Map m4 = (Map<String,Object>)m3.get("level-" + levelId);
        if(m4 != null){
            Map m5 = (Map<String,Object>)m4.get("lessons");
            if(m5 !=null){
                Map<String,Object> o = (Map<String,Object>)m5.get("lesson-" + lessonId);
                if(o != null){
                    Map<String,Object> recordMap = (Map<String,Object>)o.get("record");
                    return recordMap;
                }
            }
        }

        return null;
    }
    
    public static Map<String, Object> getLevelValueByKeys(MongoUser one,String subjectId,String levelId){
        Map m1 = (Map<String,Object>)one.getLearnHistory().get("subjects");  // 原本的value
        Map m2 = (Map<String,Object>)m1.get("subject-" + subjectId);
        Map m3 = (Map<String,Object>)m2.get("levels");
        Map m4 = (Map<String,Object>)m3.get("level-" + levelId);
        if(m4 != null){
        	Map<String,Object> returnLessonMap = new HashMap<String,Object>();
        	Map<String,Object> lessonMap = (Map<String,Object>)m4.get("lessons");
            if(lessonMap != null){
                for(Map.Entry<String,Object> entry : lessonMap.entrySet()){
                    Map<String,Object> record = (Map<String,Object>)((Map<String,Object>)entry.getValue()).get("record");
                    if(record != null){
                    	System.out.println("record000:"+record);
                        if(record.get("isFinish")!= null && (boolean)record.get("isFinish")){
                        	returnLessonMap.put(entry.getKey(), entry.getValue());
                        }
                    }
                }
            }
            System.out.println("returnLessonMap-->:{}"+returnLessonMap);
            return returnLessonMap;
        }

        return null;
    }

    public static Map<String, Object> getLevelValueByKeysForOrder(MongoUser one,String subjectId,String levelId){
        Map m1 = (Map<String,Object>)one.getLearnHistory().get("subjects");  // 原本的value
        Map m2 = (Map<String,Object>)m1.get("subject-" + subjectId);
        Map m3 = (Map<String,Object>)m2.get("levels");
        Map m4 = (Map<String,Object>)m3.get("level-" + levelId);
        if(m4 != null){
        	Map<String,Object> lessonMap = (Map<String,Object>)m4.get("lessons");
            if(lessonMap != null){
            	return lessonMap;
            }
        }

        return null;
    }
    
    public static Map<String, Object> getUnitValueByKeys(MongoUser one,String subjectId,String levelId,String lessonId,String unitId){

        try{
            Map m1 = (Map<String,Object>)one.getLearnHistory().get("subjects");  // 原本的value
            Map m2 = (Map<String,Object>)m1.get("subject-" + subjectId);
            Map m3 = (Map<String,Object>)m2.get("levels");
            Map m4 = (Map<String,Object>)m3.get("level-" + levelId);
            Map m5 = (Map<String,Object>)m4.get("lessons");
            Map m6 = (Map<String,Object>)m5.get("lesson-" + lessonId);
            Map m7 = (Map<String,Object>)m6.get("units");
            if(m7 != null){
                Map<String,Object> o = (Map<String,Object>)m7.get("unit-" + unitId);

                if(o != null){
                    Map<String,Object> recordMap = (Map<String,Object>)o.get("record");
                    return recordMap;
                }
            }
        }catch (NullPointerException e){
            return null;
        }


        return null;
    }


    /**
     * 计算该用户全部学习天数,学完天数，分享天数
     * */
    public static Map<String,Integer> alreadyFinishDayAndWordsAll(MongoUser mongoUser){
        int alreadyFinishAll = 0;
        int alreadyStudyWords = 0;


        if(mongoUser != null && mongoUser.getLearnHistory() != null){
            Map<String,Object> map =  (Map<String, Object>) mongoUser.getLearnHistory().get("subjects");
            if(map != null){
                Map<String,Object> map1 = (Map<String, Object>) ((Map<String, Object>)map.get("subject-"+SUBJECT_ID)).get("levels");
                if(map1 != null){
                    for(Map.Entry<String,Object> entry : map1.entrySet()){
                        map1 = (Map<String,Object>)((Map<String,Object>)entry.getValue()).get("lessons");
                        if(map1 != null){
                            for(Map.Entry<String,Object> entry2 : map1.entrySet()){
                                Map<String,Object> record = (Map<String,Object>)((Map<String,Object>)entry2.getValue()).get("record");
                                if(record != null){
                                    if(record.get("isFinish")!= null && (boolean)record.get("isFinish")){
                                        alreadyFinishAll ++;
                                        if(record.get("lessonKeyWords")!= null) {
                                            try {
                                                alreadyStudyWords += Integer.parseInt(record.get("lessonKeyWords").toString());
                                            }catch (Exception e){

                                            }
                                        }
                                        continue;
                                    }
                                    if(record.get("isShare")!= null && (boolean)record.get("isShare")){
                                        alreadyFinishAll ++;
                                        if(record.get("lessonKeyWords")!= null) {
                                            try {
                                                alreadyStudyWords += Integer.parseInt(record.get("lessonKeyWords").toString());
                                            }catch (Exception e){

                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }


        Map<String,Integer> map = new HashMap<>();
        map.put("alreadyFinishAll",alreadyFinishAll);
        map.put("alreadyStudyWords",alreadyStudyWords);
        return map;
    }


}
