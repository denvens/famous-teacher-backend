package com.qingclass.squirrel.mapper.statistic;

import com.qingclass.squirrel.domain.statistic.InvitationSendCashBuriedPoint;
import com.qingclass.squirrel.domain.statistic.InvitationSendCashStatistic;
import com.qingclass.squirrel.domain.statistic.SquirrelFollowUp;

import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface InvitationSendCashStatisticMapper {
	
	@Insert({
		"<script>",
		"	insert into `squirrel_statistic`.`invitation_send_cash_statistic` ",
		"   (	current_statistic_date, start_class_date, level_Id, ",
		"   	begin_days, begin_peoples, ",
		"		into_send_cash_page_count, send_invitation_count, ",
		"		goto_buy_page_count, click_buy_count, purchase_count, created ",
		"	) ",
		"	values(",
		"		#{currentStatisticDate}, #{startClassDate}, #{levelId}, ",
		"		#{beginDays}, #{beginPeoples}, ",
		"		#{intoSendCashPageCount}, #{sendInvitationCount}, ",
		"		#{gotoBuyPageCount}, #{clickBuyCount}, #{purchaseCount}, now() ",
		"	)",
		"</script>"
	})
	@Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
	void insert(InvitationSendCashStatistic sendCashStatistic);
	
	@Select({
		"<script>",
		"	select id ",
		"	from `squirrel_statistic`.`invitation_send_cash_statistic` ",
		"	where ",
		"		current_statistic_date = #{currentStatisticDate} ",
		"		and start_class_date = #{startClassDate} ",
		"		and level_Id = #{levelId} ",
		"</script>"
	})
	Integer selectSendCashStatistic(InvitationSendCashStatistic invitationSendCash);

	@Update({
			"<script>",
			"update `squirrel_statistic`.`invitation_send_cash_statistic` ",
			"<set>",
			"<if test='beginDays != null'>",
			"	begin_days = #{beginDays},",
			"</if>",
			"<if test='beginPeoples != null'>",
			"	begin_peoples = #{beginPeoples},",
			"</if>",
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
			"	id = #{id} ",
			"</script>"
	})
	void update(InvitationSendCashStatistic sendCashStatistic);
}