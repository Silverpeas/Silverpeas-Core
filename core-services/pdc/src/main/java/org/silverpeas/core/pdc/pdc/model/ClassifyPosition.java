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
package org.silverpeas.core.pdc.pdc.model;

import org.silverpeas.core.pdc.classification.Position;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Arrays;
import java.util.List;

/**
 * @author Nicolas EYSSERIC
 */
@XmlRootElement(name = "pdcPosition")
@XmlAccessorType(XmlAccessType.NONE)
public class ClassifyPosition extends Position<ClassifyValue> {
  private static final long serialVersionUID = 6588855414301219379L;

  public ClassifyPosition() {
  }

  public ClassifyPosition(List<ClassifyValue> values) {
    super(values);
  }

  public ClassifyPosition(int nPositionId, List<ClassifyValue> values) {
    super(nPositionId, values);
  }

  public String getValueOnAxis(int axisId) {
    List<ClassifyValue> values = getValues();
    for (ClassifyValue value : values) {
      if (value.getAxisId() == axisId) {
        return value.getValue();
      }
    }
    return null;
  }

  /**
   * Return true if the position is empty
   * @return true if empty, false otherwise.
   */
  public boolean isEmpty() {
    return (getPositionId() == -1 || getValues() == null);
  }

  @Override
  public String toString() {
    return "ClassifyPosition object :[ positionId=" + getPositionId() + ", "
        + " value=" + getValues();
  }

  @XmlElement(name = "axisPosition")
  public List<ClassifyValue> getListClassifyValue() {
    return super.getValues();
  }

  public void setListClassifyValue(List<ClassifyValue> values) {
    super.setValues(values);
  }


  @Override
  public boolean equals(final Object obj) {
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    ClassifyPosition other = (ClassifyPosition) obj;
    List<ClassifyValue> values = getValues();
    List<ClassifyValue> otherValues = other.getValues();
    if (values.size() != otherValues.size()) {
      return false;
    }
    for (int i = 0; i < values.size(); i++) {
      if (!values.get(i).equals(otherValues.get(i))) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    List<ClassifyValue> values = getValues();
    return Arrays.hashCode(values.toArray(new ClassifyValue[0]));
  }
}