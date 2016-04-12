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
package org.silverpeas.core.pdc.pdc.model;

import org.silverpeas.core.pdc.classification.Criteria;

/**
 * @author Nicolas EYSSERIC
 */
public class SearchCriteria extends Criteria implements java.io.Serializable, Cloneable {

  /**
   * Class version identifier
   */
  private static final long serialVersionUID = 6933474490149573303L;

  protected SearchCriteria() {
    super();
  }

  public SearchCriteria(int axisId, String value) {
    super(axisId, value);
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof SearchCriteria)) {
      return false;
    }
    SearchCriteria other = (SearchCriteria) obj;
    return getAxisId() == other.getAxisId() && getValue().equals(other.getValue());
  }

  @Override
  public String toString() {
    String axisId = String.valueOf(getAxisId());
    return "Search Criteria Object : [ axisId=" + axisId + ", value=" + getValue() + " ]";
  }

  /**
   * Support Cloneable Interface
   */
  @Override
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      return null; // this should never happened
    }
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 71 * hash + this.getAxisId();
    hash = 71 * hash + (this.getValue() != null ? this.getValue().hashCode() : 0);
    return hash;
  }
}
