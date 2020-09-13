package com.qingclass.squirrel.domain.cms;

import java.util.Date;
import java.util.List;

public class SquirrelPicturebook {
    private Integer id;
    private String name;
    private Integer part;
    private String image;

    //--temp
    private Date beginAt;
    private Integer order;
    private String voice;

    private List<PicturebookPart> partList;
    @Override
    public String toString() {
        return "SquirrelPicturebookService{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", part=" + part +
                ", image='" + image + '\'' +
                '}';
    }

    public String getVoice() {
        return voice;
    }

    public void setVoice(String voice) {
        this.voice = voice;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public List<PicturebookPart> getPartList() {
        return partList;
    }

    public void setPartList(List<PicturebookPart> partList) {
        this.partList = partList;
    }

    public Date getBeginAt() {
        return beginAt;
    }

    public void setBeginAt(Date beginAt) {
        this.beginAt = beginAt;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getPart() {
        return part;
    }

    public void setPart(Integer part) {
        this.part = part;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public SquirrelPicturebook() {
    }

    public SquirrelPicturebook(Integer id, String name, Integer part, String image) {
        this.id = id;
        this.name = name;
        this.part = part;
        this.image = image;
    }
}
