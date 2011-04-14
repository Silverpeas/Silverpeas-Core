/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
package com.stratelia.webactiv.util.score.model;

import java.io.Serializable;

import com.stratelia.webactiv.util.WAPrimaryKey;

/**
 * Class declaration
 * @author
 */
public class ScorePK extends WAPrimaryKey implements Serializable {

  private static final long serialVersionUID = 2904895551791659598L;

  /**
   * Constructor which set only the id
   * @since 1.0
   * @roseuid 3AB7343503E1
   */
  public ScorePK(String id) {
    super(id);
  }

  /**
   * Constructor which set the id The WAPrimaryKey provides space and component name
   * @since 1.0
   * @roseuid 3AB734360003
   */
  public ScorePK(String id, String spaceId, String componentId) {
    super(id, spaceId, componentId);
  }

  /**
   * Constructor which set the id The WAPrimaryKey provides space and component name
   * @since 1.0
   * @roseuid 3AB734360018
   */
  public ScorePK(String id, WAPrimaryKey pk) {
    super(id, pk);
  }

  /**
   * Return the object root table name
   * @return the root table name of the object
   * @since 1.0
   * @roseuid 3AB73436002B
   */
  public String getRootTableName() {
    return "Score";
  }

  /**
   * Return the object table name
   * @return the table name of the object
   * @since 1.0
   * @roseuid 3AB73436002B
   */
  public String getTableName() {
    return "SB_Question_Score";
  }

  /**
   * Check if an another object is equal to this object
   * @return true if other is equals to this object
   * @param other the object to compare to this PollPK
   * @since 1.0
   * @roseuid 3AB73436002C
   */
  public boolean equals(Object other) {
    if (!(other instanceof ScorePK)) {
      return false;
    }
    return (id.equals(((ScorePK) other).getId()))
        && (space.equals(((ScorePK) other).getSpace()))
        && (componentName.equals(((ScorePK) other).getComponentName()));
  }

  /**
   * Returns a hash code for the key
   * @return A hash code for this object
   * @roseuid 3AB734360036
   */
  public int hashCode() {
    return toString().hashCode();
  }

}
