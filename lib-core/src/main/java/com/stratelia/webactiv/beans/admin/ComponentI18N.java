package com.stratelia.webactiv.beans.admin;

import com.silverpeas.util.i18n.Translation;
import com.stratelia.webactiv.organization.ComponentInstanceI18NRow;

public class ComponentI18N extends Translation {
	
	private String name	= null;
	private String description = null;
	
	public ComponentI18N()
	{
	}
	
	public ComponentI18N(String lang, String name, String description)
	{
		if (lang != null)
			super.setLanguage(lang);
		this.name = name;
		this.description = description;
	}
	
	public ComponentI18N(ComponentInstanceI18NRow row)
	{
		super.setId(row.id);
		super.setLanguage(row.lang);
		name = row.name;
		description = row.description;
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}