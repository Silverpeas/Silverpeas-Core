package com.stratelia.webactiv.beans.admin;

import com.silverpeas.util.i18n.Translation;
import com.stratelia.webactiv.organization.SpaceI18NRow;

public class SpaceI18N extends Translation {

	private String name	= null;
	private String description = null;
	
	public SpaceI18N()
	{
	}
	
	public SpaceI18N(String lang, String name, String description)
	{
		if (lang != null)
			super.setLanguage(lang);
		this.name = name;
		this.description = description;
	}
	
	public SpaceI18N(SpaceI18NRow row)
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

	public String getSpaceId() {
		return super.getObjectId();
	}

	public void setSpaceId(String id) {
		super.setObjectId(id);
	}

}