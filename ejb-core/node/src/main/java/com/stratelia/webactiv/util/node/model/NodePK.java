package com.stratelia.webactiv.util.node.model;

import java.io.Serializable;
import com.stratelia.webactiv.util.WAPrimaryKey;

/**
 * It's the Node PrimaryKey object
 * It identify a Node
 * @author Nicolas Eysseric
 * @version 1.0
 */
public class NodePK extends WAPrimaryKey implements Serializable {

  // to apply the fat key pattern
  transient public NodeDetail nodeDetail = null;

  /**
	* Constructor which set only the id
	* @since 1.0
	*/
  public NodePK (String id) {
    super(id);
  }

  /**
	* Constructor which set id, space and component name
	* @since 1.0
	*/
  public NodePK (String id, String space, String componentName) {
    super(id, space, componentName);
  }
  
  public NodePK(String id, String componentId)
  {
	  super(id, componentId);
  }
  
  /**
	* Constructor which set the id
	* The WAPrimaryKey provides space and component name
	* @since 1.0
	*/
  public NodePK (String id, WAPrimaryKey pk) {
    super(id, pk);
  }
  
  /**
	* Return the object root table name
	* @return the root table name of the object
	* @since 1.0
	*/
  public String getRootTableName() {
    return "Node";
  }

  /**
	* Return the object table name
	* @return the table name of the object
	* @since 1.0
	*/
  public String getTableName() {
    return "SB_Node_Node";
  }


  /**
	* Check if an another object is equal to this object
	* @return true if other is equals to this object
	* @param other the object to compare to this NodePK
	* @since 1.0
	*/
  public boolean equals(Object other) {
    if (!(other instanceof NodePK)) return false;
    return (id.equals( ((NodePK) other).getId()) ) &&
       (componentName.equals(((NodePK) other).getComponentName()) );
  }

	/**
	* Returns a hash code for the key
	* @return A hash code for this object
	*/
	public int hashCode() {
		return this.id.hashCode() ^ this.componentName.hashCode();
		//return toString().hashCode();
	}
}