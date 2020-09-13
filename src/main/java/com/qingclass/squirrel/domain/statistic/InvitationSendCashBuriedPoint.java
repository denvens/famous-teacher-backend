package com.qingclass.squirrel.domain.statistic;

import java.util.Date;

import lombok.Data;

@Data
public class InvitationSendCashBuriedPoint {
    private Integer id;
    private String currentDate;
    private String startClassDate;
    private Integer levelId;
    private String invitationUserId;
    private String purchaseOpenId;
    
    private Integer beginDays;
    private Integer beginPeoples;
    
    private Integer intoSendCashPageCount;
    private Integer sendInvitationCount;
    private Integer gotoBuyPageCount;
    private Integer clickBuyCount;
    private Integer purchaseCount;
    private Date created;
    
}
