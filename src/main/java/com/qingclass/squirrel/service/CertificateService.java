package com.qingclass.squirrel.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.qingclass.squirrel.domain.cms.Certificate;
import com.qingclass.squirrel.domain.cms.RequestInfo;
import com.qingclass.squirrel.mapper.cms.CertificateMapper;


@Service
@Component
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class,value = "squirrelResourceTransaction")
public class CertificateService {
	
	@Autowired
    private CertificateMapper certificateMapper;

    Logger logger = LoggerFactory.getLogger(this.getClass());
    
    /**
     * 插入
     * */
    public RequestInfo insert(Certificate certificate){
        RequestInfo info;
        certificateMapper.insert(certificate);
        info = new RequestInfo();
        info.setDataObject(certificate);
        return info;
    }
    
	  /**
	  * 多条件查询
	  * */
    public RequestInfo selectBy(Certificate c){
    	RequestInfo info = new RequestInfo();
    	List<Certificate> certificate = certificateMapper.selectBy(c);
    	info.setDataList(certificate);
	    return info;
    }
    
    public RequestInfo updateByPrimaryKey(Certificate c){
		RequestInfo info = new RequestInfo();
		certificateMapper.updateByPrimaryKey(c);
		info.setDataObject(c);
		return info;
	}
    
//	/**
//     * 无条件查询
//     * */
//    public List<SquirrelLesson> selectAll(){
//        return certificateMapper.selectAll();
//    }
//
//    /**
//     * 主键查询
//     * */
//    public SquirrelLesson selectByPrimaryKey(Integer id){
//
//        return squirrelLessonMapper.selectByPrimaryKey(id);
//    }
//
//    /**
//     * 获取lesson下词库
//     * */
//    public RequestInfo getWords(Integer lessonId){
//        RequestInfo info = new RequestInfo();
//        info.setDataList(squirrelLessonMapper.getWords(lessonId));
//        return info;
//    }


}
