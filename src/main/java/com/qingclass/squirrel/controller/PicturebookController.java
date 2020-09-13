package com.qingclass.squirrel.controller;

        import com.qingclass.squirrel.domain.cms.SquirrelPicturebook;
        import com.qingclass.squirrel.entity.SessionSquirrelUser;
        import com.qingclass.squirrel.mapper.user.SquirrelUserMapper;
        import com.qingclass.squirrel.service.SquirrelPicturebookService;
        import com.qingclass.squirrel.utils.Tools;
        import org.springframework.beans.factory.annotation.Autowired;
        import org.springframework.web.bind.annotation.PostMapping;
        import org.springframework.web.bind.annotation.RequestMapping;
        import org.springframework.web.bind.annotation.RequestParam;
        import org.springframework.web.bind.annotation.RestController;

        import javax.servlet.http.HttpServletRequest;
        import java.util.*;

/**
 * 本类对应 C端功能 "绘本书架"
 *
 * @author 苏天奇
 * */
@RestController
@RequestMapping("/pbk")
public class PicturebookController {
    @Autowired
    SquirrelUserMapper squirrelUserMapper;
    @Autowired
    SquirrelPicturebookService squirrelPicturebookService;

    @PostMapping("list")
    public Map<String, Object> list(HttpServletRequest req, @RequestParam(value = "levelId",required = false)Integer levelId){
        SessionSquirrelUser sessionUserInfo = (SessionSquirrelUser) req.getSession().getAttribute(SessionSquirrelUser.SESSION_SQUIRREL_USER_KEY);
        String openId = sessionUserInfo.getOpenId();

        List<SquirrelPicturebook> picList = squirrelPicturebookService.bookshelf(openId, levelId);


        return Tools.s(picList);

    }

    @PostMapping("learn")
    public Map<String, Object> learn(@RequestParam(value = "levelId",required = false)Integer levelId,@RequestParam(value = "picIds",required = false)String picIds){

        List<SquirrelPicturebook> picList = squirrelPicturebookService.bookshelfLearn(levelId, picIds);


        return Tools.s(picList);

    }
}
