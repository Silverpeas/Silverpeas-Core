package com.stratelia.silverpeas.pdc.model;

import com.stratelia.silverpeas.classifyEngine.Criteria;

/**
* @author Nicolas EYSSERIC
*/
public class SearchCriteria extends Criteria implements java.io.Serializable
{

    protected SearchCriteria() {
        super();
    }

	public SearchCriteria(int axisId, String value)
    {
		super(axisId, value);
    }

	public boolean equals(Object other) {
		if (!(other instanceof SearchCriteria)) return false;
		return (getAxisId() == ((SearchCriteria) other).getAxisId()) &&
		(getValue().equals(((SearchCriteria) other).getValue()) );
	}
	public String toString(){
		String axisId = new Integer(getAxisId()).toString();
		return "Search Criteria Object : [ axisId="+axisId+", value="+getValue()+" ]";
	}

    /**
     * Support Cloneable Interface
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return null; // this should never happened
        }
    }

}