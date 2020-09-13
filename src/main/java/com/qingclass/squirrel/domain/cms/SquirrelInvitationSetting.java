package com.qingclass.squirrel.domain.cms;

import java.math.BigDecimal;
import java.util.Date;

import lombok.Data;
@Data
public class SquirrelInvitationSetting {
    private Integer id;
    private String img;
    private String rule;
    private Integer validDays;
    private Integer templateId;
    private Integer customId;
    private Integer levelId;
    private Date createdAt;
    private Date updateAt;
    private Integer isOpen;
    private Integer shareId;

    private Integer invitationType;
    private BigDecimal bonusAmount;
    private String bonusImg;
    private BigDecimal offerAmount;
    private String offerImg;
    
    private Integer cashSum;

    //--temp
    private String levelName;

}
