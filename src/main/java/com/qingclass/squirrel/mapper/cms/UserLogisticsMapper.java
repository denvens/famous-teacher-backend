package com.qingclass.squirrel.mapper.cms;

import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import com.qingclass.squirrel.domain.cms.Certificate;
import com.qingclass.squirrel.domain.cms.Logistics;
import com.qingclass.squirrel.domain.cms.UserLogistics;

import java.util.List;

@Repository
public interface UserLogisticsMapper {
    
	@Insert({
        "<script>",
        "insert into `msyb_resource`.`msyb_user_logistics` (",
        "	open_id, level_id, ",
        "	logistics_id, ",
        "	create_time, update_time )",
        "values (",
        "	#{p.openId}, ",
        "	#{p.levelId}, ",
        "	#{p.logisticsId},",
        "	#{p.createTime},",
        "	#{p.updateTime}	)",
        "</script>"
	})
	@Options(useGeneratedKeys = true, keyProperty = "p.id", keyColumn = "id")
	int insert(@Param("p") UserLogistics userLogistics);
	
	@Select({
        "<script>",
        "select ",
        "	id, openId, `classify`, `number`, `createDate`, `updateDate` ",
        "from msyb_certificate ",
        "where 1=1 ",
        "<if test='openId != null'>",
        "	AND `openId` = #{openId,jdbcType=VARCHAR} ",
        "</if>",
        " order by `createDate` desc",
        "</script>"
	})
	List<Certificate> selectBy(Certificate record);
	
	@Update({
        "<script>",
        "update msyb_certificate ",
        "<set>",
        "<if test='p.classify != null'>",
        "`classify` = #{p.classify},",
        "</if>",
        "<if test='p.number != null'>",
        "`number` = #{p.number},",
        "</if>",
        "<if test='p.updateDate != null'>",
        "updateDate = #{p.updateDate},",
        "</if>",
        "</set>",
        "where id = #{p.id}",
        "</script>"
	})
	int updateByPrimaryKey(@Param("p") Certificate record);
	
	@Select({
            "<script>",
            "select ",
            "	l.id, l.levelId, l.`name`, l.`order`, l.star, l.audition, l.lessonKey, ",
            "	l.isOpen, l.title, l.updateDate, l.image, l.share_image shareImage, l.relation, m.picId, m.part",
            "from ",
            "	squirrel_lessons l left join lessons_mid_picturebooks m on l.id = m.lessonId",
            "where ",
            "	l.id = #{id,jdbcType =INTEGER} order by id desc",
            "</script>"
    })
	Certificate selectByPrimaryKey(Integer id);

    @Update({
            "<script>",
            "update squirrel_lessons l LEFT JOIN squirrel_units u on l.id=u.lessonId",
            "LEFT JOIN squirrel_questions q on u.id=q.unitId set l.delKey= now(),",
            "u.delKey=now(),q.delKey=now() where l.id = #{id}",
            "</script>"
    })
    int deleteByPrimaryKey(@Param("id") Integer id);



    @Select({
            "<script>",
            "select id, levelId, `name`, `order`, star, audition, lessonKey, isOpen, title, updateDate, image,relation",
            "from squirrel_lessons",
            "where delKey = 0 order by id desc limit #{pageNo,jdbcType=INTEGER},#{pageSize,jdbcType=INTEGER}",
            "</script>"
    })
    List<Certificate> selectAll(@Param("pageNo")int pageNo,@Param("pageSize")int pageSize);

    @Select({
            "<script>",
            "select ",
            "	l.id, l.levelId, l.`name`, l.`order`, l.audition, l.lessonKey, l.isOpen, l.title, l.updateDate, ",
            "	l.image, l.share_image shareImage, l.relation ",
            "from ",
            "	squirrel_lessons l ",
            "where l.delKey = '0'",
            "AND l.levelId = #{levelid,jdbcType=INTEGER}",
            "<if test='name != null'>",
            "AND l.`name` = #{name,jdbcType=VARCHAR} ",
            "</if>",
            "<if test='audition != null'>",
            "AND l.audition = #{audition,jdbcType=VARCHAR}",
            "</if>",
            " order by `order` desc",
            "limit #{pageNo,jdbcType=INTEGER},#{pageSize,jdbcType=INTEGER}",
            "</script>"
    })
    List<Certificate> selectByPage(Certificate record);

