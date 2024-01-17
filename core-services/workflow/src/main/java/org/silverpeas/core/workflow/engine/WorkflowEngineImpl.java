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

import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.util.Mutable;
import org.silverpeas.core.workflow.api.ProcessInstanceManager;
import org.silverpeas.core.workflow.api.TaskManager;
import org.silverpeas.core.workflow.api.UpdatableProcessInstanceManager;
import org.silverpeas.core.workflow.api.WorkflowEngine;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.event.GenericEvent;
import org.silverpeas.core.workflow.api.event.QuestionEvent;
import org.silverpeas.core.workflow.api.event.ResponseEvent;
import org.silverpeas.core.workflow.api.event.TaskDoneEvent;
import org.silverpeas.core.workflow.api.event.TaskSavedEvent;
import org.silverpeas.core.workflow.api.instance.ActionStatus;
import org.silverpeas.core.workflow.api.instance.Actor;
import org.silverpeas.core.workflow.api.instance.ProcessInstance;
import org.silverpeas.core.workflow.api.instance.UpdatableHistoryStep;
import org.silverpeas.core.workflow.api.instance.UpdatableProcessInstance;
import org.silverpeas.core.workflow.api.model.Action;
import org.silverpeas.core.workflow.api.model.ProcessModel;
import org.silverpeas.core.workflow.api.model.State;
import org.silverpeas.core.workflow.api.task.Task;
import org.silverpeas.core.workflow.api.user.User;
import org.silverpeas.core.workflow.engine.instance.HistoryStepImpl;
import org.silverpeas.core.workflow.engine.instance.HistoryStepRepository;
import org.silverpeas.core.workflow.engine.instance.LockingUser;
import org.silverpeas.core.workflow.engine.instance.ProcessInstanceImpl;
import org.silverpeas.core.workflow.engine.instance.ProcessInstanceRepository;
import org.silverpeas.core.workflow.engine.model.StateImpl;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Date;

/**
 * One implementation of WorkflowEngine The workflow engine main services.
 */
@Service
@Singleton
public class WorkflowEngineImpl implements WorkflowEngine {

  @Inject
  private ProcessInstanceRepository repository;

  @Inject
  private HistoryStepRepository historyStepRepository;

  /**
   * A task has been done and sent to the workflow Engine which has to process it.
   * @param event the task event that has been done.
   */
  public void process(TaskDoneEvent event) throws WorkflowException {
    process(event, false);
  }

  /**
   * A task has been done and sent to the workflow Engine which has to process it.
   * @param event the task event that has been done.
   */
  public void process(TaskDoneEvent event, boolean ignoreControls) throws WorkflowException {
    boolean creationEvent = false;
    ProcessModel model = event.getProcessModel();
    ProcessInstance instance = event.getProcessInstance();

    // Tests if action is creation
    Action action = model.getAction(event.getActionName());
    if (action != null && "create".equals(action.getKind())) {
      if (!event.isResumingAction()) {
        UpdatableProcessInstanceManager instanceManager =
            (UpdatableProcessInstanceManager) WorkflowHub.getProcessInstanceManager();
        instance = instanceManager.createProcessInstance(model.getModelId());
        event.setProcessInstance(instance);
      }
      creationEvent = true;
    }

    if (!ignoreControls) {
      processControls(event, instance.getInstanceId(), creationEvent);
    }

    // All is OK, send the TaskDoneEvent to the WorkflowEngineThread
    WorkflowEngineTask.addTaskDoneRequest(event);
  }

  private void processControls(GenericEvent event, String id, boolean creation) {
    Transaction.performInOne(() -> {
      ProcessInstanceImpl instance = repository.getById(id);

      try {
        // Over-locks the process instance by admin
        this.manageLocks(instance);

        if (!creation) {
          // Tests if user is declared as a working user
          this.manageRights(event, instance);

          // Tests if user is declared as the working user for this state
          this.checkUserLock(event, instance);
        } else if (event instanceof TaskSavedEvent){
          instance.lock(new StateImpl(""), event.getUser());
        }

        repository.save(instance);
      } catch (WorkflowException we) {
        throw new WorkflowException("WorkflowEngineImpl.processControls",
            "workflowEngine.EX_ERR_PROCESS_EVENT", we);
      }
      return null;
    });
  }

