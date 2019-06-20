/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.thread.task;

import org.silverpeas.core.SilverpeasException;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.concurrent.Callable;

/**
 * Centralizing the management of a thread in charge of processing in the background a batch of
 * {@link Request}.<br>
 * When there is no more {@link Request} to perform, the task ends.<br>
 * When adding a new {@link Request} to perform, the request is added into a queue and the task
 * is started if it is not running.<br>
 * Requests are performed one after one.<br>
 * To add a request to process, use {@link RequestTaskManager#push(Class, Request)}
 * @param <C> the type of the context given to a {@link Request} processing.
 */
public abstract class AbstractRequestTask<C extends AbstractRequestTask.ProcessContext>
    implements Callable<Void> {

  private static final int NO_REQUEST_QUEUE_LIMIT = 0;
  RequestTaskManager.RequestTaskMonitor<? extends AbstractRequestTask, C> monitor = null;

  /**
   * Nothing is done for now.
   */
  protected AbstractRequestTask() {
  }

  /**
   * @return 0 indicates no limit, value greater than 0 will block the threads pushing new request
   * if the limit is reached until there is again possibility to push.
   */
  protected int getRequestQueueLimit() {
    return NO_REQUEST_QUEUE_LIMIT;
  }

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
  public final Void call() throws Exception {
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
        monitor.releaseAccess();
        processRequest(currentRequest);
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
      }

      // Getting the next request if any.
      currentRequest = nextRequest();
      if (currentRequest == null) {
        // No more request, but waiting 500ms in case of a nearly new one.
        debug("no more request to process, waiting 500ms about new requests");
        Thread.sleep(500);
        currentRequest = nextRequest();
      }
      if (currentRequest == null) {
        try {
          debug("no more request to process after waiting a while, invoking afterNoMoreRequest method");
          afterNoMoreRequest();
        } catch (Exception e) {
          SilverLogger.getLogger(this).error(e);
        } finally {
          // The execution of {@link #afterNoMoreRequest} can take a long time so watching for
          // new request.
          debug("requests could be pushed during afterNoMoreRequest method execution");
          currentRequest = nextRequest();
        }
      }
    }
    debug("no more request to perform, stopping a thread in charge of request processing");
    return null;
  }

  /**
   * Processes the given request.<br>
   * Useful for a task which needs to perform some stuffs around the process.
   * @param request the request to process.
   * @throws SilverpeasException on error.
   */
  protected void processRequest(final Request<C> request) throws SilverpeasException {
    debug("processing a request: {0}", request.getClass().getSimpleName());
    try {
      request.process(getProcessContext());
    } catch (Exception e) {
      throw new SilverpeasException(e);
    }
  }

  /**
   * Invoked when it does not exist {@link Request} to process anymore.
   * Is is called in any case, even if a severe error has been thrown.
   */
  protected void afterNoMoreRequest() {
  }

  /**
   * Gets the next request to process.
   * @return the next request.
   */
  private Request<C> nextRequest() {
    synchronized (monitor.requestList) {
      debug("checking the next request to process");
      final Request<C> nextRequest;
      if (!monitor.requestList.isEmpty()) {
        nextRequest = monitor.requestList.remove(0);
      } else {
        nextRequest = null;
      }
      return nextRequest;
    }
  }

  private void debug(String message, Object... parameters) {
    SilverLogger.getLogger("silverpeas.core.thread")
        .debug(getClass().getSimpleName() + " - consumer thread - " + message, parameters);
  }

  /**
   * Each request must define a method called process which will process the request.
   */
  public interface Request<C> {

    /**
     * Gets a replacement identifier.
     * <p>
     * In almost cases, it will be null and means there is no replacement to perform into queue.
     * </p>
     * <p>
     * In rare cases, it will not be null and means that if it exists already a request into
     * queue with a same identifier then this request must be replaced by the new one.
     * </p>
     * @return a string representing a unique type. If null, the request behavior is the one of
     * simple {@link Request}.
     */
    default String getReplacementId() {
      return null;
    }

    /**
     * Process the request according to the given context.
     * @param context the context of the process.
     * @throws InterruptedException in case of technical error.
     */
    void process(C context) throws InterruptedException;
  }

  /**
   * The process context given to the {@link Request#process(Object)} method.<br>
   * The process context instance is provided by {@link AbstractRequestTask#getProcessContext()}
   * method implementation.
   */
  public interface ProcessContext {}
}
