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
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.core.thread.task;

import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.annotation.Technical;
import org.silverpeas.core.thread.ManagedThreadPool;
import org.silverpeas.core.thread.task.AbstractRequestTask.Request;
import org.silverpeas.core.util.Mutable;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.annotation.PreDestroy;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
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
@Technical
@Singleton
@Bean
public class RequestTaskManager {

  private final ConcurrentMap<Class<? extends AbstractRequestTask<?>>,
      RequestTaskMonitor<? extends AbstractRequestTask<?>, ?>> tasks = new ConcurrentHashMap<>();
  private static final int RESTART_WAITING_BEFORE_GETTING_RESULT = 200;

  public static RequestTaskManager get() {
    return ServiceProvider.getSingleton(RequestTaskManager.class);
  }

  /**
   * Hidden constructor because the class is not instantiable.
   */
  private RequestTaskManager() {
  }

  private <T extends AbstractRequestTask<C>, C extends AbstractRequestTask.ProcessContext>
  boolean startIfNecessary(final RequestTaskMonitor<T, C> monitor) {
    if (!monitor.isTaskRunning()) {
      AbstractRequestTask<C> task = ServiceProvider.getService(monitor.taskClass);
      try {
        debug(monitor.taskClass, "starting a thread in charge of request processing");
        task.monitor = monitor;
        final TaskWatcher taskWatcher = new TaskWatcher(monitor);
        monitor.task = ManagedThreadPool.getPool().invoke(task);
        monitor.taskWatcher = ManagedThreadPool.getPool().invoke(taskWatcher);
        return true;
      } catch (InterruptedException e) {
        error(monitor.taskClass, "the task {0} can not be invoked",
            task.getClass().getSimpleName());
        Thread.currentThread().interrupt();
        throw new SilverpeasRuntimeException(e);
      }
    }
    return false;
  }

  private <T extends AbstractRequestTask<C>, C extends AbstractRequestTask.ProcessContext>
  boolean restartIfNecessary(final RequestTaskMonitor<T, C> monitor) {
    if (!monitor.isTaskRunning() && !monitor.requestList.isEmpty()) {
      warn(monitor.taskClass,
          "the task is not running but there are yet some requests to process!!! ({0} " +
              "requests queued) restarting it again", monitor.requestList.size());
      return startIfNecessary(monitor);
    }
    return false;
  }

  private void warn(Class<?> taskClass, String message, Object... parameters) {
    getLogger().warn(taskClass.getSimpleName() + " - " + message, parameters);
  }

  private void error(Class<?> taskClass, String message, Object... parameters) {
    getLogger().error(taskClass.getSimpleName() + " - " + message, parameters);
  }

  private void debug(Class<?> taskClass, String message, Object... parameters) {
    getLogger().debug(taskClass.getSimpleName() + " - " + message, parameters);
  }

  private SilverLogger getLogger() {
    return SilverLogger.getLogger("silverpeas.core.thread");
  }

  /**
   * This method permits to know if the task is running.
   * @param taskClass the class of the {@link AbstractRequestTask} implementation which provides
   * the
   * {@link AbstractRequestTask.Request}.
   * @param <T> the type of the task.
   */
  public <T extends AbstractRequestTask<C>, C extends AbstractRequestTask.ProcessContext>
  boolean isTaskRunning(Class<T> taskClass) {
    final Mutable<Boolean> isRunning = Mutable.of(false);
    tasks.computeIfPresent(taskClass, (i, m) -> {
      isRunning.set(m.isTaskRunning());
      return m;
    });
    return isRunning.get();
  }

