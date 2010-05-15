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
 * FLOSS exception.  You should have received a copy of the text describing
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

package com.stratelia.silverpeas.pdc.model;

import java.util.List;

import com.stratelia.silverpeas.treeManager.model.TreeNode;

public class Value extends TreeNode implements java.io.Serializable {

  /**
   * The primary key of the object Value
   */
  ValuePK pk = null;

  private int nbObjects = 0;
  private List pathValues = null;
  private String axisId = "unknown";
  private String fullPath = null;

  //
  // constructor
  //

  public Value() {
    super();
  }

  public Value(String id, String treeId, String name, String description,
      String creationDate, String creatorId, String path, int level, int order,
      String fatherId) {
    super(id, treeId, name, description, creationDate, creatorId, path, level,
        order, fatherId);
    setValuePK(new ValuePK(id));
  }

  public Value(String id, String treeId, String name, String creationDate,
      String creatorId, String path, int level, int order, String fatherId) {
    super(id, treeId, name, null, creationDate, creatorId, path, level, order,
        fatherId);
    setValuePK(new ValuePK(id));
  }

  //
  // public methods
  //

  public void setValuePK(ValuePK pk) {
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
    if (fullPath == null)
      fullPath = getPath() + getValuePK().getId() + "/";
    return fullPath;
  }

  public int getNbObjects() {
    return nbObjects;
  }

  public void setNbObjects(int nbObjects) {
    this.nbObjects = nbObjects;
  }

  public void setPathValues(List pathValues) {
    this.pathValues = pathValues;
  }

  public List getPathValues() {
    return this.pathValues;
  }

  public void setAxisId(int axisId) {
    this.axisId = new Integer(axisId).toString();
  }

  public String getAxisId() {
    return this.axisId;
  }

  public String getStringFullPath(String delimitor) {
    String fullPath = "";
    Value value = null;
    for (int i = 0; i < pathValues.size(); i++) {
      value = (Value) pathValues.get(i);
      if (i != 0)
        fullPath += delimitor;
      fullPath += value.getName();
    }
    return fullPath;
  }

  public String toString() {
    return "(pk = " + getValuePK().toString() + ")";
  }

}