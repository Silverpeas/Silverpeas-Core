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

package com.stratelia.silverpeas.classifyEngine;

import com.stratelia.webactiv.util.exception.SilverpeasException;

public class Criteria extends Object implements java.io.Serializable {

  private static final long serialVersionUID = 3824817745975131588L;
  private int nAxisId = -1;
  private String sValue = null;

  protected Criteria() {
    super();
  }

  // Constructor
  public Criteria(int nGivenAxisId, String sGivenValue) {
    nAxisId = nGivenAxisId;
    sValue = sGivenValue;
  }

  public void setAxisId(int nGivenAxisId) {
    nAxisId = nGivenAxisId;
  }

  public int getAxisId() {
    return nAxisId;
  }

  public void setValue(String sGivenValue) {
    sValue = sGivenValue;
  }

  public String getValue() {
    return sValue;
  }

  // Check that the given criteria is valid
  public void checkCriteria() throws ClassifyEngineException {
    // Check the axisId
    if (this.getAxisId() < 0)
      throw new ClassifyEngineException("Criteria.checkCriteria",
          SilverpeasException.ERROR,
          "classifyEngine.EX_INCORRECT_AXISID_CRITERIA");
  }

  public String toString() {
    String axisId = new Integer(getAxisId()).toString();
    return "Criteria Object : [ axisId=" + axisId + ", value=" + getValue()
        + " ]";
  }

}