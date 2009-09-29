package com.stratelia.webactiv.util.contact.info.model;

import java.io.Serializable;

import com.stratelia.webactiv.util.WAPrimaryKey;

public class InfoPK extends WAPrimaryKey implements Serializable {

  public InfoPK(String id) {
    super(id);
  }

  public InfoPK(String id, String space, String componentName) {
    super(id, space, componentName);
  }

  public InfoPK(String id, WAPrimaryKey pk) {
    super(id, pk);
  }

  public String getRootTableName() {
    return "Info";
  }

  public String getTableName() {
    return "SB_Contact_Info";
  }

  public boolean equals(Object other) {
    if (!(other instanceof InfoPK))
      return false;
    return (id.equals(((InfoPK) other).getId()))
        && (space.equals(((InfoPK) other).getSpace()))
        && (componentName.equals(((InfoPK) other).getComponentName()));
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