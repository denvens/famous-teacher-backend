package com.qingclass.squirrel.mapper.user;

import com.qingclass.squirrel.domain.cms.SquirrelLevel;
import com.qingclass.squirrel.entity.ConversionPush;
import com.qingclass.squirrel.entity.SquirrelUserBehavior;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import com.qingclass.squirrel.entity.SquirrelUser;

import java.util.List;
import java.util.Map;

@Repository
public interface SquirrelUserMapper {

	@Select("SELECT id, openId, unionId, nickName, sex, headImgUrl FROM squirrel_users WHERE id=#{id};")
	SquirrelUser selectById(int id);
	
	@Select("SELECT id, openId, unionId, nickName, sex, headImgUrl,bgmStatus FROM squirrel_users WHERE openId=#{openId};")
	SquirrelUser selectByOpenId(String openId);
	
	@Update("UPDATE squirrel_users SET openId=#{openId}, unionId=#{unionId}, nickName=#{nickName}, sex=#{sex}, headImgUrl=#{headImgUrl} WHERE id=#{id};")
	void update(SquirrelUser squirrelUser);

	@Update("UPDATE squirrel_users SET bgmStatus=#{bgmStatus} WHERE openId=#{openId};")
	void updateBgmStatusByOpenId(@Param("openId")String openId,@Param("bgmStatus")Integer bgmStatus);

	@Insert({ "<script>", "insert into user_levels(squirrelUserId,levelId,transactionId,createdAt,beginAt,vipBeginTime,vipEndTime)",
			"values(#{squirrelUserId,jdbcType=VARCHAR},#{levelId,jdbcType=INTEGER},#{transactionId,jdbcType=INTEGER},now(),#{beginAt,jdbcType=VARCHAR},#{vipBeginTime},#{vipEndTime})",
			"</script>" })
	int insertUserLevel(@Param(value = "squirrelUserId") int squirrelUserId, @Param(value = "levelId") Integer levelId,
			@Param(value = "transactionId") Integer transactionId, @Param(value = "beginAt") String beginAt, @Param(value = "vipBeginTime")String vipBeginTime,
						@Param(value = "vipEndTime")String vipEndTime);

	@Insert({ "<script>", "insert into user_levels(squirrelUserId,levelId,transactionId,createdAt,beginAt,vipBeginTime,vipEndTime,channelCode)",
			"values(#{squirrelUserId,jdbcType=VARCHAR},#{levelId,jdbcType=INTEGER},#{transactionId,jdbcType=INTEGER},now(),#{beginAt,jdbcType=VARCHAR},#{vipBeginTime},#{vipEndTime},#{code})",
			"</script>" })
	int insertUserLevelIncludeCode(@Param(value = "squirrelUserId") int squirrelUserId, @Param(value = "levelId") Integer levelId,
						@Param(value = "transactionId") Integer transactionId, @Param(value = "beginAt") String beginAt, @Param(value = "vipBeginTime")String vipBeginTime,
						@Param(value = "vipEndTime")String vipEndTime,@Param(value = "code")String code);

	@Update({
			"<script>",
			"update user_levels",
			"<set>",
			"<if test='beginAt != null'>",
			"beginAt = #{beginAt},",
			"</if>",
			"<if test='vipBeginTime != null'>",
			"vipBeginTime = #{vipBeginTime},",
			"</if>",
			"<if test='vipEndTime != null'>",
			"vipEndTime = #{vipEndTime},",
			"</if>",
			"</set>",
			"where id = #{id}",
			"</script>"
	})
	int updateUserLevel(@Param(value = "beginAt") String beginAt, @Param(value = "vipBeginTime")String vipBeginTime,
						@Param(value = "vipEndTime")String vipEndTime,@Param(value = "id")Integer id);



