package com.stratelia.webactiv.util.publication.model;

import java.util.Date;

import com.stratelia.webactiv.util.node.model.NodePK;

public class Alias extends NodePK {

	private String 	userId 	= null;
	private Date	date	= null;
	
	private String	userName = null;   //Not persistent
	
	public Alias(String nodeId, String instanceId)
	{
		super(nodeId, instanceId);
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public boolean equals(Object other) {
		if (other instanceof Alias) {
			Alias otherAlias = (Alias) other;
			return (otherAlias.getId().equals(super.id) && otherAlias.componentName.equals(super.componentName));
		}
		return false;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	
	
	
}
