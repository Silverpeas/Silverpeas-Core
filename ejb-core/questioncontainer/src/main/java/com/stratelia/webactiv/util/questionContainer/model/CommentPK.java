package com.stratelia.webactiv.util.questionContainer.model;

import java.io.Serializable;
import com.stratelia.webactiv.util.*;

/**
 * It's the Publication PrimaryKey object
 * It identify a Publication
 * @author Nicolas Eysseric
 * @version 1.0
 */
public class CommentPK extends WAPrimaryKey implements Serializable{
  
  /**
	* Constructor which set only the id
	* @since 1.0
	*/
  public CommentPK(String id) {
    super(id);
  }
  
   /**
	* Constructor which set the id
	* The WAPrimaryKey provides space and component name
	* @since 1.0
	*/
  public CommentPK(String id,String space,String componentName) {
    super(id, space, componentName);
  }
  
  /**
	* Constructor which set the id
	* The WAPrimaryKey provides space and component name
	* @since 1.0
	*/
  public CommentPK(String id,WAPrimaryKey pk) {
    super(id, pk);
  }
  
  /**
	* Return the object root table name
	* @return the root table name of the object
	* @since 1.0
	*/
  public String getRootTableName() {
    return "Comment";
  }
  
  /**
	* Return the object  table name
	* @return the  table name of the object
	* @since 1.0
	*/
  public String getTableName() {
    return "SB_QuestionContainer_Comment";
  }
  
  /**
	* Check if an another object is equal to this object
	* @return true if other is equals to this object
	* @param other the object to compare to this PollPK
	* @since 1.0
	*/
  public boolean equals(Object other) {
    if (!(other instanceof CommentPK)) return false;
    return (id.equals( ((CommentPK) other).getId()) ) &&
       (space.equals(((CommentPK) other).getSpace()) ) &&
       (componentName.equals(((CommentPK) other).getComponentName()) );
  }
  
  /**
	* Returns a hash code for the key
	* @return A hash code for this object
	*/
  public int hashCode() {
		return toString().hashCode();
  } 
}