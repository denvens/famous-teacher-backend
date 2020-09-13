package com.qingclass.squirrel.entity;

import lombok.Data;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

@Data
public class ScholarshipApplyFor {
	private String beginAt;
    private long id;
    private long entPayOrderScholarshipId; 
    private String bigbayTranctionId; 
    
    
    private String scholarshipOpenId;
    private Integer levelId;
    private Integer status;
    private String amount;
    private Integer learnDay;
    private Integer learnMakeUpDay;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")  
    private Date createdAt;
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")  
    private Date updatedAt;
    private Integer operationStatus;
    private Integer scholarshipType;
}