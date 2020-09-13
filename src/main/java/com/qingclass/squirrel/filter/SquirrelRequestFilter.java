package com.qingclass.squirrel.filter;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSON;
import com.qingclass.squirrel.entity.SquirrelRequest;
import com.qingclass.squirrel.utils.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SquirrelRequestFilter implements Filter {

	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub

	}

	@SuppressWarnings("unchecked")
	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		// TODO Auto-generated method stub

		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse res = (HttpServletResponse) response;

		if ("POST".equals(req.getMethod().toUpperCase()) && req.getContentType().toLowerCase().indexOf("x-www-form-urlencoded") >= 0) {

			Map<String, Object> requestParams = null;
			
			if ("SQUIRREL-REQUEST".equals(req.getParameter("REQUEST-TYPE"))) {
				// 适配h5前端的请求
				String json = req.getParameter("REQUEST-CONTENT");
				if (null == json || "".equals(json)) {
					json = "{}";
				}
				requestParams = (Map<String, Object>) JSON.parseObject(json, Map.class);
			} else {
				// 普通 application/x-www-form-urlencoded 请求
				requestParams = new HashMap<>();
				Enumeration<String> paramKeys = req.getParameterNames();
				while (paramKeys.hasMoreElements()) {
				    String paramKey = paramKeys.nextElement();
				    requestParams.put(paramKey, req.getParameter(paramKey));
				}
			}
			
			SquirrelRequest squirrelRequest = new SquirrelRequest();
			squirrelRequest.setRequest(req);
			squirrelRequest.setParams(requestParams);

			req.setAttribute(SquirrelRequest.SQUIRREL_REQUEST_KEY, squirrelRequest);
		}


		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

}