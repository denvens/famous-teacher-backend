package com.qingclass.squirrel.entity;

		import java.util.Map;

		import javax.servlet.http.HttpServletRequest;

public class SquirrelRequest {

	public static final String SQUIRREL_REQUEST_KEY = "SQUIRREL_REQUEST_KEY";

	private HttpServletRequest request;
	private Map<String, Object> params;
	public HttpServletRequest getRequest() {
		return request;
	}
	public void setRequest(HttpServletRequest req) {
		this.request = req;
	}
	public Map<String, Object> getParams() {
		return params;
	}
	public void setParams(Map<String, Object> params) {
		this.params = params;
	}

}