	@Insert("INSERT INTO "+
			"	squirrel_users(openId, unionId, nickName, sex,headImgUrl) "+
			"VALUES(#{p.openId}, #{p.unionId}, #{p.nickName}, #{p.sex}, #{p.headImgUrl})")
	@Options(useGeneratedKeys = true, keyProperty = "p.id", keyColumn = "id")
	int insert(@Param(value = "p") SquirrelUser squirrelUser);

	@Select({
			"<script>",
			"select ",
			"	beginAt,levelId,transactionId,vipBeginTime,vipEndTime ",
			"from ",
			"	user_levels ",
			"where ",
			"	levelId = #{levelId} ",
			"	and squirrelUserId in(",
			"		select id from squirrel_users where openId=#{openId}",
			")",
			"</script>"
	})
	List<SquirrelUser> selectBeginAtByOpenId(@Param("openId")String openId, @Param("levelId")Integer levelId);

	@Select({
			"<script>",
			"select ",
			"	u.openId,l.beginAt, sl.name as levelName, u.nickName as nickName, ",
			"	u.id userId, l.levelId ",
			"from msyb.user_levels l ",
			"left join msyb.squirrel_users u on l.squirrelUserId = u.id ",
			"left join `msyb_resource`.`squirrel_levels` sl on l.levelId = sl.id",
			"where ",
			"	l.beginAt like concat('%',#{beginAt},'%')",
			"	and l.vipEndTime &gt; l.beginAt",
			"</script>"
	})
	List<SquirrelUser> selectByBeginAt(@Param("beginAt")String value);

	

	@Update("UPDATE squirrel_users SET subscribe=#{subscribe} WHERE openId=#{openId};")
	void updateSubscribeByOpenId(@Param("openId") String openId,@Param("subscribe")int subscribe);

	@Select({
			"<script>",
			" select ", 
			"	id,beginAt, levelId, transactionId, vipBeginTime, vipEndTime ",
			" from ", 
			"	user_levels ",
			" where levelId = #{levelId} ",
			"	and squirrelUserId = #{userId} ",
			"	and vipBeginTime &lt;= now() ",
			"	and vipEndTime &gt;= now()",
			"</script>"
	})
	List<SquirrelUser> selectByLevelIdAndNowDate(@Param("levelId")int levelId, @Param("userId")int userId);

	@Select({
		"<script>",
		" select ", 
		"	ul.id, ul.beginAt, ul.levelId, ul.transactionId, ul.vipBeginTime, ul.vipEndTime, ",
		"	u.openId, u.nickName  ",
		" from ", 
		"	user_levels ul ",
		" left join msyb.squirrel_users u on u.id=ul.squirrelUserId ",
		" where ul.levelId = #{levelId} ",
		"	and ul.vipBeginTime &lt;= now() ",
		"	and ul.vipEndTime &gt;= now()",
		"</script>"
	})
	List<SquirrelUser> selectByLevelId(@Param("levelId")int levelId);
	
	@Select({
			"<script>",
			" select ", 
			"	id,beginAt, levelId, transactionId, vipBeginTime, vipEndTime ",
			" from ", 
			"	user_levels ",
			" where levelId = #{levelId} ",
			"	and squirrelUserId = #{userId} ",
//			"	and vipBeginTime &lt;= now() ",
//			"	and vipEndTime &gt;= now()",
			"</script>"
	})
	SquirrelUser selectUserByLevelIdAndNowDate(@Param("levelId")int levelId, @Param("userId")int userId);
	
	@Select({
			"<script>",
			"select id,beginAt,levelId,transactionId,vipBeginTime,vipEndTime from user_levels where levelId = #{levelId} and",
			"squirrelUserId = #{userId}",
			"</script>"
	})
	List<SquirrelUser> selectByLevelIdAndUserId(@Param("levelId")int levelId, @Param("userId")int userId);

