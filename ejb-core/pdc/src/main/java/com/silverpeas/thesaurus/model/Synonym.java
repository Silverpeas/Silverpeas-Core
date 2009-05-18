package com.silverpeas.thesaurus.model;

import com.stratelia.webactiv.persistence.*;
import java.lang.Comparable;
/**
* This class contains a full information about a Synonym
* a Synonym is linked to a Vocabulary and a Value (idTree - idTerm)
*/

public class Synonym extends SilverpeasBean implements Comparable {
	
	private long idVoca;
	private long idTree;
	private long idTerm;
	private String name;

   	public Synonym()
   	{
	}

	public long getIdVoca() {
		return idVoca;
	}
	
	public void setIdVoca(long idVoca) {
		this.idVoca = idVoca;
	}	
	
	public long getIdTree() {
		return idTree;
	}
	
	public void setIdTree(long idTree) {
		this.idTree = idTree;
	}		
	
	public long getIdTerm() {
		return idTerm;
	}
	
	public void setIdTerm(long idTerm) {
		this.idTerm = idTerm;
	}		
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}		

	public int compareTo(Object voca)
	{
		return this.getName().compareTo( ((Synonym) voca).getName());
	}

	public String _getTableName()
	{
		return "SB_Thesaurus_Synonym";
	}
	
	public int _getConnectionType()
	{
		return SilverpeasBeanDAO.CONNECTION_TYPE_DATASOURCE_SILVERPEAS;
	} 
}