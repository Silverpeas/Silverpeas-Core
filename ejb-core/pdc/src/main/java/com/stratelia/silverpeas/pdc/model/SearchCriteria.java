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
package com.stratelia.silverpeas.pdc.model;

import com.stratelia.silverpeas.classifyEngine.Criteria;

/**
 * @author Nicolas EYSSERIC
 */
public class SearchCriteria extends Criteria implements java.io.Serializable {

  protected SearchCriteria() {
    super();
  }

  public SearchCriteria(int axisId, String value) {
    super(axisId, value);
  }

  public boolean equals(Object other) {
    if (!(other instanceof SearchCriteria))
      return false;
    return (getAxisId() == ((SearchCriteria) other).getAxisId())
        && (getValue().equals(((SearchCriteria) other).getValue()));
  }

  public String toString() {
    String axisId = new Integer(getAxisId()).toString();
    return "Search Criteria Object : [ axisId=" + axisId + ", value="
        + getValue() + " ]";
  }

  /**
   * Support Cloneable Interface
   */
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      return null; // this should never happened
    }
  }

}