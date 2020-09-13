package com.qingclass.squirrel.service;


import com.qingclass.squirrel.domain.cms.RequestInfo;
import com.qingclass.squirrel.domain.cms.SquirrelQuestion;
import com.qingclass.squirrel.mapper.cms.SquirrelQuestionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class,value = "squirrelResourceTransaction")
public class SquirrelQuestionService {
	
	@Autowired
    private SquirrelQuestionMapper squirrelQuestionMapper;

	public RequestInfo selectBy(SquirrelQuestion squirrelQuestion){
		RequestInfo info = new RequestInfo();

	    info.setDataList(squirrelQuestionMapper.selectBy(squirrelQuestion));
	    return info;
    }

	public RequestInfo selectAll(){
		RequestInfo info = new RequestInfo();

		info.setDataList(squirrelQuestionMapper.selectAll());
		return info;
	}

	public RequestInfo insert(SquirrelQuestion squirrelQuestion){
		RequestInfo info = new RequestInfo();
		squirrelQuestionMapper.insert(squirrelQuestion);
		return info;
	}

	public RequestInfo updateByPrimaryKeySelective(SquirrelQuestion squirrelQuestion){
		RequestInfo info = new RequestInfo();
		squirrelQuestionMapper.updateByPrimaryKeySelective(squirrelQuestion);
		return info;
	}

	public RequestInfo selectByPrimaryKey(Integer id){
		RequestInfo info = new RequestInfo();
		SquirrelQuestion squirrelQuestion = squirrelQuestionMapper.selectByPrimaryKey(id);

		info.setDataObject(squirrelQuestion);
		return info;
	}

}
