package com.silverpeas.notation.model;

import java.io.Serializable;

public class NotationDetail
	implements Serializable
{
	
	private String instanceId;
	private String elementId;
	private int elementType;
	private int notesCount;
	private float globalNote;
	private int userNote;
	
	public NotationDetail(NotationPK pk)
	{
		instanceId = pk.getInstanceId();
		elementId = pk.getId();
		elementType = pk.getType();
	}
	
	public String getInstanceId()
	{
		return instanceId;
	}
	
	public void setInstanceId(String instanceId)
	{
		this.instanceId = instanceId;
	}
	
	public String getElementId()
	{
		return elementId;
	}
	
	public void setElementId(String elementId)
	{
		this.elementId = elementId;
	}
	
	public int getElementType()
	{
		return elementType;
	}
	
	public void setElementType(int elementType)
	{
		this.elementType = elementType;
	}
	
	public int getNotesCount()
	{
		return notesCount;
	}

	public void setNotesCount(int notesCount)
	{
		this.notesCount = notesCount;
	}

	public float getGlobalNote()
	{
		return globalNote;
	}
	
	public void setGlobalNote(float globalNote)
	{
		this.globalNote = globalNote;
	}
	
	public int getUserNote()
	{
		return userNote;
	}
	
	public void setUserNote(int userNote)
	{
		this.userNote = userNote;
	}
	
	public int getRoundGlobalNote()
	{
		return Math.round(globalNote);
	}

}
