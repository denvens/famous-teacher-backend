package com.qingclass.squirrel.domain.cms;

import java.util.Date;

import lombok.Data;

@Data
public class UserLevel {
    private int id;
    private int squirrelUserId;
    private int levelId;
    private int transacationId;
    private Date createdAt; //购买时间
    private String beginAt;
    private String vipBeginTime;
    private String vipEndTime;
    private Integer subjectId;
    private Integer alreadyDays;
    private Integer lessonDay;
    private Integer returnFeeDay;
    

    //--temp
    private int effectiveDate;
    private int pageNo;
    private int pageSize;
    private String findParam;
    private String nickName;
    private String headImgUrl;
    private String openId;
    private Integer userId;

    private Integer vipDays;
    private Integer sendLessonCount;
    private Integer alreadySendLessonDays;

}
