package com.qingclass.squirrel.domain.cms;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;

@Data
public class ScholarshipSetting {
    private int id;
    private int squirrelUserId;
    private int levelId;
    private Date createdAt; //购买时间
    private String beginAt;
    private String vipBeginTime;
    private String vipEndTime;
    
    private String name;
    private Integer lessonDay;
    private Integer beginDays;
    private BigDecimal scholarshipCash;
    
}
