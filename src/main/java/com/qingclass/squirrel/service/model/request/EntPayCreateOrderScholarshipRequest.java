package com.qingclass.squirrel.service.model.request;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class EntPayCreateOrderScholarshipRequest {
    private BigDecimal amount;
    private String openid;
    private String description;
    private String appId;
    private String spbillCreateIp;
    
    private String scholarshipOpenId;
    private Integer levelId;
    private BigDecimal scholarshipCash;
    
    private String beginAt;
    
}