    @Select({
            "<script>",
            "select count(id)",
            "from squirrel_lessons",
            "where delKey = 0 and levelId = #{levelId}",
            "<if test='audition != null'>",
            "AND audition = #{audition,jdbcType=VARCHAR}",
            "</if>",
            "</script>"
    })
    int count(@Param("levelId") Integer levelId,@Param("audition") Integer audition);

    @Insert({
            "<script>",
            "insert into squirrel_lessons (levelId, `name`,",
            "`order`, lessonKey, audition, isOpen, title, updateDate, image,relation)",
            "values (#{p.levelid,jdbcType=INTEGER}, #{p.name,jdbcType=VARCHAR},",
            "#{p.order,jdbcType=INTEGER}, #{p.lessonkey,jdbcType=VARCHAR}, #{p.audition,jdbcType=VARCHAR},",
            "#{p.isOpen,jdbcType=INTEGER},#{p.title,jdbcType=VARCHAR},#{p.updateDate},#{p.image,jdbcType=VARCHAR},#{p.relation})",
            "</script>"
    })
    @Options(useGeneratedKeys = true, keyProperty = "p.id", keyColumn = "id")
    int insertReturnId(@Param("p") Certificate record);

    @Select({
            "<script>",
            "select t.id as id,t.`name` as `name`,lessonKey, t.isOpen, t.title, t.updateDate, t.image from",
            "(select a.id,a.lessonKey,a.`name`, a.isOpen, a.title, a.updateDate, a.image from squirrel_lessons a",
            "LEFT JOIN squirrel_units b on a.id = b.lessonId where b.name = #{unitName,jdbcType=VARCHAR} and b.lessonId = #{lessonId,jdbcType=INTEGER} ) t",
            "</script>"
    })
    Certificate selectByUnitName(@Param("unitName") String unitName, @Param("lessonId") Integer lessonId);  //根据 unitId 查询父级

    @Select({
            "<script>",
            "select t.id as id,t.`name` as `name`,lessonKey, t.isOpen, t.title, t.updateDate, t.image from",
            "(select a.id,a.lessonKey,a.`name`, a.isOpen, a.title, a.updateDate, a.image from squirrel_lessons a",
            "LEFT JOIN squirrel_units b on a.id = b.lessonId where b.id = #{id,jdbcType=INTEGER} ) t",
            "</script>"
    })
    Certificate selectByUnitId(@Param("id") Integer id);  //根据 unitId 查询父级

    @Select({
            "<script>",
            "select t.id as id,t.`name` as `name`,lessonKey, t.isOpen, t.title, t.updateDate, t.image from",
            "(select a.id,a.lessonKey,a.`name`, a.isOpen, a.title, a.updateDate, a.image from squirrel_lessons a",
            "LEFT JOIN squirrel_units b on a.id = b.lessonId where b.id in",
            "(select unitId from squirrel_questions where questionType = #{questionType,jdbcType=VARCHAR} AND unitId = #{unitId,jdbcType=INTEGER})) t",
            "</script>"
    })
    Certificate selectByQuestionTypeAndUnitId(@Param("questionType") String questionType,@Param("unitId") Integer unitId);    //根据 questionId 查询父级以及父父级

    @Select({
            "<script>",
            "select ",
            "	t.id as id,t.`name` as `name`,lessonKey, ",
            "	t.isOpen, t.title, t.updateDate, t.image ",
            "from (",
            "	select a.id,a.lessonKey,a.`name`, a.isOpen, a.title, ",
            "		a.updateDate, a.image ",
            "	from squirrel_lessons a",
            "	where a.id in (",
            "		select lesson_id from squirrel_questions where id = #{id,jdbcType=INTEGER})",
            ") t ",
            "</script>"
    })
    Certificate selectByQuestionId(@Param("id")Integer id);    //根据 questionId 查询父级以及父父级

