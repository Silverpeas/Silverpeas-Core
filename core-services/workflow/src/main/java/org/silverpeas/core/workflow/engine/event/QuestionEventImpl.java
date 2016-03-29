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

package org.silverpeas.core.workflow.engine.event;

import java.util.Date;
import org.silverpeas.core.workflow.api.task.Task;
import org.silverpeas.core.workflow.api.event.QuestionEvent;
import org.silverpeas.core.workflow.api.user.User;
import org.silverpeas.core.workflow.api.instance.ProcessInstance;
import org.silverpeas.core.workflow.api.model.ProcessModel;
import org.silverpeas.core.workflow.api.model.State;
import org.silverpeas.core.contribution.content.form.DataRecord;

/**
 * A QuestionEvent object is the description of a question to a precedent actor. Those descriptions
 * are sent to the workflow engine by the workflow tools when the user asked a question in process
 * instance
 */
public class QuestionEventImpl implements QuestionEvent {
  /**
   * A QuestionEventImpl is built from a resolved task, a choosen target state and a filled form.
   */
  public QuestionEventImpl(Task resolvedTask, String stepId, DataRecord data) {
    this.user = resolvedTask.getUser();
    this.processModel = resolvedTask.getProcessModel();
    this.processInstance = resolvedTask.getProcessInstance();
    this.resolvedState = resolvedTask.getState();
    this.actionName = "#question#";
    this.actionDate = new Date();
    this.userRoleName = resolvedTask.getUserRoleName();
    this.data = data;
    this.stepId = stepId;
  }

  /**
   * Returns the actor.
   */
  public User getUser() {
    return user;
  }

  /**
   * Returns the process instance. Returns null when the task is an instance creation.
   */
  public ProcessInstance getProcessInstance() {
    return processInstance;
  }

  /**
   * Set the process instance (when created).
   */
  public void setProcessInstance(ProcessInstance processInstance) {
    this.processInstance = processInstance;
  }

  /**
   * Returns the process model (peas). Must be not null when the task is an instance creation.
   */
  public ProcessModel getProcessModel() {
    return processModel;
  }

  /**
   * Returns the state/activity resolved by the user.
   */
  public State getResolvedState() {
    return resolvedState;
  }

  /**
   * Returns the name of the action chosen to resolve the activity.
   */
  public String getActionName() {
    return actionName;
  }

  /**
   * Returns the action date.
   */
  public Date getActionDate() {
    return actionDate;
  }

  /**
   * Returns the data filled when the action was processed.
   */
  public DataRecord getDataRecord() {
    return data;
  }

  /**
   * Returns the role name of the actor
   */
  public String getUserRoleName() {
    return userRoleName;
  }

  /**
   * Returns the discussed step id
   */
  public String getStepId() {
    return stepId;
  }

  /*
   * Internal states.
   */
  private User user = null;
  private ProcessInstance processInstance = null;
  private ProcessModel processModel = null;
  private Date actionDate = null;
  private String actionName = null;
  private String userRoleName = null;
  private State resolvedState = null;
  private DataRecord data = null;
  private String stepId;
}
