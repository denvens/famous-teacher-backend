package com.qingclass.squirrel.mapper.cms;

import com.qingclass.squirrel.domain.cms.SquirrelUnit;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SquirrelUnitMapper {
    @Update({
            "<script>",
            "update squirrel_units set delKey = now()",
            "where id = #{id,jdbcType=INTEGER}",
            "</script>"
    })
    int deleteByPrimaryKey(Integer id);

    @Insert({
            "<script>",
            "insert into squirrel_units (id, lessonId, `name`,",
            "`order`)",
            "values (#{id,jdbcType=INTEGER}, #{lessonId,jdbcType=INTEGER}, #{name,jdbcType=VARCHAR},",
            "#{order,jdbcType=INTEGER})",
            "</script>"
    })
    int insert(SquirrelUnit record);

    int insertSelective(SquirrelUnit record);

    @Select({
            "<script>",
            "select id, lessonId, `name`, `order`",
            "from squirrel_units",
            "where delKey = 0",
            "AND id = #{id,jdbcType=INTEGER}",
            "</script>"
    })
    SquirrelUnit selectByPrimaryKey(Integer id);

    @Update({
            "<script>",
            "update squirrel_units",
            "<set>",
            "<if test='lessonId != null'>",
            "lessonId = #{lessonId,jdbcType=INTEGER},",
            "</if>",
            "<if test='name != null'>",
            "`name` = #{name,jdbcType=VARCHAR},",
            "</if>",
            "<if test='order != null'>",
            "`order` = #{order,jdbcType=INTEGER},",
            "</if>",
            "</set>",
            "where id = #{id,jdbcType=INTEGER}",
            "</script>"
    })
    int updateByPrimaryKeySelective(SquirrelUnit record);

    int updateByPrimaryKey(SquirrelUnit record);

    @Select({
            "<script>",
            "select id, lessonId, `name`, `order`",
            "from squirrel_units",
            "where delKey = 0 AND lessonId = #{lessonId}",
            "</script>"
    })
    List<SquirrelUnit> selectByLessonId(Integer lessonId);

    @Select({
            "<script>",
            "select id, lessonId, `name`, `order`",
            "from squirrel_units",
            "where delKey = 0",
            "</script>"
    })
    List<SquirrelUnit> selectAll();

    @Select({
            "<script>",
            "select id, lessonId, `name`, `order`",
            "from squirrel_units",
            "where delKey = 0",
            "<if test='lessonId'>",
            "AND lessonId = #{lessonId, jdbcType=INTEGER}",
            "</if>",
            "</script>"
    })
    List<SquirrelUnit> selectBy(SquirrelUnit record);
}
