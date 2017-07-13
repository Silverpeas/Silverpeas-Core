/*
 * Copyright (C) 2000 - 2017 Silverpeas
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

import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.thread.ManagedThreadPool;
import org.silverpeas.core.thread.task.AbstractRequestTask.Request;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

/**
 * This manager handles the threading side of {@link AbstractRequestTask} processing.
 * <p>The aim is to avoid the developer to think about how to write rightly the consummation of a
 * list of request to process.</p>
 * <p>To process a request, an {@link AbstractRequestTask} must be implemented and this
 * implementation must push request to process by using
 * {@link RequestTaskManager#push(Class, Request)} method.</p>
 * @author silveryocha
 */
public class RequestTaskManager {

  static final ConcurrentMap<Class, RequestTaskMonitor> tasks = new ConcurrentHashMap<>();
  private static final int RESTART_WAITING_BEFORE_GETTING_RESULT = 200;

  /**
   * Hidden constructor because the class is not instantiable.
   */
  private RequestTaskManager() {
  }

  @SuppressWarnings("unchecked")
  private static <T extends AbstractRequestTask, C extends AbstractRequestTask.ProcessContext>
  boolean startIfNecessary(final RequestTaskMonitor<T, C> monitor) {
    if (!monitor.isTaskRunning()) {
      AbstractRequestTask<C> task =
          (AbstractRequestTask) ServiceProvider.getService(monitor.taskClass);
      try {
        debug(monitor.taskClass, "starting a thread in charge of request processing");
        monitor.task = ManagedThreadPool.getPool().invoke(task);
        task.monitor = monitor;
        monitor.taskWatcher = ManagedThreadPool.getPool().invoke(new TaskWatcher(monitor));
        return true;
      } catch (InterruptedException e) {
        error(monitor.taskClass, "the task {0} can not be invoked",
            task.getClass().getSimpleName());
        throw new SilverpeasRuntimeException(e);
      }
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  private static <T extends AbstractRequestTask, C extends AbstractRequestTask.ProcessContext>
  boolean restartIfNecessary(final RequestTaskMonitor<T, C> monitor) {
    if (!monitor.isTaskRunning() && !monitor.requestList.isEmpty()) {
      warn(monitor.taskClass,
          "the task is not running but there are yet some requests to process!!! ({0} " +
              "requests queued) restarting it again", monitor.requestList.size());
      return startIfNecessary(monitor);
    }
    return false;
  }

  private static void warn(Class taskClass, String message, Object... parameters) {
    getLogger().warn(taskClass.getSimpleName() + " - " + message, parameters);
  }

  private static void error(Class taskClass, String message, Object... parameters) {
    getLogger().error(taskClass.getSimpleName() + " - " + message, parameters);
  }

  private static void error(Class taskClass, Exception e) {
    getLogger().error(taskClass.getSimpleName() + " - an error occurred", e);
  }

  private static void debug(Class taskClass, String message, Object... parameters) {
    getLogger().debug(taskClass.getSimpleName() + " - " + message, parameters);
  }

  private static SilverLogger getLogger() {
    return SilverLogger.getLogger("silverpeas.core.thread");
  }

  /**
   * This method is the only entry point to add a request to process.
   * <p>There is three synchronized steps performed into this method:
   * <ul><li>first, starting the consummation task if exists at least one request into the queue and
   * if the task is not running</li>
   * <li>then, acquiring a semaphore access if the queue size is limited</li>
   * <li>finally, adding the request into the queue and starting the task if it is not running</li>
   * </ul></p>
   * @param taskClass the class of the {@link AbstractRequestTask} implementation which provides
   * the
   * {@link AbstractRequestTask.Request}.
   * @param request the request to process.
   * @param <T> the type of the task.
   * @param <C> the type of the task process context.
   */
  @SuppressWarnings("unchecked")
  public static <T extends AbstractRequestTask, C extends AbstractRequestTask.ProcessContext>
  void push(
      Class<T> taskClass, Request<C> request) {
    RequestTaskMonitor<T, C> monitor = tasks.computeIfAbsent(taskClass, c -> {
      AbstractRequestTask<C> taskForInit = (AbstractRequestTask) ServiceProvider.getService(c);
      return new RequestTaskMonitor<>(taskForInit);
    });
    synchronized (monitor.requestList) {
      restartIfNecessary(monitor);
    }
    monitor.acquireAccess();
    synchronized (monitor.requestList) {
      debug(taskClass, "pushing new request {0} ({1} requests queued before push)",
          request.getClass().getSimpleName(), monitor.requestList.size());
      monitor.requestList.add(request);
      startIfNecessary(monitor);
    }
  }

  static class TaskWatcher implements Callable<Void> {
    final RequestTaskMonitor monitor;

    TaskWatcher(final RequestTaskMonitor monitor) {
      this.monitor = monitor;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Void call() {
      debug(this.monitor.taskClass, "task watcher - started");
      try {
        Thread.sleep(RESTART_WAITING_BEFORE_GETTING_RESULT);
      } catch (InterruptedException e) {
        error(TaskWatcher.class, e);
      }
      boolean endedOnError = false;
      try {
        this.monitor.task.get();
      } catch (CancellationException | InterruptedException | ExecutionException e) {
        endedOnError = true;
        error(this.monitor.taskClass, e);
      }
      synchronized (monitor.requestList) {
        debug(this.monitor.taskClass, "task watcher - watched " +
            (endedOnError ? "task has been terminated on error" : "task ended without error"));
        if (!restartIfNecessary(this.monitor) && !this.monitor.isTaskRunning()) {
          debug(this.monitor.taskClass, "task watcher - unregistering instances from monitor");
          this.monitor.task = null;
          this.monitor.taskWatcher = null;
        }
      }
      debug(this.monitor.taskClass, "task watcher - stopped");
      return null;
    }
  }

  static class RequestTaskMonitor<T extends AbstractRequestTask, C extends AbstractRequestTask
      .ProcessContext> {
    final Class taskClass;
    final List<Request<C>> requestList;
    private final Semaphore queueSemaphore;
    Future<Void> task = null;
    Future<Void> taskWatcher = null;

    /**
     * @param taskForInit a task instance for initialization, it will not be run.
     */
    RequestTaskMonitor(final T taskForInit) {
      final int queueLimit = taskForInit.getRequestQueueLimit();
      this.queueSemaphore = queueLimit > 0 ? new Semaphore(queueLimit, true) : null;
      this.requestList = queueLimit > 0 ? new ArrayList<>(queueLimit) : new ArrayList<>();
      this.taskClass = taskForInit.getClass();
    }

    boolean isTaskRunning() {
      return task != null && !task.isCancelled() && !task.isDone();
    }

    void acquireAccess() {
      if (queueSemaphore != null) {
        try {
          debug(taskClass, "acquiring queue semaphore ({0} available permits before acquire)",
              queueSemaphore.availablePermits());
          queueSemaphore.acquire();
        } catch (InterruptedException e) {
          error(taskClass, "not possible to acquire semaphore");
          throw new SilverpeasRuntimeException(e);
        }
      }
    }

    void releaseAccess() {
      if (queueSemaphore != null) {
        debug(taskClass,
            "consumer thread - releasing queue semaphore ({0} available permits before release)",
            queueSemaphore.availablePermits());
        queueSemaphore.release();
      }
    }
  }
}
