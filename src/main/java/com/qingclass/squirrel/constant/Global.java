package com.qingclass.squirrel.constant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 *
 * @author 苏天奇
 * */
@Component
public class Global {
    @Value("${oss.bucket}")
    private String ossBucket; //oss bucket
    @Value("${oss.access.key.id}")
    private String ossAccessKeyId; //oss accessKeyId
    @Value("${oss.access.key.secret}")
    private String ossAccessKeySecret;  //oss accessKeySecret
    @Value("${oss.end.point}")
    private String ossEndPoint;  //oss endPoint 
    @Value("${oss.domain}")
    private String domain;
    private String ossLessonPath = "lessons/"; //oss lessonPath

    private String ossVoicePath = "voices/";   //oss voicePath

    private String ossImagePath = "images/";  //oss imagePath

    private String ossSoundPath = "sound/";

    private String ossQrPath = "qr/";

    private Integer pageSize = 10; //page size

    private String ossLoads = "uploads/";

    public String getOssSoundPath() {
        return ossSoundPath;
    }

    public void setOssSoundPath(String ossSoundPath) {
        this.ossSoundPath = ossSoundPath;
    }

    public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getOssQrPath() {
        return ossQrPath;
    }

    public void setOssQrPath(String ossQrPath) {
        this.ossQrPath = ossQrPath;
    }

    public String getOssBucket() {
        return ossBucket;
    }

    public void setOssBucket(String ossBucket) {
        this.ossBucket = ossBucket;
    }

    public String getOssAccessKeyId() {
        return ossAccessKeyId;
    }

    public void setOssAccessKeyId(String ossAccessKeyId) {
        this.ossAccessKeyId = ossAccessKeyId;
    }

    public String getOssAccessKeySecret() {
        return ossAccessKeySecret;
    }

    public void setOssAccessKeySecret(String ossAccessKeySecret) {
        this.ossAccessKeySecret = ossAccessKeySecret;
    }

    public String getOssEndPoint() {
        return ossEndPoint;
    }

    public void setOssEndPoint(String ossEndPoint) {
        this.ossEndPoint = ossEndPoint;
    }

    public String getOssLessonPath() {
        return ossLessonPath;
    }

    public void setOssLessonPath(String ossLessonPath) {
        this.ossLessonPath = ossLessonPath;
    }

    public String getOssVoicePath() {
        return ossVoicePath;
    }

    public void setOssVoicePath(String ossVoicePath) {
        this.ossVoicePath = ossVoicePath;
    }

    public String getOssImagePath() {
        return ossImagePath;
    }

    public void setOssImagePath(String ossImagePath) {
        this.ossImagePath = ossImagePath;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getOssLoads() {
        return ossLoads;
    }

    public void setOssLoads(String ossLoads) {
        this.ossLoads = ossLoads;
    }
}
