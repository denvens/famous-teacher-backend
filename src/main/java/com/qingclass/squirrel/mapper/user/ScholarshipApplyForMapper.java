package com.qingclass.squirrel.mapper.user;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import com.qingclass.squirrel.constant.ScholarshipApplyForStatusEnum;
import com.qingclass.squirrel.entity.ConversionPush;
import com.qingclass.squirrel.entity.EntPayOrder;
import com.qingclass.squirrel.entity.EntPayOrderScholarship;
import com.qingclass.squirrel.entity.ScholarshipApplyFor;
import com.qingclass.squirrel.entity.SquirrelUser;

@Repository
public interface ScholarshipApplyForMapper {
	@Insert({
        "<script>",
        "	insert into msyb.scholarship_apply_for ",
        "		(begin_at ,scholarship_open_id, level_id, ",
        "		learn_day, learn_make_up_day, ",
        "		status, amount, created_at  ) ",
        "	values( ",
        "		#{beginAt}, ",
        "		#{scholarshipOpenId},",
        "		#{levelId},",
        "		#{learnDay},",
        "		#{learnMakeUpDay},",
        "		#{status},",
        "		#{amount},",
        "		#{createdAt})",
        "</script>"
	})
	@Options(useGeneratedKeys = true, keyProperty = "id")
	int insert(ScholarshipApplyFor scholarshipApplyFor);
    
	@Update({
		"<script>",
		"update msyb.scholarship_apply_for ",
		"<set>",
		"<if test='bigbayTranctionId != null'>",
		"	bigbay_tranction_id = #{bigbayTranctionId},",
		"</if>",
		"<if test='updatedAt != null'>",
		"	updated_at = #{updatedAt},",
		"</if>",
		"<if test='status != null'>", 
		"	status = #{status} ",
		"</if>",
		"<if test='amount != null'>", 
		"	amount = #{amount} ",
		"</if>",
		"</set>",
		"where id = #{id}",
		"</script>"
	})
	int updateScholarshipApplyFor(ScholarshipApplyFor scholarshipApplyFor);
	
	@Select({
		"<script>",
		"select ",
		"	id, ",
		"	scholarship_open_id, level_id, 	status,	amount, ",
		"	created_at createdAt, updated_at updatedAt ",
		" from msyb.scholarship_apply_for  ",
		"<where>",
		"<if test='scholarshipOpenId != null'>",
		"	scholarship_open_id = #{scholarshipOpenId} ",
		"</if>",
		"<if test='beginAt != null'>",
		"	and begin_at = #{beginAt} ",
		"</if>",
		"<if test='levelId != null'>",
		"	and level_id = #{levelId} ",
		"</if>",
		"<if test='status != null'>",
		"	and status = #{status} ",
		"</if>",
		"</where>",
		"	order by created_at desc,  updated_at desc  ", 
		"</script>"
    })
    List<ScholarshipApplyFor> selectApplyForByOpenId(ScholarshipApplyFor scholarshipApplyFor);
	
	@Select({
		"<script>",
		"select ",
		"	id, ",
		"	scholarship_open_id scholarshipOpenId, level_id levelId, 	status,	amount, ",
		"	created_at createdAt, updated_at updatedAt ",
		" from msyb.scholarship_apply_for  ",
		" order by created_at desc,  updated_at desc  ", 
		"</script>"
    })
    List<ScholarshipApplyFor> selectApplyForList();
    //------------------------------------------------------------------------------
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