    @Select({
            "<script>",
            "select a.id, a.word, a.translation, a.voice, a.keyImage, a.baseImage, a.confusionImage, b.isKey from squirrel_words a ",
            "inner join lessons_mid_words b on a.id = b.wordId",
            "where a.id in (select wordId from lessons_mid_words where lessonId = #{lessonId,jdbcType=INTEGER})",
            "and b.lessonId = #{lessonId,jdbcType=INTEGER}",
            "</script>"
    })
    List<Certificate> getWords(Integer lessonId);  //取lesson下的词库

    @Insert({
            "<script>",
            "insert into lessons_mid_words(lessonId,wordId)",
            "values",
            "<foreach collection='wordsList' item='words' index= 'index' separator =','>",
            "(",
            "#{words.lessonId,jdbcType=INTEGER},#{words.wordId,jdbcType=INTEGER}",
            ")",
            "</foreach>",
            "</script>"
    })
    int insertWords(@Param("wordsList")List<Certificate> words);

    @Select({
            "<script>",
            "select id, levelId, `name`, `order`, star, audition, lessonKey, isOpen, title, updateDate, image from squirrel_lessons where id in",
            "(select lessonId from lessons_mid_words where wordId=#{wordId})",
            "</script>"
    })
    List<Certificate> selectLessonByWordId(Integer wordId);

    @Update({
            "<script>",
            "update lessons_mid_words set isKey = #{isKey} where lessonId = #{lessonId,jdbcType=INTEGER} and wordId = #{wordId,jdbcType=INTEGER}",
            "</script>"
    })
    int updateWord(@Param("lessonId")Integer lessonId, @Param("wordId")Integer wordId, @Param("isKey")Integer isKey);

    @Delete({
            "<script>",
            "delete from lessons_mid_words",
            "where lessonId = #{lessonId,jdbcType=INTEGER} and wordId = #{wordId,jdbcType=INTEGER}",
            "</script>"
    })
    int deleteWord(@Param("lessonId")Integer lessonId, @Param("wordId")Integer wordId);


    @Insert({
            "<script>",
            "insert into lessons_mid_picturebooks(lessonId,picId,part)",
            "values(#{lessonId},#{picId},#{part})",
            "</script>"
    })
    int relationPicturebook(@Param("lessonId")Integer lessonId,@Param("picId")Integer picturebookId,@Param("part")Integer part);

    @Select({
            "<script>",
            "select id,lessonId,picId,part from lessons_mid_picturebooks where lessonId = #{lessonId}",
            "</script>"
    })
    List<Certificate> selectMidPicByLessonId(@Param("lessonId")Integer lessonId);

    @Select({
            "<script>",
            "select lmp.id,lmp.lessonId,lmp.picId,lmp.part,sp.name from lessons_mid_picturebooks lmp inner join squirrel_picturebook sp on lmp.picId = sp.id where lmp.lessonId = #{lessonId}",
            "</script>"
    })
    List<Certificate> selectPicNameAndMidPicByLessonId(@Param("lessonId")Integer lessonId);

    @Update({
            "<script>",
            "update lessons_mid_picturebooks",
            "<set>",
            "picId = #{picId},part = #{part}",
            "</set>",
            "where lessonId = #{lessonId}",
            "</script>"
    })
    int updateMidPic(@Param("lessonId")Integer lessonId,@Param("picId")Integer picturebookId,@Param("part")Integer part);

    @Select({
        "<script>",
        "SELECT id,`order` from squirrel_lessons a where a.order = #{order} and a.levelId = #{levelId} and a.audition = 0 and a.delKey = '0' and isOpen='1' limit 1",
        "</script>"
    })
    Certificate selectByCondition(@Param("order") int order, @Param("levelId") Integer levelId);

    @Select({
        "<script>",
        "SELECT max(`order`) from squirrel_lessons a where  a.levelId = #{levelId} and delKey = '0' and a.audition = '0' and isOpen='1'",
        "</script>"
    })
	Integer selectMaxOrder(@Param("levelId") Integer levelId);
}