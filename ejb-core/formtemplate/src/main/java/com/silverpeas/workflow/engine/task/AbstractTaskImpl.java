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
 * FLOSS exception.  You should have recieved a copy of the text describing
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

import com.silverpeas.form.DataRecord;
import com.silverpeas.workflow.api.WorkflowException;
import com.silverpeas.workflow.api.event.QuestionEvent;
import com.silverpeas.workflow.api.event.ResponseEvent;
import com.silverpeas.workflow.api.event.TaskDoneEvent;
import com.silverpeas.workflow.api.model.ProcessModel;
import com.silverpeas.workflow.api.task.Task;
import com.silverpeas.workflow.api.user.User;
import com.silverpeas.workflow.engine.event.QuestionEventImpl;
import com.silverpeas.workflow.engine.event.ResponseEventImpl;
import com.silverpeas.workflow.engine.event.TaskDoneEventImpl;

/**
 * AbstractTaskImpl implements methods shared by TaskImpl ans CreateTaskImpl.
 */
public abstract class AbstractTaskImpl implements Task {
  /**
   * Builds a TaskImpl.
   */
  public AbstractTaskImpl(User user, String roleName, ProcessModel processModel)
      throws WorkflowException {
    this.user = user;
    this.roleName = roleName;
    this.processModel = processModel;
  }

  /**
   * Returns the user.
   */
  public User getUser() {
    return user;
  }

  /**
   * Returns the name of the role which gived the responsability of this task to the user.
   */
  public String getUserRoleName() {
    return roleName;
  }

  /**
   * Returns the process model.
   */
  public ProcessModel getProcessModel() {
    return processModel;
  }

  /**
   * Builds a TaskDoneEvent from this Task.
   */
  public TaskDoneEvent buildTaskDoneEvent(String actionName, DataRecord data) {
    return (TaskDoneEvent) new TaskDoneEventImpl(this, actionName, data);
  }

  /**
   * Builds a QuestionEvent from this Task.
   */
  public QuestionEvent buildQuestionEvent(String stepId, DataRecord data) {
    return (QuestionEvent) new QuestionEventImpl(this, stepId, data);
  }

  /**
   * Builds a ResponseEvent from this Task.
   */
  public ResponseEvent buildResponseEvent(String questionId, DataRecord data) {
    return (ResponseEvent) new ResponseEventImpl(this, questionId, data);
  }

  /*
   * Internal fields
   */
  private User user = null;
  private String roleName = null;
  private ProcessModel processModel = null;
}