  /**
   * This method is the only entry point to add a request to process.
   * <p>There is three synchronized steps performed into this method:</p>
   * <ul><li>first, starting the consummation task if exists at least one request into the queue and
   * if the task is not running</li>
   * <li>then, acquiring a semaphore access if the queue size is limited</li>
   * <li>finally, adding the request into the queue and starting the task if it is not running</li>
   * </ul>
   * @param taskClass the class of the {@link AbstractRequestTask} implementation which provides
   * the
   * {@link AbstractRequestTask.Request}.
   * @param newRequest the request to process.
   * @param <T> the type of the task.
   * @param <C> the type of the task process context.
   */
  @SuppressWarnings("unchecked")
  public <T extends AbstractRequestTask<C>, C extends AbstractRequestTask.ProcessContext>
  void push(Class<T> taskClass, Request<C> newRequest) {
    final RequestTaskMonitor<T, C> monitor =
        (RequestTaskMonitor<T, C>) tasks.computeIfAbsent(taskClass, c -> {
          T taskForInit = (T) ServiceProvider.getService(c);
          return new RequestTaskMonitor<>(taskForInit);
        });
    synchronized (monitor.requestList) {
      restartIfNecessary(monitor);
    }
    monitor.acquireAccess();
    synchronized (monitor.requestList) {
      debug(taskClass, "pushing new request {0} ({1} requests queued before push)",
          newRequest.getClass().getSimpleName(), monitor.requestList.size());
      final String uniqueType = newRequest.getReplacementId();
      boolean replaced = false;
      if (uniqueType != null) {
        debug(taskClass, "searching awaiting request {0} of type {1} to replace",
            newRequest.getClass().getSimpleName(), uniqueType);
        final ListIterator<Request<C>> it = monitor.requestList.listIterator();
        while (it.hasNext()) {
          final Request<C> queueRequest = it.next();
          if (uniqueType.equals(queueRequest.getReplacementId())) {
            debug(taskClass, "replacing awaiting request {0} of type {1} with new one",
                newRequest.getClass().getSimpleName(), uniqueType);
            it.set(newRequest);
            replaced = true;
            break;
          }
        }
        if (!replaced) {
          debug(taskClass, "no awaiting request {0} of type {1} to replace",
              newRequest.getClass().getSimpleName(), uniqueType);
        }
      }
      if (!replaced) {
        monitor.requestList.add(newRequest);
      }
      startIfNecessary(monitor);
    }
  }

  @PreDestroy
  protected void shutdownAllTasks() {
    tasks.forEach((t, m) -> m.shutdown());
    tasks.clear();
  }

  @SuppressWarnings("java:S1452")
  protected ConcurrentMap<Class<? extends AbstractRequestTask<?>>,
      RequestTaskMonitor<? extends AbstractRequestTask<?>, ?>> getTasks() {
    return tasks;
  }

  private class TaskWatcher implements Callable<Void> {
    final RequestTaskMonitor<? extends AbstractRequestTask<?>, ? extends AbstractRequestTask
        .ProcessContext> monitor;

    TaskWatcher(final RequestTaskMonitor<? extends AbstractRequestTask<?>, ? extends AbstractRequestTask
        .ProcessContext> monitor) {
      this.monitor = monitor;
    }

    @Override
    public Void call() {
      debug(this.monitor.taskClass, "task watcher - started");
      try {
        Thread.sleep(RESTART_WAITING_BEFORE_GETTING_RESULT);
      } catch (InterruptedException e) {
        error(TaskWatcher.class, e);
        Thread.currentThread().interrupt();
      }
      boolean endedOnError = false;
      try {
        this.monitor.task.get();
      } catch (CancellationException | ExecutionException e) {
        endedOnError = true;
        error(this.monitor.taskClass, e);
      } catch (InterruptedException e) {
        endedOnError = true;
        error(this.monitor.taskClass, e);
        Thread.currentThread().interrupt();
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

    private void error(Class<?> taskClass, Exception e) {
      getLogger().error(taskClass.getSimpleName() + " - an error occurred", e);
    }
  }

  class RequestTaskMonitor<T extends AbstractRequestTask<C>, C extends AbstractRequestTask
      .ProcessContext> {
    final Class<T> taskClass;
    final List<Request<C>> requestList;
    private final Semaphore queueSemaphore;
    Future<Void> task = null;
    Future<Void> taskWatcher = null;

    /**
     * @param taskForInit a task instance for initialization, it will not be run.
     */
    @SuppressWarnings("unchecked")
    RequestTaskMonitor(final T taskForInit) {
      final int queueLimit = taskForInit.getRequestQueueLimit();
      this.queueSemaphore = queueLimit > 0 ? new Semaphore(queueLimit, true) : null;
      this.requestList = queueLimit > 0 ? new ArrayList<>(queueLimit) : new ArrayList<>();
      this.taskClass = (Class<T>) taskForInit.getClass();
    }

    boolean isTaskRunning() {
      return task != null && !task.isCancelled() && !task.isDone();
    }

    void shutdown() {
      if (isTaskRunning()) {
        boolean cancelled = task.cancel(true);
        if (!cancelled) {
          warn(RequestTaskMonitor.class, "Cannot cancel task " +
              task.getClass().getSimpleName());
        }
      }
    }

    void acquireAccess() {
      if (queueSemaphore != null) {
        try {
          debug(taskClass, "acquiring queue semaphore ({0} available permits before acquire)",
              queueSemaphore.availablePermits());
          queueSemaphore.acquire();
        } catch (InterruptedException e) {
          error(taskClass, "not possible to acquire semaphore");
          Thread.currentThread().interrupt();
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