  /**
   * A task has been saved and sent to the workflow Engine which has to process it.
   * @param event the task event that has been saved.
   */
  public void process(TaskSavedEvent event) throws WorkflowException {
    boolean creationEvent = false;
    ProcessModel model = event.getProcessModel();
    ProcessInstance instance = event.getProcessInstance();

    // Tests if action is creation
    Action action = model.getAction(event.getActionName());
    if (event.isFirstTimeSaved() && action != null && "create".equals(action.getKind())) {
      UpdatableProcessInstanceManager instanceManager =
          (UpdatableProcessInstanceManager) WorkflowHub.getProcessInstanceManager();
      instance = instanceManager.createProcessInstance(model.getModelId());
      event.setProcessInstance(instance);
      creationEvent = true;
    }

    processControls(event, instance.getInstanceId(), creationEvent);

    // All is OK, send the TaskDoneEvent to the WorkflowEngineThread
    WorkflowEngineTask.addTaskSavedRequest(event);
  }

  /**
   * A question has been sent to a previous participant
   * @param event the question event containing all necessary information
   */
  public void process(QuestionEvent event) throws WorkflowException {
    ProcessInstance instance = event.getProcessInstance();

    processControls(event, instance.getInstanceId(), false);

    // All is OK, send the QuestionEvent to the WorkflowEngineThread
    WorkflowEngineTask.addQuestionRequest(event);
  }

  /**
   * A question had been sent to a previous participant. A response is sent !
   * @param event the response event containing all necessary information
   */
  public void process(ResponseEvent event) throws WorkflowException {
    ProcessInstance instance = event.getProcessInstance();

    processControls(event, instance.getInstanceId(), false);

    // All is OK, send the ResponseEvent to the WorkflowEngineThread
    WorkflowEngineTask.addResponseRequest(event);
  }

  /**
   * Do re-affectation for given states Remove users as working users and unassign corresponding
   * tasks Add users as working users and assign corresponding tasks
   */
  public void reAssignActors(UpdatableProcessInstance instance, Actor[] unAssignedActors,
      Actor[] assignedActors, User user) throws WorkflowException {
    // Get the process instance
    ProcessInstanceManager instanceManager = WorkflowHub.getProcessInstanceManager();
    String id = instance.getInstanceId();
    Mutable<UpdatableHistoryStep> step = Mutable.empty();

    // first create the history step
    Transaction.performInOne(()-> {
      UpdatableProcessInstance processInstance = repository.getById(id);

      UpdatableHistoryStep newStep = (UpdatableHistoryStep) instanceManager.createHistoryStep();
      newStep.setUserId(user.getUserId());
      newStep.setAction("#reAssign#");
      newStep.setActionDate(new Date());
      newStep.setUserRoleName("supervisor");
      newStep.setResolvedState("DummyStateFromReassignment");
      // To be processed
      newStep.setActionStatus(ActionStatus.TO_BE_PROCESSED);
      newStep.setProcessInstance(processInstance);

      historyStepRepository.save((HistoryStepImpl) newStep);

      step.set(newStep);
      return newStep;
    });

    // add the new step to the processInstance
    instance.addHistoryStep(step.get());

    Transaction.performInOne(()-> {
      ProcessInstanceImpl processInstance = repository.getById(id);

      UpdatableHistoryStep currentStep =
          (UpdatableHistoryStep) processInstance.getHistoryStep(step.get().getId());
      // only to set the current step to that step
      processInstance.updateHistoryStep(currentStep);

      unassignTasksToWorkingUsers(unAssignedActors, processInstance);
      removeWorkingUsersFromProcessInstance(unAssignedActors, processInstance);
      assignTasksToWorkingUsers(assignedActors, user, processInstance);
      addWorkingUsersToProcessInstance(assignedActors, processInstance);

      currentStep.setActionStatus(ActionStatus.AFFECTATIONS_DONE);
      processInstance.updateHistoryStep(currentStep);

      repository.save(processInstance);

      return null;
    });
  }

