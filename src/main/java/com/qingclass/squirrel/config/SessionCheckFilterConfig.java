package com.qingclass.squirrel.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.qingclass.squirrel.filter.SessionCheckFilter;

/**
 * 这就是个权限校验
 *
 * @author 苏天奇
 * */
@Configuration
public class SessionCheckFilterConfig {
	@Bean
	public FilterRegistrationBean sessionCheckFilter(){
		// 以/user/开始的api路径需要检查session，只有登录用户才有权限访问
	    FilterRegistrationBean registrationBean = new FilterRegistrationBean();
	    registrationBean.setFilter(new SessionCheckFilter());
	    registrationBean.addUrlPatterns("/user/*");
	    registrationBean.setOrder(100);
	    return registrationBean;
	}
}
