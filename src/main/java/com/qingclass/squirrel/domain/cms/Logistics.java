package com.qingclass.squirrel.domain.cms;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * squirrel_lesson
 * @author 
 */
@Data
public class Logistics implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer id;

	private String name;
	
	private String openId;
	
    private Integer levelId;
    
    private Integer status;
    
    private String logisticsCode;
    
    private Integer transactionId;

    private String mobile;

    private String province;
    
    private String city;
    
    private String area;
    
    private String address;

    private Date createTime;
    
    private Date updateTime;
}