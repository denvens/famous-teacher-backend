package com.qingclass.squirrel.mapper.user;


import com.qingclass.squirrel.entity.InvitationRecord;
import com.qingclass.squirrel.entity.SquirrelPatchVoucher;
import com.qingclass.squirrel.entity.Voucher;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PatchVoucherMapper {

	@Select({
			"<script>",
			"select count(id) from squirrel_patch_vouchers where squirrelUserId = #{squirrelUserId} and levelId = #{levelId} and isOpen = 1 and status = 0",
			"</script>"
	})
	int selectVoucherCount(@Param("squirrelUserId")Integer squirrelUserId, @Param("levelId")Integer levelId);

	@Select({
			"<script>",
			"select id from squirrel_patch_vouchers where squirrelUserId = #{squirrelUserId} and levelId = #{levelId}",
			"and status = 0 and isOpen = 1",
			"</script>"
	})
	List<SquirrelPatchVoucher> selectVoucherByUserIdAndLevelId(@Param("squirrelUserId")Integer squirrelUserId, @Param("levelId")Integer levelId);

	@Insert({
			"<script>",
			"insert into squirrel_patch_vouchers(squirrelUserId,levelId,createdAt,status,updatedAt,isOpen) values",
			"(#{squirrelUserId},#{levelId},now(),#{status},now(),#{isOpen})",
			"</script>"
	})
	int insertVoucher(Voucher voucher);

	@Update({
			"<script>",
			"update squirrel_patch_vouchers set status = 1 , useTime = now() where id = #{id}",
			"</script>"
	})
	void useVoucher(Integer id);


	/**
	 * 邀请记录
	 * */
	@Select({
			"<script>",
			"select ",
			"	pr.id,pr.levelId,",
			"	u2.nickName as purchaseNickName, u2.openId as purchaseOpenId, ",
			"	u2.headImgUrl as purchaseImg,",
			"	pr.createdAt, ",
			"	pr.status  ",
			"from ",
			"	invitation_purchase_record pr",
			"left join squirrel_users u2 on pr.purchaseUserId = u2.id",
			"<where>",
			"<if test = 'levelId != null'>",
			"	AND pr.levelId = #{levelId}",
			"</if>",
			"<if test = 'invitationType != null'>",
			"	AND pr.invitation_type = #{invitationType}",
			"</if>",
			"	AND pr.invitationUserId = #{invitationUserId}",
			"</where>",
			"order by pr.createdAt desc",
			"</script>"
	})
	List<InvitationRecord> selectInvitationRecords(InvitationRecord invitationRecord);

	/**
	 * 插入邀请记录
	 * */
	@Insert({
			"<script>",
			"insert into ",
			"	invitation_purchase_record(invitation_type, invitationUserId,purchaseUserId,createdAt,updatedAt,levelId) ",
			"values",
			"	(#{invitationType}, #{invitationUserId},#{purchaseUserId},now(),now(),#{levelId})",
			"</script>"
	})
	int insertInvitationRecord(InvitationRecord invitationRecord);


	@Select({
			"<script>",
			"select count(id) from prohibit_temp_table where squirrelUserId = #{squirrelUserId} and levelId = #{levelId}",
			"</script>"
	})
	int selectProhibit(@Param("squirrelUserId")Integer squirrelUserId, @Param("levelId")Integer levelId);

	@Delete({
			"<script>",
			"delete from prohibit_temp_table",
			"where squirrelUserId = #{squirrelUserId} and levelId = #{levelId}",
			"</script>"
	})
	int deleteProhibit(@Param("squirrelUserId")Integer squirrelUserId, @Param("levelId")Integer levelId);
}

