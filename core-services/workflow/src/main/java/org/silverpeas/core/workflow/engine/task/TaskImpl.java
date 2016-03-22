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

import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.instance.HistoryStep;
import org.silverpeas.core.workflow.api.instance.ProcessInstance;
import org.silverpeas.core.workflow.api.instance.Question;
import org.silverpeas.core.workflow.api.model.Action;
import org.silverpeas.core.workflow.api.model.State;
import org.silverpeas.core.workflow.api.user.User;

/**
 * A task object is an activity description built by the workflow engine and sent via the
 * taskManager to an external system which will notify the end user and manage the task realisation.
 * Task objects will be created by the workflow engine when a new task is assigned to a user. Task
 * objects will be created too for the ProcessManager GUI which will be used by the user to do the
 * assigned activity.
 */
public class TaskImpl extends AbstractTaskImpl {

  /**
   * Builds a TaskImpl.
   */
  public TaskImpl(User user, String roleName, ProcessInstance processInstance,
      State state) throws WorkflowException {
    super(user, roleName, processInstance.getProcessModel());
    this.processInstance = processInstance;
    this.state = state;
    this.backSteps = null;
  }

  public TaskImpl(User user, String roleName, String groupId, ProcessInstance processInstance,
      State state) throws WorkflowException {
    super(user, roleName, groupId, processInstance.getProcessModel());
    this.processInstance = processInstance;
    this.state = state;
    this.backSteps = null;
  }

  @Override
  public void setProcessInstance(ProcessInstance processInstance) {
    this.processInstance = processInstance;
  }

  /**
   * Builds a TaskImpl.
   */
  public TaskImpl(User user, String roleName, ProcessInstance processInstance,
      State state, HistoryStep[] backSteps, Question[] sentQuestions,
      Question[] relevantQuestions, Question[] pendingQuestions)
      throws WorkflowException {
    super(user, roleName, processInstance.getProcessModel());
    this.processInstance = processInstance;
    this.state = state;
    this.backSteps = (backSteps != null ? backSteps.clone() : null);
    this.sentQuestions = (sentQuestions != null ? sentQuestions.clone() : null);
    this.relevantQuestions = (relevantQuestions != null ? relevantQuestions.clone() : null);
    this.pendingQuestions = (pendingQuestions != null ? pendingQuestions.clone() : null);
  }

  /**
   * Returns the process instance.
   */
  @Override
  public ProcessInstance getProcessInstance() {
    return processInstance;
  }

  /**
   * Returns the state to be resolved by the user.
   */
  @Override
  public State getState() {
    return state;
  }

  /**
   * Returns the history steps that user can discussed (ask a question to the actor of that step).
   */
  @Override
  public HistoryStep[] getBackSteps() {
    return backSteps;
  }

  /**
   * Returns the questions that must be answered
   */
  @Override
  public Question[] getPendingQuestions() {
    return pendingQuestions;
  }

  /**
   * Returns the (non onsolete) questions that have been answered
   */
  @Override
  public Question[] getRelevantQuestions() {
    return relevantQuestions;
  }

  /**
   * Returns the question that have been asked and are waiting for a response
   */
  @Override
  public Question[] getSentQuestions() {
    return sentQuestions;
  }

  /**
   * Returns the action names list from which the user must choose to resolve the activity.
   */
  @Override
  public String[] getActionNames() {
    Action[] actions = state.getAllowedActions();
    String[] actionNames = new String[actions.length];

    for (int i = 0; i < actions.length; i++) {
      actionNames[i] = actions[i].getName();
    }

    return actionNames;
  }

  /*
   * Internal fields
   */
  private ProcessInstance processInstance = null;
  private State state = null;
  private HistoryStep[] backSteps = null;
  private Question[] pendingQuestions = null;
  private Question[] relevantQuestions = null;
  private Question[] sentQuestions = null;
}
