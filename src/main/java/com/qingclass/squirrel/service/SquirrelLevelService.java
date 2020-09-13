package com.qingclass.squirrel.service;

import com.qingclass.squirrel.domain.cms.SquirrelLevel;
import com.qingclass.squirrel.entity.SquirrelUser;
import com.qingclass.squirrel.mapper.cms.SquirrelLevelMapper;
import com.qingclass.squirrel.mapper.user.SquirrelUserMapper;
import com.qingclass.squirrel.utils.Tools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class,value = "squirrelResourceTransaction")
public class SquirrelLevelService {
	
	@Autowired
    private SquirrelLevelMapper squirrelLevelMapper;
	@Autowired
    private SquirrelUserMapper squirrelUserMapper;

    public List<SquirrelLevel> list(SquirrelLevel squirrelLevel){
        return squirrelLevelMapper.selectBy(squirrelLevel);
    }

    public Map<Integer,String> skinList(){
        List<SquirrelLevel> squirrelLevels = squirrelLevelMapper.selectSkin(1000000);

        Map<Integer,String> map = new HashMap<>();

        squirrelLevels.forEach(e -> map.put(e.getId(),e.getSkin()));

        return map;
    }


    public SquirrelLevel selectByPrimaryKey(Integer id){
        return squirrelLevelMapper.selectByPrimaryKey(id);
    }

    public List<SquirrelLevel> powerList(int userId){
        List<SquirrelLevel> squirrelLevels = squirrelLevelMapper.selectBySquirrelUserId(userId);

        for(int i = 0 ; i < squirrelLevels.size() ; i ++){
            String beginAt = squirrelLevels.get(i).getBeginAt();
            String vipBeginTime = squirrelLevels.get(i).getVipBeginTime();
            String vipEndTime = squirrelLevels.get(i).getVipEndTime();
            DateFormat fmt =new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date date = fmt.parse(beginAt);
                Date date2 = fmt.parse(vipBeginTime);
                Date date3 = fmt.parse(vipEndTime);
                squirrelLevels.get(i).setBeginDate(date);
                squirrelLevels.get(i).setVipBeginDate(date2);
                squirrelLevels.get(i).setVipEndDate(date3);

            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
        return squirrelLevels;
    }

    /**
     * 查询在有效期内的购买记录
     *
     * */
    public boolean selectEffectiveRecord(Integer userId, String levelId, Integer order){
        List<SquirrelUser> squirrelUsers = squirrelUserMapper.selectByLevelIdAndNowDate(Integer.parseInt(levelId), userId);
        if(squirrelUsers.size() > 0){
            SquirrelUser squirrelUser = squirrelUsers.get(0);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            String beginAt = squirrelUser.getBeginAt();
            Date parse = null;
            try {
                parse = sdf.parse(beginAt);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            Date nowLesson = Tools.addDate(parse,order - 1);

            String format1 = sdf.format(nowLesson);

            String format = sdf.format(new Date());

            boolean equals = format1.equals(format);

            return equals;

        }else{
            return false;
        }
    }
    
    /**
     * 查询在有效期内的购买记录
     *
     * */
    public SquirrelUser selectEffectiveLevelUser(Integer userId, String levelId){
        SquirrelUser squirrelUsers = squirrelUserMapper.selectUserByLevelIdAndNowDate(Integer.parseInt(levelId), userId);
        return squirrelUsers;
    }

}
