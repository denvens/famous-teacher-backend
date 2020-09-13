package com.qingclass.squirrel.mapper.user;

import java.util.List;

import com.qingclass.squirrel.domain.PaymentTransactions;
import com.qingclass.squirrel.domain.cms.UserLevel;
import com.qingclass.squirrel.entity.ScholarshipApplyFor;

import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentTransactionMapper {

	@Insert({
			"<script>",
			"insert into payment_transactions(wechatTransactionId,bigbayTranctionId,merchantionId,appId,totalFee,tradeType,bankType,openId,feeType,clientIp,sellPageItemConfig,timeEnd)",
			"values(#{p.wechatTransactionId},#{p.bigbayTranctionId},#{p.merchantionId},#{p.appId},#{p.totalFee},#{p.tradeType},#{p.bankType},#{p.openId},#{p.feeType},#{p.clientIp},#{p.sellPageItemConfig},#{p.timeEnd})",
			"</script>"
	})
	@Options(useGeneratedKeys = true, keyProperty = "p.id", keyColumn = "id")
	int insert(@Param("p") PaymentTransactions paymentTransactions);


	@Select({
			"<script>",
			"select id from payment_transactions where bigbayTranctionId = #{bigbayTranctionId}",
			"</script>"
	})
	PaymentTransactions selectTrans(String bigbayTranctionId);

	@Insert({
			"<script>",
			"insert into payment_refund(",
			"		refundFee,totalFee,refundReason,outTradeNo,",
			"		refundMode,wechatTransactionId,createdAt,updatedAt) ",
			"values(",
			"		#{refundFee},#{totalFee},#{refundReason},#{outTradeNo},",
			"		#{refundMode},#{wechatTransactionId},now(),now())",
			"</script>"
	})
	int insertRefund(@Param("refundFee")String refundFee,@Param("totalFee")String totalFee,@Param("refundReason")String refundReason,@Param("outTradeNo")String outTradeNo,
	@Param("refundMode")String refundMode,@Param("wechatTransactionId")String wechatTransactionId);
	
	@Select({
		"<script>",
		"	select ",
		"		pt.totalFee, pt.bigbayTranctionId  ",
		"	from ",
		"		msyb.payment_transactions pt ",
		"	left join ",
		"		msyb.user_levels ul  on pt.id=ul.transactionId ",
		"<where>",
		"	ul.squirrelUserId=#{squirrelUserId} and ul.levelId=#{levelId}",
		"</where>",
		"</script>"
    })
	PaymentTransactions selectTotalFeeByUserIdAndLevelId(UserLevel userLevel);
	
}
