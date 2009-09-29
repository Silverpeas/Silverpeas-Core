package com.stratelia.webactiv.util.publication.info.model;

import java.io.Serializable;

import com.stratelia.webactiv.util.WAPrimaryKey;

public class InfoTextPK extends WAPrimaryKey implements Serializable {

  public InfoTextPK(String id) {
    super(id);
  }

  public InfoTextPK(String id, String space, String componentName) {
    super(id, space, componentName);
  }

  public InfoTextPK(String id, WAPrimaryKey pk) {
    super(id, pk);
  }

  public String getRootTableName() {
    return "InfoText";
  }

  public String getTableName() {
    return "SB_Publication_InfoText";
  }

  public boolean equals(Object other) {
    if (!(other instanceof InfoTextPK))
      return false;
    return (id.equals(((InfoTextPK) other).getId()))
        && (space.equals(((InfoTextPK) other).getSpace()))
        && (componentName.equals(((InfoTextPK) other).getComponentName()));
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

}