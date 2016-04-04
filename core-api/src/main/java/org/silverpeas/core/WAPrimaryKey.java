/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core;

import java.io.Serializable;

/**
 * The primary key for a entity bean defines:
 * <ul>
 * <li>the row id in the database</li>
 * <li>the space</li>
 * <li>the component name</li>
 * </ul>
 * It is now replaced by {@code {@link org.silverpeas.core.ResourceIdentifier} but it is still
 * used in old code.
 * @author Nicolas Eysseric
 * @version 1.0
 * @deprecated
 */
@Deprecated
public abstract class WAPrimaryKey implements Serializable, Cloneable {

  private static final long serialVersionUID = -2456912022917180222L;

  /**
   * The row id in the table defined by getTableName()
   * @see #getTableName()
   * @since 1.0
   */
  public String id = null;

  /**
   * The space where is implemented the entity
   * @since 1.0
   */
  public String space = null;

  /**
   * The component name in the space
   * @since 1.0
   */
  public String componentName = null;

  /**
   * Constructor which set only the id
   * @see #id
   * @since 1.0
   */
  public WAPrimaryKey(String id) {
    setId(id);
  }

  /**
   * Constructor which set id, space and component name
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
   * Constructor which set the id The WAPrimaryKey provides space and component name
   * @since 1.0
   */
  public WAPrimaryKey(String id, WAPrimaryKey pk) {
    setId(id);
    setSpace(pk.getSpace());
    setComponentName(pk.getComponentName());
  }


  /**
   * This method must be specialized - Check if an another object is equal to this object
   * @return true if obj is equals to this object
   * @param obj the object to compare to this WAPrimaryKey
   * @since 1.0
   */
  @Override
  public abstract boolean equals(Object obj);


  /**
   * Return the object root table name
   * @return the root table name of the object (exemple : Publication, Node, ...)
   * @since 1.0
   */
  public String getRootTableName() {
    return null;
  }

  /**
   * Get the row id of this object
   * @return the id
   * @see #id
   * @since 1.0
   */
  public String getId() {
    return id;
  }

  /**
   * Set the row id of this object
   * @see #id
   * @param val the row id
   * @since 1.0
   */
  public void setId(String val) {
    id = val;
  }

  /**
   * Get the space of this object
   * @return the space
   * @see #space
   * @since 1.0
   */
  public String getSpace() {
    return space;
  }

  /**
   * Set the space of this object
   * @see #space
   * @param space the space
   * @since 1.0
   */
  public void setSpace(String space) {
    this.space = space;
  }

  /**
   * Get the component name of this object
   * @return the component name
   * @see #componentName
   * @since 1.0
   */
  public String getComponentName() {
    return componentName;
  }

  /**
   * Set the component name of this object
   * @see #componentName
   * @param componentName the component name
   * @since 1.0
   */
  public void setComponentName(String componentName) {
    this.componentName = componentName;
  }

  /**
   * Get the database table name where the object is stored
   * @return the database table name where the object is stored : space + componentName +
   * rootTableName (ex : ED1KmeliaPublication)
   * @see #space
   * @see #componentName
   * @see #getRootTableName
   * @since 1.0
   */
  public String getTableName() {
    return space + componentName + getRootTableName();
  }

  /**
   * Get the database table name where the object is stored
   * @return the database table name where the object is stored : space + componentName +
   * rootTableName (ex : ED1KmeliaPublication)
   * @param space a space name
   * @param componentName a component name
   * @since 1.0
   */
  public String getTableName(String space, String componentName) {
    return space + componentName + getRootTableName();
  }

  /**
   * Converts the contents of the key into a readable String.
   * @return The string representation of this object
   */
  @Override
  public String toString() {
    return "(id = " + getId() + ", instanceId = " + getInstanceId() + ")";
  }


  /**
   * Returns a hash code for the key
   * @return A hash code for this object
   */
  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  public String getSpaceId() {
    return getSpace();
  }

  public String getInstanceId() {
    return getComponentName();
  }

  @SuppressWarnings("CloneDoesntDeclareCloneNotSupportedException")
  @Override
  public WAPrimaryKey clone() {
    try {
      return (WAPrimaryKey) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
  }
}