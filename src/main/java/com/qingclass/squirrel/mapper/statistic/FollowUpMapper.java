package com.qingclass.squirrel.mapper.statistic;

import com.qingclass.squirrel.domain.statistic.SquirrelFollowUp;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FollowUpMapper {


	@Insert({
			"<script>",
			"insert into follow_up_action(openId,levelId,createdAt) values",
			"(#{p.openId},#{p.levelId},now())",
			"</script>"
	})
	@Options(useGeneratedKeys = true, keyProperty = "p.id", keyColumn = "id")
	void insert(@Param("p")SquirrelFollowUp squirrelFollowUp);

	@Update({
			"<script>",
			"update follow_up_action",
			"<set>",
			"<if test='clickWordUp != null'>",
			"clickWordUp = #{clickWordUp},",
			"</if>",
			"<if test='clickPicbookUp != null'>",
			"clickPicbookUp = #{clickPicbookUp},",
			"</if>",
			"<if test='finishWordUp != null'>",
			"finishWordUp = #{finishWordUp},",
			"</if>",
			"<if test='finishPicbookUp != null'>",
			"finishPicbookUp = #{finishPicbookUp},",
			"</if>",
			"<if test='shareWordUp != null'>",
			"shareWordUp = #{shareWordUp},",
			"</if>",
			"<if test='sharePicbookUp != null'>",
			"sharePicbookUp = #{sharePicbookUp},",
			"</if>",
			"</set>",
			"where",
			"openId = #{openId} and levelId = #{levelId}",
			"</script>"
	})
	void update(SquirrelFollowUp squirrelFollowUp);

	@Select({
			"<script>",
			"select id from follow_up_action where openId = #{openId} and levelId = #{levelId,jdbcType=INTEGER}",
			"</script>"
	})
	Integer selectByOpenIdAndLevelId(@Param("openId")String openId,@Param("levelId")Integer levelId);

	@Select({
			"<script>",
			"select levelId from follow_up_action where date_format(createdAt,'%Y-%m-%d') = #{date} group by levelId",
			"</script>"
	})
	List<Integer> selectLevelId(String date);

	@Select({
			"<script>",
			"select openId,clickWordUp,clickPicbookUp,finishWordUp,finishPicbookUp,shareWordUp,sharePicbookUp,createdAt from follow_up_action where levelId = #{levelId}",
			"and date_format(createdAt,'%Y-%m-%d') = #{date}",
			"</script>"
	})
	List<SquirrelFollowUp> selectByLevelId(@Param("levelId") Integer levelId, @Param("date")String date);

	@Delete({
			"<script>",
			"delete from follow_up_action where levelId = #{levelId} and date_format(createdAt,'%Y-%m-%d') = #{date}",
			"</script>"
	})
	void deleteAction(@Param("levelId")Integer levelId, @Param("date")String date);

	@Select({
			"<script>",
			"select id from follow_up_statistic",
			"<where>",
			"date = #{date}",
			"<if test = 'levelId != null'>",
			"AND levelId = #{levelId,jdbcType=INTEGER}",
			"</if>",
			"</where>",
			"</script>"
	})
	Integer followUpStatisticExist(@Param("date")String date, @Param("levelId")Integer levelId);


	@Insert({
			"<script>",
			"insert into follow_up_statistic(levelId,onRead,date,createdAt) values",
			"(#{p.levelId},#{p.onRead},#{p.date},now())",
			"</script>"
	})
	@Options(useGeneratedKeys = true, keyProperty = "p.id", keyColumn = "id")
	void followUpStatisticInsert(@Param("p") SquirrelFollowUp squirrelFollowUp);

	@Update({
			"<script>",
			"update follow_up_statistic",
			"<set>",
			"<if test = 'onRead != null'>",
			"onRead = #{onRead},",
			"</if>",
			"<if test = 'clickWordUp != null'>",
			"clickWordUp = #{clickWordUp},",
			"</if>",
			"<if test = 'clickPicbookUp != null'>",
			"clickPicbookUp = #{clickPicbookUp},",
			"</if>",
			"<if test = 'finishWordUp != null'>",
			"finishWordUp = #{finishWordUp},",
			"</if>",
			"<if test = 'finishPicbookUp != null'>",
			"finishPicbookUp = #{finishPicbookUp},",
			"</if>",
			"<if test = 'finishAllUp != null'>",
			"finishAllUp = #{finishAllUp},",
			"</if>",
			"<if test = 'shareWordUp != null'>",
			"shareWordUp = #{shareWordUp},",
			"</if>",
			"<if test = 'sharePicbookUp != null'>",
			"sharePicbookUp = #{sharePicbookUp},",
			"</if>",
			"<if test = 'entryShare != null'>",
			"entryShare = entryShare + #{entryShare},",
			"</if>",
			"<if test = 'purchaseCount != null'>",
			"purchaseCount = #{purchaseCount},",
			"</if>",
			"</set>",
			"where id = #{id}",
			"</script>"
	})
	void followUpStatisticUpdate(SquirrelFollowUp squirrelFollowUp);

}