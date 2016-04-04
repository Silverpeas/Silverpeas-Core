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

package org.silverpeas.core.importexport.versioning;

import java.io.Serializable;

import org.silverpeas.core.WAPrimaryKey;

/**
 * This object contains the info about PrimaryKey of document
 * @author Georgy Shakirin
 * @version 1.0
 */

public class DocumentPK extends WAPrimaryKey implements Serializable {
  private static final long serialVersionUID = -93533696421871014L;

  /**
   * Constructor declaration
   * @param id
   * @see
   */
  public DocumentPK(int id) {
    super(String.valueOf(id));
  }

  /**
   * Constructor declaration
   * @param id
   * @param spaceId
   * @param componentId
   * @see
   */
  public DocumentPK(int id, String spaceId, String componentId) {
    super(String.valueOf(id), spaceId, componentId);
  }

  /**
   * Constructor declaration
   * @param id
   * @param componentId
   * @see
   */
  public DocumentPK(int id, String componentId) {
    super(String.valueOf(id), componentId);
  }

  /**
   * Constructor declaration
   * @param id
   * @param pk
   * @see
   */
  public DocumentPK(int id, WAPrimaryKey pk) {
    super(String.valueOf(id), pk);
  }

  @Override
  public String getRootTableName() {
    return "Version";
  }

  @Override
  public String getTableName() {
    return "SB_Version_Document";
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder(200);
    builder.append("(id = ").append(getId()).append(", space = ").append(getSpace());
    builder.append(", componentName = ").append(getComponentName()).append(")");
    return builder.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || !(o instanceof DocumentPK)) {
      return false;
    }
    DocumentPK that = (DocumentPK) o;
    if (componentName != null ? !componentName.equals(that.componentName) :
        that.componentName != null) {
      return false;
    }
    if (id != null ? !id.equals(that.id) : that.id != null) {
      return false;
    }
    if (space != null ? !space.equals(that.space) : that.space != null) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int result = id != null ? id.hashCode() : 0;
    result = 31 * result + (space != null ? space.hashCode() : 0);
    result = 31 * result + (componentName != null ? componentName.hashCode() : 0);
    return result;
  }

}