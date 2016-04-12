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

package org.silverpeas.core.workflow.engine;

import org.silverpeas.core.contribution.content.form.Field;
import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.workflow.api.ProcessInstanceManager;
import org.silverpeas.core.workflow.api.TaskManager;
import org.silverpeas.core.workflow.api.UserManager;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.event.GenericEvent;
import org.silverpeas.core.workflow.api.event.QuestionEvent;
import org.silverpeas.core.workflow.api.event.ResponseEvent;
import org.silverpeas.core.workflow.api.event.TaskDoneEvent;
import org.silverpeas.core.workflow.api.event.TaskSavedEvent;
import org.silverpeas.core.workflow.api.event.TimeoutEvent;
import org.silverpeas.core.workflow.api.instance.Actor;
import org.silverpeas.core.workflow.api.instance.Participant;
import org.silverpeas.core.workflow.api.instance.Question;
import org.silverpeas.core.workflow.api.instance.UpdatableHistoryStep;
import org.silverpeas.core.workflow.api.instance.UpdatableProcessInstance;
import org.silverpeas.core.workflow.api.model.Action;
import org.silverpeas.core.workflow.api.model.Consequence;
import org.silverpeas.core.workflow.api.model.Consequences;
import org.silverpeas.core.workflow.api.model.ProcessModel;
import org.silverpeas.core.workflow.api.model.QualifiedUsers;
import org.silverpeas.core.workflow.api.model.State;
import org.silverpeas.core.workflow.api.model.Trigger;
import org.silverpeas.core.workflow.api.task.Task;
import org.silverpeas.core.workflow.api.user.User;
import org.silverpeas.core.workflow.engine.instance.ProcessInstanceImpl;
import org.silverpeas.core.workflow.engine.instance.ProcessInstanceManagerImpl;
import org.silverpeas.core.workflow.engine.jdo.WorkflowJDOManager;
import org.silverpeas.core.workflow.engine.model.StateImpl;
import org.silverpeas.core.workflow.external.ExternalAction;
import org.exolab.castor.jdo.Database;
import org.exolab.castor.jdo.PersistenceException;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

/**
 * This Thread is no longer used. We keep it for merge refactoring purpose.
 * @deprecated
 */
public class WorkflowEngineThread {
  /**
   * The constructor is private : only one WorkflowEngineThread will be created to process all the
   * request.
   */
  private WorkflowEngineThread() {
  }
}

/**
 * Each request must define a method called process which will process the request with a given
 * WorkflowEngine.
 */
interface Request {

  /**
   * Method declaration
   */
  public void process() throws WorkflowException;
}

/**
 * A TaskDone indicates the workflow engine that a task has been done
 */
class TaskDoneRequest implements Request {

  /**
   * Constructor declaration
   */
  TaskDoneRequest(TaskDoneEvent event) {
    this.event = event;
  }

