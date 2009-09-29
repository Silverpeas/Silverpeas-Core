package com.silverpeas.util;

import java.io.Serializable;

import com.stratelia.webactiv.util.WAPrimaryKey;

/**
 * This PrimaryKey object must be used between two differents and independants
 * modules It avoids circular dependencies
 * 
 * @author Nicolas Eysseric
 * @version 1.0
 */
public class ForeignPK extends WAPrimaryKey implements Serializable {

  public ForeignPK(String id) {
    super(id);
  }

  public ForeignPK(String id, String componentId) {
    super(id, componentId);
  }

  public ForeignPK(String id, WAPrimaryKey pk) {
    super(id, pk.getInstanceId());
  }

  public ForeignPK(WAPrimaryKey pk) {
    super(pk.getId(), pk.getInstanceId());
  }

  /**
   * Return the object root table name
   * 
   * @return the root table name of the object
   * @since 1.0
   */
  public String getRootTableName() {
    return "Useless";
  }

  /**
   * Return the object table name
   * 
   * @return the table name of the object
   * @since 1.0
   */
  public String getTableName() {
    return "Useless";
  }

  /**
   * Check if an another object is equal to this object
   * 
   * @return true if other is equals to this object
   * @param other
   *          the object to compare to this NodePK
   * @since 1.0
   */
  public boolean equals(Object other) {
    if (!(other instanceof ForeignPK))
      return false;
    return (id.equals(((ForeignPK) other).getId()))
        && (componentName.equals(((ForeignPK) other).getComponentName()));
  }

  /**
   * Returns a hash code for the key
   * 
   * @return A hash code for this object
   */
  public int hashCode() {
    return this.id.hashCode() ^ this.componentName.hashCode();
  }
}