package com.stratelia.silverpeas.pdc.model;

import java.util.ArrayList;

import com.stratelia.silverpeas.containerManager.ContainerPositionInterface;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
* @author Nicolas EYSSERIC
*/
public class SearchContext implements ContainerPositionInterface, java.io.Serializable
{
	private ArrayList 	criterias 	= new ArrayList();
	private String		userId		= null;  //user who search

	public SearchContext() {
	}

	public SearchContext(ArrayList criterias) {
		this.criterias = criterias;
	}

	public ArrayList getCriterias() {
		return criterias;
	}

	public void addCriteria(SearchCriteria criteria) {
		SilverTrace.info("Pdc", "SearchContext.addCriteria()", "root.MSG_GEN_PARAM_VALUE", "criteria = " + criteria.toString());
		if (criterias == null) {
			criterias = new ArrayList();
		}
		
		//recherche de l'existance d'un critère sur l'axe
		SearchCriteria existingCriteriaOnAxis = getCriteriaOnAxis(criteria.getAxisId());
		if (existingCriteriaOnAxis != null)
		{
			//un critère sur l'axe existe déjà
			//on le supprime du contexte
			removeCriteria(existingCriteriaOnAxis);
		}
		
		criterias.add(criteria);
	}

    public void clearCriterias() {
		SilverTrace.info("Pdc", "SearchContext.clearCriterias()", "root.MSG_GEN_PARAM_VALUE");
        criterias = new ArrayList();
	}

	public void removeCriteria(SearchCriteria criteria) {
		if (criterias != null) {
			criterias.remove(criteria);
		}
	}
	
	public void removeCriteria(int axisId) {
		SearchCriteria criteria = getCriteriaOnAxis(axisId);
		if (criteria != null)
		{
			removeCriteria(criteria);
		}
	}

	/** Return true if the position is empty */
	public boolean isEmpty()
	{
		return (criterias.size()==0);
	}

	public SearchCriteria getCriteriaOnAxis(int axisId)
	{
		SearchCriteria criteria = null;
		for (int c=0; criterias != null && c<criterias.size(); c++)
		{
			criteria = (SearchCriteria) criterias.get(c);
			if (criteria.getAxisId() == axisId)
				return criteria;
		}
		return null;
	}
	
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

}