package com.qingclass.squirrel.mapper.cms;

import com.qingclass.squirrel.domain.wx.WxChannel;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface WxChannelMapper {

    @Select({
            "<script>",
            "select id,name,validity,type,messages,sendTime,url from squirrel_channel",
            "where code = #{code} and delKey = '0'",
            "</script>"
    })
    WxChannel getChannelByCode(String code);

    @Select({
            "<script>",
            "select site from squirrel_channel",
            "where code = #{code} and delKey = '0'",
            "</script>"
    })
    String getChannelSiteByCode(String code);

    @Select({
            "<script>",
            "select id from squirrel_levels where channelId in",
            "(select id from squirrel_channel where code = #{code})",
            "</script>"
    })
    List<Integer> getLevelIdByChannelCode(String code);
}
