package com.stratelia.silverpeas.pdc.model;

import java.io.Serializable;
import com.stratelia.webactiv.util.WAPrimaryKey;

/**
* It's the UsedAxis PrimaryKey object
* It identify an axe used by an instance
* @author Nicolas EYSSERIC
*/
public class UsedAxisPK extends WAPrimaryKey implements Serializable {


	/**
	* Constructor which set only the id
	*/
	public UsedAxisPK (String id) {
		super(id);
	}

	public UsedAxisPK (int id) {
		super( (new Integer(id)).toString() );
	}

	/**
	* Constructor which set id, space and component name
	*/
	public UsedAxisPK (String id, String space, String componentName) {
		super(id, space, componentName);
	}
  
	/**
	* Constructor which set the id
	* The WAPrimaryKey provides space and component name
	*/
	public UsedAxisPK (String id, WAPrimaryKey pk) {
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
		return "SB_Pdc_UsedAxis";
	}
  
	/**
	* Check if an another object is equal to this object
	* @return true if other is equals to this object
	* @param other the object to compare to this NodePK
	*/
	public boolean equals(Object other) {
		if (!(other instanceof UsedAxisPK)) 
			return false;
    
		return	(id.equals( ((UsedAxisPK) other).getId()) ) &&
				(space.equals(((UsedAxisPK) other).getSpace()) ) &&
				(componentName.equals(((UsedAxisPK) other).getComponentName()) );
	}

	/**
	* Returns a hash code for the key
	* @return A hash code for this object
	*/
	public int hashCode() {
		return toString().hashCode();
	}


}