  /**
   * Method declaration
   */
  @Override
  public void process() throws WorkflowException {


    // Get the process instance
    UpdatableProcessInstance instance = (UpdatableProcessInstance) event.getProcessInstance();
    ProcessInstanceManager instanceManager = WorkflowHub.getProcessInstanceManager();
    Database db = null;
    UpdatableHistoryStep step = null;

    try {
      // Get database connection
      db = WorkflowJDOManager.getDatabase();

      // begin transaction
      db.begin();

      // Re-load process instance
      instance =
          (UpdatableProcessInstance) db.load(ProcessInstanceImpl.class, instance.getInstanceId());

      // if task has previously been saved as draft, step already exists
      if (event.isResumingAction()) {
        step = (UpdatableHistoryStep) instance.getSavedStep(event.getUser().getUserId());
      } else {
        // first create the history step
        try {
          step = (UpdatableHistoryStep) instanceManager.createHistoryStep();
          step.setUserId(event.getUser().getUserId());
          step.setAction(event.getActionName());
          step.setActionDate(event.getActionDate());
          step.setUserRoleName(event.getUserRoleName());
          if (event.getResolvedState() != null) {
            step.setResolvedState(event.getResolvedState().getName());
          }
          step.setActionStatus(0); // To be processed

          // add the new step to the processInstance
          instance.addHistoryStep(step);
        } catch (WorkflowException we) {
          db.rollback();
          WorkflowHub.getErrorManager().saveError(instance, event, null, we);

          // begin transaction
          db.begin();

          // Re-load process instance
          instance = (UpdatableProcessInstance) db
              .load(ProcessInstanceImpl.class, instance.getInstanceId());

          // set errorStatus to true
          instance.setErrorStatus(true);

          // begin transaction
          db.commit();

          throw new WorkflowException("WorkflowEngineThread.process",
              "EX_ERR_PROCESS_ADD_TASKDONE_REQUEST", we);
        }
      }
      // commit
      db.commit();

      // begin transaction
      db.begin();

      // Re-load process instance
      instance =
          (UpdatableProcessInstance) db.load(ProcessInstanceImpl.class, instance.getInstanceId());

      // Do workflow stuff
      try {
        boolean removeInstance = processEvent(instance, step.getId());
        if (removeInstance) {
          // remove data associated to forms and tasks
          ((ProcessInstanceManagerImpl) instanceManager).removeProcessInstanceData(instance);

          // remove instance itself
          db.remove(instance);

          // remove errors
          WorkflowHub.getErrorManager().removeErrorsOfInstance(instance.getInstanceId());
        }
      } catch (WorkflowException we) {
        db.rollback();
        WorkflowHub.getErrorManager().saveError(instance, event, step, we);

        // begin transaction
        db.begin();

        // Re-load process instance
        instance =
            (UpdatableProcessInstance) db.load(ProcessInstanceImpl.class, instance.getInstanceId());

        // set errorStatus to true
        instance.setErrorStatus(true);

        // begin transaction
        db.commit();

        throw new WorkflowException("WorkflowEngineThread.process",
            "workflowEngine.EX_ERR_PROCESS_ADD_TASKDONE_REQUEST", we);
      }

      // commit
      db.commit();
    } catch (PersistenceException pe) {
      WorkflowHub.getErrorManager().saveError(instance, event, step, pe);
      throw new WorkflowException("WorkflowEngineThread.process",
          "workflowEngine.EX_ERR_PROCESS_ADD_TASKDONE_REQUEST", pe);
    } finally {
      WorkflowJDOManager.closeDatabase(db);
    }
  }

  /**
   * Method declaration
   */
  private boolean processEvent(UpdatableProcessInstance instance, String stepId)
      throws WorkflowException {
    // Get the process instance and process model associated to this event
    event.getProcessModel();
    WorkflowHub.getProcessInstanceManager();

    UpdatableHistoryStep step = (UpdatableHistoryStep) instance.getHistoryStep(stepId);

    // only to set the current step of instance to that step
    instance.updateHistoryStep(step);

    // special case : user is resuming creation action - working user must be explicitly removed
    if (event.getResolvedState() == null) {
      instance.removeWorkingUser(event.getUser(), new StateImpl(""), event.getUserRoleName());
    }

    // Remove user from locking users
    instance.unLock(event.getResolvedState(), event.getUser());

    if (WorkflowTools.processAction(instance, event, step, true)) {
      return true;
    }

    // unlock process instance
    instance.unLock();

    return false;
  }

  private final TaskDoneEvent event;
}

// --

/**
 * vient A TaskSaved indicates the workflow engine that a task has been saved
 */
class TaskSavedRequest implements Request {

  /**
   * Constructor declaration
   */
  TaskSavedRequest(TaskSavedEvent event) {
    this.event = event;
  }

