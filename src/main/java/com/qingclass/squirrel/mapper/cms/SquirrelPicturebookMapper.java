package com.qingclass.squirrel.mapper.cms;

import com.qingclass.squirrel.domain.cms.LessonMidPicturebook;
import com.qingclass.squirrel.domain.cms.SquirrelPicturebook;
import com.qingclass.squirrel.domain.cms.SquirrelSubject;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SquirrelPicturebookMapper {
    int deleteByPrimaryKey(Integer id);

    @Insert({
            "<script>",
            "insert into squirrel_picturebook(`name`,part,image)",
            "values(#{name,jdbcType=VARCHAR},#{part,jdbcType=INTEGER},#{image,jdbcType=VARCHAR})",
            "</script>"
    })
    int insert(SquirrelPicturebook record);

    int insertSelective(SquirrelSubject record);

    SquirrelPicturebook selectByPrimaryKey(Integer id);

    @Select({
            "<script>",
            "select id,part from squirrel_picturebook",
            "where id in",
            "<foreach item='item' index='index' collection='ids' open='(' separator=',' close=')'>",
            "#{item}",
            "</foreach>",
            "</script>"
    })
    List<SquirrelPicturebook> selectByPrimaryKeys(@Param("ids") List<Integer> ids);

    int updateByPrimaryKeySelective(SquirrelSubject record);

    int updateByPrimaryKey(SquirrelSubject record);

    @Select("select id,name,part,image from squirrel_picturebook where delKey = '0'")
    List<SquirrelPicturebook> selectAll();

    List<SquirrelPicturebook> selectBy(SquirrelSubject record);  //多条件查询

    @Select({
            "<script>",
            "select id,picId,lessonId,part from lessons_mid_picturebooks",
            "where lessonId in",
            "<foreach item='item' index='index' collection='lessons' open='(' separator=',' close=')'>",
            "#{item}",
            "</foreach>",
            "</script>"
    })
    List<LessonMidPicturebook> selectByLessonId(@Param("lessons") List<Integer> lessons);

    @Select({
            "<script>",
            "select count(*) from (",
            "select picId from lessons_mid_picturebooks",
            "where lessonId in",
            "<foreach item='item' index='index' collection='lessons' open='(' separator=',' close=')'>",
            "#{item}",
            "</foreach>",
            "group by picId",
            ") lmp",
            "</script>"
    })
    int selectGroupCountByLessonIds(@Param("lessons") List<Integer> lessons);

}