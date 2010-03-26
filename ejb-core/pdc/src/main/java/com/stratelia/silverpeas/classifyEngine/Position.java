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
package com.stratelia.silverpeas.classifyEngine;

import java.util.*;

import com.stratelia.webactiv.util.exception.*;

public class Position extends Object implements java.io.Serializable {
  private int nPositionId = -1;
  private List alValues = null; // List of Value

  // Constructor
  public Position() {
  }

  public Position(int nGivenPositionId, List alGivenValues) {
    nPositionId = nGivenPositionId;
    alValues = alGivenValues;
  }

  public Position(List alGivenValues) {
    alValues = alGivenValues;
  }

  public void setPositionId(int nGivenPositionId) {
    nPositionId = nGivenPositionId;
  }

  public int getPositionId() {
    return nPositionId;
  }

  public void setValues(List alGivenValues) {
    alValues = alGivenValues;
  }

  public List getValues() {
    return alValues;
  }

  public void addValue(Value value) {
    if (alValues == null)
      alValues = new ArrayList();
    alValues.add(value);
  }

  public Value getValueByAxis(int nUsedAxisId) {
    List values = getValues();
    // liste toutes les valeurs
    // et cherche
    Value value = new Value();
    for (int i = 0; i < values.size(); i++) {
      value = (Value) values.get(i);
      // compare nUsedAxisId avec l'axisId de l'objet Value
      if (nUsedAxisId == value.getAxisId()) {
        break;
      }
    }

    return value;
  }

  public void checkPosition() throws ClassifyEngineException {
    // Check the array of Values
    if (this.getValues() == null)
      throw new ClassifyEngineException("Position.checkPosition",
          SilverpeasException.ERROR, "classifyEngine.EX_NULL_VALUE_POSITION");

    // check that there is at least one value
    if (this.getValues().size() == 0)
      throw new ClassifyEngineException("Position.checkPosition",
          SilverpeasException.ERROR, "classifyEngine.EX_EMPTY_VALUE_POSITION");

    // Check each value
    for (int nI = 0; nI < this.getValues().size(); nI++)
      ((Value) this.getValues().get(nI)).checkValue();
  }

}