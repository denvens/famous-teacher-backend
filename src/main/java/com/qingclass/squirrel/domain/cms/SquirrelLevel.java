package com.qingclass.squirrel.domain.cms;

import java.io.Serializable;
import java.util.Date;

/**
 * squirrel_SquirrelLevel
 * @author 
 */
public class SquirrelLevel implements Serializable {
    private Integer id;

    private Integer subjectId;

    private String name;

    private Integer order;

    private Integer minWord;

    private Integer maxWord;

    private String image;

    private Integer isOpen;

    private String buySite;

    private String skin;

    private Integer isShow;

    private Integer levelId;

    private String introduction;
    //---temp

    private String beginAt;
    private Date beginDate;
    private String vipBeginTime;
    private String vipEndTime;
    private Date vipBeginDate;
    private Date vipEndDate;
    private Integer sendLessonDays;
    private Integer validDays;
    private Integer vouchersCount; //补卡劵数量
    private Boolean isSwitchLevel;
    private Integer squirrelUserId;

    public Integer getSquirrelUserId() {
        return squirrelUserId;
    }

    public void setSquirrelUserId(Integer squirrelUserId) {
        this.squirrelUserId = squirrelUserId;
    }

    public Boolean getSwitchLevel() {
        return isSwitchLevel;
    }

    public void setSwitchLevel(Boolean switchLevel) {
        isSwitchLevel = switchLevel;
    }

    public Integer getLevelId() {
        return levelId;
    }

    public void setLevelId(Integer levelId) {
        this.levelId = levelId;
    }

    public Integer getIsShow() {
        return isShow;
    }

    public void setIsShow(Integer isShow) {
        this.isShow = isShow;
    }

    public Integer getVouchersCount() {
        return vouchersCount;
    }

    public void setVouchersCount(Integer vouchersCount) {
        this.vouchersCount = vouchersCount;
    }

    public Integer getValidDays() {
        return validDays;
    }

    public void setValidDays(Integer validDays) {
        this.validDays = validDays;
    }

    public Integer getSendLessonDays() {
        return sendLessonDays;
    }

    public void setSendLessonDays(Integer sendLessonDays) {
        this.sendLessonDays = sendLessonDays;
    }

    public String getSkin() {
        return skin;
    }

    public void setSkin(String skin) {
        this.skin = skin;
    }

    public Date getVipBeginDate() {
        return vipBeginDate;
    }

    public void setVipBeginDate(Date vipBeginDate) {
        this.vipBeginDate = vipBeginDate;
    }

    public Date getVipEndDate() {
        return vipEndDate;
    }

    public void setVipEndDate(Date vipEndDate) {
        this.vipEndDate = vipEndDate;
    }

    public String getVipBeginTime() {
        return vipBeginTime;
    }

    public void setVipBeginTime(String vipBeginTime) {
        this.vipBeginTime = vipBeginTime;
    }

    public String getVipEndTime() {
        return vipEndTime;
    }

    public void setVipEndTime(String vipEndTime) {
        this.vipEndTime = vipEndTime;
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public String getBuySite() {
        return buySite;
    }

    public void setBuySite(String buySite) {
        this.buySite = buySite;
    }

    public String getBeginAt() {
        return beginAt;
    }

    public void setBeginAt(String beginAt) {
        this.beginAt = beginAt;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Integer getIsOpen() {
        return isOpen;
    }

    public void setIsOpen(Integer isOpen) {
        this.isOpen = isOpen;
    }

    public Integer getMinWord() {
        return minWord;
    }

    public void setMinWord(Integer minWord) {
        this.minWord = minWord;
    }

    public Integer getMaxWord() {
        return maxWord;
    }

    public void setMaxWord(Integer maxWord) {
        this.maxWord = maxWord;
    }

    public SquirrelLevel(Integer id, Integer subjectId, String name, Integer order) {
        this.id = id;
        this.subjectId = subjectId;
        this.name = name;
        this.order = order;
    }

    public SquirrelLevel() {
    }

    @Override
    public String toString() {
        return "SquirrelLevel{" +
                "id=" + id +
                ", subjectId=" + subjectId +
                ", name='" + name + '\'' +
                ", order=" + order +
                '}';
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Integer subjectId) {
        this.subjectId = subjectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

	public String getIntroduction() {
		return introduction;
	}

	public void setIntroduction(String introduction) {
		this.introduction = introduction;
	}
    
    
}