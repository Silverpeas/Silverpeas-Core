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

package org.silverpeas.core.workflow.api.task;

import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.workflow.api.event.QuestionEvent;
import org.silverpeas.core.workflow.api.event.ResponseEvent;
import org.silverpeas.core.workflow.api.event.TaskDoneEvent;
import org.silverpeas.core.workflow.api.event.TaskSavedEvent;
import org.silverpeas.core.workflow.api.instance.HistoryStep;
import org.silverpeas.core.workflow.api.instance.ProcessInstance;
import org.silverpeas.core.workflow.api.instance.Question;
import org.silverpeas.core.workflow.api.model.ProcessModel;
import org.silverpeas.core.workflow.api.model.State;
import org.silverpeas.core.workflow.api.user.User;

/**
 * A task object is an activity description built by the workflow engine and sent via the
 * taskManager to an external system which will notify the end user and manage the task realisation.
 * Task objects will be created by the workflow engine when a new task is assigned to a user. Task
 * objects will be created too for the ProcessManager GUI which will be used by the user to do the
 * assigned activity.
 */
public interface Task {
  /**
   * Returns the actor.
   */
  public User getUser();

  /**
   * Returns the name of the role which gived the responsability of this task to the user.
   */
  public String getUserRoleName();

  public String getGroupId();

  /**
   * Returns the process model (peas). The id of this workflow internal information must be stored
   * by the external system to be sent to the workflow engine when the activity is done.
   */
  public ProcessModel getProcessModel();

  /**
   * Returns the process instance. The id of this workflow internal information must be stored by
   * the external system to be sent to the workflow engine when the activity is done.
   */
  public ProcessInstance getProcessInstance();

  /**
   * Returns the state/activity to be resolved by the user. The name of this workflow internal
   * information must be stored by the external system to be sent to the workflow engine when the
   * activity is done.
   */
  public State getState();

  /**
   * Returns the history steps that user can discussed (ask a question to the actor of that step).
   */
  public HistoryStep[] getBackSteps();

  /**
   * Returns the question that must be answered
   */
  public Question[] getPendingQuestions();

  /**
   * Returns the (non onsolete) questions that have been answered
   */
  public Question[] getRelevantQuestions();

  /**
   * Returns the question that have been asked and are waiting for a response
   */
  public Question[] getSentQuestions();

  /**
   * Returns the action names list from which the user must choose to resolve the activity.
   */
  public String[] getActionNames();

  /**
   * When this Task is done, builds a TaskDoneEvent giving the choosed action name and the filled
   * form.
   */
  public TaskDoneEvent buildTaskDoneEvent(String actionName, DataRecord data);

  /**
   * When this Task is saved, builds a TaskSavedEvent giving the choosed action name and the filled
   * form.
   */
  public TaskSavedEvent buildTaskSavedEvent(String actionName, DataRecord data);

  /**
   * When this Question is asked for a task, builds a QuestionEvent giving the choosed step that
   * must give the answer.
   */
  public QuestionEvent buildQuestionEvent(String stepId, DataRecord data);

  /**
   * When this Response is answer for a task, builds a ResponseEvent giving the question id that
   * must give the answer.
   */
  public ResponseEvent buildResponseEvent(String questionId, DataRecord data);

  /**
   * Set process instance associated with task
   */
  public void setProcessInstance(ProcessInstance currentProcessInstance);
}
