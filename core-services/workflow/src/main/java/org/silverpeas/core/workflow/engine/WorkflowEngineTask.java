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

import org.silverpeas.core.workflow.api.event.QuestionEvent;
import org.silverpeas.core.workflow.api.event.ResponseEvent;
import org.silverpeas.core.workflow.api.event.TaskDoneEvent;
import org.silverpeas.core.workflow.api.event.TaskSavedEvent;
import org.silverpeas.core.workflow.api.event.TimeoutEvent;
import org.silverpeas.core.silvertrace.SilverTrace;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ebonnet
 */
public class WorkflowEngineTask implements Runnable {

  /**
   * The requests are stored in a shared list of Requests. In order to guarantee serial access, all
   * access will be synchronized on this list. Futhermore this list is used to synchronize the
   * providers and the consumers of the list :
   * <p>
   * <PRE>
   * // provider
   * synchronized(requestList)
   * {
   * requestList.add(...);
   * requestList.notify();
   * }
   * // consumer
   * synchronized(requestList)
   * {
   * requestList.wait();
   * ... = requestList.remove(...);
   * }
   * </PRE>
   */
  static private final List<Request> requestList = new ArrayList<>();

  /**
   * Add a request 'TaskDoneEvent'
   */
  static public void addTaskDoneRequest(TaskDoneEvent event) {
    synchronized (requestList) {
      TaskDoneRequest request = new TaskDoneRequest(event);
      SilverTrace
          .info("workflowEngine", "WorkflowEngineThread", "workflowEngine.INFO_ADDS_ADD_REQUEST",
              request.toString());
      requestList.add(request);
      requestList.notify();
    }
  }

  /**
   * Add a request 'TaskSavedEvent'
   */
  public static void addTaskSavedRequest(TaskSavedEvent event) {
    synchronized (requestList) {
      TaskSavedRequest request = new TaskSavedRequest(event);
      SilverTrace
          .info("workflowEngine", "WorkflowEngineThread", "workflowEngine.INFO_ADDS_ADD_REQUEST",
              request.toString());
      requestList.add(request);
      requestList.notify();
    }
  }

  /**
   * Add a request 'QuestionEvent'
   */
  static public void addQuestionRequest(QuestionEvent event) {
    synchronized (requestList) {
      QuestionRequest request = new QuestionRequest(event);
      SilverTrace
          .info("workflowEngine", "WorkflowEngineThread", "workflowEngine.INFO_ADDS_ADD_REQUEST",
              request.toString());
      requestList.add(request);
      requestList.notify();
    }
  }

  /**
   * Add a request 'ResponseEvent'
   */
  static public void addResponseRequest(ResponseEvent event) {
    synchronized (requestList) {
      ResponseRequest request = new ResponseRequest(event);
      SilverTrace
          .info("workflowEngine", "WorkflowEngineThread", "workflowEngine.INFO_ADDS_ADD_REQUEST",
              request.toString());
      requestList.add(request);
      requestList.notify();
    }
  }

  /**
   * Add a request 'TimeoutEvent'
   */
  static public void addTimeoutRequest(TimeoutEvent event) {
    synchronized (requestList) {
      TimeoutRequest request = new TimeoutRequest(event);
      SilverTrace
          .info("workflowEngine", "WorkflowEngineThread", "workflowEngine.INFO_ADDS_ADD_REQUEST",
              request.toString());
      requestList.add(request);
      requestList.notify();
    }
  }


  @Override
  public void run() {
    Request request;
    try {
      while (true) {

        /*
         * First, all the requests are processed until the queue becomes empty.
         */
        do {
          request = null;

          synchronized (requestList) {
            if (!requestList.isEmpty()) {
              request = requestList.remove(0);
            }
          }

          /*
           * Each request is processed out of the synchronized block so the others threads (which
           * put the requests) will not be blocked.
           */
          if (request != null) {
            try {
              request.process();
            } catch (Exception e) {
              SilverTrace.error("workflowEngine", "WorkflowEngineThread",
                  "workflowEngine.EX_ERROR_PROCESSING_REQUEST", request.toString(), e);
            }
          }

        } while (request != null);

        /*
         * Finally, we wait the notification of a new request to be processed.
         */
        try {
          synchronized (requestList) {
            if (requestList.isEmpty()) {
              requestList.wait();
            }
          }
        } catch (InterruptedException e) {

        }
      }
    } catch (Throwable error) {
      /*
       * Keep this log, we really need to know why this thread has been down. Problem can happen
       * when external workflow is not synchronize with current Silverpeas platform version.
       */
      SilverTrace
          .fatal("workflowEngine", "WorkflowEngineThread", "Exit from workflow thread loop", error);
      throw new RuntimeException("End of thread", error);
    }
  }
}
