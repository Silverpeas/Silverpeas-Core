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

package org.silverpeas.core.pdc.tree.model;

import java.io.Serializable;
import org.silverpeas.core.WAPrimaryKey;

/**
 * Class declaration
 * @author
 */
public class TreeNodePK extends WAPrimaryKey implements Serializable {

  private static final long serialVersionUID = -2135967099552497544L;

  /**
   * Constructor declaration
   * @param id
   * @see
   */
  public TreeNodePK(String id) {
    super(id);
  }

  /**
   * Constructor declaration
   * @param id
   * @param space
   * @param componentName
   * @see
   */
  public TreeNodePK(String id, String space, String componentName) {
    super(id, space, componentName);
  }

  /**
   * Constructor declaration
   * @param id
   * @param pk
   * @see
   */
  public TreeNodePK(String id, WAPrimaryKey pk) {
    super(id, pk);
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getRootTableName() {
    return "Tree";
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getTableName() {
    return "SB_Tree_Tree";
  }

  /**
   * Method declaration
   * @param other
   * @return
   * @see
   */
  public boolean equals(Object other) {
    if (!(other instanceof TreeNodePK)) {
      return false;
    }
    return (id.equals(((TreeNodePK) other).getId()))
        && (space.equals(((TreeNodePK) other).getSpace()))
        && (componentName.equals(((TreeNodePK) other).getComponentName()));
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
