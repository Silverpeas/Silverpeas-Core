//Source file: d:\\webactiv\\util\\com\\stratelia\\webactiv\\util\\answer\\model\\AnswerPK.java

package com.stratelia.webactiv.util.answer.model;

import com.stratelia.webactiv.util.*;
import java.io.Serializable;

/**
 * It's the Publication PrimaryKey object It identify a Publication
 * 
 * @author Nicolas Eysseric
 * @version 1.0
 */
public class AnswerPK extends WAPrimaryKey implements Serializable {

  /**
   * Constructor which set only the id
   * 
   * @since 1.0
   * @roseuid 3AB7338B0329
   */
  public AnswerPK(String id) {
    super(id);
  }

  /**
   * Constructor which set the id The WAPrimaryKey provides space and component
   * name
   * 
   * @since 1.0
   * @roseuid 3AB7338B0334
   */
  public AnswerPK(String id, String spaceId, String componentId) {
    super(id, spaceId, componentId);
  }

  /**
   * Constructor which set the id The WAPrimaryKey provides space and component
   * name
   * 
   * @since 1.0
   * @roseuid 3AB7338B0348
   */
  public AnswerPK(String id, WAPrimaryKey pk) {
    super(id, pk);
  }

  /**
   * Return the object root table name
   * 
   * @return the root table name of the object
   * @since 1.0
   * @roseuid 3AB7338B035B
   */
  public String getRootTableName() {
    return "Answer";
  }

  /**
   * Return the object table name
   * 
   * @return the table name of the object
   * @since 1.0
   * @roseuid 3AB7338B035B
   */
  public String getTableName() {
    return "SB_Question_Answer";
  }

  /**
   * Check if an another object is equal to this object
   * 
   * @return true if other is equals to this object
   * @param other
   *          the object to compare to this PollPK
   * @since 1.0
   * @roseuid 3AB7338B035C
   */
  public boolean equals(Object other) {
    if (!(other instanceof AnswerPK))
      return false;
    return (id.equals(((AnswerPK) other).getId()))
        && (space.equals(((AnswerPK) other).getSpace()))
        && (componentName.equals(((AnswerPK) other).getComponentName()));
  }

  /**
   * Returns a hash code for the key
   * 
   * @return A hash code for this object
   * @roseuid 3AB7338B0365
   */
  public int hashCode() {
    return toString().hashCode();
  }
}