  /**
   * Method declaration
   */
  @Override
  public void process() throws WorkflowException {


    // Get the process instance
    UpdatableProcessInstance instance = (UpdatableProcessInstance) event.getProcessInstance();
    ProcessInstanceManager instanceManager = WorkflowHub.getProcessInstanceManager();
    Database db = null;
    UpdatableHistoryStep step = null;

    try {
      // Get database connection
      db = WorkflowJDOManager.getDatabase();

      // begin transaction
      db.begin();

      // Re-load process instance
      instance =
          (UpdatableProcessInstance) db.load(ProcessInstanceImpl.class, instance.getInstanceId());

      // first time saved : history step must be created
      if (event.isFirstTimeSaved()) {
        try {
          step = (UpdatableHistoryStep) instanceManager.createHistoryStep();
          step.setUserId(event.getUser().getUserId());
          step.setAction(event.getActionName());
          step.setActionDate(event.getActionDate());
          step.setUserRoleName(event.getUserRoleName());
          if (event.getResolvedState() != null) {
            step.setResolvedState(event.getResolvedState().getName());
          }
          step.setActionStatus(0); // To be processed

          // add the new step to the processInstance
          instance.addHistoryStep(step);
        } catch (WorkflowException we) {
          db.rollback();
          WorkflowHub.getErrorManager().saveError(instance, event, null, we);

          // begin transaction
          db.begin();

          // Re-load process instance
          instance = (UpdatableProcessInstance) db
              .load(ProcessInstanceImpl.class, instance.getInstanceId());

          // set errorStatus to true
          instance.setErrorStatus(true);

          // begin transaction
          db.commit();

          throw new WorkflowException("WorkflowEngineThread.process",
              "EX_ERR_PROCESS_ADD_TASKSAVED_REQUEST", we);
        }
      } else {
        // reload historystep that has been already created
        step = (UpdatableHistoryStep) instance.getSavedStep(event.getUser().getUserId());
      }

      // commit
      db.commit();

      // begin transaction
      db.begin();

      // Re-load process instance
      instance =
          (UpdatableProcessInstance) db.load(ProcessInstanceImpl.class, instance.getInstanceId());

      // Do workflow stuff
      try {
        processEvent(instance, step.getId(), event.getResolvedState());
      } catch (WorkflowException we) {
        db.rollback();
        WorkflowHub.getErrorManager().saveError(instance, event, step, we);

        // begin transaction
        db.begin();

        // Re-load process instance
        instance =
            (UpdatableProcessInstance) db.load(ProcessInstanceImpl.class, instance.getInstanceId());

        // set errorStatus to true
        instance.setErrorStatus(true);

        // begin transaction
        db.commit();

        throw new WorkflowException("WorkflowEngineThread.process",
            "workflowEngine.EX_ERR_PROCESS_ADD_TASKSAVED_REQUEST", we);
      }

      // commit
      db.commit();
    } catch (PersistenceException pe) {
      WorkflowHub.getErrorManager().saveError(instance, event, step, pe);
      throw new WorkflowException("WorkflowEngineThread.process",
          "workflowEngine.EX_ERR_PROCESS_ADD_TASKSAVED_REQUEST", pe);
    } finally {
      WorkflowJDOManager.closeDatabase(db);
    }
  }

  /**
   * Method declaration
   */
  private boolean processEvent(UpdatableProcessInstance instance, String stepId, State state)
      throws WorkflowException {
    // Get the process instance and process model associated to this event
    event.getProcessModel();
    WorkflowHub.getProcessInstanceManager();

    UpdatableHistoryStep step = (UpdatableHistoryStep) instance.getHistoryStep(stepId);
    instance.updateHistoryStep(step); // only to set the current step of instance to that step

    // Saving data of step and process instance
    if (event.getDataRecord() != null) {
      instance.saveActionRecord(step, event.getDataRecord());
    }

    // Add current user as working user
    state = (state != null) ? state : new StateImpl("");
    instance.addWorkingUser(step.getUser(), state, step.getUserRoleName());

    // unlock process instance
    instance.unLock();

    step.setActionStatus(3); // Saved

    return false;
  }

  private final TaskSavedEvent event;
}

// --

/**
 * A QuestionRequest indicates the workflow engine that a user ask a back to a precedent
 * actor/activity
 */
class QuestionRequest implements Request {

  /**
   * Constructor declaration
   */
  QuestionRequest(QuestionEvent event) {
    this.event = event;
  }

  /**
   * Method declaration
   */
  @Override
  public void process() throws WorkflowException {


    // Get the process instance
    UpdatableProcessInstance instance = (UpdatableProcessInstance) event.getProcessInstance();
    ProcessInstanceManager instanceManager = WorkflowHub.getProcessInstanceManager();
    Database db = null;
    UpdatableHistoryStep step = null;

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
        step.setUserId(event.getUser().getUserId());
        step.setAction("#question#");
        step.setActionDate(event.getActionDate());
        step.setUserRoleName(event.getUserRoleName());
        step.setResolvedState(event.getResolvedState().getName());
        step.setActionStatus(0); // To be processed

        // add the new step to the processInstance
        instance.addHistoryStep(step);
      } catch (WorkflowException we) {
        db.rollback();
        WorkflowHub.getErrorManager().saveError(instance, event, null, we);

        // begin transaction
        db.begin();

        // Re-load process instance
        instance =
            (UpdatableProcessInstance) db.load(ProcessInstanceImpl.class, instance.getInstanceId());

        // set errorStatus to true
        instance.setErrorStatus(true);

        // begin transaction
        db.commit();

        throw new WorkflowException("WorkflowEngineThread.process",
            "workflowEngine.EX_ERR_PROCESS_ADD_TASKDONE_REQUEST", we);
      }

