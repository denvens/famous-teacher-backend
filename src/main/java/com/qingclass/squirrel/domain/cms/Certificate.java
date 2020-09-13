package com.qingclass.squirrel.domain.cms;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * squirrel_lesson
 * @author 
 */
@Data
public class Certificate implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Integer id;

    private Integer classify;

    private String openId;

    private String number;

    private Date createDate;
    
    private Date updateDate;


//    @Override
//    public boolean equals(Object that) {
//        if (this == that) {
//            return true;
//        }
//        if (that == null) {
//            return false;
//        }
//        if (getClass() != that.getClass()) {
//            return false;
//        }
//        CertificateLesson other = (CertificateLesson) that;
//        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
//            && (this.getLevelid() == null ? other.getLevelid() == null : this.getLevelid().equals(other.getLevelid()))
//            && (this.getName() == null ? other.getName() == null : this.getName().equals(other.getName()))
//            && (this.getOrder() == null ? other.getOrder() == null : this.getOrder().equals(other.getOrder()))
//            && (this.getLessonkey() == null ? other.getLessonkey() == null : this.getLessonkey().equals(other.getLessonkey()));
//    }
//
//    @Override
//    public int hashCode() {
//        final int prime = 31;
//        int result = 1;
//        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
//        result = prime * result + ((getLevelid() == null) ? 0 : getLevelid().hashCode());
//        result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
//        result = prime * result + ((getOrder() == null) ? 0 : getOrder().hashCode());
//        result = prime * result + ((getLessonkey() == null) ? 0 : getLessonkey().hashCode());
//        return result;
//    }
//
//    @Override
//    public String toString() {
//        StringBuilder sb = new StringBuilder();
//        sb.append(getClass().getSimpleName());
//        sb.append(" [");
//        sb.append("Hash = ").append(hashCode());
//        sb.append(", id=").append(id);
//        sb.append(", levelid=").append(levelid);
//        sb.append(", name=").append(name);
//        sb.append(", order=").append(order);
//        sb.append(", lessonkey=").append(lessonkey);
//        sb.append(", serialVersionUID=").append(serialVersionUID);
//        sb.append("]");
//        return sb.toString();
//    }
}