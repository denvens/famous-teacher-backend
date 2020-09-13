package com.qingclass.squirrel.domain.statistic;

import java.util.Date;

/**
 * squirrel_kvalue_statistics
 * @author 
 */
public class SquirrelKvalueStatistic{
	
	public static final String INIT = "4";
	public static final String BUYSUCCESS = "6";
	public static final String ONREAD = "1";
	public static final String BUY = "5";
	public static final String CARE = "7";
	public static final String SHARE = "2";
	public static final String GOSHARE = "3";

	public static final String BUYANDAUDITION = "8";//购买并且试听
	public static final String CLICKAUDITION = "12"; //点击试听
	public static final String CLICKBUYATAUDITION = "13";//试听的页面点击购买
	public static final String LEARNAUDITION = "14"; //学习试听课人数
	public static final String LEARNFINISH = "15"; //学习完全部unit
	public static final String AUDITIONINIT = "16";//进入购买页
	public static final String AUDITIONBUYSUCCESS = "17";//试听购买成功
	public static final String AUDITIONCLICK = "18";//购买页点击购买按钮
	
    private Integer id;

    private Date date;

    private Integer onRead;

    private Integer share;

    private Integer goShare;

    private Integer care;

    private Integer init;

    private Integer buy;

    private Integer buySuccess;
    
    private Integer levelId;

    private Date createAt;
    /**
     * 点击立即试听人数
     */
    private Integer clickAudition;

    /**
     * 学习试听课人数
     */
    private Integer learnAudition;

    /**
     * 点击立即购买按钮人数
     */
    private Integer clickBuyAtAudition;
    
    /**
     * 学习完成人数
     */
    private Integer learnFinish;

    /**
     * 学习完成人数
     */
    private Integer learnAndBuy;
    private Integer auditionInit;
    private Integer auditionClick;
    private Integer auditionBuySuccess;
     
    
	public Integer getAuditionInit() {
		return auditionInit;
	}

	public void setAuditionInit(Integer auditionInit) {
		this.auditionInit = auditionInit;
	}

	public Integer getAuditionClick() {
		return auditionClick;
	}

	public void setAuditionClick(Integer auditionClick) {
		this.auditionClick = auditionClick;
	}

	public Integer getAuditionBuySuccess() {
		return auditionBuySuccess;
	}

	public void setAuditionBuySuccess(Integer auditionBuySuccess) {
		this.auditionBuySuccess = auditionBuySuccess;
	}

	public Integer getLearnAndBuy() {
		return learnAndBuy;
	}

	public void setLearnAndBuy(Integer learnAndBuy) {
		this.learnAndBuy = learnAndBuy;
	}

	public Integer getClickAudition() {
		return clickAudition;
	}

	public void setClickAudition(Integer clickAudition) {
		this.clickAudition = clickAudition;
	}

	public Integer getLearnAudition() {
		return learnAudition;
	}

	public void setLearnAudition(Integer learnAudition) {
		this.learnAudition = learnAudition;
	}

	public Integer getClickBuyAtAudition() {
		return clickBuyAtAudition;
	}

	public void setClickBuyAtAudition(Integer clickBuyAtAudition) {
		this.clickBuyAtAudition = clickBuyAtAudition;
	}

	public Integer getLearnFinish() {
		return learnFinish;
	}

	public void setLearnFinish(Integer learnFinish) {
		this.learnFinish = learnFinish;
	}

	public Integer getLevelId() {
		return levelId;
	}

	public void setLevelId(Integer levelId) {
		this.levelId = levelId;
	}

	public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Integer getOnRead() {
        return onRead;
    }

    public void setOnRead(Integer onRead) {
        this.onRead = onRead;
    }

    public Integer getShare() {
        return share;
    }

    public void setShare(Integer share) {
        this.share = share;
    }

    public Integer getGoShare() {
        return goShare;
    }

    public void setGoShare(Integer goShare) {
        this.goShare = goShare;
    }

    public Integer getCare() {
        return care;
    }

    public void setCare(Integer care) {
        this.care = care;
    }

    public Integer getInit() {
        return init;
    }

    public void setInit(Integer init) {
        this.init = init;
    }

    public Integer getBuy() {
        return buy;
    }

    public void setBuy(Integer buy) {
        this.buy = buy;
    }

    public Integer getBuySuccess() {
        return buySuccess;
    }

    public void setBuySuccess(Integer buySuccess) {
        this.buySuccess = buySuccess;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }
}