      // commit
      db.commit();

      // begin transaction
      db.begin();

      // Re-load process instance
      instance =
          (UpdatableProcessInstance) db.load(ProcessInstanceImpl.class, instance.getInstanceId());

      // Do workflow stuff
      try {
        processEvent(instance, step.getId());
      } catch (WorkflowException we) {
        db.rollback();

        // begin transaction
        db.begin();

        // Re-load process instance
        instance =
            (UpdatableProcessInstance) db.load(ProcessInstanceImpl.class, instance.getInstanceId());

        // set errorStatus to true
        instance.setErrorStatus(true);

        // begin transaction
        db.commit();

        throw new WorkflowException("WorkflowEngineThread.process",
            "workflowEngine.EX_ERR_PROCESS_ADD_TASKDONE_REQUEST", we);
      }

      // commit
      db.commit();
    } catch (PersistenceException pe) {
      WorkflowHub.getErrorManager().saveError(instance, event, step, pe);
      throw new WorkflowException("WorkflowEngineThread.process",
          "workflowEngine.EX_ERR_PROCESS_ADD_TASKDONE_REQUEST", pe);
    } finally {
      WorkflowJDOManager.closeDatabase(db);
    }
  }

  /**
   * Method declaration
   */
  private void processEvent(UpdatableProcessInstance instance, String stepId)
      throws WorkflowException {
    // Get the process instance manager
    WorkflowHub.getProcessInstanceManager();

    // Create a history step for the question
    UpdatableHistoryStep step = (UpdatableHistoryStep) instance.getHistoryStep(stepId);
    instance.updateHistoryStep(step); // only to set the current step of
    // instance to that step

    // add the question
    String question;
    try {
      question = (String) event.getDataRecord().getField("Content").getObjectValue();
    } catch (FormException fe) {
      throw new WorkflowException("WorkflowEngineThread.process", "workflowEngine.EXP_UNKNOWN_ITEM",
          fe);
    }
    State state = instance.addQuestion(question, event.getStepId(), event.getResolvedState(), event.
        getUser());

    // add the state that is discussed in the list of active states
    instance.addActiveState(state);

    // change the action status of the step
    step.setActionStatus(1); // Processed
    instance.updateHistoryStep(step);

    // get the last participant for the discussed state
    Participant participant = instance.getParticipant(state.getName());

    // Get the task manager
    TaskManager taskManager = WorkflowHub.getTaskManager();

    // Assign task to this participant
    Task task = taskManager.createTask(participant, instance);
    taskManager.assignTask(task, participant.getUser());

    // Declare this user as a working user in instance
    instance.addWorkingUser(participant.getUser(), state, participant.getUserRoleName());

    // change the action status of the step
    step.setActionStatus(2); // Affectations done
    instance.updateHistoryStep(step);

    // unlock process instance
    instance.unLock();
  }

  private final QuestionEvent event;
}

/**
 * A ResponseRequest indicates the workflow engine that a user answer a question to an user who had
 * requested a back to a precedent actor/activity
 */
class ResponseRequest implements Request {

  /**
   * Constructor declaration
   */
  public ResponseRequest(ResponseEvent event) {
    this.event = event;
  }

