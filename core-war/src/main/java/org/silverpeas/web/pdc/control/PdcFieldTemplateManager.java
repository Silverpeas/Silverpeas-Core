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
import java.util.StringTokenizer;

import org.silverpeas.core.contribution.content.form.record.GenericFieldTemplate;
import org.silverpeas.core.contribution.content.form.record.Parameter;
import org.silverpeas.core.contribution.content.form.record.ParameterValue;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.pdc.pdc.model.UsedAxis;
import org.silverpeas.core.pdc.pdc.model.UsedAxisPK;

/**
 * Manages the axis and values from PDC to define a PDC field.
 * @author ahedin
 */
public class PdcFieldTemplateManager {

  // If true, indicates that the controller which refers to this manager is in PDC field mode.
  private boolean enabled;
  // The current action on the PDC field : creation or update.
  private String actionForm;
  // The template describing the PDC field.
  private GenericFieldTemplate fieldTemplate;
  // The axis used to define the PDC field.
  private ArrayList<UsedAxis> usedAxisList;

  protected PdcFieldTemplateManager() {
    reset();
  }

  public boolean isEnabled() {
    return enabled;
  }

  public String getActionForm() {
    return actionForm;
  }

  public GenericFieldTemplate getFieldTemplate() {
    return fieldTemplate;
  }

  public ArrayList<UsedAxis> getUsedAxisList() {
    return usedAxisList;
  }

  /**
   * Initializes and enables the manager.
   * @param fieldTemplate The field template.
   * @param actionForm The current action on the field (creation or update).
   */
  public void init(GenericFieldTemplate fieldTemplate, String actionForm) {
    enabled = true;
    this.actionForm = actionForm;
    this.fieldTemplate = fieldTemplate;
    loadUsedAxisList();
  }

  /**
   * Resets and disables the manager.
   */
  public void reset() {
    enabled = false;
    actionForm = null;
    fieldTemplate = null;
    usedAxisList = null;
  }

  /**
   * @return A field template updated with the used axis data.
   */
  public GenericFieldTemplate getUpdatedFieldTemplate() {
    Parameter parameter = new Parameter("pdcAxis", "dummy");
    parameter.getParameterValuesObj().add(new ParameterValue("fr", getUsedAxisListToString()));
    fieldTemplate.getParametersObj().add(parameter);
    return fieldTemplate;
  }

  /**
   * @param usedAxisId The searched axis' id.
   * @return The axis corresponding to the id given as parameter.
   */
  public UsedAxis getUsedAxis(String usedAxisId) {
    UsedAxis usedAxis;
    for (int i = 0; i < usedAxisList.size(); i++) {
      usedAxis = usedAxisList.get(i);
      if (usedAxis.getPK().getId().equals(usedAxisId)) {
        return usedAxis;
      }
    }
    return null;
  }

  /**
   * Adds the axis given as parameter to the used axis list.
   * @param usedAxis The axis to add to the list.
   */
  public void addUsedAxis(UsedAxis usedAxis) {
    usedAxis.setPK(new UsedAxisPK(usedAxisList.size()));
    usedAxisList.add(usedAxis);
  }

  /**
   * Updates the axis given as parameter into the used axis list.
   * @param usedAxis The axis to update into the list.
   */
  public void updateUsedAxis(UsedAxis usedAxis) {
    UsedAxis currentUsedAxis;
    int i = 0;
    boolean axisFound = false;
    while (i < usedAxisList.size() && !axisFound) {
      currentUsedAxis = usedAxisList.get(i);
      if (currentUsedAxis.getPK().getId().equals(usedAxis.getPK().getId())) {
        usedAxis.setAxisId(currentUsedAxis.getAxisId());
        usedAxisList.set(i, usedAxis);
        axisFound = true;
      }
      i++;
    }
  }

  /**
   * Delete the axis correponding to the id given as parameter from the used axis list.
   * @param usedAxisId The is of the axis to delete.
   */
  public void deleteUsedAxis(String usedAxisId) {
    UsedAxis usedAxis;
    boolean axisRemoved = false;
    int i = 0;
    while (i < usedAxisList.size() && !axisRemoved) {
      usedAxis = usedAxisList.get(i);
      if (usedAxis.getPK().getId().equals(usedAxisId)) {
        usedAxisList.remove(i);
        axisRemoved = true;
      } else {
        i++;
      }
    }
  }

  /**
   * Updates the id of every axis from the used axis list.
   */
  public void updateUsedAxisIds() {
    for (int i = 0; i < usedAxisList.size(); i++) {
      usedAxisList.get(i).getPK().setId(String.valueOf(i));
    }
  }

  /**
   * Loads the used axis list by using the field template. The pattern taken from the template looks
   * like : axisId1,baseValueId1,mandatory1,variant1.axisId2,baseValueId2,mandatory2,variant2...
   */
  private void loadUsedAxisList() {
    String pdcAxis = fieldTemplate.getParameter("pdcAxis", "fr");
    usedAxisList = buildUsedAxisList(pdcAxis);
  }

  /**
   * @param pdcAxis The description of used axis which are needed, following the pattern :
   * axisId1,baseValueId1,mandatory1,variant1.axisId2,baseValueId2,mandatory2,variant2...
   * @return The list of used axis corresponding to the description given as parameter.
   */
  private static ArrayList<UsedAxis> buildUsedAxisList(String pdcAxis) {
    ArrayList<UsedAxis> axisList = new ArrayList<UsedAxis>();
    if (StringUtil.isDefined(pdcAxis)) {
      StringTokenizer st = new StringTokenizer(pdcAxis, ".");
      String[] axisData;
      int usedAxisId = 0;
      String axisId;
      String baseValue;
      String mandatory;
      String variant;
      while (st.hasMoreTokens()) {
        axisData = st.nextToken().split(",");
        if (axisData.length == 4) {
          axisId = axisData[0];
          baseValue = axisData[1];
          mandatory = axisData[2];
          variant = axisData[3];
          UsedAxis usedAxis = new UsedAxis(usedAxisId, "unknown", Integer.parseInt(axisId),
              Integer.parseInt(baseValue), Integer.parseInt(mandatory), Integer.parseInt(variant));
          axisList.add(usedAxis);
          usedAxisId++;
        }
      }
    }
    return axisList;
  }

  /**
   * @return A pattern corresponding to the used axis list, which looks like :
   * axisId1,baseValueId1,mandatory1,variant1.axisId2,baseValueId2,mandatory2,variant2...
   */
  private String getUsedAxisListToString() {
    StringBuffer result = new StringBuffer();
    if (usedAxisList != null) {
      UsedAxis usedAxis;
      for (int i = 0; i < usedAxisList.size(); i++) {
        if (i > 0) {
          result.append(".");
        }
        usedAxis = usedAxisList.get(i);
        result.append(usedAxis.getAxisId()).append(",")
            .append(usedAxis.getBaseValue()).append(",")
            .append(usedAxis.getMandatory()).append(",")
            .append(usedAxis.getVariant());
      }
    }
    return result.toString();
  }

}
