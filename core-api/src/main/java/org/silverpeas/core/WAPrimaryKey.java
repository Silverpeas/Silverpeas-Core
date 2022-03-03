/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * "https://www.silverpeas.org/legal/floss_exception.html"
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
 * It is now replaced by {@link org.silverpeas.core.ResourceIdentifier} but it is still
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
  protected String id = null;

  /**
   * The space where is implemented the entity
   * @since 1.0
   */
  protected String space = null;

  /**
   * The component name in the space
   * @since 1.0
   */
  protected String componentName = null;

  /**
   * Constructor which set only the id
   * @param id the unique identifier value
   * @since 1.0
   */
  public WAPrimaryKey(String id) {
    setId(id);
  }

  /**
   * Constructor which set id, space and component name
   * @param  id the unique identifier value
   * @param  space the space identifier
   * @param  componentName the component instance identifier
   * @since 1.0
   */
  public WAPrimaryKey(String id, String space, String componentName) {
    setId(id);
    setSpace(space);
    setComponentName(componentName);
  }

  /**
   * Constructor which set id, space and component name
   * @param  id the unique identifier value
   * @param  componentId the component instance identifier
   * @since 1.0
   */
  public WAPrimaryKey(String id, String componentId) {
    setId(id);
    setSpace(null);
    setComponentName(componentId);
  }

  /**
   * Constructor which set the id. The WAPrimaryKey provides space and component instance
   * identifiers.
   * @param id the unique identifier value
   * @param pk another primary key from which is taken the space and component instance identifiers.
   * @since 1.0
   */
  public WAPrimaryKey(String id, WAPrimaryKey pk) {
    setId(id);
    setSpace(pk.getSpace());
    setComponentName(pk.getComponentName());
  }

  public ResourceReference toResourceReference() {
    return new ResourceReference(this);
  }


  /**
   * This method must be specialized - Check if an another object is equal to this object
   * @param obj the object to compare to this WAPrimaryKey
   * @return true if obj is equals to this object
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
   * @since 1.0
   */
  public String getId() {
    return id;
  }

  /**
   * Set the row id of this object
   * @param val the row id
   * @since 1.0
   */
  public void setId(String val) {
    id = val;
  }

  /**
   * Get the space of this object
   * @return the space
   * @since 1.0
   */
  public String getSpace() {
    return space;
  }

  /**
   * Set the space of this object
   * @param space the space
   * @since 1.0
   */
  public void setSpace(String space) {
    this.space = space;
  }

  /**
   * Get the component name of this object
   * @return the component name
   * @since 1.0
   */
  public String getComponentName() {
    return componentName;
  }

  /**
   * Set the component name of this object
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
   * @see #getRootTableName
   * @since 1.0
   */
  public String getTableName() {
    return space + componentName + getRootTableName();
  }

  /**
   * Get the database table name where the object is stored
   * rootTableName (ex : ED1KmeliaPublication)
   * @param space a space name
   * @param componentName a component name
   * @return the database table name where the object is stored : space + componentName +
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
      throw new SilverpeasRuntimeException(e);
    }
  }
}