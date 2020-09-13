package com.qingclass.squirrel.mapper.cms;

import com.qingclass.squirrel.domain.cms.SquirrelQuestion;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SquirrelQuestionMapper {
    @Update({
            "<script>",
            "update squirrel_questions set delKey = now()",
            "where id = #{id,jdbcType=INTEGER}",
            "</script>"
    })
    int deleteByPrimaryKey(Integer id);

    @Insert({
            "<script>",
            "insert into squirrel_questions (id, unitId, questionType, questionData,",
            "`order`, questionKey)",
            "values (#{id,jdbcType=INTEGER}, #{unitId,jdbcType=INTEGER}, #{questionType,jdbcType=VARCHAR},",
            "#{questionData,jdbcType=INTEGER},#{order,jdbcType=INTEGER},#{questionKey,jdbcType=VARCHAR})",
            "</script>"
    })
    int insert(SquirrelQuestion record);

    int insertSelective(SquirrelQuestion record);


    @Select({
            "<script>",
            "select id, unitId, questionType, questionData, `order`, questionKey",
            "from squirrel_questions",
            "where delKey = '0' AND id = #{id,jdbcType=INTEGER}",
            "</script>"
    })
    SquirrelQuestion selectByPrimaryKey(Integer id);

    @Update({
            "<script>",
            "update squirrel_questions",
            "<if test='unitId != null'>",
            "unitId = #{unitId,jdbcType=INTEGER},",
            "</if>",
            "<if test='questionType != null'>",
            "questionType = #{questionType,jdbcType=VARCHAR},",
            "</if>",
            "<if test='questionData != null'>",
            "questionData = #{questionData,jdbcType=VARCHAR},",
            "</if>",
            "<if test='questionKey != null'>",
            "questionKey = #{questionKey,jdbcType=VARCHAR}",
            "</if>",
            "<if test='order != null'>",
            "`order` = #{order,jdbcType=INTEGER}",
            "</if>",
            "where id = #{id,jdbcType=INTEGER}",
            "</script>"
    })
    int updateByPrimaryKeySelective(SquirrelQuestion record);

    int updateByPrimaryKey(SquirrelQuestion record);

    @Select({
            "<script>",
            "select id, unitId, questionType, questionData, `order`, questionKey",
            "from squirrel_questions",
            "where delKey = '0'",
            "<if test='unitId != null'>",
            "AND unitId = #{unitId,jdbcType=INTEGER}",
            "</if>",
            "<if test='questionType != null'>",
            "AND questionType = #{questionType,jdbcType=VARCHAR}",
            "</if>",
            "<if test='questionKey != null'>",
            "AND questionKey = #{questionKey,jdbcType=VARCHAR}",
            "</if>",
            "<if test='order != null'>",
            "AND `order` = #{order,jdbcType=INTEGER}",
            "</if>",
            "</script>"
    })
    List<SquirrelQuestion> selectBy(SquirrelQuestion record);

    @Select({
            "<script>",
            "select id, unitId, questionType, questionData, `order`, questionKey",
            "from squirrel_questions",
            "where delKey = '0'",
            "</script>"
    })
    List<SquirrelQuestion> selectAll();

}