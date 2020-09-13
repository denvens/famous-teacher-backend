package com.qingclass.squirrel.mapper.statistic;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import com.qingclass.squirrel.domain.statistic.SquirrelKvalueStatistic;

@Repository
public interface SquirrelKvalueStatisticMapper {

	@Insert("insert into squirrel_kvalue_statistics (auditionInit,auditionClick,auditionBuySuccess,learnAndBuy,clickAudition,learnAudition,clickBuyAtAudition,learnFinish,`date`, onRead, \n" + 
			"      `share`, goShare, care, \n" + 
			"      init, buy, buySuccess,levelId)\n" + 
			"    values (#{auditionInit},#{auditionClick},#{auditionBuySuccess},#{learnAndBuy},#{clickAudition},#{learnAudition},#{clickBuyAtAudition},#{learnFinish},#{date,jdbcType=DATE}, #{onRead,jdbcType=INTEGER}, \n" + 
			"      #{share,jdbcType=INTEGER}, #{goShare,jdbcType=INTEGER}, #{care,jdbcType=INTEGER}, \n" + 
			"      #{init,jdbcType=INTEGER}, #{buy,jdbcType=INTEGER}, #{buySuccess,jdbcType=INTEGER},#{levelId})")
    int insert(SquirrelKvalueStatistic record);

	@Select("select ifnull(count(1),0) from squirrel_kvalue_statistics a where `date`= DATE_SUB(STR_TO_DATE(curdate(), '%Y-%m-%d'), INTERVAL 1 DAY)")
	int selectStatistic();
}