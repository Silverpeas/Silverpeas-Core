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

package org.silverpeas.web.pdc.control;

import java.util.ArrayList;
import java.util.List;

import org.silverpeas.core.pdc.form.displayers.PdcFieldDisplayer;
import org.silverpeas.core.pdc.pdc.model.ClassifyPosition;
import org.silverpeas.core.pdc.pdc.model.ClassifyValue;
import org.silverpeas.core.pdc.pdc.model.UsedAxis;

/**
 * Manages the positions of a PDC field.
 * @author ahedin
 */
public class PdcFieldPositionsManager {

  // If true, indicates that the controller which refers to this manager is in PDC field mode.
  private boolean enabled;
  // Positions of the field.
  private ArrayList<ClassifyPosition> positions;
  // Available axis to order the object (publication) containing the field.
  private ArrayList<UsedAxis> usedAxisList;
  // Field name.
  private String fieldName;
  // PDC field displayer.
  private PdcFieldDisplayer displayer;

  /**
   * Constructor
   */
  protected PdcFieldPositionsManager() {
    reset();
  }

  public boolean isEnabled() {
    return enabled;
  }

  public String getFieldName() {
    return fieldName;
  }

  public ArrayList<ClassifyPosition> getPositions() {
    return positions;
  }

  public ArrayList<UsedAxis> getUsedAxisList() {
    return usedAxisList;
  }

  /**
   * Initializes and enables the manager.
   * @param fieldName The field name.
   * @param pattern The description of positions, following the pattern :
   * axisId1_1,valueId1_1;axisId1_2,valueId1_2.axisId2_1,valueId2_1... where axisIdi_j and
   * valueIdi_j correspond to the value #j of the position #i.
   * @param axis
   */
  public void init(String fieldName, String pattern, String axis) {
    enabled = true;
    this.fieldName = fieldName;
    displayer = new PdcFieldDisplayer();
    positions = displayer.getPositions(pattern);
    usedAxisList = displayer.getUsedAxisList(axis);
  }

  /**
   * Resets and disables the manager.
   */
  public void reset() {
    enabled = false;
    fieldName = null;
    displayer = null;
    positions = null;
    usedAxisList = null;
  }

  /**
   * Add the position to the positions list.
   * @param position The new position to add.
   */
  public void addPosition(ClassifyPosition position) {
    position.setPositionId(positions.size());
    positions.add(position);
    refreshPositions();
  }

  /**
   * Update the position.
   * @param position The position to update.
   * @return the status of the update.
   */
  public int updatePosition(ClassifyPosition position) {
    ClassifyPosition currentPosition;
    int i = 0;
    boolean positionFound = false;
    while (i < positions.size() && !positionFound) {
      currentPosition = positions.get(i);
      if (currentPosition.getPositionId() == position.getPositionId()) {
        positions.set(i, position);
        positionFound = true;
      }
      i++;
    }
    refreshPositions();
    return -1;
  }

  /**
   * Deletes the position which id corresponds to the one given as parameter.
   * @param positionId The id of the position to delete.
   */
  public void deletePosition(int positionId) {
    ClassifyPosition position;
    boolean axisRemoved = false;
    int i = 0;
    while (i < positions.size() && !axisRemoved) {
      position = positions.get(i);
      if (position.getPositionId() == positionId) {
        positions.remove(i);
        axisRemoved = true;
      } else {
        i++;
      }
    }
    if (axisRemoved) {
      while (i < positions.size()) {
        positions.get(i).setPositionId(i);
        i++;
      }
    }
    refreshPositions();
  }

  /**
   * Calls the PDC field displayer to update and complete the description of positions.
   */
  private void refreshPositions() {
    positions = displayer.getPositions(getPositionsToString());
  }

  /**
   * @return A pattern describing the positions :
   * axisId1_1,valueId1_1;axisId1_2,valueId1_2.axisId2_1,valueId2_1... where axisIdi_j and
   * valueIdi_j correspond to the value #j of the position #i.
   */
  public String getPositionsToString() {
    StringBuffer result = new StringBuffer();
    if (positions != null) {
      ClassifyPosition position;
      ClassifyValue classifyValue;
      String[] values;
      String valuesPath;
      for (int i = 0; i < positions.size(); i++) {
        if (i > 0) {
          result.append(".");
        }
        position = positions.get(i);
        List<ClassifyValue> classifyValues = position.getValues();
        for (int j = 0; j < classifyValues.size(); j++) {
          if (j > 0) {
            result.append(";");
          }
          classifyValue = classifyValues.get(j);
          result.append(classifyValue.getAxisId()).append(",");

          valuesPath = classifyValue.getValue();
          if (valuesPath != null && valuesPath.length() > 0) {
            values = valuesPath.split("/");
            result.append(values[values.length - 1]);
          }
        }
      }
    }
    return result.toString();
  }

}
