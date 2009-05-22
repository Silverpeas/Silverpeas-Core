package com.stratelia.webactiv.util.contact.model;

import java.io.Serializable;

import com.stratelia.webactiv.util.WAPrimaryKey;

/**
 * It's the Contact PrimaryKey object
 * It identify a Contact
 * @author Nicolas Eysseric
 * @version 1.0
 */
public class ContactPK extends WAPrimaryKey implements Serializable{

  // for flat pk design pattern
  public transient ContactDetail pubDetail = null;

  /**
	* Constructor which set only the id
	* @since 1.0
	*/
  public ContactPK (String id) {
    super(id);
  }
  
   /**
	* Constructor which set the id
	* The WAPrimaryKey provides space and component name
	* @since 1.0
	*/
  public ContactPK (String id, String space, String componentName) {
    super(id, space, componentName);
  }
  
  /**
	* Constructor which set the id
	* The WAPrimaryKey provides space and component name
	* @since 1.0
	*/
  public ContactPK (String id, WAPrimaryKey pk) {
    super(id, pk);
  }
  
  /**
	* Return the object root table name
	* @return the root table name of the object
	* @since 1.0
	*/
  public String getRootTableName() {
    return "Contact";
  }

  /**
	* Return the object  table name
	* @return the  table name of the object
	* @since 1.0
	*/
  public String getTableName() {
    return "SB_Contact_Contact";
  }
  
  /**
	* Check if an another object is equal to this object
	* @return true if other is equals to this object
	* @param other the object to compare to this NodePK
	* @since 1.0
	*/
  public boolean equals(Object other) {
    if (!(other instanceof ContactPK)) return false;
    return (id.equals( ((ContactPK) other).getId()) ) &&
       (space.equals(((ContactPK) other).getSpace()) ) &&
       (componentName.equals(((ContactPK) other).getComponentName()) );
  }
  
  /**
	* Returns a hash code for the key
	* @return A hash code for this object
	*/
  public int hashCode() {
		return toString().hashCode();
  } 
}