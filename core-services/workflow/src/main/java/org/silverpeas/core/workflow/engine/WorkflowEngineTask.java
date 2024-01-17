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

import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.annotation.Technical;
import org.silverpeas.core.thread.task.AbstractRequestTask;
import org.silverpeas.core.thread.task.RequestTaskManager;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.workflow.api.event.QuestionEvent;
import org.silverpeas.core.workflow.api.event.ResponseEvent;
import org.silverpeas.core.workflow.api.event.TaskDoneEvent;
import org.silverpeas.core.workflow.api.event.TaskSavedEvent;
import org.silverpeas.core.workflow.api.event.TimeoutEvent;

/**
 * @author ebonnet
 */
@Technical
@Bean
public class WorkflowEngineTask extends AbstractRequestTask<AbstractRequestTask.ProcessContext> {

  protected WorkflowEngineTask() {
    super();
  }

  /**
   * Add a request 'TaskDoneEvent'
   */
  public static void addTaskDoneRequest(TaskDoneEvent event) {
    TaskDoneRequest request = TaskDoneRequest.get(event);
    SilverLogger.getLogger(WorkflowEngineTask.class)
        .info("Add task done request: {0}", request.toString());
    push(request);
  }

  /**
   * Add a request 'TaskSavedEvent'
   */
  public static void addTaskSavedRequest(TaskSavedEvent event) {
    TaskSavedRequest request = TaskSavedRequest.get(event);
    SilverLogger.getLogger(WorkflowEngineTask.class)
        .info("Add task saved request: {0}", request.toString());
    push(request);
  }

  /**
   * Add a request 'QuestionEvent'
   */
  public static void addQuestionRequest(QuestionEvent event) {
    QuestionRequest request = QuestionRequest.get(event);
    SilverLogger.getLogger(WorkflowEngineTask.class)
        .info("Add question request: {0}", request.toString());
    push(request);
  }

  /**
   * Add a request 'ResponseEvent'
   */
  public static void addResponseRequest(ResponseEvent event) {
    ResponseRequest request = ResponseRequest.get(event);
    SilverLogger.getLogger(WorkflowEngineTask.class)
        .info("Add response request: {0}", request.toString());
    push(request);
  }

  /**
   * Add a request 'TimeoutEvent'
   */
  public static void addTimeoutRequest(TimeoutEvent event) {
    TimeoutRequest request = TimeoutRequest.get(event);
    SilverLogger.getLogger(WorkflowEngineTask.class)
        .info("Add timeout request: {0}", request.toString());
    push(request);
  }

  private static void push(AbstractRequest request) {
    RequestTaskManager.get().push(WorkflowEngineTask.class, request);
  }
}