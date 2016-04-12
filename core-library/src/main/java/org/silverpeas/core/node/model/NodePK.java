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

package org.silverpeas.core.node.model;

import org.silverpeas.core.WAPrimaryKey;

import java.io.Serializable;

/**
 * It's the Node PrimaryKey object It identify a Node
 * @author Nicolas Eysseric
 * @version 1.0
 */
public class NodePK extends WAPrimaryKey implements Serializable {

  private static final long serialVersionUID = 444396186497175804L;

  public static final String UNDEFINED_NODE_ID = "-1";
  public static final String ROOT_NODE_ID = "0";
  public static final String BIN_NODE_ID = "1";
  public static final String UNCLASSED_NODE_ID = "2";

  /**
   * Constructor which set only the id
   * @param id
   * @since 1.0
   */
  public NodePK(String id) {
    super(id);
  }

  /**
   * Constructor which set id, space and component name
   * @param id
   * @param space
   * @param componentName
   * @since 1.0
   */
  public NodePK(String id, String space, String componentName) {
    super(id, space, componentName);
  }

  public NodePK(String id, String componentId) {
    super(id, componentId);
  }

  /**
   * Constructor which set the id The WAPrimaryKey provides space and component name
   * @param id
   * @param pk
   * @since 1.0
   */
  public NodePK(String id, WAPrimaryKey pk) {
    super(id, pk);
  }

  public boolean isTrash() {
    return BIN_NODE_ID.equals(id);
  }

  public boolean isRoot() {
    return ROOT_NODE_ID.equals(id);
  }

  public boolean isUnclassed() {
    return UNCLASSED_NODE_ID.equals(id);
  }

  public boolean isUndefined() {
    return UNDEFINED_NODE_ID.equals(id);
  }

  /**
   * Return the object root table name
   * @return the root table name of the object
   * @since 1.0
   */
  @Override
  public String getRootTableName() {
    return "Node";
  }

  /**
   * Return the object table name
   * @return the table name of the object
   * @since 1.0
   */
  @Override
  public String getTableName() {
    return "SB_Node_Node";
  }

  /**
   * Check if an another object is equal to this object
   * @param obj the object to compare to this NodePK
   * @return true if other is equals to this object
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof NodePK)) {
      return false;
    }
    NodePK other = (NodePK) obj;
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!id.equals(other.id)) {
      return false;
    }
    if (componentName == null) {
      if (other.componentName != null) {
        return false;
      }
    } else if (!componentName.equals(other.componentName)) {
      return false;
    }
    return true;
  }

  /**
   * Returns a hash code for the key
   * @return A hash code for this object
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((id == null) ? 0 : id.hashCode());
    result = prime * result + ((componentName == null) ? 0 : componentName.hashCode());
    return result;
  }
}
