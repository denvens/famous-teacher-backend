package com.qingclass.squirrel.service.model.request;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class EntPayCreateOrderRequest {
    private Integer amount;
    private String openid;
    private String description;
    private String appId;
    private String spbillCreateIp;
    
    private String invitationOpenId;
    private String purchaseOpenId;
    private Integer levelId;
    private Integer invitationUserId;
    
}
