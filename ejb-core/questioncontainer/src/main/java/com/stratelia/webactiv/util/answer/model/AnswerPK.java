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
//Source file: d:\\webactiv\\util\\com\\stratelia\\webactiv\\util\\answer\\model\\AnswerPK.java

package com.stratelia.webactiv.util.answer.model;

import com.stratelia.webactiv.util.*;
import java.io.Serializable;

/**
 * It's the Publication PrimaryKey object It identify a Publication
 * @author Nicolas Eysseric
 * @version 1.0
 */
public class AnswerPK extends WAPrimaryKey implements Serializable {

  /**
   * Constructor which set only the id
   * @since 1.0
   * @roseuid 3AB7338B0329
   */
  public AnswerPK(String id) {
    super(id);
  }

  /**
   * Constructor which set the id The WAPrimaryKey provides space and component name
   * @since 1.0
   * @roseuid 3AB7338B0334
   */
  public AnswerPK(String id, String spaceId, String componentId) {
    super(id, spaceId, componentId);
  }

  /**
   * Constructor which set the id The WAPrimaryKey provides space and component name
   * @since 1.0
   * @roseuid 3AB7338B0348
   */
  public AnswerPK(String id, WAPrimaryKey pk) {
    super(id, pk);
  }

  /**
   * Return the object root table name
   * @return the root table name of the object
   * @since 1.0
   * @roseuid 3AB7338B035B
   */
  public String getRootTableName() {
    return "Answer";
  }

  /**
   * Return the object table name
   * @return the table name of the object
   * @since 1.0
   * @roseuid 3AB7338B035B
   */
  public String getTableName() {
    return "SB_Question_Answer";
  }

  /**
   * Check if an another object is equal to this object
   * @return true if other is equals to this object
   * @param other the object to compare to this PollPK
   * @since 1.0
   * @roseuid 3AB7338B035C
   */
  public boolean equals(Object other) {
    if (!(other instanceof AnswerPK))
      return false;
    return (id.equals(((AnswerPK) other).getId()))
        && (space.equals(((AnswerPK) other).getSpace()))
        && (componentName.equals(((AnswerPK) other).getComponentName()));
  }

  /**
   * Returns a hash code for the key
   * @return A hash code for this object
   * @roseuid 3AB7338B0365
   */
  public int hashCode() {
    return toString().hashCode();
  }
}
