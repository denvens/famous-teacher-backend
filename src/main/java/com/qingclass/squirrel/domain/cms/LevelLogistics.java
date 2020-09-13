package com.qingclass.squirrel.domain.cms;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

import com.google.gson.JsonObject;

import lombok.Data;

/**
 * squirrel_SquirrelLevel
 * @author 
 */
@Data
public class LevelLogistics implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Integer levelId;

    private String levelName;

    private Integer code;

    private ArrayList<Map<String, Object>> data;
    
    //---------------------------------------------------------------------------------------------------------
    private Integer status;

    private String logisticsInfo;
    
}