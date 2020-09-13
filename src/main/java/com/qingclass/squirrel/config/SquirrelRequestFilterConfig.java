package com.qingclass.squirrel.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.qingclass.squirrel.filter.SquirrelRequestFilter;

/**
 *
 *
 * @author 苏天奇
 * */
@Configuration
public class SquirrelRequestFilterConfig {
	@Bean
	public FilterRegistrationBean generateSquirrelRequestFilter() {
		FilterRegistrationBean registrationBean = new FilterRegistrationBean();
		registrationBean.setFilter(new SquirrelRequestFilter());
		registrationBean.setOrder(200);
		return registrationBean;
	}
}
