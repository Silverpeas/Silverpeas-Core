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
 * This PrimaryKey object must be used between two different and independents modules. It avoids
 * circular dependencies
 * @author Nicolas Eysseric
 */
@Deprecated
public class ForeignPK extends WAPrimaryKey implements Serializable {

  private static final long serialVersionUID = 1551181996404764039L;

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
   * @return the root table name of the object
   * @since 1.0
   */
  @Override
  public String getRootTableName() {
    return "Useless";
  }

  /**
   * Return the object table name
   * @return the table name of the object
   * @since 1.0
   */
  @Override
  public String getTableName() {
    return "Useless";
  }

  /**
   * Check if an another object is equal to this object
   * @return true if other is equals to this object
   * @param other the object to compare to this NodePK
   * @since 1.0
   */
  @Override
  public boolean equals(Object other) {
    if (!(other instanceof ForeignPK)) {
      return false;
    }
    return (id.equals(((ForeignPK) other).getId()))
        && (componentName.equals(((ForeignPK) other).getComponentName()));
  }

  /**
   * Returns a hash code for the key
   * @return A hash code for this object
   */
  @Override
  public int hashCode() {
    return this.id.hashCode() ^ this.componentName.hashCode();
  }
}