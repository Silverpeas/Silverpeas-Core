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

public class Value implements java.io.Serializable {

  private static final long serialVersionUID = 6903903413201603630L;
  private int nAxisId = -1;
  private int physicalAxisId = -1;
  private String sValue = null;

  // Constructor
  public Value(int nGivenAxisId, String sGivenValue) {
    nAxisId = nGivenAxisId;
    sValue = sGivenValue;
  }

  public Value() {
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

  public void checkValue() throws ClassifyEngineException {
    // Check the axisId
    if (this.getAxisId() < 0)
      throw new ClassifyEngineException("Value.checkValue",
          SilverpeasException.ERROR, "classifyEngine.EX_INCORRECT_AXISID_VALUE");
  }

  public void setPhysicalAxisId(int id) {
    physicalAxisId = id;
  }

  public int getPhysicalAxisId() {
    return physicalAxisId;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Value other = (Value) obj;
    if (this.nAxisId != other.nAxisId) {
      return false;
    }
    if ((this.sValue == null) ? (other.sValue != null) : !this.sValue.equals(other.sValue)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 97 * hash + this.nAxisId;
    hash = 97 * hash + (this.sValue != null ? this.sValue.hashCode() : 0);
    return hash;
  }


}