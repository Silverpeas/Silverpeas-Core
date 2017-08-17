/*
 * Copyright (C) 2000 - 2017 Silverpeas
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
package org.silverpeas.core.workflow.api;

import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.workflow.engine.WorkflowHub;

import java.util.HashMap;

/**
 * The Workflow class is the main entry to the workflow engine public services. This is a proxy
 * class to the WorkflowHub class which gives all the workflow services.
 */
public final class Workflow {

  private static final HashMap<String, LocalizationBundle> localLabels = new HashMap<>();

  /**
   * @return the ProcessModelManager
   */
  public static ProcessModelManager getProcessModelManager()
      throws WorkflowException {
    return WorkflowHub.getProcessModelManager();
  }

  /**
   * @return the ProcessInstanceManager
   */
  public static ProcessInstanceManager getProcessInstanceManager()
      throws WorkflowException {
    return WorkflowHub.getProcessInstanceManager();
  }

  /**
   * @return the UserManager
   */
  public static UserManager getUserManager() throws WorkflowException {
    return WorkflowHub.getUserManager();
  }

  /**
   * @return the WorkflowEngine
   */
  public static WorkflowEngine getWorkflowEngine()
      throws WorkflowException {
    return WorkflowHub.getWorkflowEngine();
  }

  /**
   * @return the TaskManager
   */
  public static TaskManager getTaskManager() throws WorkflowException {
    return WorkflowHub.getTaskManager();
  }

  /**
   * @return the ErrorManager
   */
  public static ErrorManager getErrorManager() throws WorkflowException {
    return WorkflowHub.getErrorManager();
  }

  /**
   * Returns the localized label.
   */
  public static String getLabel(String labelName, String lang) {
    LocalizationBundle labels = localLabels.get(lang);
    if (labels == null) {
      labels = ResourceLocator.getLocalizationBundle(
          "org.silverpeas.workflow.multilang.workflowEngineBundle", lang);

      localLabels.put(lang, labels);
    }

    return labels.getString(labelName);
  }

}
