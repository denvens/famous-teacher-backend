package com.qingclass.squirrel.domain.cms;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * squirrel_lesson
 * @author 
 */
public class SquirrelLesson implements Serializable {
    private Integer id;

    private Integer levelid;

    private String name;

    private Integer order;

    private Integer star;

    private String lessonkey;

    private Integer audition;

    private Integer isOpen;

    private Date updateDate;

    private String image;
    
    private String shareImage;

    private String title;

    private Integer alreadyUnitCount;

    private String levelName;

    private Integer isShow;

    public Integer getIsShow() {
        return isShow;
    }

    public void setIsShow(Integer isShow) {
        this.isShow = isShow;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

    public Integer getAlreadyUnitCount() {
        return alreadyUnitCount;
    }

    public void setAlreadyUnitCount(Integer alreadyUnitCount) {
        this.alreadyUnitCount = alreadyUnitCount;
    }

    public Integer getAudition() {
        return audition;
    }

    public void setAudition(Integer audition) {
        this.audition = audition;
    }

    public Integer getIsOpen() {
        return isOpen;
    }

    public void setIsOpen(Integer isOpen) {
        this.isOpen = isOpen;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getStar() {
        return star;
    }

    public void setStar(Integer star) {
        this.star = star;
    }

    //--temp
    private List<SquirrelUnit> unitList;

    public List<SquirrelUnit> getUnitList() {
        return unitList;
    }

    public void setUnitList(List<SquirrelUnit> unitList) {
        this.unitList = unitList;
    }

    private static final long serialVersionUID = 1L;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getLevelid() {
        return levelid;
    }

    public void setLevelid(Integer levelid) {
        this.levelid = levelid;
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

    public String getLessonkey() {
        return lessonkey;
    }

    public String getShareImage() {
		return shareImage;
	}

	public void setShareImage(String shareImage) {
		this.shareImage = shareImage;
	}

	public SquirrelLesson() {
    }

    public SquirrelLesson(String name) {
        this.name = name;
    }

    public void setLessonkey(String lessonkey) {
        this.lessonkey = lessonkey;
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        SquirrelLesson other = (SquirrelLesson) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getLevelid() == null ? other.getLevelid() == null : this.getLevelid().equals(other.getLevelid()))
            && (this.getName() == null ? other.getName() == null : this.getName().equals(other.getName()))
            && (this.getOrder() == null ? other.getOrder() == null : this.getOrder().equals(other.getOrder()))
            && (this.getLessonkey() == null ? other.getLessonkey() == null : this.getLessonkey().equals(other.getLessonkey()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getLevelid() == null) ? 0 : getLevelid().hashCode());
        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
        result = prime * result + ((getOrder() == null) ? 0 : getOrder().hashCode());
        result = prime * result + ((getLessonkey() == null) ? 0 : getLessonkey().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", levelid=").append(levelid);
        sb.append(", name=").append(name);
        sb.append(", order=").append(order);
        sb.append(", lessonkey=").append(lessonkey);
        sb.append(", serialVersionUID=").append(serialVersionUID);
        sb.append("]");
        return sb.toString();
    }
}