  /**
   * Method declaration
   */
  public void process() throws WorkflowException {


    // Get the process instance
    UpdatableProcessInstance instance = (UpdatableProcessInstance) event.getProcessInstance();
    ProcessInstanceManager instanceManager = WorkflowHub.getProcessInstanceManager();
    Database db = null;
    UpdatableHistoryStep step = null;

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
        step.setUserId(event.getUser().getUserId());
        step.setAction("#response#");
        step.setActionDate(event.getActionDate());
        step.setUserRoleName(event.getUserRoleName());
        step.setResolvedState(event.getResolvedState().getName());
        step.setActionStatus(0); // To be processed

        // add the new step to the processInstance
        instance.addHistoryStep(step);
      } catch (WorkflowException we) {
        db.rollback();
        WorkflowHub.getErrorManager().saveError(instance, event, null, we);

        // begin transaction
        db.begin();

        // Re-load process instance
        instance =
            (UpdatableProcessInstance) db.load(ProcessInstanceImpl.class, instance.getInstanceId());

        // set errorStatus to true
        instance.setErrorStatus(true);

        // begin transaction
        db.commit();

        throw new WorkflowException("WorkflowEngineThread.process",
            "workflowEngine.EX_ERR_PROCESS_ADD_TASKDONE_REQUEST", we);
      }

      // commit
      db.commit();

      // begin transaction
      db.begin();

      // Re-load process instance
      instance =
          (UpdatableProcessInstance) db.load(ProcessInstanceImpl.class, instance.getInstanceId());

      // Do workflow stuff
      try {
        processEvent(instance, step.getId());
      } catch (WorkflowException we) {
        db.rollback();

        // begin transaction
        db.begin();

        // Re-load process instance
        instance =
            (UpdatableProcessInstance) db.load(ProcessInstanceImpl.class, instance.getInstanceId());

        // set errorStatus to true
        instance.setErrorStatus(true);

        // begin transaction
        db.commit();

        throw new WorkflowException("WorkflowEngineThread.process",
            "workflowEngine.EX_ERR_PROCESS_ADD_TASKDONE_REQUEST", we);
      }

      // commit
      db.commit();
    } catch (PersistenceException pe) {
      WorkflowHub.getErrorManager().saveError(instance, event, step, pe);
      throw new WorkflowException("WorkflowEngineThread.process",
          "workflowEngine.EX_ERR_PROCESS_ADD_TASKDONE_REQUEST", pe);
    } finally {
      WorkflowJDOManager.closeDatabase(db);
    }
  }

  /**
   * Method declaration
   */
  private void processEvent(UpdatableProcessInstance instance, String stepId)
      throws WorkflowException {
    // Get the process instance and process model associated to this event
    WorkflowHub.getProcessInstanceManager();

    // get the history step
    UpdatableHistoryStep step = (UpdatableHistoryStep) instance.getHistoryStep(stepId);
    instance.updateHistoryStep(step); // only to set the current step of
    // instance to that step

    // add the answer
    String answer = null;
    try {
      answer = (String) event.getDataRecord().getField("Content").getObjectValue();
    } catch (FormException fe) {
      throw new WorkflowException("WorkflowEngineThread.process", "workflowEngine.EXP_UNKNOWN_ITEM",
          fe);
    }
    State state = instance.answerQuestion(answer, event.getQuestionId());

    // remove the state that was discussed from the list of active states
    instance.removeActiveState(event.getResolvedState());

    // change the action status of the step
    step.setActionStatus(1); // Processed
    instance.updateHistoryStep(step);

    // get the last participant for the discussed state
    Participant participant = instance.getParticipant(state.getName());

    // Get the task manager
    TaskManager taskManager = WorkflowHub.getTaskManager();

    // Unassign task to this participant
    Task task = taskManager.createTask(participant, instance);
    taskManager.unAssignTask(task);

    // Remove this user of working user list
    instance.removeWorkingUser(participant.getUser(), state, participant.getUserRoleName());

    // change the action status of the step
    step.setActionStatus(2); // Affectations done
    instance.updateHistoryStep(step);

    // unlock process instance
    instance.unLock();
  }

  private final ResponseEvent event;
}

/**
 * A TimeoutRequest indicates the workflow engine that an instance is in an active state since a
 * too
 * long period
 */
class TimeoutRequest implements Request {

  /**
   * Constructor declaration
   */
  TimeoutRequest(TimeoutEvent event) {
    this.event = event;
  }

