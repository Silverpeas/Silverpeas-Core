package com.stratelia.webactiv.util.questionResult.model;

import java.io.Serializable;
import com.stratelia.webactiv.util.*;

/**
 * It's the Publication PrimaryKey object It identify a Publication
 * 
 * @author Nicolas Eysseric
 * @version 1.0
 */
public class QuestionResultPK extends WAPrimaryKey implements Serializable {

  /**
   * Constructor which set only the id
   * 
   * @since 1.0
   */
  public QuestionResultPK(String id) {
    super(id);
  }

  /**
   * Constructor which set the id The WAPrimaryKey provides space and component
   * name
   * 
   * @since 1.0
   */
  public QuestionResultPK(String id, String space, String componentName) {
    super(id, space, componentName);
  }

  /**
   * Constructor which set the id The WAPrimaryKey provides space and component
   * name
   * 
   * @since 1.0
   */
  public QuestionResultPK(String id, WAPrimaryKey pk) {
    super(id, pk);
  }

  /**
   * Return the object root table name
   * 
   * @return the root table name of the object
   * @since 1.0
   */
  public String getRootTableName() {
    return "QuestionResult";
  }

  /**
   * Return the object root table name
   * 
   * @return the root table name of the object
   * @since 1.0
   */
  public String getTableName() {
    return "SB_Question_QuestionResult";
  }

  /**
   * Check if an another object is equal to this object
   * 
   * @return true if other is equals to this object
   * @param other
   *          the object to compare to this PollPK
   * @since 1.0
   */
  public boolean equals(Object other) {
    if (!(other instanceof QuestionResultPK))
      return false;
    return (id.equals(((QuestionResultPK) other).getId()))
        && (space.equals(((QuestionResultPK) other).getSpace()))
        && (componentName.equals(((QuestionResultPK) other).getComponentName()));
  }

  /**
   * Returns a hash code for the key
   * 
   * @return A hash code for this object
   */
  public int hashCode() {
    return toString().hashCode();
  }
}