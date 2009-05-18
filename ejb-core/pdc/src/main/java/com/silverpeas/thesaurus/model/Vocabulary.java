package com.silverpeas.thesaurus.model;

import com.stratelia.webactiv.persistence.*;
import java.lang.Comparable;
/**
* This class contains a full information about a Vocabulary
*/

public class Vocabulary extends SilverpeasBean implements Comparable {
	
	private String name;
	private String description;

   	public Vocabulary()
   	{
   	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}	
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}			

	public boolean equals(Object voca)
	{
		return this.getName().equals( ((Vocabulary) voca).getName());
	}

	public int compareTo(Object voca)
	{
		return this.getName().compareTo( ((Vocabulary) voca).getName());
	}

	public String _getTableName()
	{
		return "SB_Thesaurus_Vocabulary";
	}
	
	public int _getConnectionType()
	{
		return SilverpeasBeanDAO.CONNECTION_TYPE_DATASOURCE_SILVERPEAS;
	} 
}