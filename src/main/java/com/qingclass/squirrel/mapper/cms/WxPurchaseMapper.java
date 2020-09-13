package com.qingclass.squirrel.mapper.cms;

import com.qingclass.squirrel.domain.cms.SquirrelLessonRemind;
import com.qingclass.squirrel.domain.wx.WxCustom;
import com.qingclass.squirrel.domain.wx.WxShare;
import com.qingclass.squirrel.domain.wx.WxTemplate;
import com.qingclass.squirrel.entity.InvitationRecord;

import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;



@Repository
public interface WxPurchaseMapper {
    @Select({
            "<script>",
            "select id,type,content,isOpen,url from squirrel_wx_templates",
            "where type = #{type} and isOpen = 1",
            "</script>"
    })
    WxTemplate selectTemplateByType(String type);

    @Select({
            "<script>",
            "select id,type,content,isOpen from squirrel_wx_custom",
            "where type = #{type} and isOpen = 1",
            "</script>"
    })
    WxCustom selectCustomByType(String type);

    /**
     *
     * */
    @Select({
            "<script>",
            "select id,remindRate,firstRemind,secondRemind,type",
            "from squirrel_lesson_remind",
            "where type = #{type}",
            "</script>"
    })
    SquirrelLessonRemind selectRemindTimeDefault(String type);

    @Select({
            "<script>",
            "select id,url,freTitle,spaceTitle,content,img,shareContent ",
            "from squirrel_wx_share",
            "where id in(",
            "	select shareId from squirrel_invitation_setting ",
            "	where isOpen = 1 and levelId = #{levelId} ",
            "	and invitation_type = #{invitationType} ",
            ")",
            "</script>"
    })
    WxShare selectSharePageBylevelId(InvitationRecord invitationRecord);



    @Select({
            "<script>",
            "select id,type,content,isOpen,url from squirrel_wx_templates",
            "where ",
            "	id in(",
            "		select templateId ",
            "		from squirrel_invitation_setting ",
            "		where ",
            "			levelId = #{levelId} ",
            "			and invitation_type =#{invitationType} ",
            "			and isOpen = 1",
            ")",
            "and isOpen = 1",
            "</script>"
    })
    WxTemplate selectInvitationTemplate(InvitationRecord invitationRecord);
    
    @Select({
            "<script>",
            "select id,type,content,isOpen from squirrel_wx_custom",
            "where id in(",
            "	select customId ",
            "	from squirrel_invitation_setting ",
            "	where ",
            "		levelId = #{levelId} ",
            "		and invitation_type =#{invitationType} ",
            "		and isOpen = 1)",
            "and isOpen = 1",
            "</script>"
    })
    WxCustom selectInvitationCustom(InvitationRecord invitationRecord);
}
