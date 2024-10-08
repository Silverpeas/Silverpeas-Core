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

import org.silverpeas.core.pdc.tree.model.TreeNode;

import java.util.List;
import java.util.Objects;

public class Value extends TreeNode implements java.io.Serializable {

  private static final long serialVersionUID = 2248040737072584720L;

  /**
   * The primary key of the object Value
   */
  ValuePK pk = null;

  private int nbObjects = 0;
  private List<Value> pathValues = null;
  private String axisId = "unknown";
  private String fullPath = null;

  //
  // constructor
  //


  public Value() {
    super();
  }

  public Value(String id, String treeId) {
    super(id, treeId);
    setValuePK(new ValuePK(id));
  }

  public Value(TreeNode treeNode) {
    super(treeNode.getPK().getId(), treeNode.getTreeId());
    setName(treeNode.getName());
    setDescription(treeNode.getDescription());
    setCreationDate(treeNode.getCreationDate());
    setCreatorId(treeNode.getCreatorId());
    setPath(treeNode.getPath());
    setLevelNumber(treeNode.getLevelNumber());
    setOrderNumber(treeNode.getOrderNumber());
    setFatherId(treeNode.getFatherId());
    setValuePK(new ValuePK(treeNode.getPK().getId()));
  }

  public final void setValuePK(ValuePK pk) {
    this.pk = pk;
  }

  public ValuePK getValuePK() {
    return this.pk;
  }

  /**
   * Returns the id of the mother value
   * @return an id
   */
  public String getMotherId() {
    return getFatherId();
  }

  public String getFullPath() {
    if (fullPath == null) {
      fullPath = getPath() + getValuePK().getId() + "/";
    }
    return fullPath;
  }

  public int getNbObjects() {
    return nbObjects;
  }

  public void setNbObjects(int nbObjects) {
    this.nbObjects = nbObjects;
  }

  @SuppressWarnings("unused")
  public void setPathValues(List<Value> pathValues) {
    this.pathValues = pathValues;
  }

  @SuppressWarnings("unused")
  public List<Value> getPathValues() {
    return this.pathValues;
  }

  public void setAxisId(int axisId) {
    this.axisId = Integer.toString(axisId);
  }

  public String getAxisId() {
    return this.axisId;
  }

  @Override
  public String toString() {
    return "(pk = " + getValuePK().toString() + ")";
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Value other = (Value) obj;
    if (!Objects.equals(this.axisId, other.axisId)) {
      return false;
    }
    return getFullPath() == null ? other.getFullPath() == null :
        getFullPath().equals(other.getFullPath());
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 97 * hash + (this.axisId != null ? this.axisId.hashCode() : 0);
    hash = 97 * hash + (this.fullPath != null ? this.fullPath.hashCode() : 0);
    return hash;
  }

}