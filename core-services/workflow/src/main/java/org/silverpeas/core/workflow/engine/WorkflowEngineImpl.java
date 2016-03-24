/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.workflow.engine;

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
import org.silverpeas.core.workflow.api.instance.Actor;
import org.silverpeas.core.workflow.api.instance.UpdatableHistoryStep;
import org.silverpeas.core.workflow.api.instance.UpdatableProcessInstance;
import org.silverpeas.core.workflow.api.model.Action;
import org.silverpeas.core.workflow.api.model.ProcessModel;
import org.silverpeas.core.workflow.api.model.State;
import org.silverpeas.core.workflow.api.task.Task;
import org.silverpeas.core.workflow.api.user.User;
import org.silverpeas.core.workflow.engine.instance.LockingUser;
import org.silverpeas.core.workflow.engine.instance.ProcessInstanceImpl;
import org.silverpeas.core.workflow.engine.jdo.WorkflowJDOManager;
import org.silverpeas.core.workflow.engine.model.StateImpl;
import org.silverpeas.core.thread.ManagedThreadPool;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.PersistenceException;

import java.util.Date;

/**
 * One implementation of WorkflowEngine The workflow engine main services.
 */
@Singleton
public class WorkflowEngineImpl implements WorkflowEngine {

  @PostConstruct
  public void start() {
    // Use the ManagedThreadPool in order to access container context (JPA and Transaction)
    ManagedThreadPool.invoke(new WorkflowEngineTask());
  }

  /**
   * A task has been done and sent to the workflow Enginewhich has to process it.
   * @param event the task event that has been done.
   */
  public void process(TaskDoneEvent event) throws WorkflowException {
    process(event, false);
  }

  /**
   * A task has been done and sent to the workflow Enginewhich has to process it.
   * @param event the task event that has been done.
   */
  public void process(TaskDoneEvent event, boolean ignoreControls) throws WorkflowException {

    boolean creationEvent = false;
    ProcessModel model = event.getProcessModel();
    Database db = null;
    UpdatableProcessInstance instance = null;

    // Tests if action is creation
    Action action = model.getAction(event.getActionName());
    if (action != null && action.getKind().equals("create")) {
      if (!event.isResumingAction()) {
        UpdatableProcessInstanceManager instanceManager =
            (UpdatableProcessInstanceManager) WorkflowHub.getProcessInstanceManager();
        instance =
            (UpdatableProcessInstance) instanceManager.createProcessInstance(model.getModelId());
        event.setProcessInstance(instance);
      } else {
        instance = (UpdatableProcessInstance) event.getProcessInstance();
      }
      creationEvent = true;
    } else {
      instance = (UpdatableProcessInstance) event.getProcessInstance();
    }

    if (!ignoreControls) {
      try {
        // Get database connection
        db = WorkflowJDOManager.getDatabase();

        // begin transaction
        db.begin();

        // Re-load process instance
        instance =
            (UpdatableProcessInstance) db.load(ProcessInstanceImpl.class, instance.getInstanceId());

        // Do workflow stuff
        try {
          // Over-locks the process instance by admin
          this.manageLocks(event, instance);

          // Tests if user is declared as a working user
          if (!creationEvent) {
            this.manageRights(event, instance);
          }

          // Tests if user is declared as the working user for this state
          if (!creationEvent) {
            this.checkUserLock(event, instance);
          }

          // Checks the datas associated to the event
          /* xoxox a faire en concordance avec les specs du form manager */

        } catch (WorkflowException we) {
          db.rollback();
          throw new WorkflowException("WorkflowEngineImpl.process",
              "workflowEngine.EX_ERR_PROCESS_EVENT", we);
        }

        // commit
        db.commit();
      } catch (PersistenceException pe) {
        throw new WorkflowException("WorkflowEngineImpl.process",
            "workflowEngine.EX_ERR_PROCESS_EVENT", pe);
      } finally {
        WorkflowJDOManager.closeDatabase(db);
      }
    }

    // All is OK, send the TaskDoneEvent to the WorkflowEngineThread
    WorkflowEngineTask.addTaskDoneRequest(event);
  }

