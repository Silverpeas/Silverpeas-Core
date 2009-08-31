package com.stratelia.webactiv.beans.admin.instance.control;

public class PasteDetail {
	String fromComponentId;
	String toComponentId;
	String userId;
	
	public PasteDetail(String fromComponentId, String toComponentId, String userId)
	{
		setFromComponentId(fromComponentId);
		setToComponentId(toComponentId);
		setUserId(userId);
	}
	
	public String getFromComponentId() {
		return fromComponentId;
	}
	public void setFromComponentId(String fromComponentId) {
		this.fromComponentId = fromComponentId;
	}
	public String getToComponentId() {
		return toComponentId;
	}
	public void setToComponentId(String toComponentId) {
		this.toComponentId = toComponentId;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	
	
	
}
