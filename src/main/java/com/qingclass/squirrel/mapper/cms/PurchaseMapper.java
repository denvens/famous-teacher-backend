package com.qingclass.squirrel.mapper.cms;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import com.qingclass.squirrel.entity.EntPayOrder;
import com.qingclass.squirrel.entity.InvitationRecord;


@Repository
public interface PurchaseMapper {
	
	@Update({
	        "<script>",
	        "update msyb.invitation_purchase_record p  ",
	        "left join msyb.squirrel_users u  on p.purchaseUserId=u.id  ",
	        "left join msyb.payment_transactions t  on t.openId = u.openId  ",
	        "<set>",
	        "<if test='status != null'>",
	        " p.status = #{status},",
	        "</if>",
	        "</set>",
	        "where t.wechatTransactionId = #{wechatTransactionId}",
	        "</script>"
	})
	int updatePurchaseRecordStatus(
			@Param(value = "wechatTransactionId") String wechatTransactionId, 
	        @Param(value = "status")Integer status);
	
	@Select({
		"<script>",
		"select id,invitation_type, invitationUserId, purchaseUserId, status ",
		" from squirrel.invitation_purchase_record ",
		"<where>",
		" levelId = #{levelId}  ",
		" AND invitation_type = #{invitationType}",
		" AND invitationUserId = #{invitationUserId} ",
		" AND status = 0 ",
		"</where>",
		"</script>"
    })
    List<InvitationRecord> selectPurchaseRecord(InvitationRecord invitationRecord);
}