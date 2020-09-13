
package com.qingclass.squirrel.mapper.cms;


import com.qingclass.squirrel.entity.ConversionPush;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversionPushMapper {

    @Select({
            "<script>",
            "select cp.id,levelId,pushTime,content as customContent,scope from squirrel_conversion_push cp inner join squirrel_wx_custom wc on cp.customId = wc.id where cp.isOpen = 1",
            "</script>"
    })
    List<ConversionPush> selectAll();



}
