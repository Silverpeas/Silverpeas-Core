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

package org.silverpeas.core.tagcloud.dao;

import java.io.Serializable;

import org.silverpeas.core.WAPrimaryKey;

/**
 * Primary key of a tagcloud.
 */
public class TagCloudPK extends WAPrimaryKey implements Serializable {

  private static final long serialVersionUID = 1238115183281503737L;
  private int type;

  public TagCloudPK(String id) {
    super(id);
  }

  public TagCloudPK(String id, String spaceId, String componentId) {
    super(id, spaceId, componentId);
  }

  public TagCloudPK(String id, String componentId) {
    super(id, componentId);
  }

  public TagCloudPK(String id, String componentId, int type) {
    super(id, componentId);
    this.type = type;
  }

  public TagCloudPK(String id, WAPrimaryKey pk) {
    super(id, pk);
  }

  public int getType() {
    return type;
  }

  public void setType(int type) {
    this.type = type;
  }

  public String getRootTableName() {
    return "TagCloud";
  }

  public String getTableName() {
    return "SB_TagCloud_TagCloud";
  }

  public boolean equals(Object other) {
    return ((other instanceof TagCloudPK)
        && (id.equals(((TagCloudPK) other).getId()))
        && (space.equals(((TagCloudPK) other).getSpace())) && (componentName
        .equals(((TagCloudPK) other).getComponentName())));
  }

  public String toString() {
    return new StringBuffer().append("(id = ").append(getId()).append(
        ", space = ").append(getSpace()).append(", componentName = ").append(
        getComponentName()).append(")").toString();
  }

  public int hashCode() {
    return toString().hashCode();
  }

}