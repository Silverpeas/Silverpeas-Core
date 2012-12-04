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

package com.silverpeas.workflow.engine;

import com.silverpeas.workflow.api.ErrorManager;
import com.silverpeas.workflow.api.ProcessInstanceManager;
import com.silverpeas.workflow.api.ProcessModelManager;
import com.silverpeas.workflow.api.TaskManager;
import com.silverpeas.workflow.api.TimeoutManager;
import com.silverpeas.workflow.api.UserManager;
import com.silverpeas.workflow.api.WorkflowEngine;
import com.silverpeas.workflow.api.WorkflowException;
import com.silverpeas.workflow.engine.error.ErrorManagerImpl;
import com.silverpeas.workflow.engine.instance.ProcessInstanceManagerImpl;
import com.silverpeas.workflow.engine.model.ProcessModelManagerImpl;
import com.silverpeas.workflow.engine.task.TaskManagerImpl;
import com.silverpeas.workflow.engine.timeout.TimeoutManagerImpl;
import com.silverpeas.workflow.engine.user.UserManagerImpl;

/**
 * The workflowHub manages all the workflow components implementations. This singleton builds the
 * several workflow components and exports them as services interfaces.
 */
public class WorkflowHub {
  /**
   * @return the TimeoutManager
   */
  static public final TimeoutManager getTimeoutManager() throws WorkflowException {
    return createInstance().timeoutManager;
  }

  /**
   * @return the ProcessModelManager
   */
  static public final ProcessModelManager getProcessModelManager() throws WorkflowException {
    return createInstance().processModelManager;
  }

  /**
   * @return the ProcessInstanceManager
   */
  static public final ProcessInstanceManager getProcessInstanceManager() throws WorkflowException {
    return createInstance().processInstanceManager;
  }

  /**
   * @return the UserManager
   */
  static public final UserManager getUserManager() throws WorkflowException {
    return createInstance().userManager;
  }

  /**
   * @return the WorkflowEngine
   */
  static public final WorkflowEngine getWorkflowEngine() throws WorkflowException {
    return createInstance().workflowEngine;
  }

  /**
   * @return the TaskManager
   */
  static public final TaskManager getTaskManager() throws WorkflowException {
    return createInstance().taskManager;
  }

  /**
   * @return the ErrorManager
   */
  static public final ErrorManager getErrorManager() throws WorkflowException {
    return createInstance().errorManager;
  }

  /**
   * As a singleton class, the constructor is private. After creation the init method <em>must </em>
   * be called.
   * @see createInstance()
   * @see init()
   */
  private WorkflowHub() {
  }

  /**
   * Creates the singleton instance.
   */
  static synchronized private final WorkflowHub createInstance()
      throws WorkflowException {
    if (instance == null) {
      instance = new WorkflowHub();
      try {
        instance.init();
      } catch (WorkflowException e) {
        instance = null;
        throw e;
      }
    }
    return instance;
  }

  /**
   * Builds the differents components.
   */
  private void init() throws WorkflowException {
    timeoutManager = new TimeoutManagerImpl();
    errorManager = new ErrorManagerImpl();
    userManager = new UserManagerImpl();
    taskManager = new TaskManagerImpl();
    processModelManager = new ProcessModelManagerImpl();
    processInstanceManager = new ProcessInstanceManagerImpl();
    workflowEngine = new WorkflowEngineImpl();
  }

  /**
   * The singleton instance.
   */
  static private WorkflowHub instance;

  /**
   * the TimeoutManager
   */
  private TimeoutManager timeoutManager = null;

  /**
   * the ProcessModelManager
   */
  private ProcessModelManager processModelManager = null;

  /**
   * the ProcessInstanceManager
   */
  private ProcessInstanceManager processInstanceManager = null;

  /**
   * the UserManager
   */
  private UserManager userManager = null;

  /**
   * the WorkflowEngine
   */
  private WorkflowEngine workflowEngine = null;

  /**
   * the TaskManager
   */
  private TaskManager taskManager = null;

  /**
   * the ErrorManager
   */
  private ErrorManager errorManager = null;
  
}
