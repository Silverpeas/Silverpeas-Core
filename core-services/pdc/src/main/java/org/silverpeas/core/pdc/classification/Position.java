/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.pdc.classification;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.NONE)
public class Position<T extends Value> implements java.io.Serializable {
  private static final long serialVersionUID = -6444526234909683822L;
  private int nPositionId = -1;
  private List<T> alValues = null; // List of Value

  // Constructor
  public Position() {
  }

  public Position(int nGivenPositionId, List<T> alGivenValues) {
    nPositionId = nGivenPositionId;
    alValues = alGivenValues;
  }

  public Position(List<T> alGivenValues) {
    alValues = alGivenValues;
  }

  public void setPositionId(int nGivenPositionId) {
    nPositionId = nGivenPositionId;
  }

  public int getPositionId() {
    return nPositionId;
  }

  public void setValues(List<T> alGivenValues) {
    alValues = alGivenValues;
  }

  public List<T> getValues() {
    return alValues;
  }

  public void addValue(T value) {
    if (alValues == null) {
      alValues = new ArrayList<>();
    }
    alValues.add(value);
  }

  public Value getValueByAxis(int nUsedAxisId) {
    List<T> values = getValues();
    for (T value : values) {
      // compare nUsedAxisId avec l'axisId de l'objet Value
      if (nUsedAxisId == value.getAxisId()) {
        return value;
      }
    }

    return new Value();
  }

  public void checkPosition() throws ClassifyEngineException {
    // Check the array of Values
    if (this.getValues() == null) {
      throw new ClassifyEngineException("Null position");
    }

    // check that there is at least one value
    if (this.getValues().isEmpty()) {
      throw new ClassifyEngineException("Empty position");
    }

    // Check each value
    for (int nI = 0; nI < this.getValues().size(); nI++) {
      this.getValues().get(nI).checkValue();
    }
  }

}