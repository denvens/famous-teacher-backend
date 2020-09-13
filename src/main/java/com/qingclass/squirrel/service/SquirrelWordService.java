package com.qingclass.squirrel.service;

import com.qingclass.squirrel.domain.cms.RequestInfo;
import com.qingclass.squirrel.domain.cms.SquirrelWord;
import com.qingclass.squirrel.mapper.cms.SquirrelWordMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class,value = "squirrelResourceTransaction")
public class SquirrelWordService {
	
	@Autowired
    SquirrelWordMapper squirrelWordMapper;


	public RequestInfo selectByPrimaryKey(Integer id){
        RequestInfo info = new RequestInfo();

        SquirrelWord squirrelWord = squirrelWordMapper.selectByPrimaryKey(id);
        info.setDataObject(squirrelWord);
        return info;
    }

    public RequestInfo updateByPrimaryKeySelective(SquirrelWord squirrelWord){
        RequestInfo info = new RequestInfo();

        int i = squirrelWordMapper.updateByPrimaryKeySelective(squirrelWord);

        return info;
    }

    public RequestInfo selectBy(SquirrelWord squirrelWord){
        RequestInfo info = new RequestInfo();

        List<SquirrelWord> squirrelWords = squirrelWordMapper.selectBy(squirrelWord);

        info.setDataList(squirrelWords);
        return info;
    }

    public RequestInfo selectAll(Integer pageNo, Integer pageSize){
        RequestInfo info = new RequestInfo();

        pageNo = (pageNo-1)*pageSize;

        List<SquirrelWord> squirrelWords = squirrelWordMapper.selectAll(pageNo, pageSize);

        info.setDataList(squirrelWords);
        return info;
    }

    public RequestInfo insert(SquirrelWord squirrelWord){
        RequestInfo info = new RequestInfo();

        int insert = squirrelWordMapper.insert(squirrelWord);

        return info;
    }
}
