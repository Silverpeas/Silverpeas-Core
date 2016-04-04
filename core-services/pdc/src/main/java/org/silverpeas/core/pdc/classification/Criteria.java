/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.pdc.classification;

import org.silverpeas.core.exception.SilverpeasException;

public class Criteria implements java.io.Serializable {

  private static final long serialVersionUID = 3824817745975131588L;
  private int axisId = -1;
  private String value;

  protected Criteria() {
    super();
    value = null;
  }

  public Criteria(int nGivenAxisId, String sGivenValue) {
    axisId = nGivenAxisId;
    value = sGivenValue;
  }

  public void setAxisId(int givenAxisId) {
    axisId = givenAxisId;
  }

  public int getAxisId() {
    return axisId;
  }

  public void setValue(String givenValue) {
    value = givenValue;
  }

  public String getValue() {
    return value;
  }

  // Check that the given criteria is valid
  public void checkCriteria() throws ClassifyEngineException {
    if (this.getAxisId() < 0) {
      throw new ClassifyEngineException("Criteria.checkCriteria",
          SilverpeasException.ERROR, "classifyEngine.EX_INCORRECT_AXISID_CRITERIA");
    }
  }

  public String toString() {
    return "Criteria Object : [ axisId=" + getAxisId() + ", value=" + getValue() + " ]";
  }

}