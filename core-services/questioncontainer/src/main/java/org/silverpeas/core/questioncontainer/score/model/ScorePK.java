/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.questioncontainer.score.model;

import java.io.Serializable;

import org.silverpeas.core.WAPrimaryKey;

/**
 * ScorePK represents a score identifier
 */
public class ScorePK extends WAPrimaryKey implements Serializable {

  private static final long serialVersionUID = 2904895551791659598L;

  /**
   * Constructor which set only the id
   */
  public ScorePK(String id) {
    super(id);
  }

  /**
   * Constructor which set the id The WAPrimaryKey provides space and component name
   */
  public ScorePK(String id, String spaceId, String componentId) {
    super(id, spaceId, componentId);
  }

  /**
   * Constructor which set the id The WAPrimaryKey provides space and component name
   */
  public ScorePK(String id, WAPrimaryKey pk) {
    super(id, pk);
  }

  /**
   * Return the object root table name
   * @return the root table name of the object
   */
  public String getRootTableName() {
    return "Score";
  }

  /**
   * Return the object table name
   * @return the table name of the object
   */
  public String getTableName() {
    return "SB_Question_Score";
  }

  /**
   * Check if an another object is equal to this object
   * @param other the object to compare to this PollPK
   * @return true if other is equals to this object
   */
  public boolean equals(Object other) {
    if (!(other instanceof ScorePK)) {
      return false;
    }
    return (id.equals(((ScorePK) other).getId())) && (space.equals(((ScorePK) other).getSpace())) &&
        (componentName.equals(((ScorePK) other).getComponentName()));
  }

  /**
   * Returns a hash code for the key
   * @return A hash code for this object
   */
  public int hashCode() {
    return toString().hashCode();
  }

}
