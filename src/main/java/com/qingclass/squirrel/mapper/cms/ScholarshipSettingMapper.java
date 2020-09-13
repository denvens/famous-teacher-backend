package com.qingclass.squirrel.mapper.cms;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import com.qingclass.squirrel.domain.cms.ScholarshipSetting;
import com.qingclass.squirrel.domain.cms.ScholarshipSettingDetails;
import com.qingclass.squirrel.domain.cms.UserLevel;


@Repository
public interface ScholarshipSettingMapper {

	@Select({
        "<script>",
        "	select ",
        "		u.id ",
        "	from ",
        "		squirrel.user_levels u ",
        "	left join squirrel_resource.squirrel_scholarship_setting schol ",
        "	on 	u.levelId=schol.level_id	",
        "	where ",
        "		u.createdAt between schol.buy_start_time and schol.buy_end_time ",
        "		and u.delKey = 0 ",
        "		AND u.vipEndTime &gt;= now() ",
        "		AND u.squirrelUserId = #{userId}",
        "</script>"
	})
	List<ScholarshipSetting> getScholarshipByUserId(@Param("userId")Integer userId);

	@Select({
        "<script>",
        "	select ",
        "		sl.name,   ",
        "		sl.lessonDay, ",
        "		sl.return_fee_day returnFeeDay,",
        "		u.beginAt beginClassTime, ",
        "		date_add(u.beginAt, interval sl.lessonDay-1 day) endClassTime,",
        "		u.levelId,   ",
        "		sl.subjectId ",
        "	from ",
        "		msyb.user_levels u ",
        "	left join msyb_resource.squirrel_levels sl on sl.id=u.levelId ",
        "	where ",
        "		u.delKey = 0 ",
        "		AND u.vipEndTime &gt;= now() ",
        "		AND u.squirrelUserId = #{userId}",
        "</script>"
	})
	List<ScholarshipSettingDetails> getScholarshipDetailsByUserId(@Param("userId")Integer userId);
	
	@Select({
        "<script>",
        "	select ",
        "		u.beginAt beginClassTime, ",
        "		date_add(u.beginAt, interval sl.lessonDay-1 day) endClassTime,",
        "		u.vipEndTime, ",
        "		u.beginAt, ",
        "		su.openId, ",
        "		sl.subjectId, ",
        "		u.levelId, sl.name,   ",
        "		sl.lessonDay,  ",
        "		to_days(now())-to_days(beginAt)+1 as beginDays  ",
        "	from ",
        "		squirrel.user_levels u ",
        "	left join squirrel_resource.squirrel_levels sl on sl.id=u.levelId ",
        "	left join squirrel.squirrel_users su on su.id=u.squirrelUserId  ",
        "	where ",
        "		u.delKey = 0 ",
        "		AND u.vipEndTime &gt;= now() ",
        "		AND u.squirrelUserId = #{userId} ",
        "		and u.levelId = #{levelId} ",
        "</script>"
	})
	List<ScholarshipSettingDetails> getScholarshipCashByUserIdAndlevelId(
			@Param("userId")Integer userId,
			@Param("levelId")Integer levelId);
}