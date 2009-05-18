package com.stratelia.silverpeas.pdc.model;

import java.util.List;

/**
* @author Nicolas EYSSERIC
*/
public class ClassifyValue extends com.stratelia.silverpeas.classifyEngine.Value implements java.io.Serializable {

	private List fullPath = null;

	private String axisName = null;
	
	public ClassifyValue() {}

	public ClassifyValue(int nGivenAxisId, String sGivenValue)
    {
		super(nGivenAxisId, sGivenValue);
    }

	//return a list of Value objects
	public List getFullPath() {
		return this.fullPath;
	}

	public void setFullPath(List fullPath) {
		this.fullPath = fullPath;
	}

	public String getAxisName() {
		return this.axisName;
	}

	public void setAxisName(String axisName) {
		this.axisName = axisName;
	}

    public String toString() {
        return "ClassifyValue object :[ AxisId=" + getAxisId() + ", " +
                 " value=" + getValue();
    }
}