package com.qingclass.squirrel.mapper.statistic;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import com.qingclass.squirrel.domain.statistic.UserAction;

@Repository
public interface UserActionMapper {
	
    int deleteByPrimaryKey(Integer id);

    @Insert("insert into user_actions (openId, `type`,note,levelId)\n" + 
    		"    values (#{openId,jdbcType=VARCHAR}, #{type,jdbcType=VARCHAR}, #{note,jdbcType=VARCHAR},#{levelId})")
    int insert(UserAction record);
 
    @Select("select * from user_actions a where  createAt >= DATE_SUB(STR_TO_DATE(curdate(), '%Y-%m-%d'), INTERVAL 1 DAY) and createAt < curdate()")
	List<UserAction> selectUserAction();

    @Insert("<script>" +
			"insert into user_action_del_backups(id,openId,`type`,note,levelId) values" +
			" <foreach collection='userActions' item='item'   separator=','> " +
			"(#{item.id},#{item.openId},#{item.type},#{item.note},#{item.levelId})" +
			"</foreach>" +
			"</script>")
	int batchInsert(@Param("userActions") List<UserAction> userActions);

    @Delete("<script>" +
			"delete from user_actions where id in " +
			" <foreach collection='ids' item='item'   open='(' separator=',' close=')'>" +
				"(#{item})" +
			"</foreach>" +
			"</script>")
	void batchDelete(@Param("ids") List<Integer> ids);

	@Select("select id from user_actions where levelId = #{levelId} and openId = #{openId} and type = #{type} and createAt >= curdate() and createAt < DATE_ADD(STR_TO_DATE(curdate(), '%Y-%m-%d'), INTERVAL 1 DAY)")
	Map<String, Object> selectUserActionByCondition(Map<String, Object> params);
	
	
	
	@Insert("insert into bz_action(openId,createdAt) values(#{openId},now())")
	int insertBzAction(@Param("openId")String openId);
   
	@Select("select count(id) from bz_action where openId = #{openId}")
	int selectBzActionCount(@Param("openId")String openId);
    
}