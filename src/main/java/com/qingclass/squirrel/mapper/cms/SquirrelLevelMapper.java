package com.qingclass.squirrel.mapper.cms;

import com.qingclass.squirrel.domain.cms.SquirrelLesson;
import com.qingclass.squirrel.domain.cms.SquirrelLevel;
import com.qingclass.squirrel.domain.cms.UserLevel;
import com.qingclass.squirrel.domain.wx.WxShare;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SquirrelLevelMapper {


	@Select({
        "<script>",
        "select id, subjectId, `name`, `order`, minWord, maxWord, image, buySite,skin",
        "from squirrel_levels",
        "where delKey = 0",
        "AND isOpen = 1",
        "</script>"
	})
	List<SquirrelLevel> selectSquirrelLevel();

    @Select({
            "<script>",
            "select id, subjectId, `name`, `order`, minWord, maxWord, image, buySite,skin",
            "from squirrel_levels",
            "where delKey = 0",
            "AND id = #{id, jdbcType=INTEGER}",
            "AND isOpen = 1",
            "</script>"
    })
    SquirrelLevel selectByPrimaryKey(Integer id);


    @Select({
            "<script>",
            "select id, skin",
            "from squirrel_levels",
            "where delKey = 0",
            "AND subjectId = #{subjectId, jdbcType=INTEGER}",
            "AND isOpen = 1",
            "</script>"
    })
    List<SquirrelLevel> selectSkin(Integer subjectId);


    @Select({
            "<script>",
            "select id, subjectId, `name`, introduction, `order`, minWord, maxWord, image, buySite, skin, isShow",
            "from squirrel_levels",
            "where delKey = 0",
            "AND subjectId = #{subjectId, jdbcType=INTEGER}",
            "<if test='name != null'>",
            "AND `name` = #{name, jdbcType=VARCHAR}",
            "</if>",
            "AND isOpen = 1",
            "</script>"
    })
    List<SquirrelLevel> selectBy(SquirrelLevel record);

    @Select({
            "<script>",
            "select id, subjectId, `name`, `order`, minWord, maxWord, image, buySite",
            "from squirrel_levels",
            "where delKey = 0",
            "AND id in(select levelId from squirrel.user_levels where squirrelUserId = #{openId,jdbcType=VARCHAR})",
            "AND isOpen = 1",
            "</script>"
    })
    List<SquirrelLevel> selectByOpenId(String openId);

    @Select({
            "<script>",
            "select ", 
            "	a.id, a.subjectId, a.`name`, a.introduction, a.`order`, a.minWord, a.maxWord, a.image, ",
            "	b.beginAt, a.buySite,b.vipBeginTime,b.vipEndTime,b.sendLessonDays",
            "from ",
            "	msyb_resource.squirrel_levels a ",
            "left join msyb.user_levels b on a.id = b.levelId ",
            "where a.delKey = 0 ",
            "	AND b.squirrelUserId =#{squirrelUserId,jdbcType=INTEGER} ",
            "	AND a.isOpen = 1 ",
            "	AND b.vipEndTime &gt;= now() ",
            "order by b.createdAt asc",
            "</script>"
    })
    List<SquirrelLevel> selectBySquirrelUserId(int squirrelUserId);

    @Select({
            "<script>",
            "select ",
            "	s.id, s.url, s.spaceTitle, s.freTitle, s.content, s.img, s.type, s.shareContent, l.buySite ",
            "from squirrel_wx_share s ",
            "left join squirrel_levels l on l.shareId=s.id ",
            "where l.id = #{levelId}",
            "</script>"
    })
    WxShare getShareByLevelId(int levelId);

    @Select({
            "<script>",
            "select id,levelId,type,shareContent from wx_share_templates",
            "where levelId = #{levelId}",
            "</script>"
    })
    WxShare getShareTemplateByLevelId(int levelId);

    @Select({
            "<script>",
            "select a.id, a.`name`",
            "from ",
            "	msyb_resource.squirrel_levels a ",
            "left join ",
            "	msyb.user_levels b on a.id = b.levelId",
            "where a.delKey = 0",
            "	AND b.squirrelUserId =#{squirrelUserId,jdbcType=INTEGER}",
            "	AND a.isOpen = 1",
            "	AND b.vipBeginTime &lt;= now()",
            "	AND b.vipEndTime &gt;= now()",
            "order by b.createdAt asc",
            "</script>"
    })
    List<SquirrelLevel> getEffectiveLevelList(int squirrelUserId);

    @Select({
	        "<script>",
	        "select ",
	        "	u.id, u.squirrelUserId, u.levelId, u.beginAt, sl.subjectId, sl.lessonDay, sl.return_fee_day returnFeeDay ",
	        "from ",
	        "	msyb.user_levels u ",
	        "left join msyb_resource.squirrel_levels sl on u.levelId=sl.id ",
	        "where ",
	        "	u.delKey = 0 ",
            "	AND u.vipEndTime &gt;= now() ",
	        "	AND u.levelId = #{levelId} ",
	        "	AND u.squirrelUserId = #{userId}",
	        "</script>"
	})
	List<UserLevel> getUserLevelsByLevelIdAndUserId(@Param("levelId")Integer levelId, @Param("userId")Integer userId);
}