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

package com.silverpeas.workflow.engine;

import com.silverpeas.workflow.api.*;

/**
 * The workflowHub manages all the workflow components implementations. This singleton builds the
 * several workflow components and exports them as services interfaces.
 */
public class WorkflowHub {
  /**
   * @return the TimeoutManager
   */
  static public final TimeoutManager getTimeoutManager()
      throws WorkflowException {
    if (instance == null) {
      instance = createInstance();
    }
    return instance.timeoutManager;
  }

  /**
   * @return the ProcessModelManager
   */
  static public final ProcessModelManager getProcessModelManager()
      throws WorkflowException {
    if (instance == null) {
      instance = createInstance();
    }
    return instance.processModelManager;
  }

  /**
   * @return the ProcessInstanceManager
   */
  static public final ProcessInstanceManager getProcessInstanceManager()
      throws WorkflowException {
    if (instance == null) {
      instance = createInstance();
    }
    return instance.processInstanceManager;
  }

  /**
   * @return the UserManager
   */
  static public final UserManager getUserManager() throws WorkflowException {
    if (instance == null) {
      instance = createInstance();
    }
    return instance.userManager;
  }

  /**
   * @return the WorkflowEngine
   */
  static public final WorkflowEngine getWorkflowEngine()
      throws WorkflowException {
    if (instance == null) {
      instance = createInstance();
    }
    return instance.workflowEngine;
  }

  /**
   * @return the TaskManager
   */
  static public final TaskManager getTaskManager() throws WorkflowException {
    if (instance == null) {
      instance = createInstance();
    }
    return instance.taskManager;
  }

  /**
   * @return the ErrorManager
   */
  static public final ErrorManager getErrorManager() throws WorkflowException {
    if (instance == null) {
      instance = createInstance();
    }
    return instance.errorManager;
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
    timeoutManager = new com.silverpeas.workflow.engine.timeout.TimeoutManagerImpl();
    errorManager = new com.silverpeas.workflow.engine.error.ErrorManagerImpl();
    userManager = new com.silverpeas.workflow.engine.user.UserManagerImpl();
    taskManager = new com.silverpeas.workflow.engine.task.TaskManagerImpl();
    processModelManager = new com.silverpeas.workflow.engine.model.ProcessModelManagerImpl();
    processInstanceManager =
        new com.silverpeas.workflow.engine.instance.ProcessInstanceManagerImpl();
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
