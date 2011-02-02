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

package com.stratelia.silverpeas.pdc.model;

import com.stratelia.silverpeas.classifyEngine.Position;
import java.util.List;

import com.stratelia.silverpeas.containerManager.ContainerPositionInterface;

/**
 * @author Nicolas EYSSERIC
 */
public class ClassifyPosition extends Position<ClassifyValue> implements
    ContainerPositionInterface, java.io.Serializable {
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
   * @return 
   */
  public boolean isEmpty() {
    return (getPositionId() == -1 || getValues() == null);
  }

  @Override
  public String toString() {
    return "ClassifyPosition object :[ positionId=" + getPositionId() + ", "
        + " value=" + getValues();
  }

  /**
   * Méthodes nécéssaire pour le mapping castor du module importExport.
   * @return
   */
  @SuppressWarnings("unchecked")
  public List<ClassifyValue> getListClassifyValue() {
    return super.getValues();
  }

  /**
   * Méthodes nécéssaire pour le mapping castor du module importExport.
   * @param values
   */
  public void setListClassifyValue(List<ClassifyValue> values) {
    super.setValues(values);
  }

}