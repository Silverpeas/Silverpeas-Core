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
package com.silverpeas.notation.model;

import java.io.Serializable;

import com.stratelia.webactiv.util.WAPrimaryKey;

/**
 * Primary key of a notation.
 */
public class NotationPK extends WAPrimaryKey implements Serializable {

  private static final long serialVersionUID = -635994271608460859L;
  private int type;
  private String userId;

  public NotationPK(String id) {
    super(id);
    this.type = Notation.TYPE_UNDEFINED;
  }

  public NotationPK(String id, String componentId, int type) {
    super(id, componentId);
    this.type = type;
  }

  public NotationPK(String id, String componentId, int type, String userId) {
    this(id, componentId, type);
    this.userId = userId;
  }

  public int getType() {
    return type;
  }

  public String getUserId() {
    return userId;
  }

  /**
   * Comparison between two notation primary key. Since various attributes of the both elements can
   * be null, using toString() method to compare the elements avoids to check null cases for each
   * attribute.
   */
  public boolean equals(Object other) {
    return ((other instanceof NotationPK) && (toString()
        .equals(((NotationPK) other).toString())));
  }

  public String toString() {
    return new StringBuffer().append("(id = ").append(getId()).append(
        ", space = ").append(getSpace()).append(", componentName = ").append(
        getComponentName()).append(", type = ").append(getType()).append(
        ", userId = ").append(getUserId()).append(")").toString();
  }

}