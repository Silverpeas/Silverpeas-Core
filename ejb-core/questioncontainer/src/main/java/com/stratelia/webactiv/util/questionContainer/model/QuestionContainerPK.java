package com.stratelia.webactiv.util.questionContainer.model;

import java.io.Serializable;
import com.stratelia.webactiv.util.*;

/**
 * It's the QuestionContainer PrimaryKey object It identify a QuestionContainer
 * 
 * @author Nicolas Eysseric
 * @version 1.0
 */
public class QuestionContainerPK extends WAPrimaryKey implements Serializable {

  public QuestionContainerPK(String id) {
    super(id);
  }

  public QuestionContainerPK(String id, String space, String componentName) {
    super(id, space, componentName);
  }

  public QuestionContainerPK(String id, WAPrimaryKey pk) {
    super(id, pk);
  }

  public String getRootTableName() {
    return "QuestionContainer";
  }

  public String getTableName() {
    return "SB_QuestionContainer_QC";
  }

  public boolean equals(Object other) {
    if (!(other instanceof QuestionContainerPK))
      return false;
    return (id.equals(((QuestionContainerPK) other).getId()))
        && (space.equals(((QuestionContainerPK) other).getSpace()))
        && (componentName.equals(((QuestionContainerPK) other)
            .getComponentName()));
  }

  public int hashCode() {
    return toString().hashCode();
  }
}