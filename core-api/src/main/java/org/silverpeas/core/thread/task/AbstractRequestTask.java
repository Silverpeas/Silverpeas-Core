/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.thread.task;

import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.List;

/**
 * Centralizing the management of a thread in charge of processing in the background a batch of
 * {@link Request}.<br/>
 * When there is no more {@link Request} to perform, the thread ends.<br/>
 * When adding a new {@link Request} to perform, the request is added into a queue and the thread
 * is started if it is not running.<br/>
 * Requests are performed one after one.<br/>
 * All implementations of this abstract class have to handle final static variables:
 * <ul>
 *   <li>
 *     {@code List<Request<C>> requestList} (or another name): this is representing the list of
 *     {@link Request} to process. All access to this variable must be surrounded by a synchronized
 *     clause on static request list instance. The implementation of {@link #getRequestList()}
 *     must return this static instance.
 *     <pre>
 *     // From context of the {@link AbstractRequestTask} thread instance
 *     synchronized (getRequestList()) {
 *       ...
 *       getRequestList().remove(0);
 *       ...
 *     }
 *
 *     // From static context of the {@link AbstractRequestTask} class
 *     synchronized (requestList) {
 *       ...
 *       requestList.remove(0);
 *       ...
 *     }
 *     </pre>
 *   </li>
 *   <li>
 *     {@code List<Request<C>> requestList} (or another name): this is representing the simple
 *     running state of the task thread. All access to this variable must be surrounded by a
 *     synchronized clause on static request list instance. The implementation of
 *     {@link #taskIsEnding()} must set this static running indicator in order to indicate a not
 *     running state.
 *     <pre>
 *     // From context of the {@link AbstractRequestTask} thread instance
 *     synchronized (getRequestList()) {
 *       ...
 *       taskIsEnding();
 *       ...
 *     }
 *
 *     // From static context of the {@link AbstractRequestTask} child class
 *     synchronized (requestList) {
 *       ...
 *       running = false;
 *       ...
 *     }
 *     </pre>
 *   </li>
 * </ul>
 * @param <C> the type of the context given to a {@link Request} processing.
 */
public abstract class AbstractRequestTask<C extends AbstractRequestTask.ProcessContext>
    implements Runnable {

  /**
   * Nothing is done for now.
   */
  protected AbstractRequestTask() {
  }

  /**
   * Sets the running state indicator.<br/>
   * This method is called in a context of synchronization (please consult the code of {@link
   * #nextRequest()} method).
   */
  protected abstract void taskIsEnding();

  /**
   * Gets the list of {@link Request} to process.
   * @return a list of {@link Request}.
   */
  protected abstract List<Request<C>> getRequestList();

  /**
   * Gets the context given for each request to process.
   * @return the instance of process context.
   */
  protected C getProcessContext() {
    return null;
  }

  /**
   * Process all the requests. This method should be private but is already declared public in the
   * base class Thread.
   */
  @Override
  public final void run() {

    Request<C> currentRequest = nextRequest();

    // The loop condition must be verified on a private attribute of run method (not on the static
    // running attribute) in order to avoid concurrent access.
    while (currentRequest != null) {
      CacheServiceProvider.clearAllThreadCaches();

        /*
         * Each request is processed out of the synchronized block so the others threads (which put
         * the requests) will not be blocked.
         */
      try {
        currentRequest.process(getProcessContext());
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e.getMessage(), e);
      }

      // Getting the next request if any.
      currentRequest = nextRequest();
    }
  }

  /**
   * Gets the next request to process.
   * @return the next request.
   */
  private Request<C> nextRequest() {
    synchronized (getRequestList()) {
      final Request<C> nextRequest;
      if (!getRequestList().isEmpty()) {
        nextRequest = getRequestList().remove(0);
      } else {
        nextRequest = null;
        taskIsEnding();
      }
      return nextRequest;
    }
  }

  /**
   * Each request must define a method called process which will process the request.
   */
  public interface Request<C> {
    void process(C context) throws InterruptedException;
  }

  /**
   * The process context given to the {@link Request#process(Object)} method.<br/>
   * The process context instance is provided by {@link AbstractRequestTask#getProcessContext()}
   * method implementation.
   */
  public interface ProcessContext {}
}

