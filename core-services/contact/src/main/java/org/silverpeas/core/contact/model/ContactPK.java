/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.contact.model;

import org.silverpeas.core.ResourceReference;

import java.io.Serializable;

/**
 * It's the Contact PrimaryKey object It identify a Contact
 * @author Nicolas Eysseric
 * @version 1.0
 */
public class ContactPK extends ResourceReference implements Serializable {

  // for flat pk design pattern
  private transient ContactDetail contactDetail = null;

  /**
   * Constructor which set only the id
   * @since 1.0
   */
  public ContactPK(String id) {
    super(id);
  }

  /**
   * Constructor which set the id The WAPrimaryKey provides space and component name
   * @since 1.0
   */
  public ContactPK(String id, String componentName) {
    super(id, componentName);
  }

  /**
   * Constructor which set the id The WAPrimaryKey provides space and component name
   * @since 1.0
   */
  public ContactPK(String id, ResourceReference pk) {
    this(id, pk.getInstanceId());
    setSpace(pk.getSpace());
  }

  /**
   * Return the object root table name
   * @return the root table name of the object
   * @since 1.0
   */
  @Override
  public String getRootTableName() {
    return "Contact";
  }

  /**
   * Return the object table name
   * @return the table name of the object
   * @since 1.0
   */
  @Override
  public String getTableName() {
    return "SB_Contact_Contact";
  }

  /**
   * Check if an another object is equal to this object
   * @param other the object to compare to this ContactPK
   * @return true if other is equals to this object
   * @since 1.0
   */
  @Override
  public boolean equals(Object other) {
    return other instanceof ContactPK && (id.equals(((ContactPK) other).getId())) &&
        (space.equals(((ContactPK) other).getSpace())) &&
        (componentName.equals(((ContactPK) other).getComponentName()));
  }

  public ContactDetail getContactDetail() {
    return contactDetail;
  }

  public void setContactDetail(final ContactDetail contactDetail) {
    this.contactDetail = contactDetail;
  }

  /**
   * Returns a hash code for the key
   * @return A hash code for this object
   */
  @Override
  public int hashCode() {
    return toString().hashCode();
  }
}