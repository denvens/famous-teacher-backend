package com.qingclass.squirrel.mapper.cms;

import com.qingclass.squirrel.domain.cms.SquirrelInvitationSetting;
import com.qingclass.squirrel.domain.cms.SquirrelLevel;
import com.qingclass.squirrel.entity.InvitationRecord;

import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface InvitationSettingMapper {

    @Select({
            "<script>",
            "select i.id,i.img,i.rule,i.validDays,i.shareId,i.templateId,i.customId,l.id as levelId,l.name as levelName  from squirrel_invitation_setting i left join squirrel_levels l on i.levelId = l.id",
            "where i.id = #{id}",
            "</script>"
    })
    SquirrelInvitationSetting selectByPrimaryKey(Integer id);

    @Select({
            "<script>",
            "select ",
            "	i.id,	i.img,	i.rule, ",
            "	i.invitation_type invitationType, ",
            "	i.bonus_amount bonusAmount, i.bonus_img bonusImg, ",
            "	i.offer_amount offerAmount, i.offer_img offerImg, ",
            "	i.validDays,i.shareId,i.templateId,i.customId,	",
            "	l.id as levelId,l.name as levelName  ",
            "from squirrel_invitation_setting i ",
            "left join squirrel_levels l on i.levelId = l.id",
            "where i.levelId = #{levelId} ",
            "and i.invitation_type = #{invitationType} ",
            "and i.isOpen = 1",
            "</script>"
    })
    SquirrelInvitationSetting selectByLevelId(InvitationRecord invitationRecord);

    @Select({
            "<script>",
            "select a.id, a.subjectId, a.`name`, a.`order`, a.minWord, a.maxWord, a.image, b.beginAt, a.buySite,b.vipBeginTime as vipBeginDate,b.vipEndTime as vipEndDate,b.sendLessonDays,c.validDays",
            "from squirrel_levels a left join squirrel.user_levels b on a.id = b.levelId",
            "left join squirrel_invitation_setting c on a.id = c.levelId and c.isOpen = 1 and c.invitation_type=0 ",
            "where a.delKey = 0",
            "AND b.squirrelUserId =#{squirrelUserId,jdbcType=INTEGER}",
            "AND a.isOpen = 1",
            "order by b.createdAt asc",
            "</script>"
    })
    List<SquirrelLevel> selectBySquirrelUserId(Integer squirrelUserId);

}
