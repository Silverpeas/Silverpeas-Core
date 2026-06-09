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

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.workflow.api.event.*;
import org.silverpeas.kernel.annotation.Technical;
import org.silverpeas.core.thread.task.AbstractRequestTask;
import org.silverpeas.core.thread.task.RequestTaskManager;
import org.silverpeas.kernel.logging.SilverLogger;

/**
 * @author ebonnet
 */
@Technical
@Bean
public class WorkflowTaskEngine extends AbstractRequestTask<AbstractRequestTask.ProcessContext> {

  @Inject
  private Instance<AbstractRequest> requests;

  @Inject
  private RequestTaskManager requestTaskManager;

  protected WorkflowTaskEngine() {
    super();
  }

  /**
   * Add a request 'TaskDoneEvent'
   */
  public void addTaskDoneRequest(TaskDoneEvent event) {
    requests.select(TaskDoneRequest.class).get();
    TaskDoneRequest request = getRequest(TaskDoneRequest.class, event);
    SilverLogger.getLogger(WorkflowTaskEngine.class)
        .info("Add task done request: {0}", request.toString());
    push(request);
  }

  /**
   * Add a request 'TaskSavedEvent'
   */
  public void addTaskSavedRequest(TaskSavedEvent event) {
    TaskSavedRequest request = getRequest(TaskSavedRequest.class, event);
    SilverLogger.getLogger(WorkflowTaskEngine.class)
        .info("Add task saved request: {0}", request.toString());
    push(request);
  }

  /**
   * Add a request 'QuestionEvent'
   */
  public void addQuestionRequest(QuestionEvent event) {
    QuestionRequest request = getRequest(QuestionRequest.class, event);
    SilverLogger.getLogger(WorkflowTaskEngine.class)
        .info("Add question request: {0}", request.toString());
    push(request);
  }

  /**
   * Add a request 'ResponseEvent'
   */
  public void addResponseRequest(ResponseEvent event) {
    ResponseRequest request = getRequest(ResponseRequest.class, event);
    SilverLogger.getLogger(WorkflowTaskEngine.class)
        .info("Add response request: {0}", request.toString());
    push(request);
  }

  /**
   * Add a request 'TimeoutEvent'
   */
  public void addTimeoutRequest(TimeoutEvent event) {
    TimeoutRequest request = getRequest(TimeoutRequest.class, event);
    SilverLogger.getLogger(WorkflowTaskEngine.class)
        .info("Add timeout request: {0}", request.toString());
    push(request);
  }

  private void push(AbstractRequest request) {
    requestTaskManager.push(WorkflowTaskEngine.class, request);
  }

  private <T extends AbstractRequest, E extends GenericEvent> T getRequest(Class<T> requestType,
      E event) {
    T request = requests.select(requestType).get();
    request.setEvent(event);
    return request;
  }
}