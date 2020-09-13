package com.qingclass.squirrel.domain.cms;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;

@Data
public class ScholarshipSettingDetails {
	
	private String name;
	private Integer lessonDay;
	private Integer returnFeeDay;
	private String beginClassTime;
	private String endClassTime;
	private Integer levelId;
	private Integer alreadyDays;
	private Integer subjectId;
	
	private String beginAt;
    private Integer beginDays;
    private BigDecimal scholarshipCash;
    private Integer status;
    private String createdAt;
    private String updatedAt;
    private String groupLink;
    private String vipEndTime;
    private Integer operationStatus;
    private Integer scholarshipType;
    private Long SpecialScholarshipId;
    
}
