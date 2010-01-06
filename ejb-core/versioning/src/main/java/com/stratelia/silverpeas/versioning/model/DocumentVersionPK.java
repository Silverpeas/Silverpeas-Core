/**
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.silverpeas.versioning.model;

import java.io.Serializable;

import com.stratelia.webactiv.util.WAPrimaryKey;

/**
 * This object contains the info about PrimaryKey of document version
 * @author Georgy Shakirin
 * @version 1.0
 */

public class DocumentVersionPK extends WAPrimaryKey implements Serializable, Cloneable {

  private static final long serialVersionUID = 1L;

  /**
   * Constructor declaration
   * @param id
   * @see
   */
  public DocumentVersionPK(int id) {
    super(String.valueOf(id));
  }

  /**
   * Constructor declaration
   * @param id
   * @param spaceId
   * @param componentId
   * @see
   */
  public DocumentVersionPK(int id, String spaceId, String componentId) {
    super(String.valueOf(id), spaceId, componentId);
  }

  /**
   * Constructor declaration
   * @param id
   * @param pk
   * @see
   */
  public DocumentVersionPK(int id, WAPrimaryKey pk) {
    super(String.valueOf(id), pk);
  }

  /**
   * **********
   */

  public String getRootTableName() {
    return "Version";
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getTableName() {
    return "SB_Version_Version";
  }

  /**
   * Method declaration
   * @param other
   * @return
   * @see
   */
  public boolean equals(Object other) {
    if (!(other instanceof DocumentPK)) {
      return false;
    }
    return (id.equals(((DocumentPK) other).getId()))
        && (space.equals(((DocumentPK) other).getSpace()))
        && (componentName.equals(((DocumentPK) other).getComponentName()));
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String toString() {
    return "(id = " + getId() + ", Space = " + getSpace()
        + ", componentName = " + getComponentName() + ")";
  }

  /**
   * Returns a hash code for the key
   * @return A hash code for this object
   */
  public int hashCode() {
    return toString().hashCode();
  }

  /**
   * Support Cloneable Interface
   */
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      return null; // this should never happened
    }
  }

}