  /**
   * A task has been saved and sent to the workflow Enginewhich has to process it.
   * @param event the task event that has been saved.
   */
  public void process(TaskSavedEvent event) throws WorkflowException {

    boolean creationEvent = false;
    ProcessModel model = event.getProcessModel();
    Database db = null;
    UpdatableProcessInstance instance;

    // Tests if action is creation
    Action action = model.getAction(event.getActionName());
    if (event.isFirstTimeSaved() && action != null && action.getKind().equals("create")) {
      UpdatableProcessInstanceManager instanceManager =
          (UpdatableProcessInstanceManager) WorkflowHub.getProcessInstanceManager();
      instance =
          (UpdatableProcessInstance) instanceManager.createProcessInstance(model.getModelId());
      event.setProcessInstance(instance);
      creationEvent = true;
    } else {
      instance = (UpdatableProcessInstance) event.getProcessInstance();
    }

    try {
      // Get database connection
      db = WorkflowJDOManager.getDatabase();

      // begin transaction
      db.begin();

      // Re-load process instance
      instance =
          (UpdatableProcessInstance) db.load(ProcessInstanceImpl.class, instance.getInstanceId());

      // Do workflow stuff
      try {
        // Over-locks the process instance by admin
        this.manageLocks(event, instance);

        // Tests if user is declared as a working user
        if (!creationEvent) {
          this.manageRights(event, instance);
        } else {
          instance.lock(new StateImpl(""), event.getUser());
        }
        // Tests if user is declared as the working user for this state
        if (!creationEvent) {
          this.checkUserLock(event, instance);
        }
        // Checks the datas associated to the event
        /* xoxox a faire en concordance avec les specs du form manager */

      } catch (WorkflowException we) {
        db.rollback();
        throw new WorkflowException("WorkflowEngineImpl.process",
            "workflowEngine.EX_ERR_PROCESS_EVENT", we);
      }

      // commit
      db.commit();
    } catch (PersistenceException pe) {
      throw new WorkflowException("WorkflowEngineImpl.process",
          "workflowEngine.EX_ERR_PROCESS_EVENT", pe);
    } finally {
      WorkflowJDOManager.closeDatabase(db);
    }

    // All is OK, send the TaskDoneEvent to the WorkflowEngineThread
    WorkflowEngineTask.addTaskSavedRequest(event);
  }

  /**
   * A question has been sent to a previous participant
   * @param event the question event containing all necessary information
   */
  public void process(QuestionEvent event) throws WorkflowException {
    UpdatableProcessInstance instance = (UpdatableProcessInstance) event.getProcessInstance();
    Database db = null;

    try {
      // Get database connection
      db = WorkflowJDOManager.getDatabase();

      // begin transaction
      db.begin();

      // Re-load process instance
      instance =
          (UpdatableProcessInstance) db.load(ProcessInstanceImpl.class, instance.getInstanceId());

      // Do workflow stuff
      try {
        // Over-locks the process instance by admin
        this.manageLocks(event, instance);

        // Tests if user is declared as a working user
        this.manageRights(event, instance);

        // Tests if user is declared as the working user for this state
        this.checkUserLock(event, instance);

        // Checks the datas associated to the event
        /* xoxox a faire en concordance avec les specs du form manager */

      } catch (WorkflowException we) {
        db.rollback();
        throw new WorkflowException("WorkflowEngineImpl.process",
            "workflowEngine.EX_ERR_PROCESS_EVENT", we);
      }

      // commit
      db.commit();
    } catch (PersistenceException pe) {
      throw new WorkflowException("WorkflowEngineImpl.process",
          "workflowEngine.EX_ERR_PROCESS_EVENT", pe);
    } finally {
      WorkflowJDOManager.closeDatabase(db);
    }

    // All is OK, send the QuestionEvent to the WorkflowEngineThread
    WorkflowEngineTask.addQuestionRequest(event);
  }

