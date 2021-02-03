/*
 * Copyright (C) 2000 - 2021 Silverpeas
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

import org.silverpeas.core.workflow.api.instance.Actor;
import org.silverpeas.core.workflow.api.instance.ProcessInstance;
import org.silverpeas.core.workflow.api.model.ProcessModel;
import org.silverpeas.core.workflow.api.task.Task;
import org.silverpeas.core.workflow.api.user.User;

/**
 * The workflow engine services relate to task management.
 */
public interface TaskManager {
  /**
   * Adds a new task in the user's todos.
   */
  void assignTask(Task task, User delegator) throws WorkflowException;

  /**
   * Removes a task from the user's todos.
   */
  void unAssignTask(Task task) throws WorkflowException;

  /**
   * Builds a new task (assigned or assignable).
   */
  Task createTask(Actor actor, ProcessInstance processInstance)
      throws WorkflowException;

  /**
   * Builds new tasks (assigned or assignable).
   */
  Task[] createTasks(Actor[] actors, ProcessInstance processInstance)
      throws WorkflowException;

  /**
   * Returns the tasks assigned to a user on a processInstance.
   */
  Task[] getTasks(User user, String roleName,
      ProcessInstance processInstance) throws WorkflowException;

  /**
   * Returns the creation task of a processModel or null if the user is not allowed to create a new
   * instance.
   */
  Task getCreationTask(User user, String roleName,
      ProcessModel processModel) throws WorkflowException;

  /**
   * Get the process instance Id referred by the task to do with the given identifier.
   */
  String getProcessInstanceIdFromExternalTodoId(String externalTodoId)
      throws WorkflowException;

  /**
   * Get the role name of task referred by the task to do with the given identifier
   */
  String getRoleNameFromExternalTodoId(String externalTodoId)
      throws WorkflowException;

  /**
   * Notify user that an action has been done
   */
  void notifyActor(Task task, User sender, Actor user, String text, boolean linkDisabled)
      throws WorkflowException;
}
