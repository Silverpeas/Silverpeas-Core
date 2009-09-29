package com.stratelia.webactiv.util;

import java.io.Serializable;

/**
 * The webactiv primary key for a entity bean defines : - the row id in the
 * database, - the space, - the component name.
 * 
 * @author Nicolas Eysseric
 * @version 1.0
 */
public abstract class WAPrimaryKey implements Serializable {

  /**
   * The row id in the table defined by getTableName()
   * 
   * @see #getTableName()
   * @since 1.0
   */
  public String id = null;

  /**
   * The space where is implemented the entity
   * 
   * @since 1.0
   */
  public String space = null;

  /**
   * The component name in the space
   * 
   * @since 1.0
   */
  public String componentName = null;

  /**
   * Constructor which set only the id
   * 
   * @see #id
   * @since 1.0
   */
  public WAPrimaryKey(String id) {
    setId(id);
  }

  /**
   * Constructor which set id, space and component name
   * 
   * @see #id
   * @see #space
   * @see #componentName
   * @since 1.0
   */
  public WAPrimaryKey(String id, String space, String componentName) {
    setId(id);
    setSpace(space);
    setComponentName(componentName);
  }

  public WAPrimaryKey(String id, String componentId) {
    setId(id);
    setSpace(null);
    setComponentName(componentId);
  }

  /**
   * Constructor which set the id The WAPrimaryKey provides space and component
   * name
   * 
   * @since 1.0
   */
  public WAPrimaryKey(String id, WAPrimaryKey pk) {
    setId(id);
    setSpace(pk.getSpace());
    setComponentName(pk.getComponentName());
  }

  /**
   * This method must be specialized - Check if an another object is equal to
   * this object
   * 
   * @return true if obj is equals to this object
   * @param obj
   *          the object to compare to this WAPrimaryKey
   * @since 1.0
   */
  public abstract boolean equals(Object obj);

  /**
   * Return the object root table name
   * 
   * @return the root table name of the object (exemple : Publication, Node,
   *         ...)
   * @since 1.0
   */
  public String getRootTableName() {
    return null;
  }

  /**
   * Get the row id of this object
   * 
   * @return the id
   * @see #id
   * @since 1.0
   */
  public String getId() {
    return id;
  }

  /**
   * Set the row id of this object
   * 
   * @see #id
   * @param val
   *          the row id
   * @since 1.0
   */
  public void setId(String val) {
    id = val;
  }

  /**
   * Get the space of this object
   * 
   * @return the space
   * @see #space
   * @since 1.0
   */
  public String getSpace() {
    return space;
  }

  /**
   * Set the space of this object
   * 
   * @see #space
   * @param space
   *          the space
   * @since 1.0
   */
  public void setSpace(String space) {
    this.space = space;
  }

  /**
   * Get the component name of this object
   * 
   * @return the component name
   * @see #componentName
   * @since 1.0
   */
  public String getComponentName() {
    return componentName;
  }

  /**
   * Set the component name of this object
   * 
   * @see #componentName
   * @param componentName
   *          the component name
   * @since 1.0
   */
  public void setComponentName(String componentName) {
    this.componentName = componentName;
  }

  /**
   * Get the database table name where the object is stored
   * 
   * @return the database table name where the object is stored : space +
   *         componentName + rootTableName (ex : ED1KmeliaPublication)
   * @see #space
   * @see #componentName
   * @see #getRootTableName
   * @since 1.0
   */
  public String getTableName() {
    /*
     * if ((space == null) || (componentName == null)) {Debug.println(
     * "WAPrimaryKey.getTableName() : error : some column of the primary key are null: "
     * + toString() + " Impossible to give a table name."); throw new
     * EJBException(
     * "WAPrimaryKey.getTableName() : error : some column of the primary key are null: "
     * + toString() + " Impossible to give a table name."); }
     */
    return space + componentName + getRootTableName();
  }

  /**
   * Get the database table name where the object is stored
   * 
   * @return the database table name where the object is stored : space +
   *         componentName + rootTableName (ex : ED1KmeliaPublication)
   * @param #space a space name
   * @param #componentName a component name
   * @see #space
   * @see #componentName
   * @see #getRootTableName
   * @since 1.0
   */
  public String getTableName(String space, String componentName) {
    return space + componentName + getRootTableName();
  }

  /**
   * Converts the contents of the key into a readable String.
   * 
   * @return The string representation of this object
   */
  public String toString() {
    return "(id = " + getId() + ", space = " + getSpace()
        + ", componentName = " + getComponentName() + ")";
  }

  /**
   * Returns a hash code for the key
   * 
   * @return A hash code for this object
   */
  public int hashCode() {
    return toString().hashCode();
  }

  public String getSpaceId() {
    return getSpace();
  }

  public String getInstanceId() {
    return getComponentName();
  }
}