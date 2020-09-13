package com.qingclass.squirrel.constant;



public enum InvitationTypeEnum { 

	InvitationCoupon(0, "邀请送券"),
	InvitationCash(1, "邀请送现金");

	private Integer key;
	private String value;

	private InvitationTypeEnum(Integer key, String value) {
		this.key = key;
		this.value = value;
	}

	public Integer getKey() {
		return key;
	}

	public void setKey(Integer key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public static String getValue(String key) {
		for (InvitationTypeEnum st : InvitationTypeEnum.values()) {
			if (key.equals(st.key)) {
				return st.value;
			}
		}
		return "";
	}
}
