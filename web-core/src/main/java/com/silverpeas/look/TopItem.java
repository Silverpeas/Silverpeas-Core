package com.silverpeas.look;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.URLManager;

public class TopItem {
	
	public static final int SPACE = 0;
	public static final int COMPONENT = 1;
	
	private String label;
	private String componentId;
	private String spaceId;
	private String subSpaceId;
	private String url;
	
	public TopItem()
	{
		
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getComponentId() {
		return componentId;
	}

	public void setComponentId(String componentId) {
		this.componentId = componentId;
	}

	public String getSpaceId() {
		return spaceId;
	}

	public void setSpaceId(String spaceId) {
		this.spaceId = spaceId;
	}

	public String getSubSpaceId() {
		return subSpaceId;
	}

	public void setSubSpaceId(String subSpaceId) {
		this.subSpaceId = subSpaceId;
	}
	
	public boolean isComponent()
	{
		return StringUtil.isDefined(getComponentId());
	}
	
	public boolean isSpace()
	{
		return !isComponent() && StringUtil.isDefined(getSpaceId());
	}
	
	public String getUrl() {
		if (isSpace())
			return "/admin/jsp/MainSilverpeasV5.jsp?SpaceId="+getSubSpaceId();
		else if (isComponent())
			return URLManager.getURL(null, getComponentId())+"Main";
		else
			return "#";
	}

	public String getId()
	{
		if (isComponent())
			return getComponentId();
		else if (isSpace())
			return getSubSpaceId();
		else
			return "anotherId";
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	

}
