/**
 * Copyright (C) 2000 - 2011 Silverpeas
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

package com.silverpeas.workflow.api;

import java.util.HashMap;

import com.silverpeas.workflow.engine.WorkflowHub;

import com.stratelia.webactiv.util.ResourceLocator;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

/**
 * The Workflow class is the main entry to the workflow engine public services. This is a proxy
 * class to the WorkflowHub class which gives all the workflow services.
 */
public final class Workflow {
  /**
   * @return the ProcessModelManager
   */
  static public final ProcessModelManager getProcessModelManager()
      throws WorkflowException {
    return WorkflowHub.getProcessModelManager();
  }

  /**
   * @return the ProcessInstanceManager
   */
  static public final ProcessInstanceManager getProcessInstanceManager()
      throws WorkflowException {
    return WorkflowHub.getProcessInstanceManager();
  }

  /**
   * @return the UserManager
   */
  static public final UserManager getUserManager() throws WorkflowException {
    return WorkflowHub.getUserManager();
  }

  /**
   * @return the WorkflowEngine
   */
  static public final WorkflowEngine getWorkflowEngine()
      throws WorkflowException {
    return WorkflowHub.getWorkflowEngine();
  }

  /**
   * @return the TaskManager
   */
  static public final TaskManager getTaskManager() throws WorkflowException {
    return WorkflowHub.getTaskManager();
  }

  /**
   * @return the ErrorManager
   */
  static public final ErrorManager getErrorManager() throws WorkflowException {
    return WorkflowHub.getErrorManager();
  }

  /**
   * Returns the localized label.
   */
  static public String getLabel(String labelName, String lang) {
    ResourceLocator labels = (ResourceLocator) localLabels.get(lang);
    if (labels == null) {
      labels = new ResourceLocator(
          "com.silverpeas.workflow.multilang.workflowEngineBundle", lang);

      localLabels.put(lang, labels);
    }

    return labels.getString(labelName);
  }

  /**
   * Initialize the Workflow inspector the Workflow inspector will verify periodically the timeout
   * on
   */
  static public void initialize() {
    try {
      TimeoutManager timeoutManager = WorkflowHub.getTimeoutManager();
      timeoutManager.initialize();
    } catch (WorkflowException we) {
      SilverTrace.error("workflowEngine", "Workflow.initialize",
          "workflowEngine.EX_ERR_INITIALIZE", we);
    }
  }

  static private HashMap localLabels = new HashMap();
}