	@Select({
			"<script>",
			"select ul.id,ul.beginAt as beginAtDate,ul.levelId,ul.vipBeginTime as vipBeginDate,ul.vipEndTime as vipEndDate,sl.buySite,sl.name as levelName,sl.id as levelId ",
			"from user_levels ul ",
			"left join msyb_resource.squirrel_levels sl on ul.levelId = sl.id ",
			"where ",
			" ul.squirrelUserId  in(select id from msyb.squirrel_users where openId = #{openId}) ",
			" and ul.levelId = #{levelId}",
			"and ul.vipBeginTime &lt;= now() and ul.vipEndTime &gt;= now()",
			"order by createdAt asc",
			"</script>"
	})
	List<SquirrelUser> selectPurchaseRecordByOpenIdAndLevelIdAtVip(@Param("openId") String openId, @Param("levelId")Integer levelId);

	@Select({
			"<script>",
			"select id,beginAt,levelId,transactionId,vipBeginTime,vipEndTime from user_levels where levelId = #{levelId} and",
			"squirrelUserId = #{userId} and ((vipBeginTime &lt;= now() and vipEndTime &gt;= now()) or vipBeginTime > now())",
			"</script>"
	})
	List<SquirrelUser> selectByLevelIdAndNowDateAfter(@Param("levelId")int levelId, @Param("userId")int userId);

	@Select({
			"<script>",
			"select ",
			"	ul.id,ul.beginAt as beginAtDate,ul.levelId,ul.vipBeginTime as vipBeginDate,",
			"	ul.vipEndTime as vipEndDate,sl.buySite,sl.name as levelName,sl.id as levelId,ul.sendLessonDays ",
			"from ",
		    "	user_levels ul ",
		    "left join ",
		    "	squirrel_resource.squirrel_levels sl on ul.levelId = sl.id where",
			"	squirrelUserId = #{userId} ",
			"order by createdAt asc",
			"</script>"
	})
	List<SquirrelUser> selectPurchaseRecordByUserId(@Param("userId") Integer userId);

	@Select({
			"<script>",
			"select ",
			"	ul.id,ul.beginAt as beginAtDate,ul.levelId,ul.vipBeginTime as vipBeginDate,",
			"	ul.vipEndTime as vipEndDate,sl.buySite,sl.name as levelName,sl.id as levelId ",
			"from msyb.user_levels ul ",
			"left join ",
			"	msyb_resource.squirrel_levels sl on ul.levelId = sl.id ",
			"where",
			"	ul.squirrelUserId in(",
			"		select id from msyb.squirrel_users where openId = #{openId}",
			"	) and ul.levelId = #{levelId}	",
			"order by createdAt asc",
			"</script>"
	})
	List<SquirrelUser> selectPurchaseRecordByOpenIdAndLevelId(@Param("openId") String openId, @Param("levelId")Integer levelId);

	@Select({
			"<script>",
			"select id,openId,channelCode,subscribe from user_behavior where",
			"openId = #{openId} and channelCode = #{channelCode} and type = 'subscribe'",
			"</script>"
	})
	SquirrelUserBehavior selectUserBehaviorByOpenIdAndChannelCode(@Param("openId")String openId, @Param("channelCode")String channelCode);

	@Select({
			"<script>",
			"select id,openId,channelCode,subscribe from user_behavior where",
			"openId = #{openId} and type = 'subscribe'",
			"</script>"
	})
	List<SquirrelUserBehavior> selectUserBehaviorByOpenIdAndSubscribe(@Param("openId")String openId);

	@Select({
			"<script>",
			"select openId from user_behavior where",
			"channelCode = #{code} and type = 'subscribe' and subscribe = 1 and DATE_FORMAT(createdAt,'%Y-%m-%d') = #{date} ",
			"</script>"
	})
	List<String> selectUserBehaviorByDateAndSubscribeAndCode(@Param("code")String code,@Param("date")String date);

