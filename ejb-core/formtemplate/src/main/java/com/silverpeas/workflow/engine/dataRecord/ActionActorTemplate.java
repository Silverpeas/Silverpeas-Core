/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.workflow.engine.dataRecord;

import com.silverpeas.form.Field;
import com.silverpeas.workflow.api.Workflow;
import com.silverpeas.workflow.api.WorkflowException;
import com.silverpeas.workflow.api.instance.HistoryStep;
import com.silverpeas.workflow.api.instance.ProcessInstance;
import com.silverpeas.workflow.api.model.Action;

/**
 * A ActionActorTemplate builds fields giving the title of a process instance.
 */
public class ActionActorTemplate extends ProcessInstanceFieldTemplate {
  public ActionActorTemplate(String fieldName, Action action, String role,
      String lang) {
    super(fieldName, "text", "text", Workflow.getLabel("actionActorFieldLabel",
        lang));
    actionName = action.getName();
  }

  /**
   * Returns a field built from this template and filled from the given process instance.
   */
  public Field getField(ProcessInstance instance) {
    HistoryStep step = instance.getMostRecentStep(actionName);
    if (step != null) {
      try {
        return new TextRoField(step.getUser().getFullName());
      } catch (WorkflowException e) {
        return new TextRoField(null);
      }
    } else {
      return new TextRoField(null);
    }
  }

  private final String actionName;
}