  /**
   * Method declaration
   */
  @Override
  public void process() throws WorkflowException {


    // Get the process instance
    UpdatableProcessInstance instance = (UpdatableProcessInstance) event.getProcessInstance();
    ProcessInstanceManager instanceManager = WorkflowHub.getProcessInstanceManager();
    Database db = null;
    UpdatableHistoryStep step = null;

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
        step.setAction(event.getActionName());
        step.setActionDate(new Date());
        step.setUserRoleName("supervisor");
        step.setResolvedState(event.getResolvedState().getName());
        step.setActionStatus(0); // To be processed

        // add the new step to the processInstance
        instance.addHistoryStep(step);
      } catch (WorkflowException we) {
        db.rollback();
        throw new WorkflowException("WorkflowEngineThread.process",
            "workflowEngine.EX_ERR_PROCESS_ADD_TASKDONE_REQUEST", we);
      }

      // commit
      db.commit();

      // begin transaction
      db.begin();

      // Re-load process instance
      instance =
          (UpdatableProcessInstance) db.load(ProcessInstanceImpl.class, instance.getInstanceId());

      // Do workflow stuff
      try {
        boolean removeInstance = processEvent(instance, step.getId());
        if (removeInstance) {
          // remove data associated to forms and tasks
          ((ProcessInstanceManagerImpl) instanceManager).removeProcessInstanceData(instance);

          // remove instance itself
          db.remove(instance);

          // remove errors
          WorkflowHub.getErrorManager().removeErrorsOfInstance(instance.getInstanceId());
        }
      } catch (WorkflowException we) {
        db.rollback();

        // begin transaction
        db.begin();

        // Re-load process instance
        instance =
            (UpdatableProcessInstance) db.load(ProcessInstanceImpl.class, instance.getInstanceId());

        // set errorStatus to true
        instance.setErrorStatus(true);

        // begin transaction
        db.commit();

        throw new WorkflowException("WorkflowEngineThread.process",
            "workflowEngine.EX_ERR_PROCESS_ADD_TIMEOUT_REQUEST", we);
      }

      // commit
      db.commit();
    } catch (PersistenceException pe) {
      WorkflowHub.getErrorManager().saveError(instance, event, step, pe);
      throw new WorkflowException("WorkflowEngineThread.process",
          "workflowEngine.EX_ERR_PROCESS_ADD_TIMEOUT_REQUEST", pe);
    } finally {
      WorkflowJDOManager.closeDatabase(db);
    }
  }

  /**
   * Method declaration
   */
  private boolean processEvent(UpdatableProcessInstance instance, String stepId)
      throws WorkflowException {
    // Get the process instance and process model associated to this event
    WorkflowHub.getProcessInstanceManager();

    // get the history step
    UpdatableHistoryStep step = (UpdatableHistoryStep) instance.getHistoryStep(stepId);
    instance.updateHistoryStep(step); // only to set the current step of
    // instance to that step

    // set timeout status on given state
    instance.addTimeout(event.getResolvedState());

    // process Action
    return WorkflowTools.processAction(instance, event, step, false);
  }

  private final TimeoutEvent event;
}

/**
 * A TimeoutRequest indicates the workflow engine that an instance is in an active state since a
 * too long period
 */
class WorkflowTools {

