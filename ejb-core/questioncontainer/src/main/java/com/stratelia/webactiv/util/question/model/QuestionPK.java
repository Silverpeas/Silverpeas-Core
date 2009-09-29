//Source file: d:\\webactiv\\util\\com\\stratelia\\webactiv\\util\\question\\model\\QuestionPK.java

package com.stratelia.webactiv.util.question.model;

import com.stratelia.webactiv.util.*;
import java.io.Serializable;

/**
 * It's the Publication PrimaryKey object It identify a Publication
 * 
 * @author Nicolas Eysseric
 * @version 1.0
 */
public class QuestionPK extends WAPrimaryKey implements Serializable {

  /**
   * Constructor which set only the id
   * 
   * @since 1.0
   * @roseuid 3AB7343503E1
   */
  public QuestionPK(String id) {
    super(id);
  }

  /**
   * Constructor which set the id The WAPrimaryKey provides space and component
   * name
   * 
   * @since 1.0
   * @roseuid 3AB734360003
   */
  public QuestionPK(String id, String spaceId, String componentId) {
    super(id, spaceId, componentId);
  }

  /**
   * Constructor which set the id The WAPrimaryKey provides space and component
   * name
   * 
   * @since 1.0
   * @roseuid 3AB734360018
   */
  public QuestionPK(String id, WAPrimaryKey pk) {
    super(id, pk);
  }

  /**
   * Return the object root table name
   * 
   * @return the root table name of the object
   * @since 1.0
   * @roseuid 3AB73436002B
   */
  public String getRootTableName() {
    return "Question";
  }

  /**
   * Return the object table name
   * 
   * @return the table name of the object
   * @since 1.0
   * @roseuid 3AB73436002B
   */
  public String getTableName() {
    return "SB_Question_Question";
  }

  /**
   * Check if an another object is equal to this object
   * 
   * @return true if other is equals to this object
   * @param other
   *          the object to compare to this PollPK
   * @since 1.0
   * @roseuid 3AB73436002C
   */
  public boolean equals(Object other) {
    if (!(other instanceof QuestionPK))
      return false;
    return (id.equals(((QuestionPK) other).getId()))
        && (space.equals(((QuestionPK) other).getSpace()))
        && (componentName.equals(((QuestionPK) other).getComponentName()));
  }

  /**
   * Returns a hash code for the key
   * 
   * @return A hash code for this object
   * @roseuid 3AB734360036
   */
  public int hashCode() {
    return toString().hashCode();
  }
}
