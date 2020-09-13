package com.qingclass.squirrel.entity;

import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="users")
public class MongoUser {
	
	@Id
	private String id;
	
	@Indexed(unique = true)
	private int userId;
	
	private Map<String, Object> learnHistory;
	
	public Map<String, Object> getLearnHistory() {
		return learnHistory;
	}
	public void setLearnHistory(Map<String, Object> learnHistory) {
		this.learnHistory = learnHistory;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
	
}
