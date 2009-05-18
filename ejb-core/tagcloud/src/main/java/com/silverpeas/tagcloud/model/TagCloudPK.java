package com.silverpeas.tagcloud.model;

import java.io.Serializable;

import com.stratelia.webactiv.util.WAPrimaryKey;

/**
 * Primary key of a tagcloud.
 */
public class TagCloudPK
	extends WAPrimaryKey
	implements Serializable
{
	
	private int type;
	
	public TagCloudPK(String id)
	{
		super(id);
	}
	
	public TagCloudPK(String id, String spaceId, String componentId)
	{
		super(id, spaceId, componentId);
	}
	
	public TagCloudPK(String id, String componentId)
	{
		super(id, componentId);
	}
	
	public TagCloudPK(String id, String componentId, int type)
	{
		super(id, componentId);
		this.type = type;
	}

	public TagCloudPK(String id, WAPrimaryKey pk)
	{
		super(id, pk);
	}

	public int getType()
	{
		return type;
	}

	public void setType(int type)
	{
		this.type = type;
	}

	public String getRootTableName()
	{
		return "TagCloud";
	}
	
	public String getTableName()
	{
		return "SB_TagCloud_TagCloud";
	}
	
	public boolean equals(Object other)
	{
		return ((other instanceof TagCloudPK)
			&& (id.equals(((TagCloudPK) other).getId()))
			&& (space.equals(((TagCloudPK) other).getSpace()))
			&& (componentName.equals(((TagCloudPK) other).getComponentName())));
	}
	
	public String toString() {
		return new StringBuffer()
			.append("(id = ").append(getId())
			.append(", space = ").append(getSpace())
			.append(", componentName = ").append(getComponentName()).append(")")
			.toString();
	}

	public int hashCode()
	{
		return toString().hashCode();
	}

}