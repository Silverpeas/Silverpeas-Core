package com.stratelia.silverpeas.pdc.model;

import com.stratelia.webactiv.persistence.*;

public class CompleteUsedAxis extends SilverpeasBean implements java.io.Serializable {
		
	private Axis		axis		= null;
	private UsedAxis	usedAxis	= null;

	public CompleteUsedAxis(){
	}

	public CompleteUsedAxis(Axis axis, UsedAxis usedAxis){
		this.axis = axis;
		this.usedAxis = usedAxis;
	}

	public void setAxis(Axis axis){
		this.axis = axis;
	}

	public void setUsedAxis(UsedAxis usedAxis){
		this.usedAxis = usedAxis;
	}
	
	public Axis getAxis(){
		return this.axis;
	}

	public UsedAxis getUsedAxis(){
		return this.usedAxis;
	}

}