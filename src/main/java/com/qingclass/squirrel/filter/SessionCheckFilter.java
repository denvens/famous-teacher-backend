package com.qingclass.squirrel.filter;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.qingclass.squirrel.entity.SessionSquirrelUser;
import com.qingclass.squirrel.utils.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionCheckFilter implements Filter {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;
		String requestPath = req.getServletPath();
		if("/user/record-sellPage-action-info".equals(requestPath)) {
			chain.doFilter(request, response);
			return;
		}
		 
		SessionSquirrelUser sessionUserInfo = null;

		try{
			sessionUserInfo = (SessionSquirrelUser) req.getSession().getAttribute(SessionSquirrelUser.SESSION_SQUIRREL_USER_KEY);
		}catch (Exception e ){
			e.printStackTrace();
			logger.error("sign in error, Deserialize failed");
			res.setContentType("application/json; charset=utf-8");
			String deniedJson = Tools.mapToJson(Tools.f("登录失败，请重新登录。"));
			PrintWriter writer = response.getWriter();
			writer.println(deniedJson);
			writer.close();
			return;
		}

		if (null == sessionUserInfo) {
			res.setContentType("application/json; charset=utf-8");
			String deniedJson = Tools.mapToJson(Tools.d());
			PrintWriter writer = response.getWriter();
			writer.println(deniedJson);
			writer.close();
			return;
		}
		
		chain.doFilter(request, response);
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

}
