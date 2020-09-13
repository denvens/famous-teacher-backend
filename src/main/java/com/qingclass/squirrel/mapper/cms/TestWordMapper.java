package com.qingclass.squirrel.mapper.cms;

import com.qingclass.squirrel.domain.cms.SquirrelLessonRemind;
import com.qingclass.squirrel.domain.cms.TestWord;
import com.qingclass.squirrel.domain.wx.WxCustom;
import com.qingclass.squirrel.domain.wx.WxShare;
import com.qingclass.squirrel.domain.wx.WxTemplate;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface TestWordMapper {


    @Select("select * from test_word")
    List<TestWord> getAll();
}
