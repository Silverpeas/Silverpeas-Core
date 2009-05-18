package com.stratelia.webactiv.organization;

import com.stratelia.webactiv.beans.admin.ComponentI18N;

public class ComponentInstanceI18NRow
{
   public int 		id = -1;
   public int		componentId = -1;
   public String	lang = null;
   public String 	name = null;
   public String 	description = null;
   
   public ComponentInstanceI18NRow()
   {	   
   }
   
   public ComponentInstanceI18NRow(ComponentI18N componentI18N)
   {
	   id = componentI18N.getId();
	   componentId = Integer.parseInt(componentI18N.getObjectId());
	   lang = componentI18N.getLanguage();
	   name = componentI18N.getName();
	   description = componentI18N.getDescription();
   }
   
   public ComponentInstanceI18NRow(ComponentInstanceRow component)
   {
	   componentId = component.id;
       lang = component.lang;
       name = component.name;
       description = component.description;
   }
}
