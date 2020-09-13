package com.qingclass.squirrel.mapper.user;

import java.math.BigDecimal;
import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import com.qingclass.squirrel.entity.ConversionPush;
import com.qingclass.squirrel.entity.EntPayOrder;

@Repository
public interface EntPayOrderMapper {
    @Insert({
            "<script>",
            "	insert into squirrel.ent_pay_order",
            "		(invitation_open_id, purchase_open_id, level_id, ",
            "		order_id, amount, open_id, ",
            "		partner_trade_no, payment_no, payment_time, ",
            " 		spbill_create_ip, status, created_at, updated_at)",
            "	values( ",
            "		#{invitationOpenId},",
            "		#{purchaseOpenId},",
            "		#{levelId},",
            "		#{orderId},",
            "		#{amount},",
            "		#{openId},",
            "		#{partnerTradeNo},",
            "		#{paymentNo},",
            "		#{paymentTime},",
            "		#{spbillCreateIp},",
            "		#{status},",
            "		#{createdAt},",
            "		#{updatedAt})",
            "</script>"
    })
    int insert(EntPayOrder entPayOrder);
    
    @Select({
		"<script>",
		"select id,invitation_open_id, purchase_open_id, level_id, status ",
		" from squirrel.ent_pay_order ",
		"<where>",
		"<if test='invitationOpenId != null'>",
		" invitation_open_id = #{invitationOpenId}",
		"</if>",
		"<if test='purchaseOpenId != null'>",
		"AND purchase_open_id = #{purchaseOpenId}",
		"</if>",
		"<if test='levelId != null'>",
		"AND level_id = #{levelId}",
		"</if>",
		"<if test='status != null'>",
		"AND status = #{status}",
		"</if>",
		"</where>",
		"</script>"
    })
    List<EntPayOrder> selectEntPayOrderd(EntPayOrder entPayOrder);
    
    @Select({
		"<script>",
		"	select ifnull(sum(epo.amount),0) cashSum   ",
		" 	FROM (",
		" 		select u.openId uOpenId, pu.openId puOpenId  ", 
		" 		FROM squirrel.invitation_purchase_record r ",
		" 		left join squirrel.squirrel_users u on r.invitationUserId = u.id ", 
		" 		left join squirrel.squirrel_users pu on r.purchaseUserId = pu.id  ", 
		" 		WHERE ",
		" 		r.invitation_type=1 ",
		" 		and r.status =0 ",
		" 		and r.invitationUserId=#{invitationUserId} ",
		" 		and r.levelId=#{levelId} ",
		"	)purchaseRecord ",
		"	left join squirrel.ent_pay_order epo ",
		" 		on epo.invitation_open_id=purchaseRecord.uOpenId  ",
		" 		and epo.purchase_open_id=purchaseRecord.puOpenId ",
		"	where epo.level_id=#{levelId} ",
		"</script>"
    })
    BigDecimal getInvitationCashSum(EntPayOrder entPayOrder);
    
}