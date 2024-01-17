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
package org.silverpeas.core.questioncontainer.result.model;

import java.io.Serializable;

import org.silverpeas.core.WAPrimaryKey;

/**
 * It's the Publication PrimaryKey object It identify a Publication
 * @author Nicolas Eysseric
 */
public class QuestionResultPK extends WAPrimaryKey implements Serializable {

  private static final long serialVersionUID = -5577542804758389325L;

  /**
   * Constructor which set only the id
   */
  public QuestionResultPK(String id) {
    super(id);
  }

  /**
   * Constructor which set the id The WAPrimaryKey provides space and component name
   */
  public QuestionResultPK(String id, String space, String componentName) {
    super(id, space, componentName);
  }

  /**
   * Constructor which set the id The WAPrimaryKey provides space and component name
   */
  public QuestionResultPK(String id, WAPrimaryKey pk) {
    super(id, pk);
  }

  /**
   * Return the object root table name
   * @return the root table name of the object
   */
  @Override
  public String getRootTableName() {
    return "QuestionResult";
  }

  /**
   * Return the object root table name
   * @return the root table name of the object
   */
  @Override
  public String getTableName() {
    return "SB_Question_QuestionResult";
  }

  /**
   * Check if an another object is equal to this object
   * @param other the object to compare to this PollPK
   * @return true if other is equals to this object
   */
  @Override
  public boolean equals(Object other) {
    if (!(other instanceof QuestionResultPK)) {
      return false;
    }
    return (id.equals(((QuestionResultPK) other).getId())) &&
        (space.equals(((QuestionResultPK) other).getSpace())) &&
        (componentName.equals(((QuestionResultPK) other).getComponentName()));
  }

  /**
   * Returns a hash code for the key
   * @return A hash code for this object
   */
  @Override
  public int hashCode() {
    return toString().hashCode();
  }
}