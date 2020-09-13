package com.qingclass.squirrel.constant;



public enum RefundStatusEnum { 

	Member(0, "会员"),
	Refund(1, "已退款");

	private Integer key;
	private String value;

	private RefundStatusEnum(Integer key, String value) {
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
		for (RefundStatusEnum st : RefundStatusEnum.values()) {
			if (key.equals(st.key)) {
				return st.value;
			}
		}
		return "";
	}
}