  /**
   * A question had been sent to a previous participant. A response is sent !
   * @param event the response event containing all necessary information
   */
  public void process(ResponseEvent event) throws WorkflowException {
    UpdatableProcessInstance instance = (UpdatableProcessInstance) event.getProcessInstance();
    Database db = null;

    try {
      // Get database connection
      db = WorkflowJDOManager.getDatabase();

      // begin transaction
      db.begin();

      // Re-load process instance
      instance =
          (UpdatableProcessInstance) db.load(ProcessInstanceImpl.class, instance.getInstanceId());

      // Do workflow stuff
      try {
        // Over-locks the process instance by admin
        this.manageLocks(event, instance);

        // Tests if user is declared as a working user
        this.manageRights(event, instance);

        // Tests if user is declared as the working user for this state
        this.checkUserLock(event, instance);

        // Checks the datas associated to the event
        /* xoxox a faire en concordance avec les specs du form manager */

      } catch (WorkflowException we) {
        db.rollback();
        throw new WorkflowException("WorkflowEngineImpl.process",
            "workflowEngine.EX_ERR_PROCESS_EVENT", we);
      }

      // commit
      db.commit();
    } catch (PersistenceException pe) {
      throw new WorkflowException("WorkflowEngineImpl.process",
          "workflowEngine.EX_ERR_PROCESS_EVENT", pe);
    } finally {
      WorkflowJDOManager.closeDatabase(db);
    }

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
    Database db = null;
    UpdatableHistoryStep step;

    // Get the task manager
    TaskManager taskManager = WorkflowHub.getTaskManager();

    try {
      // Get database connection
      db = WorkflowJDOManager.getDatabase();

      // begin transaction
      db.begin();

      // Re-load process instance
      instance =
          (UpdatableProcessInstance) db.load(ProcessInstanceImpl.class, instance.getInstanceId());

      // first create the history step
      try {
        step = (UpdatableHistoryStep) instanceManager.createHistoryStep();
        step.setUserId(user.getUserId());
        step.setAction("#reAssign#");
        step.setActionDate(new Date());
        step.setUserRoleName("supervisor");
        step.setActionStatus(0); // To be processed

        // add the new step to the processInstance
        instance.addHistoryStep(step);
      } catch (WorkflowException we) {
        db.rollback();
        throw new WorkflowException("WorkflowEngineImpl.assignActors", "EX_ERR_REASSIGN_ACTORS",
            we);
      }

      // commit
      db.commit();

      // then do affectations
      db.begin();

      // Re-load process instance
      instance =
          (UpdatableProcessInstance) db.load(ProcessInstanceImpl.class, instance.getInstanceId());
      step = (UpdatableHistoryStep) instance.getHistoryStep(step.getId());
      instance.updateHistoryStep(step); // only to set the current step of
      // instance to that step

      // first create the history step
      try {
        // Unassign tasks to these working users
        Task[] tasks = taskManager.createTasks(unAssignedActors, instance);
        for (final Task task : tasks) {
          taskManager.unAssignTask(task);
        }

        // Remove these working users from instance
        for (final Actor unAssignedActor : unAssignedActors) {
          instance.removeWorkingUser(unAssignedActor.getUser(), unAssignedActor.getState(),
              unAssignedActor.getUserRoleName());
        }

        // Assign tasks to these working users
        tasks = taskManager.createTasks(assignedActors, instance);
        for (final Task task : tasks) {
          taskManager.assignTask(task, user);
        }

        // Declare these working users in instance
        for (final Actor assignedActor : assignedActors) {
          instance.addWorkingUser(assignedActor.getUser(), assignedActor.getState(),
              assignedActor.getUserRoleName());
        }

        step.setActionStatus(2);
        instance.updateHistoryStep(step);
      } catch (WorkflowException we) {
        db.rollback();
        throw new WorkflowException("WorkflowEngineImpl.reAssignActors", "EX_ERR_REASSIGN_ACTORS",
            we);
      }

      // commit
      db.commit();
    } catch (PersistenceException pe) {
      throw new WorkflowException("WorkflowEngine.reAssignActors",
          "workflowEngine.EX_ERR_REASSIGN_ACTORS", pe);
    } finally {
      WorkflowJDOManager.closeDatabase(db);
    }
  }

  /**
   * Over-locks the process instance by admin
   */
  private void manageLocks(GenericEvent event, UpdatableProcessInstance instance)
      throws WorkflowException {
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
    State resolvedState;
    User actor;
    Actor[] wkUsers;
    boolean validUser = false;

    resolvedState = event.getResolvedState();
    if (resolvedState == null) {
      resolvedState = new StateImpl("");
    }
    actor = event.getUser();
    wkUsers = instance.getWorkingUsers(resolvedState.getName(), event.getUserRoleName());
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
    State resolvedState;
    LockingUser lockingUser;
    User actor;

    resolvedState = event.getResolvedState();
    if (resolvedState == null) {
      resolvedState = new StateImpl("");
    }
    lockingUser = instance.getLockingUser(resolvedState.getName());
    actor = event.getUser();

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