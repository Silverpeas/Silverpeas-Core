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

package com.silverpeas.workflow.api;

import com.silverpeas.workflow.api.instance.ProcessInstance;
import com.silverpeas.workflow.api.model.State;
import com.silverpeas.workflow.api.user.User;

/**
 * The workflow engine services relate to process instance management.
 */
public interface UpdatableProcessInstanceManager extends ProcessInstanceManager {
  /**
   * Creates a new process instance
   * @param modelId model id
   * @return the new ProcessInstance object
   */
  public ProcessInstance createProcessInstance(String modelId)
      throws WorkflowException;

  /**
   * Removes a new process instance
   * @param instanceId instance id
   */
  public void removeProcessInstance(String instanceId) throws WorkflowException;

  /**
   * Locks the given instance for the given instance and state
   * @param instance instance that have to be locked
   * @param state state that have to be locked
   * @param user the locking user
   */
  public void lock(ProcessInstance instance, State state, User user)
      throws WorkflowException;

  /**
   * Locks the given instance for the given instance and state
   * @param instance instance that have to be locked
   * @param state state that have to be locked
   * @param user the locking user
   */
  public void unlock(ProcessInstance instance, State state, User user)
      throws WorkflowException;
}