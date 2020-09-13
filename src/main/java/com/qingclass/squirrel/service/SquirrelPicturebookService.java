package com.qingclass.squirrel.service;

import com.alibaba.fastjson.JSONObject;
import com.qingclass.squirrel.domain.cms.LessonMidPicturebook;
import com.qingclass.squirrel.domain.cms.PicturebookPart;
import com.qingclass.squirrel.domain.cms.RequestInfo;
import com.qingclass.squirrel.domain.cms.SquirrelPicturebook;
import com.qingclass.squirrel.entity.SquirrelUser;
import com.qingclass.squirrel.mapper.cms.SquirrelLessonMapper;
import com.qingclass.squirrel.mapper.cms.SquirrelPicturebookMapper;
import com.qingclass.squirrel.mapper.user.SquirrelUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class,value = "squirrelResourceTransaction")
public class SquirrelPicturebookService {


    @Autowired
    private SquirrelPicturebookMapper squirrelPicturebookMapper;
    @Autowired
    SquirrelUserMapper squirrelUserMapper;
    @Autowired
    SquirrelLessonMapper squirrelLessonMapper;


    public RequestInfo selectAll(){
        RequestInfo info = new RequestInfo();
        List<SquirrelPicturebook> squirrelPicturebooks = squirrelPicturebookMapper.selectAll();
        info.setDataList(squirrelPicturebooks);
        return info;
    }

    public RequestInfo insert(SquirrelPicturebook squirrelPicturebook){
        RequestInfo info = new RequestInfo();
        squirrelPicturebookMapper.insert(squirrelPicturebook);
        return info;
    }


    public int userAlreadyStudyCount(List<Integer> lessonIds){
        List<LessonMidPicturebook> lessonMidPicturebooks = squirrelPicturebookMapper.selectByLessonId(lessonIds);
        List<Integer> picIdList = new ArrayList<>();
        HashMap<Integer,Integer> map = new HashMap<>();

        lessonMidPicturebooks.forEach(e -> map.put(e.getPicId(),0));

        for(HashMap.Entry<Integer, Integer> entry : map.entrySet()){
            Integer key = entry.getKey();
            picIdList.add(entry.getKey());
            int part = 0;

            for(LessonMidPicturebook mid : lessonMidPicturebooks){
                if(mid.getPicId().toString().equals(key.toString()) ){
                    part ++;
                }
            }
            entry.setValue(part);
        }
        int alreadyStudyPicturebook = 0;

        List<SquirrelPicturebook> spbs = new ArrayList<>();
        if(picIdList.size() != 0){
            spbs = squirrelPicturebookMapper.selectByPrimaryKeys(picIdList);
        }


        for(HashMap.Entry<Integer, Integer> entry : map.entrySet()){
            SquirrelPicturebook squirrelPicturebook = null;
            for(SquirrelPicturebook pb : spbs){
                if(pb.getId() == Integer.parseInt(entry.getKey().toString())){
                    squirrelPicturebook = pb;
                    break;
                }
            }

            if(squirrelPicturebook != null){
                if(squirrelPicturebook.getPart() == Integer.parseInt(entry.getValue().toString())){
                    alreadyStudyPicturebook ++;
                }
            }


        }

        return alreadyStudyPicturebook;
    }

    public List<SquirrelPicturebook> bookshelf(String openId, Integer levelId){
        List<SquirrelUser> squirrelUsers = squirrelUserMapper.selectBeginAtByOpenId(openId, levelId);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date beginAtDate = null;
        try {
            beginAtDate = sdf.parse(squirrelUsers.get(0).getBeginAt());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date date = new Date();
        long from1 = beginAtDate.getTime();
        long to1 = date.getTime();
        int days = (int) ((to1 - from1) / (1000 * 60 * 60 * 24));
        days = days + 1;

        List<SquirrelPicturebook> picList = squirrelLessonMapper.selectPicturebookPart(levelId, days);
        Calendar rightNow = Calendar.getInstance();
        for(SquirrelPicturebook spb : picList){
            rightNow.setTime(beginAtDate);
            rightNow.add(Calendar.DAY_OF_YEAR,spb.getOrder()-1);
            Date dt1=rightNow.getTime();
            spb.setBeginAt(dt1);
        }
        return picList;
    }

    public List<SquirrelPicturebook> bookshelfLearn(Integer levelId, String picIds){
        String[] split = picIds.split(",");
        List<Integer> picIdList = new ArrayList<>();
        for(String s : split){
            picIdList.add(Integer.parseInt(s));
        }
        List<PicturebookPart> picturebookParts = squirrelLessonMapper.selectPicturebookPartPicIds(levelId, picIdList);
        List<SquirrelPicturebook> picList = new ArrayList<>();

        while(true){//数据结构转换
            int pid = 0;
            SquirrelPicturebook spb = new SquirrelPicturebook();
            List<PicturebookPart> ppl = new ArrayList<>();
            boolean b = true;
            for(int i = 0 ; i < picturebookParts.size() ; i ++){
                PicturebookPart picturebookPart = picturebookParts.get(i);
                if(picturebookPart.getPicId() == null || picturebookPart.getPicId() == 0){
                    break;
                }
                JSONObject jsonObject = JSONObject.parseObject(picturebookPart.getPartContent());
                Map<String, Object> stringObjectMap = parseJsonObject(jsonObject);
                HashMap<String, Object> map = new HashMap<>();
                map.put("data", stringObjectMap);
                if(pid == 0){
                    picturebookPart.setPartContent(null);
                    picturebookPart.setDataMap(map);
                    pid = picturebookPart.getPicId();
                    spb.setName(picturebookPart.getPicName());
                    spb.setImage(picturebookPart.getImage());

                    String voice = (String)jsonObject.get("voice");

                    spb.setVoice(voice);//封面语音
                    ppl.add(picturebookPart);
                    picturebookParts.remove(i);
                    i--;
                }else{
                    if(pid == picturebookPart.getPicId()){
                        picturebookPart.setPartContent(null);
                        picturebookPart.setDataMap(map);
                        ppl.add(picturebookPart);
                        picturebookParts.remove(i);
                        i--;
                    }
                }
                if(i+1 == picturebookParts.size()){
                    b = false;
                }
            }

            List<PicturebookPart> newPpl = ppl.stream().sorted(Comparator.comparing(PicturebookPart::getPart)).collect(Collectors.toList());
            spb.setPartList(newPpl);
            if(spb.getName() != null){
                picList.add(spb);
            }
            if(b){
                break;
            }
        }
        return picList;
    }

    private Map<String,Object> parseJsonObject(JSONObject jsonObject){
        List<Map<String,Object>> picbookExplainList;
        try{
            picbookExplainList = (List<Map<String,Object>>)jsonObject.get("picbookExplainList");
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }


        for(Map<String,Object> map : picbookExplainList){
            Object image = map.get("image");
            Object sentence = map.get("sentence");
            Object sentenceTranslate = map.get("sentenceTranslate");
            Object sentenceVoice = map.get("sentenceVoice");
            map.clear();
            map.put("image",image);
            map.put("sentence",sentence);
            map.put("sentenceTranslate",sentenceTranslate);
            map.put("sentenceVoice",sentenceVoice);
        }
        Map<String, Object> map = new HashMap<String,Object>();
        map.put("picbookExplainList",picbookExplainList);
        return map ;
    }

}
