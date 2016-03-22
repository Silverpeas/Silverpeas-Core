/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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

package org.silverpeas.core.workflow.api;

import org.silverpeas.core.workflow.api.instance.ProcessInstance;
import org.silverpeas.core.workflow.api.instance.Actor;
import org.silverpeas.core.workflow.api.instance.HistoryStep;
import org.silverpeas.core.workflow.api.user.User;
import org.silverpeas.core.workflow.api.model.State;

/**
 * The workflow engine services relate to process instance management.
 */
public interface ProcessInstanceManager {
  /**
   * Get the list of process instances for a given peas Id, user and role.
   * @param peasId id of processManager instance
   * @param user user for who the process instance list is
   * @param role role name of the user for who the process instance list is (useful when user has
   * different roles)
   * @return an array of ProcessInstance objects
   */
  public ProcessInstance[] getProcessInstances(String peasId, User user,
      String role) throws WorkflowException;

  /**
   * Get the list of process instances for a given peas Id, user and role, and user's roles.
   * @param peasId id of processManager instance
   * @param user user for who the process instance list is
   * @param role role name of the user for who the process instance list is (useful when user has
   * @param userRoles all role names that user has for this component instance different roles)
   * @return an array of ProcessInstance objects
   */
  public ProcessInstance[] getProcessInstances(String peasId, User user,
      String role, String[] userRoles, String[] groupIds) throws WorkflowException;

  /**
   * Get the list of process instances for a given peas Id, that have the given state activated
   * @param peasId id of processManager instance
   * @param state activated state
   * @return an array of ProcessInstance objects
   */
  public ProcessInstance[] getProcessInstancesInState(String peasId, State state)
      throws WorkflowException;

  /**
   * Get the process instances for a given instance id
   * @param instanceId id of searched instance
   * @return the searched process instance
   */
  public ProcessInstance getProcessInstance(String instanceId)
      throws WorkflowException;

  /**
   * Build a new HistoryStep Return an object implementing HistoryStep interface
   */
  public HistoryStep createHistoryStep();

  /**
   * Builds an actor from a user and a role.
   */
  public Actor createActor(User user, String roleName, State state);

  /**
   * Get the list of process instances for which timeout date is over
   * @return an array of ProcessInstance objects
   * @throws WorkflowException
   */
  public ProcessInstance[] getTimeOutProcessInstances() throws WorkflowException;

}
