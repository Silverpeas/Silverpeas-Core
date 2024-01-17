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
package org.silverpeas.core.workflow.engine;

import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.workflow.api.ErrorManager;
import org.silverpeas.core.workflow.api.ProcessInstanceManager;
import org.silverpeas.core.workflow.api.ProcessModelManager;
import org.silverpeas.core.workflow.api.TaskManager;
import org.silverpeas.core.workflow.api.UserManager;
import org.silverpeas.core.workflow.api.WorkflowEngine;

/**
 * The workflowHub manages all the workflow components implementations. This instance-orphan class
 * gives a single point of access to the implementation of the different service interfaces without
 * having any knowledge about them and about the life-cycle.
 */
public class WorkflowHub {

  private WorkflowHub() {
  }

  /**
   * @return an instance of {@link ProcessModelManager}
   */
  public static ProcessModelManager getProcessModelManager() {
    return ServiceProvider.getService(ProcessModelManager.class);
  }

  /**
   * @return an instance of {@link ProcessInstanceManager}
   */
  public static ProcessInstanceManager getProcessInstanceManager() {
    return ServiceProvider.getService(ProcessInstanceManager.class);
  }

  /**
   * @return an instance of {@link UserManager}
   */
  public static UserManager getUserManager() {
    return ServiceProvider.getService(UserManager.class);
  }

  /**
   * @return an instance of {@link WorkflowEngine}
   */
  public static WorkflowEngine getWorkflowEngine() {
    return ServiceProvider.getService(WorkflowEngine.class);
  }

  /**
   * @return an instance of {@link TaskManager}
   */
  public static TaskManager getTaskManager() {
    return ServiceProvider.getService(TaskManager.class);
  }

  /**
   * @return an instance of {@link ErrorManager}
   */
  public static ErrorManager getErrorManager() {
    return ServiceProvider.getService(ErrorManager.class);
  }
}