	@Select({
			"<script>",
			"select count(id) from user_levels where DATE_FORMAT(createdAt,'%Y-%m-%d') = #{date} and levelId = #{levelId} and squirrelUserId in",
			"<foreach item='item' index='index' collection='openIds' open='(' separator=',' close=')'>",
			"(select id from squirrel_users where openId = #{item})",
			"</foreach>",
			"</script>"
	})
	int selectPurchaseCountByDate(@Param("openIds")List<String> openIds,@Param("date")String date,@Param("levelId")Integer levelId);

	@Insert({
			"<script>",
			"insert into user_behavior(openId,channelCode,subscribe,createdAt,updatedAt,type,note,levelId)",
			"values(#{openId},#{channelCode},#{subscribe},now(),now(),#{type},#{note},#{levelId})",
			"</script>"
	})
	void insertUserBehavior(@Param("openId")String openId, @Param("channelCode")String channelCode, @Param("subscribe")Integer subscribe,
							@Param("type")String type,@Param("note")String note,@Param("levelId")Integer levelId);

	@Update({
			"<script>",
			"update user_behavior ",
			"<set>",
			"<if test='channelCode != null'>",
			"channelCode = #{channelCode},",
			"</if>",
			"<if test='subscribe != null'>",
			"subscribe = #{subscribe},",
			"</if>",
			"<if test='levelId != null'>",
			"levelId = #{levelId},",
			"</if>",
			"</set>",
			"where id = #{id}",
			"</script>"
	})
	void updateUserBehavior(@Param("id")Integer id, @Param("channelCode")String channelCode, @Param("subscribe")Integer subscribe, @Param("levelId")Integer levelId);


	

	@Select("select * from user_behavior a where subscribe=1 and updatedAt >= DATE_SUB(STR_TO_DATE(curdate(), '%Y-%m-%d'), INTERVAL 1 DAY) and updatedAt < curdate()")
	List<SquirrelUserBehavior> selectUserBehavior();

	@Select("SELECT a.levelId,count(1) as onRead FROM  user_levels a,squirrel_resource.squirrel_levels b,squirrel_resource.squirrel_lessons c " +
			"where a.levelId=b.id and b.id=c.levelId and CURRENT_DATE()=DATE_ADD(a.beginAt, INTERVAL c.`order` DAY) and b.isOpen='1' and b.delKey='0' and c.delKey='0' and c.audition='0'  GROUP BY a.levelId")
	List<Map<String, Integer>> selectOnReadCount();

	@Select("SELECT count(a.id) FROM  user_levels a,squirrel_resource.squirrel_levels b,squirrel_resource.squirrel_lessons c " +
			"where a.levelId=b.id and b.id=c.levelId and CURRENT_DATE()=DATE_ADD(a.beginAt, INTERVAL c.`order` DAY) and b.isOpen='1' and b.delKey='0' and c.delKey='0' and c.audition='0' and a.levelId = #{levelId}")
	int selectOnReadCountByLevelId(Integer levelId);

	@Select("select ifnull(count(1),0) from user_behavior a where levelId = #{levelId} and openId = #{openId} and subscribe=1 and updatedAt < DATE_ADD(STR_TO_DATE(curdate(), '%Y-%m-%d'), INTERVAL 1 DAY) and updatedAt >= curdate()")
	int selectBehavior(Map<String, Object> params);


	@Select({
			"<script>",
			"select channelCode from user_behavior where openId = #{openId} and subscribe = 1 and type = 'subscribe'",
			"</script>"
	})
	String selectCodeInBehavior(String openId);


	@Select({
			"<script>",
			"select id,openId,flag,levelId,isSend,isPurchase,createdAt,updatedAt from user_conversion_push",
			"<where>",
			"<if test='openId != null'>",
			"AND openId = #{openId}",
			"</if>",
			"<if test='flag != null'>",
			"AND flag = #{flag}",
			"</if>",
			"<if test='levelId != null'>",
			"AND levelId = #{levelId}",
			"</if>",
			"<if test='isSend != null'>",
			"AND isSend = #{isSend}",
			"</if>",
			"<if test='isPurchase != null'>",
			"AND isPurchase = #{isPurchase}",
			"</if>",
			"</where>",
			"</script>"
	})
	List<ConversionPush> selectConversionByOpenId(ConversionPush conversionPush);


