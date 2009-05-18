package com.stratelia.silverpeas.pdc.model;

import com.silverpeas.util.i18n.Translation;

/**
* This class contains headers of axis.
* And uses the persistence class for the DAO.
* The user can access to the axis main information.
* @author Sébastien Antonio
*/
public class AxisHeaderI18N extends Translation implements java.io.Serializable {
	
	private String name = null;
	private String  description = null;

	public AxisHeaderI18N()
	{
	}

	public AxisHeaderI18N(int axisId, String lang,  String name,  String description){
		if (lang != null)
			super.setLanguage(lang);
		setObjectId(Integer.toString(axisId));
		this.name = name;
		this.description = description;
	}
	
	public String getName(){
		return this.name;
	}

	public void setName(String name){
		this.name = name;
	}
	
	public String getDescription(){
		return this.description;
	}
 
	public void setDescription(String description){
		this.description = description;
	}
}