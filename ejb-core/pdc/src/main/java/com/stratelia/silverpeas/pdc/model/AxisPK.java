package com.stratelia.silverpeas.pdc.model;

import java.io.Serializable;
import com.stratelia.webactiv.util.WAPrimaryKey;

/**
* It's the Axis PrimaryKey object
* It identify an axe
* @author Sébastien Antonio
*/
public class AxisPK extends WAPrimaryKey implements Serializable {


	/**
	* Constructor which set only the id
	*/
	public AxisPK (String id) {
		super(id);
	}

	public AxisPK (int id) {
		super( (new Integer(id)).toString() );
	}

	/**
	* Constructor which set id, space and component name
	*/
	public AxisPK (String id, String space, String componentName) {
		super(id, space, componentName);
	}
  
	/**
	* Constructor which set the id
	* The WAPrimaryKey provides space and component name
	*/
	public AxisPK (String id, WAPrimaryKey pk) {
		super(id, pk);
	}
  
	/**
	* Return the object root table name
	* @return the root table name of the object
	*/
	public String getRootTableName() {
		return "Pdc";
	}

	/**
	* Return the object table name
	* @return the table name of the object
	*/
	public String getTableName() {
		return "SB_PdcAxis";
	}
  
	/**
	* Check if an another object is equal to this object
	* @return true if other is equals to this object
	* @param other the object to compare to this NodePK
	*/
	public boolean equals(Object other) {
		if (!(other instanceof AxisPK)) 
			return false;
    
		return	(id.equals( ((AxisPK) other).getId()) ) &&
				(space.equals(((AxisPK) other).getSpace()) ) &&
				(componentName.equals(((AxisPK) other).getComponentName()) );
	}

	/**
	* Returns a hash code for the key
	* @return A hash code for this object
	*/
	public int hashCode() {
		return toString().hashCode();
	}


}