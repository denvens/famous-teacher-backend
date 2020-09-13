package com.qingclass.squirrel.domain.wx;

public class WxShare {
    private int id;
    private String url;
    private String spaceTitle;
    private String freTitle;
    private String content;
    private String img;

    private String type;
    private String shareContent;

    private String buySite;
    
    //--temp
    //---分页
    private Integer pageNo;

    private Integer pageTotal;

    private Integer pageSize;

    private Integer squirrelUserId;

    public Integer getSquirrelUserId() {
        return squirrelUserId;
    }

    public void setSquirrelUserId(Integer squirrelUserId) {
        this.squirrelUserId = squirrelUserId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getShareContent() {
        return shareContent;
    }

    public void setShareContent(String shareContent) {
        this.shareContent = shareContent;
    }

    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public Integer getPageTotal() {
        return pageTotal;
    }

    public void setPageTotal(Integer pageTotal) {
        this.pageTotal = pageTotal;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSpaceTitle() {
        return spaceTitle;
    }

    public void setSpaceTitle(String spaceTitle) {
        this.spaceTitle = spaceTitle;
    }

    public String getFreTitle() {
        return freTitle;
    }

    public void setFreTitle(String freTitle) {
        this.freTitle = freTitle;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

	public String getBuySite() {
		return buySite;
	}

	public void setBuySite(String buySite) {
		this.buySite = buySite;
	}
    
}