	@Select({
			"<script>",
			"select id,openId from user_conversion_push",
			"where",
			"<if test = 'levelId == null'>",
			"levelId is null",
			"</if>",
			"<if test = 'levelId != null'>",
			"levelId = #{levelId}",
			"</if>",
			"and flag = #{flag} and updatedAt like concat(#{updatedAt},'%') and isSend = 0 and isPurchase = 0",
			"</script>"
	})
	List<ConversionPush> selectConversionByLevelIdAndUpdatedAtAndFlag(@Param("levelId")Integer levelId,@Param("flag")Integer flag,@Param("updatedAt")String updatedAt);

	@Insert({
			"<script>",
			"insert into user_conversion_push(openId,flag,levelId,isSend,isPurchase,createdAt,updatedAt) values",
			"(#{openId},#{flag},#{levelId},#{isSend},#{isPurchase},now(),now())",
			"</script>"
	})
	void insertConversionPush(ConversionPush conversionPush);

	@Update({
			"<script>",
			"update user_conversion_push",
			"<set>",
			"<if test = 'flag != null'>",
			"flag = #{flag},",
			"</if>",
			"<if test = 'levelId != null'>",
			"levelId = #{levelId},",
			"</if>",
			"<if test = 'isSend != null'>",
			"isSend = #{isSend},",
			"</if>",
			"<if test = 'isPurchase != null'>",
			"isPurchase = #{isPurchase},",
			"</if>",
			"</set>",
			"<where>",
			"<if test='openId != null'>",
			"AND openId = #{openId}",
			"</if>",
			"</where>",
			"</script>"
	})
	void updateConversionPush(ConversionPush conversionPush);

	@Update({
			"<script>",
			"update user_conversion_push",
			"<set>",
			"<if test = 'flag != null'>",
			"flag = #{flag},",
			"</if>",
			"<if test = 'isSend != null'>",
			"isSend = #{isSend},",
			"</if>",
			"<if test = 'isPurchase != null'>",
			"isPurchase = #{isPurchase},",
			"</if>",
			"</set>",
			"<where>",
			"<if test='openId != null'>",
			"AND openId = #{openId}",
			"</if>",
			"<if test = 'levelId != null'>",
			"AND levelId = #{levelId}",
			"</if>",
			"</where>",
			"</script>"
	})
	void updateConversionPushFlag(ConversionPush conversionPush);

	@Update({
			"<script>",
			"update user_conversion_push set isSend = 1",
			"where id in",
			"<foreach item='item' index='index' collection='ids' open='(' separator=',' close=')'>",
			"#{item}",
			"</foreach>",
			"</script>"
	})
	void updateConversionPushIsSend(@Param(value = "ids")List<Integer> ids);

	@Delete("delete from user_conversion_push where id = #{id}")
	void deleteConversionPush(Integer id);

	@Update({
			"<script>",
			"update user_levels set levelId = #{targetLevelId} where squirrelUserId = #{squirrelUserId} and levelId = #{sourceLevelId}",
			"</script>"
	})
	void changeLevelId(@Param("squirrelUserId")int squirrelUserId, @Param("sourceLevelId")int sourceLevelId, @Param("targetLevelId")int targetLevelId);

	@Delete({
			"delete from user_levels where transactionId = #{transactionId}"
	})
	void deleteUserLevel(Integer transactionId);

	@Select({
			"select id,squirrelUserId,beginAt,levelId,transactionId from user_levels where transactionId = #{transactionId}"
	})
	SquirrelLevel selectByTransactionId(Integer transactionId);
}
