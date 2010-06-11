/**
 * Copyright (C) 2000 - 2009 Silverpeas
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

package com.silverpeas.workflow.engine.task;

import com.silverpeas.workflow.api.TaskManager;
import com.silverpeas.workflow.api.WorkflowException;
import com.silverpeas.workflow.api.instance.Actor;
import com.silverpeas.workflow.api.instance.HistoryStep;
import com.silverpeas.workflow.api.instance.ProcessInstance;
import com.silverpeas.workflow.api.instance.Question;
import com.silverpeas.workflow.api.model.ProcessModel;
import com.silverpeas.workflow.api.model.State;
import com.silverpeas.workflow.api.task.Task;
import com.silverpeas.workflow.api.user.User;

/**
 * The AbstractTaskManager implements all the silverpeas internal TaskManager methods. This Class
 * will be extended for each external todo managenent system.
 */
abstract public class AbstractTaskManager implements TaskManager {
  /**
   * Builds a new task (assigned or assignable).
   */
  public Task createTask(Actor actor, ProcessInstance processInstance)
      throws WorkflowException {
    return new TaskImpl(actor.getUser(), actor.getUserRoleName(), actor.getGroupId(),
        processInstance, actor.getState());
  }

  /**
   * Builds new tasks (assigned or assignable).
   */
  public Task[] createTasks(Actor[] actors, ProcessInstance processInstance)
      throws WorkflowException {
    Task[] tasks = new TaskImpl[actors.length];
    for (int i = 0; i < actors.length; i++) {
      tasks[i] = this.createTask(actors[i], processInstance);
    }

    return tasks;
  }

  /**
   * Returns the tasks assigned to a user on a processInstance.
   */
  public Task[] getTasks(User user, String roleName,
      ProcessInstance processInstance) throws WorkflowException {
    ProcessModel model = processInstance.getProcessModel();
    String[] stateNames = processInstance.getAssignedStates(user, roleName);
    Task[] tasks = new Task[stateNames.length];
    State state = null;

    for (int i = 0; i < stateNames.length; i++) {
      // Get the steps that can be discussed
      HistoryStep[] steps = processInstance.getBackSteps(user, roleName,
          stateNames[i]);
      Question[] pendingQuestions = processInstance
          .getPendingQuestions(stateNames[i]);
      Question[] sentQuestions = processInstance
          .getSentQuestions(stateNames[i]);
      Question[] relevantQuestions = processInstance
          .getRelevantQuestions(stateNames[i]);
      state = model.getState(stateNames[i]);
      if (state == null) {
        throw new WorkflowException("TaskManager.getTasks",
            "workflowEngine.EXP_UNKNOWN_STATE");
      }
      tasks[i] = new TaskImpl(user, roleName, processInstance, state, steps,
          sentQuestions, relevantQuestions, pendingQuestions);
    }

    return tasks;
  }

  /**
   * Returns the creation task of a processModel or null if the user is not allowed to create a new
   * instance.
   */
  public Task getCreationTask(User user, String roleName,
      ProcessModel processModel) throws WorkflowException {
    return new CreationTaskImpl(user, roleName, processModel);
  }
}
