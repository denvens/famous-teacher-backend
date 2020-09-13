package com.qingclass.squirrel.mapper.statistic;

import com.qingclass.squirrel.domain.statistic.SquirrelBatchesStatistic;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BatchesStatisticMapper {



	@Select({
			"<script>",
			"select openId from batches_temp_table where beginDate = #{beginDate} and levelId = #{levelId}",
			"</script>"
	})
	List<String> selectTempOpenId(@Param("beginDate")String beginDate, @Param("levelId")Integer levelId);

	@Select({
			"<script>",
			"select currentDate from batches_temp_table where beginDate = #{beginDate} and levelId = #{levelId} order by currentDate desc limit 1",
			"</script>"
	})
	String selectCurrentDate(@Param("beginDate")String beginDate, @Param("levelId")Integer levelId);


	@Select({
			"<script>",
			"select beginAt as beginDate, levelId from `squirrel`.`user_levels` where beginAt &lt; now() group by beginAt, levelId",
			"</script>"
	})
	List<SquirrelBatchesStatistic> selectBeginAtGroup();

	@Select({
			"<script>",
			"select count(id) from `squirrel`.`user_levels` where beginAt = #{beginAt} and levelId = #{levelId}",
			"</script>"
	})
	int selectCountByBeginAt(@Param("beginAt") String beginAt, @Param("levelId") Integer levelId);

	@Select({
			"<script>",
			"select su.openId from `squirrel`.`user_levels` ul inner join `squirrel`.`squirrel_users` su on ul.squirrelUserId = su.id where beginAt = #{beginAt} and levelId = #{levelId} and vipEndTime &gt; now()",
			"</script>"
	})
	List<String> selectBatchesOpenId(@Param("beginAt")String beginAt, @Param("levelId")Integer levelId);

	@Delete({
			"<script>",
			"delete from batches_temp_table where levelId = #{levelId} and beginDate = #{beginDate}",
			"</script>"
	})
	void deleteTempTableOpenId(@Param("levelId")Integer levelId, @Param("beginDate")String beginDate);

	@Delete({
			"<script>",
			"delete from batches_temp_table",
			"</script>"
	})
	void deleteTempTableOpenIdAll();
	@Insert({
			"<script>",
			"insert into batches_temp_table(beginDate,levelId,openId,currentDate,createdAt) values",
			"<foreach collection='openList' item='op' index='index' separator=','>",
			"(",
			"#{op.beginDate},",
			"#{op.levelId},",
			"#{op.openId},",
			"#{op.currentDate},",
			"now()",
			")",
			"</foreach>",
			"</script>"
	})
	void insertTempTableOpenId(@Param(value = "openList")List<SquirrelBatchesStatistic> openList);

	@Insert({
			"<script>",
			"insert into batches_statistic(beginDate,levelId,currentDate,beginCount,studyCount,finishCount,shareCount,beginDay,createdAt,updatedAt) values",
			"(#{beginDate},#{levelId},#{currentDate},#{beginCount},#{studyCount},#{finishCount},#{shareCount},#{beginDay},now(),now())",
			"</script>"
	})
	void insertBatchesStatistic(SquirrelBatchesStatistic squirrelBatchesStatistic);
}