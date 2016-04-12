/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.comment.model;

import java.io.Serializable;

import org.silverpeas.core.WAPrimaryKey;

import org.apache.commons.lang3.ObjectUtils;

/**
 * This object contains the info about PrimaryKey of document
 *
 * @author Georgy Shakirin
 * @version 1.0
 */
public class CommentPK extends WAPrimaryKey implements Serializable {

  private static final long serialVersionUID = 3246647638847423692L;

  /**
   * Constructor declaration
   *
   * @param id
   * @see
   */
  public CommentPK(String id) {
    super(id);
  }

  /**
   * Constructs a new comment primary key from the specified identifier and Silverpeas component
   * identifier.
   *
   * @param id the identifier of the comment.
   * @param componentId the identifier of the component to which the comment belongs.
   */
  public CommentPK(String id, String componentId) {
    super(id, componentId);
  }

  /**
   * Constructor declaration
   *
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
   *
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
  @Override
  public String getRootTableName() {
    return "comment";
  }

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  @Override
  public String getTableName() {
    return "sb_comment_comment";
  }

  /**
   * Method declaration
   *
   * @param other
   * @return
   * @see
   */
  @Override
  public boolean equals(Object other) {
    if (!(other instanceof CommentPK)) {
      return false;
    }
    CommentPK otherPk = (CommentPK) other;
    return ObjectUtils.equals(id, otherPk.getId()) && ObjectUtils.equals(space, otherPk.getSpace())
        && ObjectUtils.equals(componentName, otherPk.getComponentName());
  }

  /**
   * Method declaration
   *
   * @return
   * @see
   */
  @Override
  public String toString() {
    return "(id = " + getId() + ", space = " + getSpace() + ", componentName = "
        + getComponentName() + ")";
  }

  /**
   * Returns a hash code for the key
   *
   * @return A hash code for this object
   */
  @Override
  public int hashCode() {
    return toString().hashCode();
  }

}
