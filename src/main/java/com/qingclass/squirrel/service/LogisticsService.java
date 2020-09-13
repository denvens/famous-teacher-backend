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
import com.qingclass.squirrel.domain.cms.Logistics;
import com.qingclass.squirrel.domain.cms.RequestInfo;
import com.qingclass.squirrel.domain.cms.UserLogistics;
import com.qingclass.squirrel.mapper.cms.CertificateMapper;
import com.qingclass.squirrel.mapper.cms.LogisticsMapper;
import com.qingclass.squirrel.mapper.cms.UserLogisticsMapper;


@Service
@Component
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class,value = "squirrelResourceTransaction")
public class LogisticsService {
	
	@Autowired
    private LogisticsMapper logisticsMapper;
	
    Logger logger = LoggerFactory.getLogger(this.getClass());
    
    /**
     * 插入
     * */
    public RequestInfo insert(Logistics logistics){
        RequestInfo info;
        logisticsMapper.insert(logistics);
        info = new RequestInfo();
        info.setDataObject(logistics);
        return info;
    }
    
  
    public RequestInfo updateByPrimaryKey(Logistics logistics){
		RequestInfo info = new RequestInfo();
		logisticsMapper.updateByPrimaryKey(logistics);
		info.setDataObject(logistics);
		return info;
	}
 
    
	/**
	 * 多条件查询
	 * */
	public RequestInfo selectBy(Logistics logistics){
		RequestInfo info = new RequestInfo();
		List<Logistics> logisticsList = logisticsMapper.selectBy(logistics);
		info.setDataList(logisticsList);
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
