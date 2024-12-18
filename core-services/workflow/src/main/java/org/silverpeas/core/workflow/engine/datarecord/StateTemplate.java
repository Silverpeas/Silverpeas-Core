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
package org.silverpeas.core.workflow.engine.datarecord;

import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.workflow.api.Workflow;
import org.silverpeas.core.workflow.api.instance.ProcessInstance;
import org.silverpeas.core.workflow.api.model.ProcessModel;
import org.silverpeas.core.workflow.api.model.State;

/**
 * A StateTemplate builds fields giving the current state of a process instance.
 */
public class StateTemplate extends ProcessInstanceFieldTemplate {
  public StateTemplate(String fieldName, ProcessModel processModel,
      String role, String lang) {
    super(fieldName, "text", "text", Workflow.getLabel("stateFieldLabel", lang));
    this.processModel = processModel;
    this.role = role;
    this.lang = lang;
  }

  /**
   * Returns a field built from this template and filled from the given process instance.
   */
  public Field getField(ProcessInstance instance) {
    StringBuilder stateLabels = new StringBuilder();
    String[] stateNames = instance.getActiveStates();
    for (String stateName: stateNames) {
      State state = processModel.getState(stateName);
      if (state != null) {
        if (stateLabels.length() > 0)
          stateLabels.append(" - ");
        stateLabels.append(state.getLabel(role, lang));
      }
    }
    return new TextRoField(stateLabels.toString());
  }

  private final ProcessModel processModel;
  private final String role;
  private final String lang;
}
