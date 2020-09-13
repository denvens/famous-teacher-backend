package com.qingclass.squirrel.domain.cms;

import java.math.BigDecimal;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

@Data
public class ScholarshipApplyForResult {
	private String message;
    private String createdAt;
    private String updatedAt;
    private String groupLink;
    private Integer status;
    
    
}
