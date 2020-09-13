package com.qingclass.squirrel.mapper.statistic;

import com.qingclass.squirrel.domain.statistic.InvitationSendCashBuriedPoint;
import com.qingclass.squirrel.domain.statistic.SquirrelBatchesStatistic;
import com.qingclass.squirrel.domain.statistic.SquirrelFollowUp;

import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface InvitationSendCashBuriedPointMapper {
	@Select({
			"<script>",
			"	select id ",
			"	from `squirrel_statistic`.`invitation_send_cash_buried_point` ",
			"	where ",
			"		start_class_date = #{startClassDate} ",
			"		and level_Id = #{levelId} ",
			"		and invitation_user_id = #{invitationUserId} ",
			"<if test='purchaseOpenId != null'>",
			"		and purchase_open_id = #{purchaseOpenId} ",
			"</if>",
			"</script>"
	})
	Integer selectSendCashPoint(InvitationSendCashBuriedPoint invitationSendCash);
	
	@Insert({
			"<script>",
			"	insert into `squirrel_statistic`.`invitation_send_cash_buried_point` ",
			"   (start_class_date, level_Id, invitation_user_id, ",
			"<if test='purchaseOpenId != null'>",
			"		purchase_open_id, ",
			"</if>",
			"		into_send_cash_page_count, send_invitation_count, ",
			"		goto_buy_page_count, click_buy_count, purchase_count, created ",
			"	) ",
			"	values(",
			"		#{startClassDate}, #{levelId}, #{invitationUserId}, ",
			"<if test='purchaseOpenId != null'>",
			"		#{purchaseOpenId},",
			"</if>",
			"		#{intoSendCashPageCount}, #{sendInvitationCount}, ",
			"		#{gotoBuyPageCount}, #{clickBuyCount}, #{purchaseCount}, now() ",
			"	)",
			"</script>"
	})
	@Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
	void insert(InvitationSendCashBuriedPoint invitationSendCash);

	@Update({
			"<script>",
			"update `squirrel_statistic`.`invitation_send_cash_buried_point` ",
			"<set>",
			"<if test='intoSendCashPageCount != null'>",
			"	into_send_cash_page_count = #{intoSendCashPageCount},",
			"</if>",
			"<if test='sendInvitationCount != null'>",
			"	send_invitation_count = #{sendInvitationCount},",
			"</if>",
			"<if test='gotoBuyPageCount != null'>",
			"	goto_buy_page_count = #{gotoBuyPageCount},",
			"</if>",
			"<if test='clickBuyCount != null'>",
			"	click_buy_count = #{clickBuyCount},",
			"</if>",
			"<if test='purchaseCount != null'>",
			"	purchase_count = #{purchaseCount},",
			"</if>",
			"</set>",
			"where",
			"		start_class_date = #{startClassDate} ",
			"		and level_Id = #{levelId} ",
			"		and invitation_user_id = #{invitationUserId} ",
			"<if test='purchaseOpenId != null'>",
			"		and purchase_open_id = #{purchaseOpenId} ",
			"</if>",
			"</script>"
	})
	void update(InvitationSendCashBuriedPoint invitationSendCash);
	
	@Select({
		"<script>",
		"select ",
		"	date_format(point.created,'%Y-%m-%d') currentDate,",
		"	point.startClassDate, ", 
		"	point.levelId, ", 
		"	point.beginDays,  ",
		"	ifnull(users.beginClassCount,0) beginPeoples,",
		"	point.intoSendCashPageCount,",
		"	point.sendInvitationCount,",
		"	point.gotoBuyPageCount,",
		"	point.clickBuyCount,",
		"	point.purchaseCount",
		"	from (",
		"		SELECT p.created, p.start_class_date startClassDate, p.level_Id levelId, ",
		"			to_days(now())-to_days(start_class_date)+1 as beginDays,", 
		"			count(case when p.into_send_cash_page_count=1 then 1 end) as intoSendCashPageCount ,",
		"			count(case when p.send_invitation_count=1 then 1 end) as sendInvitationCount,  ", 
		"			count(case when p.goto_buy_page_count=1 then 1 end) as gotoBuyPageCount, ",  
		"			count(case when p.click_buy_count=1 then 1 end) as clickBuyCount,  ",
		"			count(case when p.purchase_count=1 then 1 end) as purchaseCount",
		"		FROM squirrel_statistic.invitation_send_cash_buried_point p  ",
		"		where ",
		"			p.level_Id=#{levelId}  ",
		"			and to_days(p.created) = to_days(now())-1 ",
		" 			and p.start_class_date = #{startClassDate}  ",
		"		group by p.start_class_date  ",
		"	) point ",
		"	left join ",
		"	(",
		"		select ",
		"			beginAt, count(id) beginClassCount  ",
		"		from `squirrel`.`user_levels` ",
		"		where  levelId = #{levelId} ",
		"			   and beginAt = #{startClassDate} ",
		"		group by beginAt  ",
		"	) users on users.beginAt=point.startClassDate ",
		"</script>"
	})
	List<InvitationSendCashBuriedPoint> invitationSendCashBuriedPoint(InvitationSendCashBuriedPoint invitationSendCashBuriedPoint);
	
	@Select({
		"<script>",
		"select beginAt as beginDate, levelId ",
		"from `squirrel`.`user_levels` ",
		"where ",
		" levelId = #{levelId} ",
		" and beginAt &lt; now() ",
		"group by beginAt, levelId",
		"</script>"
	})
	List<SquirrelBatchesStatistic> selectBeginAtByLevelId(int levelId);
	
	@Select({
		"<script>",
		"		select ",
		"			beginAt startClassDate,  ",
		"			ifnull(count(id),0) beginPeoples,",
		"			to_days(now())-to_days(beginAt)+1 as beginDays  ",
		"		from `squirrel`.`user_levels` ",
		"		where  levelId = #{levelId} ",
		"			   and beginAt = #{startClassDate} ",
		"		group by beginAt  ",
		"</script>"
	})
	InvitationSendCashBuriedPoint selectBeginDaysAndPeoples(InvitationSendCashBuriedPoint invitationSendCashBuriedPoint);

}