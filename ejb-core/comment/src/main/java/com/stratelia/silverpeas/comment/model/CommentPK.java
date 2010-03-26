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
package com.stratelia.silverpeas.comment.model;

import java.io.Serializable;
import com.stratelia.webactiv.util.WAPrimaryKey;

/**
 * This object contains the info about PrimaryKey of document
 * @author Georgy Shakirin
 * @version 1.0
 */

public class CommentPK extends WAPrimaryKey implements Serializable {

  /**
   * Constructor declaration
   * @param id
   * @see
   */
  public CommentPK(String id) {
    super(id);
  }

  /**
   * Constructor declaration
   * @param id
   * @param spaceId
   * @param componentId
   * @see
   */
  public CommentPK(String id, String spaceId, String componentId) {
    super(id, spaceId, componentId);
  }

  /**
   * Constructor declaration
   * @param id
   * @param pk
   * @see
   */
  public CommentPK(String id, WAPrimaryKey pk) {
    super(id, pk);
  }

  /**
   * **********
   */

  public String getRootTableName() {
    return "Comment";
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String getTableName() {
    return "SB_Comment_Comment";
  }

  /**
   * Method declaration
   * @param other
   * @return
   * @see
   */
  public boolean equals(Object other) {
    if (!(other instanceof CommentPK)) {
      return false;
    }
    return (id.equals(((CommentPK) other).getId()))
        && (space.equals(((CommentPK) other).getSpace()))
        && (componentName.equals(((CommentPK) other).getComponentName()));
  }

  /**
   * Method declaration
   * @return
   * @see
   */
  public String toString() {
    return "(id = " + getId() + ", space = " + getSpace()
        + ", componentName = " + getComponentName() + ")";
  }

  /**
   * Returns a hash code for the key
   * @return A hash code for this object
   */
  public int hashCode() {
    return toString().hashCode();
  }

}