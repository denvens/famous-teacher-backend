package com.qingclass.squirrel.service;


import com.qingclass.squirrel.domain.cms.RequestInfo;
import com.qingclass.squirrel.domain.cms.SquirrelUnit;
import com.qingclass.squirrel.mapper.cms.SquirrelUnitMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class,value = "squirrelResourceTransaction")
public class SquirrelUnitService {
	
	@Autowired
    private SquirrelUnitMapper squirrelUnitMapper;


	/**
     *
     * */
    public RequestInfo selectBy(SquirrelUnit squirrelUnit){
        RequestInfo info = new RequestInfo();

        List<SquirrelUnit> squirrelUnits = squirrelUnitMapper.selectBy(squirrelUnit);

        info.setDataList(squirrelUnits);

        return info;
    }

    /**
     *
     * */
    public RequestInfo selectAll(){
        RequestInfo info = new RequestInfo();

        List<SquirrelUnit> squirrelUnits = squirrelUnitMapper.selectAll();

        info.setDataList(squirrelUnits);
        return info;
    }

    /**
     *
     * */
    public RequestInfo selectByPrimaryKey(Integer id){
        RequestInfo info = new RequestInfo();

        SquirrelUnit squirrelUnit = squirrelUnitMapper.selectByPrimaryKey(id);

        info.setDataObject(squirrelUnit);

        return info;
    }

    /**
     *
     * */
    public RequestInfo insert(SquirrelUnit squirrelUnit){
        RequestInfo info = new RequestInfo();

        int insert = squirrelUnitMapper.insert(squirrelUnit);

        return info;
    }

    /**
     *
     * */
    public RequestInfo update(SquirrelUnit squirrelUnit){
        RequestInfo info = new RequestInfo();

        int i = squirrelUnitMapper.updateByPrimaryKeySelective(squirrelUnit);

        return info;
    }
}
