package com.stratelia.webactiv.beans.admin;

public class ProfiledObject {

	private String 	instanceId;
	private int		objectId;
	private int		fatherId;
	private String	objectType;
	
	public ProfiledObject()
	{
		
	}
	
	public int getFatherId() {
		return fatherId;
	}
	public void setFatherId(int fatherId) {
		this.fatherId = fatherId;
	}
	public String getInstanceId() {
		return instanceId;
	}
	public void setInstanceId(String instanceId) {
		this.instanceId = instanceId;
	}
	public int getObjectId() {
		return objectId;
	}
	public void setObjectId(int objectId) {
		this.objectId = objectId;
	}
	public String getObjectType() {
		return objectType;
	}
	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}
	
	
}