  /**
   * Compute the new active states and updates affections
   * @param event the task done event
   */
  public static boolean processAction(UpdatableProcessInstance instance, GenericEvent event,
      UpdatableHistoryStep step, boolean unactivateResolvedState) throws WorkflowException {
    // Get the process model
    ProcessModel model = instance.getProcessModel();

    // Get the task manager
    TaskManager taskManager = WorkflowHub.getTaskManager();

    // Get the user manager
    UserManager userManager = WorkflowHub.getUserManager();

    // All active states must be eligible
    String[] states = instance.getActiveStates();
    String resolvedStateName = (event.getResolvedState() == null) ? "" : event.getResolvedState().
        getName();
    boolean backStatus =
        (event.getResolvedState() != null) && instance.isStateInBackStatus(resolvedStateName);
    Hashtable<String, String> oldActiveStates = new Hashtable<>();
    Hashtable<String, String> eligibleStates = new Hashtable<>();

    // Check for actions "redone"
    for (int i = 0; backStatus && i < states.length; i++) {
      if (states[i].equals(resolvedStateName)) {
        instance.reDoState(resolvedStateName, event.getActionDate());
      }
    }

    // Check for questions with no response sent from the resolved state
    checkQuestions(instance, resolvedStateName);

    // Compute eligibility for states
    states = instance.getActiveStates();
    for (final String state : states) {
      eligibleStates.put(state, state);
      oldActiveStates.put(state, state);
    }

    Consequence consequence = null;
    try {
      // Saving data of step and process instance
      if (event.getDataRecord() != null) {
        instance.saveActionRecord(step, event.getDataRecord());
      }

      // removes eligibility of resolved state
      // and removes the resolved state from active state list
      if (unactivateResolvedState && event.getResolvedState() != null) {
        eligibleStates.remove(event.getResolvedState().getName());
      }

      // remove eventual timeOut flag
      /*
       * if (event.getResolvedState() != null) { instance.removeTimeout(event.getResolvedState()); }
       */

      // Retrieves action's consequences
      Action action = model.getAction(event.getActionName());
      Consequences consequences = action.getConsequences();

      // Find first consequence according to comparisons
      Iterator<Consequence> iConsequences = consequences.getConsequenceList().iterator();

      boolean verified = false;
      while (!verified && iConsequences.hasNext()) {
        consequence = iConsequences.next();
        if (consequence.getItem() != null) {
          Field fieldToCompare = instance.getFolder().getField(consequence.getItem());
          if ((fieldToCompare != null) && (fieldToCompare.getStringValue() != null)) {
            verified = consequence.isVerified(fieldToCompare.getStringValue());
          }
        } else {
          verified = true;
        }
      }

      SilverLogger.getLogger(WorkflowTools.class)
          .info("Process action {0}: item = {1}, operator = {2}, value = {3}",
              event.getActionName(), consequence.getItem(), consequence.getOperator(),
              consequence.getValue());

      // if no consequence is verified, then last one will be used
      if (consequence.getKill()) {
        for (final String state : states) {
          removeAffectations(instance, event, model.getState(state));
        }

        // process external actions
        processTriggers(consequence, instance, event);

        return true;
      }
      State[] targetStates = consequence.getTargetStates();
      State[] unsetStates = consequence.getUnsetStates();

      // for each target state, set eligibility to true
      for (final State targetState : targetStates) {
        if (targetState != null) {
          String name = targetState.getName();
          eligibleStates.put(name, name);
        }
      }

      // for each unset state, set eligibility to false
      for (final State unsetState : unsetStates) {
        if (unsetState != null) {
          String name = unsetState.getName();
          eligibleStates.remove(name);
        }
      }

      // notify user
      List<QualifiedUsers> notifiedUsersList = consequence.getNotifiedUsers();
      for (QualifiedUsers notifiedUsers : notifiedUsersList) {
        Actor[] actors = instance.getActors(notifiedUsers, null);
        Task[] tasks = taskManager.createTasks(actors, instance);
        String message;

        for (int i = 0; i < actors.length; i++) {
          message = notifiedUsers.getMessage();
          if (message == null || message.length() == 0) {
            message = action.getDescription("", "");
          }

          // check if sender has been hardcoded in the model
          String senderId = notifiedUsers.getSenderId();
          User forcedUser = null;
          if (senderId != null) {
            try {
              forcedUser = userManager.getUser(senderId);
            } catch (WorkflowException we) {
              SilverLogger.getLogger(WorkflowTools.class)
                  .error("Error while processing {0}: impossible to find the sender with id {1}",
                      event.getActionName(), senderId);
            }
          }

          User sender = (forcedUser == null) ? event.getUser() : forcedUser;
          taskManager.notifyActor(tasks[i], sender, actors[i].getUser(), message);
        }
      }
    } catch (Exception e) {
      // change the action status of the step
      step.setActionStatus(-1); // Process Failed
      instance.updateHistoryStep(step);
      throw new WorkflowException("WorkflowEngineThread.process",
          "workflowEngine.EX_ERR_PROCESS_EVENT", e);
    }

    // change the action status of the step
    step.setActionStatus(1); // Processed
    instance.updateHistoryStep(step);

    // Compute states and affectations
    computeStates(instance, event, eligibleStates, oldActiveStates);

    // change the action status of the step
    step.setActionStatus(2); // Affectations done
    instance.updateHistoryStep(step);

    // Process external actions
    processTriggers(consequence, instance, event);

    return false;
  }

