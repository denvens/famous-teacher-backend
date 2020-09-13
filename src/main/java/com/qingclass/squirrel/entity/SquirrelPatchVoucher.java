package com.qingclass.squirrel.entity;

public class SquirrelPatchVoucher {
    private Integer id;
    private Integer squirrelUserId;
    private Integer levelId;
    private String createdAt;
    private Integer status;
    private String useTime;
    private Integer isOpen;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getSquirrelUserId() {
        return squirrelUserId;
    }

    public void setSquirrelUserId(Integer squirrelUserId) {
        this.squirrelUserId = squirrelUserId;
    }

    public Integer getLevelId() {
        return levelId;
    }

    public void setLevelId(Integer levelId) {
        this.levelId = levelId;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getUseTime() {
        return useTime;
    }

    public void setUseTime(String useTime) {
        this.useTime = useTime;
    }

    public Integer getIsOpen() {
        return isOpen;
    }

    public void setIsOpen(Integer isOpen) {
        this.isOpen = isOpen;
    }
}
