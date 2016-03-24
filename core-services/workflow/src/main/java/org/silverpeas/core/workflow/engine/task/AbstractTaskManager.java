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

package org.silverpeas.core.workflow.engine.task;

import org.silverpeas.core.workflow.api.TaskManager;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.instance.Actor;
import org.silverpeas.core.workflow.api.instance.HistoryStep;
import org.silverpeas.core.workflow.api.instance.ProcessInstance;
import org.silverpeas.core.workflow.api.instance.Question;
import org.silverpeas.core.workflow.api.model.ProcessModel;
import org.silverpeas.core.workflow.api.model.State;
import org.silverpeas.core.workflow.api.task.Task;
import org.silverpeas.core.workflow.api.user.User;
import org.silverpeas.core.workflow.engine.model.StateImpl;
import org.silverpeas.core.util.StringUtil;

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

    // Getting assigned states
    String[] stateNames = processInstance.getAssignedStates(user, roleName);
    Task[] tasks = new Task[stateNames.length];
    State state;

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

      if (StringUtil.isDefined(stateNames[i])) {
        state = model.getState(stateNames[i]);
      } else {
        state = new StateImpl("");
      }

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
