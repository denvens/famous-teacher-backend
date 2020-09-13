package com.qingclass.squirrel.domain.cms;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * squirrel_lesson
 * @author 
 */
@Data
public class UserLogistics implements Serializable {

	private static final long serialVersionUID = 1L;

	private Integer id;

    private String openId;

    private Integer levelId;

    private Integer logisticsId;
    
    private Date createDate;
    
    private Date updateDate;
}