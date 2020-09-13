package com.qingclass.squirrel.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.qingclass.squirrel.entity.SessionSquirrelUserResolver;

/**
 *
 *
 * @author 苏天奇
 * */
@Configuration
public class SessionSquirrelUserContext extends WebMvcConfigurerAdapter {

	@Override
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
		argumentResolvers.add(new SessionSquirrelUserResolver());
	}

}