  private void addWorkingUsersToProcessInstance(final Actor[] assignedActors,
      final UpdatableProcessInstance processInstance) throws WorkflowException {
    // Declare these working users in instance
    for (final Actor assignedActor : assignedActors) {
      processInstance.addWorkingUser(assignedActor.getUser(), assignedActor.getState(),
          assignedActor.getUserRoleName());
    }
  }

  private void assignTasksToWorkingUsers(final Actor[] assignedActors, final User user,
      final UpdatableProcessInstance processInstance) throws WorkflowException {
    // Assign tasks to these working users
    TaskManager taskManager = WorkflowHub.getTaskManager();
    Task[] tasks = taskManager.createTasks(assignedActors, processInstance);
    for (final Task task : tasks) {
      taskManager.assignTask(task, user);
    }
  }

  private void removeWorkingUsersFromProcessInstance(final Actor[] unAssignedActors,
      final UpdatableProcessInstance processInstance) throws WorkflowException {
    // Remove these working users from instance
    for (final Actor unAssignedActor : unAssignedActors) {
      processInstance.removeWorkingUser(unAssignedActor.getUser(), unAssignedActor.getState(),
          unAssignedActor.getUserRoleName());
    }
  }

  private void unassignTasksToWorkingUsers(final Actor[] unAssignedActors,
      final UpdatableProcessInstance processInstance) throws WorkflowException {
    // Unassign tasks to these working users
    TaskManager taskManager = WorkflowHub.getTaskManager();
    Task[] tasks = taskManager.createTasks(unAssignedActors, processInstance);
    for (final Task task : tasks) {
      taskManager.unAssignTask(task);
    }
  }

  /**
   * Over-locks the process instance by admin
   */
  private void manageLocks(UpdatableProcessInstance instance) throws WorkflowException {
    if (instance != null) {
      instance.lock();
    } else {
      throw new WorkflowException("WorkflowEngineImpl.manageLocks",
          "EX_ERR_EVENT_WITHOUT_INSTANCE");
    }
  }

  /**
   * Tests if user is declared as a working user
   */
  private void manageRights(GenericEvent event, UpdatableProcessInstance instance)
      throws WorkflowException {
    boolean validUser = false;
    State resolvedState = event.getResolvedState();
    if (resolvedState == null) {
      resolvedState = new StateImpl("");
    }
    User actor = event.getUser();
    Actor[] wkUsers = instance.getWorkingUsers(resolvedState.getName(), event.getUserRoleName());
    if (wkUsers != null) {
      for (final Actor wkUser : wkUsers) {
        if (wkUser.getUser().equals(actor)) {
          validUser = true;
        }
      }
    }

    if (!validUser) {
      throw new WorkflowException("WorkflowEngineImpl.manageRights", "EX_ERR_FORBIDDEN_ACTION");
    }
  }

  /**
   * Tests if user has locked this instance
   */
  private void checkUserLock(GenericEvent event, UpdatableProcessInstance instance)
      throws WorkflowException {
    State resolvedState = event.getResolvedState();
    if (resolvedState == null) {
      resolvedState = new StateImpl("");
    }
    LockingUser lockingUser = instance.getLockingUser(resolvedState.getName());
    User actor = event.getUser();

    if (lockingUser == null) {
      throw new WorkflowException("WorkflowEngineImpl.process(TaskDoneEvent)",
          "EX_ERR_NO_LOCK_BEFORE_ACTION");
    }
    User user = WorkflowHub.getUserManager().getUser(lockingUser.getUserId());
    if (!user.equals(actor)) {
      throw new WorkflowException("WorkflowEngineImpl.process(TaskDoneEvent)",
          "EX_ERR_INSTANCE_LOCKED_BY_ANOTHER_USER");
    }
  }
}