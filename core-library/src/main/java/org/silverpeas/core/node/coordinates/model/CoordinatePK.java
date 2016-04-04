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

/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent)
 ---*/

package org.silverpeas.core.node.coordinates.model;

import org.silverpeas.core.WAPrimaryKey;

import java.io.Serializable;

/**
 * @author Nicolas Eysseric
 * @version 1.0
 */
public class CoordinatePK extends WAPrimaryKey implements Serializable {

  /**
   * Constructor declaration
   * @param id
   * @see
   */
  public CoordinatePK(String id) {
    super(id);
  }

  /**
   * Constructor declaration
   * @param id
   * @param spaceId
   * @param componentId
   * @see
   */
  public CoordinatePK(String id, String spaceId, String componentId) {
    super(id, spaceId, componentId);
  }

  /**
   * Constructor declaration
   * @param id
   * @param pk
   * @see
   */
  public CoordinatePK(String id, WAPrimaryKey pk) {
    super(id, pk);
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getRootTableName() {
    return "Coordinates";
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getTableName() {
    return "SB_Coordinates_Coordinates";
  }

  /**
   * Method declaration
   * @param other
   * @return
   * @see
   */
  public boolean equals(Object other) {
    if (!(other instanceof CoordinatePK)) {
      return false;
    }
    return (id.equals(((CoordinatePK) other).getId()))
        && (space.equals(((CoordinatePK) other).getSpace()))
        && (componentName.equals(((CoordinatePK) other).getComponentName()));
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public int hashCode() {
    return toString().hashCode();
  }

}
