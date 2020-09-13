package com.qingclass.squirrel.mapper.cms;


import com.qingclass.squirrel.domain.cms.PicturebookPart;
import com.qingclass.squirrel.domain.cms.SquirrelLesson;
import com.qingclass.squirrel.domain.cms.SquirrelPicturebook;
import com.qingclass.squirrel.domain.cms.SquirrelWord;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SquirrelLessonMapper {
     
	@Select({
            "<script>",
            "select id, levelId, `name`, `order`, lessonKey, audition, title, image",
            "from squirrel_lessons",
            "where id = #{id,jdbcType=INTEGER}",
            "AND isOpen = 1",
            "</script>"
    })
    SquirrelLesson selectByPrimaryKey(Integer id);


    @Select({
            "<script>",
            "select id, levelId, `name`, `order`, lessonKey, audition, title, image",
            "from squirrel_lessons",
            "where delKey = 0",
            "AND isOpen = 1",
            "</script>"
    })
    List<SquirrelLesson> selectAll();

    @Select({
            "<script>",
            "select ",
            "	id, levelId, `name`, `order`, lessonKey, audition, title, image, share_image shareImage ",
            "from ",
            "	squirrel_lessons",
            "where ",
            "	delKey = 0",
            "	AND levelId = #{levelid,jdbcType=INTEGER}",
            "	AND isOpen = 1 ",
            "order by `order` asc",
            "</script>"
    })
    @Results({
            @Result(id=true,property = "id",column = "id"),
            @Result(property = "levelid",column = "levelId"),
            @Result(property = "name",column = "name"),
            @Result(property = "order",column = "order"),
            @Result(property = "lessonkey",column = "lessonKey"),
            @Result(property = "audition",column = "audition"),
            @Result(property = "title",column = "title"),
            @Result(property = "image",column = "image"),
            @Result(property = "shareImage",column = "shareImage")
    })
    List<SquirrelLesson> selectBy(SquirrelLesson record);

    @Select({
            "<script>",
            "select levelId",
            "from squirrel_lessons",
            "where delKey = 0",
            "AND isOpen = 1",
            "AND lessonKey = #{lessonKey}",
            "</script>"
    })
    String selectByLessonKey(String lessonKey);

    @Select({
            "<script>",
            "select count(id)",
            "from squirrel_lessons",
            "where delKey = 0",
            "AND audition = 0",
            "AND isOpen = 1",
            "AND levelId = #{levelId}",
            "</script>"
    })
    int selectCountByLevelId(Integer levelId);

    @Select({
            "<script>",
            "select * from squirrel_words a ",
            "where a.id in (select wordId from lessons_mid_words where lessonId = #{lessonId,jdbcType=INTEGER})",
            "</script>"
    })
    List<SquirrelWord> getWords(Integer lessonId);  //取lesson下的词库

    @Select({
            "<script>",
            "select ",
            "	id, lessonKey, `name`, `order`",
            "from ",
            "	squirrel_lessons",
            "where ",
            "	delKey = 0",
            "	AND isOpen = 1 AND audition = 0 AND levelId = #{levelId} ",
            "	AND `order` &lt;= #{sendDays} ",
            "order by `order` asc",
            "</script>"
    })
    List<SquirrelLesson> selectSendLessonByDays(@Param("levelId")int lesson, @Param("sendDays")int sendDays);

    @Select({
            "<script>",
            "select count(id) from squirrel_words a ",
            "where a.id in (select wordId from lessons_mid_words where lessonId = #{lessonId,jdbcType=INTEGER} and isKey=1)",
            "</script>"
    })
    int getLessonWordsCount(Integer lessonId);  //取lesson下的词库关键词数量

    @Select({
            "<script>",
            "select count(id) from squirrel_words a ",
            "where a.id in (select wordId from lessons_mid_words where ",
            "lessonId in(select id from squirrel_lessons where levelId=#{levelId}) and isKey=1)",
            "</script>"
    })
    int getLevelKeyWordsCount(Integer levelId);  //取level下的所有lesson词库关键词数量

    @Select({
            "<script>",
            "select lv.name as levelName,ls.name,ls.order, ls.lessonKey ",
            "from msyb_resource.squirrel_lessons ls ",
            "left join msyb_resource.squirrel_levels lv on ls.levelId = lv.id",
            "where ls.delKey = '0' and ls.order = #{order} and ls.levelId = #{levelId}",
            "</script>"
    })
    List<SquirrelLesson> getLessonAndLevelByLevelIdAndOrder(@Param("order")Integer order,@Param("levelId")Integer levelId);

    @Select({
            "<script>",
            "SELECT max(`order`) from squirrel_lessons a where  a.levelId = #{levelId}  and delKey = '0' and isOpen = 1",
            "</script>"
    })
    Integer selectMaxOrder(@Param("levelId") Integer levelId);

    @Select({
            "<script>",
            "select p.name,p.image,p.id,l.order from",
            "squirrel_lessons l",
            "inner join lessons_mid_picturebooks m on l.id = m.lessonId",
            "inner join squirrel_picturebook p on m.picId = p.id",
            "where l.levelId = #{levelId} and l.`order` &lt;= #{order} and l.delKey = '0' and m.part = 1",
            "order by l.`order`",
            "</script>",
    })
    List<SquirrelPicturebook> selectPicturebookPart(@Param("levelId")Integer levelId, @Param("order")Integer order);

    @Select({
            "<script>",
            "select q.id as id,l.id as lessonId,q.questionData as partContent,m.part as part,p.name as picName,p.id as picId,p.image as image from squirrel_questions q inner join squirrel_units u on q.unitId = u.id",
            "inner join squirrel_lessons l on u.lessonId = l.id",
            "inner join lessons_mid_picturebooks m on l.id = m.lessonId",
            "inner join squirrel_picturebook p on m.picId = p.id",
            "where q.questionType = 'picture-book-explain' and l.levelId = #{levelId} and p.id in",
            "<foreach item='item' index='index' collection='picIds' open='(' separator=',' close=')'>",
            "#{item}",
            "</foreach>",
            " and q.delKey = '0' and l.delKey = '0' and u.delKey = '0' and l.audition = 0",
            "order by l.`order`",
            "</script>",
    })
    List<PicturebookPart> selectPicturebookPartPicIds(@Param("levelId")Integer levelId, @Param("picIds")List<Integer> picIds);


    @Select({
            "<script>",
            "select id from squirrel_lessons where `order` = #{order} and levelId = #{levelId} and delKey = '0' and audition = 0",
            "</script>"
    })
    Integer selectIdByOrderAndLevelId(@Param("order")Integer order, @Param("levelId")Integer levelId);
}