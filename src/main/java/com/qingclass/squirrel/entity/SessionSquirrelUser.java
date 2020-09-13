package com.qingclass.squirrel.entity;


import java.io.Serializable;

public class SessionSquirrelUser implements Serializable {

	private static final long serialVersionUID = -5067411505985347516L;
	public static final String SESSION_SQUIRREL_USER_KEY = "SESSION_SQUIRREL_USER_KEY";
	private int id;
	private String openId;
	private String unionId;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}

	public String getUnionId() {
		return unionId;
	}

	public void setUnionId(String unionId) {
		this.unionId = unionId;
	}

}
