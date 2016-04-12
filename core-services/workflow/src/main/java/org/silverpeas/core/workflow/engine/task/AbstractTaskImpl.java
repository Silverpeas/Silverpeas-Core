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

import org.silverpeas.core.contribution.content.form.DataRecord;
import org.silverpeas.core.workflow.api.event.QuestionEvent;
import org.silverpeas.core.workflow.api.event.ResponseEvent;
import org.silverpeas.core.workflow.api.event.TaskDoneEvent;
import org.silverpeas.core.workflow.api.event.TaskSavedEvent;
import org.silverpeas.core.workflow.api.model.ProcessModel;
import org.silverpeas.core.workflow.api.task.Task;
import org.silverpeas.core.workflow.api.user.User;
import org.silverpeas.core.workflow.engine.event.QuestionEventImpl;
import org.silverpeas.core.workflow.engine.event.ResponseEventImpl;
import org.silverpeas.core.workflow.engine.event.TaskDoneEventImpl;
import org.silverpeas.core.workflow.engine.event.TaskSavedEventImpl;

/**
 * AbstractTaskImpl implements methods shared by TaskImpl ans CreateTaskImpl.
 */
public abstract class AbstractTaskImpl implements Task {

  /**
   * Builds a TaskImpl.
   */
  public AbstractTaskImpl(User user, String roleName, ProcessModel processModel) {
    this.user = user;
    this.roleName = roleName;
    this.processModel = processModel;
  }

  public AbstractTaskImpl(User user, String roleName, String groupId, ProcessModel processModel) {
    this.user = user;
    this.roleName = roleName;
    this.groupId = groupId;
    this.processModel = processModel;
  }

  /**
   * Returns the user.
   */
  @Override
  public User getUser() {
    return user;
  }

  /**
   * Returns the name of the role which gived the responsability of this task to the user.
   */
  @Override
  public String getUserRoleName() {
    return roleName;
  }

  @Override
  public String getGroupId() {
    return groupId;
  }

  /**
   * Returns the process model.
   */
  @Override
  public ProcessModel getProcessModel() {
    return processModel;
  }

  /**
   * Builds a TaskDoneEvent from this Task.
   */
  @Override
  public TaskDoneEvent buildTaskDoneEvent(String actionName, DataRecord data) {
    return new TaskDoneEventImpl(this, actionName, data);
  }

  /**
   * Builds a TaskSavedEvent from this Task.
   */
  @Override
  public TaskSavedEvent buildTaskSavedEvent(String actionName, DataRecord data) {
    return new TaskSavedEventImpl(this, actionName, data);
  }

  /**
   * Builds a QuestionEvent from this Task.
   */
  @Override
  public QuestionEvent buildQuestionEvent(String stepId, DataRecord data) {
    return new QuestionEventImpl(this, stepId, data);
  }

  /**
   * Builds a ResponseEvent from this Task.
   */
  @Override
  public ResponseEvent buildResponseEvent(String questionId, DataRecord data) {
    return new ResponseEventImpl(this, questionId, data);
  }

  /*
   * Internal fields
   */
  private User user = null;
  private String groupId = null;
  private String roleName = null;
  private ProcessModel processModel = null;
}
