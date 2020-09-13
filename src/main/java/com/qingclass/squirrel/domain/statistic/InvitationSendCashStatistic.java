package com.qingclass.squirrel.domain.statistic;

import java.util.Date;

import lombok.Data;

@Data
public class InvitationSendCashStatistic {
    private Integer id;
    private String currentStatisticDate;
    private String startClassDate;
    private Integer levelId;
   
    private Integer beginDays;
    private Integer beginPeoples;
    
    private Integer intoSendCashPageCount;
    private Integer sendInvitationCount;
    private Integer gotoBuyPageCount;
    private Integer clickBuyCount;
    private Integer purchaseCount;
    private Date created;
    
}