  private static void processTriggers(Consequence consequence, UpdatableProcessInstance instance,
      GenericEvent event) {
    if (consequence != null) {
      Iterator<Trigger> triggers = consequence.getTriggers().iterateTrigger();
      while (triggers.hasNext()) {
        Trigger trigger = triggers.next();
        if (trigger != null) {
          try {
            ExternalAction externalAction = (ExternalAction) Class.forName(trigger.getClassName()).
                newInstance();
            externalAction.setProcessInstance(instance);
            externalAction.setEvent(event);
            externalAction.setTrigger(trigger);
            externalAction.execute();
          } catch (Exception e) {
            SilverLogger.getLogger(WorkflowTools.class)
                .error("Error while processing triggers: action = {0}, trigger = {1}",
                    new String[] {event.getActionName(), trigger.getName()}, e);
          }
        }
      }
    }
  }

  /**
   * Compute the new active states and updates affections
   * @param event the task done event
   */
  public static void computeStates(UpdatableProcessInstance instance, GenericEvent event,
      Hashtable eligibleStates, Hashtable oldActiveStates) throws WorkflowException {
    // Get the process model associated to this event
    ProcessModel model = instance.getProcessModel();

    // Get all the states defined in the abstract model
    State[] states = model.getStates();

    // Test if each state must be activated or not
    for (final State state : states) {
      boolean eligible = (eligibleStates.containsKey(state.getName()));
      // xoxox a completer : gestion de la liste des pre-requis

      if (eligible) {
        instance.addActiveState(state);
        if (oldActiveStates.contains(state.getName())) {
          // State is already affected (it's a loop) ==> remove old tasks and
          // add new ones
          removeAffectations(instance, event, state);
        }
        computeAffectations(instance, event, state);
      } else {
        if (oldActiveStates.contains(state.getName())) {
          // State was affected ==> unassign tasks and remove Active state
          instance.removeActiveState(state);
          removeAffectations(instance, event, state);
        }
      }
    }
  }

  /**
   * Compute the new affectations for a given state
   * @param event the event
   */
  public static void computeAffectations(UpdatableProcessInstance instance, GenericEvent event,
      State state) throws WorkflowException {
    // Get the process model associated to this event
    instance.getProcessModel();

    // Get the task manager
    TaskManager taskManager = WorkflowHub.getTaskManager();

    // Get the working users
    QualifiedUsers wkUsers = state.getWorkingUsers();
    Actor[] actors = instance.getActors(wkUsers, state);

    // Assign tasks to these working users (except for automatic event, in this cas no user for this
    // event)
    if (event.getUser() != null) {
      Task[] tasks = taskManager.createTasks(actors, instance);
      for (final Task task : tasks) {
        taskManager.assignTask(task, event.getUser());
      }
    }

    // Declare these working users in instance
    for (final Actor actor : actors) {
      instance.addWorkingUser(actor, state);
    }

    // Get the interested users
    QualifiedUsers intUsers = state.getInterestedUsers();
    actors = instance.getActors(intUsers, state);

    // Declare these interested users in instance
    for (final Actor actor : actors) {
      instance.addInterestedUser(actor, state);
    }
  }

  /**
   * Remove the old affectations for this state and unassign tasks
   * @param event the event
   */
  public static void removeAffectations(UpdatableProcessInstance instance, GenericEvent event,
      State state) throws WorkflowException {
    // Get process model associated to this event
    instance.getProcessModel();

    // Get the task manager
    TaskManager taskManager = WorkflowHub.getTaskManager();

    // Get the working users
    Actor[] actors = instance.getWorkingUsers(state.getName());

    // Unassign tasks to these working users
    Task[] tasks = taskManager.createTasks(actors, instance);
    for (final Task task : tasks) {
      taskManager.unAssignTask(task);
    }

    // removes interested users and working users for resolved state
    instance.removeInterestedUsers(state);
    instance.removeWorkingUsers(state);
  }

  /**
   * Cancel pending questions sent from the given state (cascading is done to the questions from
   * questions and so...)
   * @param instance Process instance
   * @param resolvedStateName state name
   */
  static public void checkQuestions(UpdatableProcessInstance instance, String resolvedStateName)
      throws WorkflowException {
    Question[] questions = instance.getSentQuestions(resolvedStateName);

    for (int i = 0; questions != null && i < questions.length; i++) {
      instance.cancelQuestion(questions[i]);
    }
  }
}