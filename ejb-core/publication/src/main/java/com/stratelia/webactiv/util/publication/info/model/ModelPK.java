package com.stratelia.webactiv.util.publication.info.model;

import java.io.Serializable;

import com.stratelia.webactiv.util.WAPrimaryKey;

public class ModelPK extends WAPrimaryKey implements Serializable {

  public ModelPK(String id) {
    super(id);
  }

  public ModelPK(String id, String space, String componentName) {
    super(id, space, componentName);
  }

  public ModelPK(String id, WAPrimaryKey pk) {
    super(id, pk);
  }

  public String getRootTableName() {
    return "Model";
  }

  public boolean equals(Object other) {
    if (!(other instanceof ModelPK))
      return false;
    return (id.equals(((ModelPK) other).getId()))
        && (space.equals(((ModelPK) other).getSpace()))
        && (componentName.equals(((ModelPK) other).getComponentName()));
  }

  public String toString() {
    return "(id = " + getId() + ", space = " + getSpace()
        + ", componentName = " + getComponentName() + ")";
  }

  /**
   * 
   * Returns a hash code for the key
   * 
   * @return A hash code for this object
   */
  public int hashCode() {
    return toString().hashCode();
  }

  public String getTableName() {
    // this component is common for all the other.
    return getRootTableName();
  }

}