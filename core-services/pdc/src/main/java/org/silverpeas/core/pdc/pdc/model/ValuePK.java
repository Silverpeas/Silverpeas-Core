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
package org.silverpeas.core.pdc.pdc.model;

import java.io.Serializable;
import org.silverpeas.core.WAPrimaryKey;

/**
 * It's the Value PrimaryKey object It identify values of the an axe
 */
public class ValuePK extends WAPrimaryKey implements Serializable {

  private static final long serialVersionUID = 2507337218364427387L;

  /**
   * Constructor which set only the id
   */
  public ValuePK(String id) {
    super(id);
  }

  public ValuePK(int id) {
    super((Integer.valueOf(id)).toString());
  }

  /**
   * Constructor which set id, space and component name
   */
  public ValuePK(String id, String space, String componentName) {
    super(id, space, componentName);
  }

  /**
   * Constructor which set the id The WAPrimaryKey provides space and component name
   */
  public ValuePK(String id, WAPrimaryKey pk) {
    super(id, pk);
  }

  /**
   * Return the object root table name
   * @return the root table name of the object
   */
  @Override
  public String getRootTableName() {
    return "Pdc";
  }

  /**
   * Return the object table name
   * @return the table name of the object
   */
  @Override
  public String getTableName() {
    return "";
  }

  /**
   * Check if an another object is equal to this object
   * @return true if other is equals to this object
   * @param other the object to compare to this NodePK
   */
  @Override
  public boolean equals(Object other) {
    if (!(other instanceof ValuePK))
      return false;

    return (id.equals(((ValuePK) other).getId()))
        && (space.equals(((ValuePK) other).getSpace()))
        && (componentName.equals(((ValuePK) other).getComponentName()));
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