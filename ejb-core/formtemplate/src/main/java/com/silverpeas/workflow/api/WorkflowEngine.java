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

package com.silverpeas.workflow.api;

import com.silverpeas.workflow.api.user.User;
import com.silverpeas.workflow.api.instance.Actor;
import com.silverpeas.workflow.api.instance.UpdatableProcessInstance;
import com.silverpeas.workflow.api.event.TaskDoneEvent;
import com.silverpeas.workflow.api.event.QuestionEvent;
import com.silverpeas.workflow.api.event.ResponseEvent;
import com.silverpeas.workflow.api.event.TaskSavedEvent;
import com.silverpeas.workflow.api.WorkflowException;

/**
 * The workflow engine main services.
 */
public interface WorkflowEngine {
  /**
   * A task has been done and sent to the workflow Enginewhich has to process it.
   * @param event the task event that has been done.
   * @param ignoreControls if true, ignore controls about locks and permissions.
   */
  public void process(TaskDoneEvent event, boolean ignoreControls) throws WorkflowException;

  /**
   * A task has been done and sent to the workflow Enginewhich has to process it.
   * @param event the task event that has been done.
   */
  public void process(TaskDoneEvent event) throws WorkflowException;

  /**
   * A task has been saved and sent to the workflow Enginewhich has to process it.
   * @param event the task event that has been saved.
   */
  public void process(TaskSavedEvent event) throws WorkflowException;

  /**
   * A question has been sent to a previous participant
   * @param event the question event containing all necessary information
   */
  public void process(QuestionEvent event) throws WorkflowException;

  /**
   * A question had been sent to a previous participant. A response is sent !
   * @param event the response event containing all necessary information
   */
  public void process(ResponseEvent event) throws WorkflowException;

  /**
   * Do re-affectation for given states Remove users as working users and unassign corresponding
   * tasks Add users as working users and assign corresponding tasks
   */
  public void reAssignActors(UpdatableProcessInstance instance,
      Actor[] unAssignedActors, Actor[] assignedActors, User user)
      throws WorkflowException;

}
