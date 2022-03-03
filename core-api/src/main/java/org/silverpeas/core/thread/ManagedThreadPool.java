/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
package org.silverpeas.core.thread;

import org.silverpeas.core.annotation.Bean;
import org.silverpeas.core.annotation.Technical;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedThreadFactory;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.silverpeas.core.thread.ManagedThreadPool.ExecutionConfig.defaultConfig;

/**
 * A pool of threads that are managed by the underlying application server in which runs Silverpeas.
 * This pool manages the life-cycle of threads and distributes over them the different tasks that
 * are passed to it.
 * <p>
 * This useful managed thread pool permits to invoke instances of {@link java.lang.Runnable} or
 * {@link java.util.concurrent.Callable} by using the managed thread pools provided by the
 * application server.
 * </p>
 * @author Yohann Chastagnier
 */
@Technical
@Bean
@Singleton
public class ManagedThreadPool {

  @Resource
  private ManagedThreadFactory managedThreadFactory;

  protected ManagedThreadPool() {
    // constructor to be used only by the IoD container
  }

  /**
   * Gets a pool of managed threads.
   * @return a {@link ManagedThreadPool} instance ready to take in charge the passed executions
   * in different the threads of the pool.
   */
  public static ManagedThreadPool getPool() {
    return ServiceProvider.getSingleton(ManagedThreadPool.class);
  }

  /**
   * Invokes the given {@link java.lang.Runnable} instances into a managed thread.<br>
   * Each {@link java.lang.Runnable} instance will be used one managed thread.<br>
   * If the application server has no more thread to supply, then the execution will wait until it
   * exists one again available.
   * @param runnables the {@link java.lang.Runnable} instances to invoke.
   * @return the list of threads that have been invoked.
   */
  public List<Thread> invoke(Runnable... runnables) {
    List<Thread> threads = new ArrayList<>();
    for (Runnable runnable : runnables) {
      Thread thread = this.managedThreadFactory.newThread(runnable);
      threads.add(thread);
      thread.start();
    }
    return threads;
  }

  /**
   * Invokes the given {@link java.lang.Runnable} instances into a managed thread and waiting for
   * the end of execution of all of it.<br>
   * Each {@link java.lang.Runnable} instance will be used one managed thread.<br>
   * If the application server has no more thread to supply, then the execution will wait until it
   * exists one again available.
   * @param runnables the {@link java.lang.Runnable} instances to invoke.
   * @throws ManagedThreadPoolException if the invocation fails.
   */
  public void invokeAndAwaitTermination(Runnable ... runnables)
      throws ManagedThreadPoolException {
    invokeAndAwaitTermination(Arrays.asList(runnables), defaultConfig());
  }

  /**
   * Invokes the given {@link java.lang.Runnable} instances into a managed thread and waiting for
   * the end of execution of all of it.<br>
   * Each {@link java.lang.Runnable} instance will be used one managed thread.<br>
   * If the application server has no more thread to supply, then the execution will wait until it
   * exists one again available.
   * @param runnables the {@link java.lang.Runnable} instances to invoke.
   * @throws ManagedThreadPoolException if the invocation fails.
   */
  public void invokeAndAwaitTermination(List<? extends Runnable> runnables)
      throws ManagedThreadPoolException {
    invokeAndAwaitTermination(runnables, defaultConfig());
  }

  /**
   * Invokes the given {@link java.lang.Runnable} instances into a managed thread and waiting for
   * the end of execution of all of it.<br>
   * Each {@link java.lang.Runnable} instance will be used one managed thread.<br>
   * If the application server has no more thread to supply, then the execution will wait until it
   * exists one again available.
   * @param runnables the {@link java.lang.Runnable} instances to invoke.
   * @param config the {@link java.lang.Runnable} instances execution configuration.
   * @throws ManagedThreadPoolException if the invocation fails.
   */
  public void invokeAndAwaitTermination(List<? extends Runnable> runnables,
      ExecutionConfig config) throws ManagedThreadPoolException {
   invokeAndAwaitTermination(runnables.stream(), config);
  }

  /**
   * Invokes the given stream of {@link java.lang.Runnable} instances into a managed thread and
   * waiting for the end of execution of all of it.<br>
   * Each {@link java.lang.Runnable} instance in the stream will be used one managed thread.<br>
   * If the application server has no more thread to supply, then the execution will wait until it
   * exists one again available.
   * @param runnables a stream of {@link java.lang.Runnable} instances to invoke.
   * @param config the {@link java.lang.Runnable} instances execution configuration.
   * @throws ManagedThreadPoolException if the invocation fails.
   */
  public void invokeAndAwaitTermination(Stream<? extends Runnable> runnables,
      ExecutionConfig config) throws ManagedThreadPoolException {
    try {
      ExecutorService executorService = getExecutorService(config);
      List<Future<?>> threadExecutionResults = new ArrayList<>();
      try {
        threadExecutionResults
            .addAll(runnables.map(executorService::submit).collect(Collectors.toList()));
      } finally {
        executorService.shutdown();
      }
      if (config.isTimeout()) {
        boolean allRunnablesAreTerminated =
            executorService.awaitTermination(config.getTimeout(), config.getTimeUnit());
        if (!allRunnablesAreTerminated && !config.isRunningInBackgroundAfterTimeout()) {
          List<Runnable> notExecutedRunnables = executorService.shutdownNow();
          for (Runnable noExecutedRunnable : notExecutedRunnables) {
            SilverLogger.getLogger(config).error("Runnable {0} has not been processed.",
                noExecutedRunnable.getClass().getName());
          }
        }
      } else {
        for (Future<?> future : threadExecutionResults) {
          future.get();
        }
      }
    } catch (Exception e) {
      throw new ManagedThreadPoolException(e);
    }
  }

  /**
   * Invokes the given {@link java.util.concurrent.Callable} instance into a managed thread.<br>
   * If the application server has no more thread to supply, then the execution will wait until it
   * exists one again available.
   * @param callable the callable to invoke.
   * @param <V> the type of the returned value of a {@link java.util.concurrent.Callable} instance.
   * @return the {@link java.util.concurrent.Future} returned by the invocation of the given
   * {@link java.util.concurrent.Callable} instance.
   * @throws InterruptedException if interrupted while waiting
   */
  public <V> Future<V> invoke(Callable<V> callable) throws InterruptedException {
    return invoke(callable, defaultConfig());
  }

  /**
   * Invokes the given {@link java.util.concurrent.Callable} instance into a managed thread.<br>
   * If the application server has no more thread to supply, then the execution will wait until it
   * exists one again available.
   * @param callable the callable to invoke.
   * @param config the {@link java.util.concurrent.Callable} instances execution configuration.
   * @param <V> the type of the returned value of a {@link java.util.concurrent.Callable} instance.
   * @return the {@link java.util.concurrent.Future} returned by the invocation of the given
   * {@link java.util.concurrent.Callable} instance.
   * @throws InterruptedException if interrupted while waiting
   */
  public <V> Future<V> invoke(Callable<V> callable, ExecutionConfig config) throws InterruptedException {
    return invoke(Collections.singletonList(callable), config).get(0);
  }

  /**
   * Invokes the given {@link java.util.concurrent.Callable} instances into managed threads.<br>
   * Each {@link java.util.concurrent.Callable} instance will be used one managed thread.<br>
   * If the application server has no more thread to supply, then the execution will wait until it
   * exists one again available.
   * @param callables the {@link java.util.concurrent.Callable} instances to invoke.
   * @param <V> the type of the returned value of a {@link java.util.concurrent.Callable} instance.
   * @return the list of {@link java.util.concurrent.Future} returned by the invocation of each
   * given {@link java.util.concurrent.Callable} instances.
   * @throws InterruptedException if interrupted while waiting
   */
  public <V> List<Future<V>> invoke(List<? extends Callable<V>> callables)
      throws InterruptedException {
    return invoke(callables, defaultConfig());
  }

  /**
   * Invokes the given {@link java.util.concurrent.Callable} instances into managed threads.<br>
   * Each {@link java.util.concurrent.Callable} instance will be used one managed thread.<br>
   * If the application server has no more thread to supply, then the execution will wait until it
   * exists one again available.<br>
   * A difference with invoking {@link java.lang.Runnable} instances is that if a timeout is set the
   * caller will get back the hand after the successful execution of all threads or after the
   * timeout, but never before.
   * @param callables the {@link java.util.concurrent.Callable} instances to invoke.
   * @param config the {@link java.util.concurrent.Callable} instances execution configuration.
   * @param <V> the type of the returned value of a {@link java.util.concurrent.Callable} instance.
   * @return the list of {@link java.util.concurrent.Future} returned by the invocation of each
   * given {@link java.util.concurrent.Callable} instances.
   * @throws InterruptedException if interrupted while waiting
   */
  public <V> List<Future<V>> invoke(List<? extends Callable<V>> callables,
      ExecutionConfig config) throws InterruptedException {
    ExecutorService executorService = getExecutorService(config);
    List<Future<V>> futures = new ArrayList<>();
    try {
      for (Callable<V> callable : callables) {
        futures.add(executorService.submit(callable));
      }
    } finally {
      executorService.shutdown();
    }
    if (config.isTimeout()) {
      boolean allRunnablesAreTerminated =
          executorService.awaitTermination(config.getTimeout(), config.getTimeUnit());
      if (!allRunnablesAreTerminated && !config.isRunningInBackgroundAfterTimeout()) {
        List<Runnable> notExecutedRunnables = executorService.shutdownNow();
        for (Runnable noExecutedRunnable : notExecutedRunnables) {
          SilverLogger.getLogger(config).error("Runnable {0} has not been processed.",
              noExecutedRunnable.getClass().getName());
        }
      }
    }
    return futures;
  }

  /**
   * Gets a new executor service according to the given configuration.
   * @param config the configuration of thread execution.
   * @return the new {@link ExecutorService} instance.
   */
  private ExecutorService getExecutorService(ExecutionConfig config) {
    final ExecutorService executorService;
    final int maxThreadPoolSize = config.getMaxThreadPoolSize();
    if (maxThreadPoolSize > 0) {
      if (maxThreadPoolSize == 1) {
        executorService = Executors.newSingleThreadExecutor(this.managedThreadFactory);
      } else {
        executorService = Executors.newFixedThreadPool(maxThreadPoolSize, this.managedThreadFactory);
      }
    } else {
      executorService = Executors.newCachedThreadPool(this.managedThreadFactory);
    }
    return executorService;
  }

  /**
   * Class that permits to specify the execution configuration.
   */
  public static class ExecutionConfig {
    private int maxThreadPoolSize = 0;
    private boolean isTimeout = false;
    private long timeout = 0;
    private TimeUnit timeUnit = TimeUnit.MILLISECONDS;
    private boolean runInBackgroundAfterTimeout = true;

    /**
     * Gets an instance of an execution configuration with a specified maximum pool of thread.<br>
     * This is only useful when invoking {@link Callable} instances.
     * @param maxThreadPoolSize the maximum size of the treads pool.
     * @return an instance of an execution configuration with a specified maximum pool of thread.
     */
    public static ExecutionConfig maxThreadPoolSizeOf(int maxThreadPoolSize) {
      return defaultConfig().withMaxThreadPoolSizeOf(maxThreadPoolSize);
    }

    /**
     * Gets an instance of an execution configuration with a specified timeout in milliseconds.
     * @param timeout the timeout
     * @return an instance of an execution configuration with a specified timeout in milliseconds.
     */
    public static ExecutionConfig timeoutOf(long timeout) {
      return defaultConfig().withTimeoutOf(timeout);
    }

    /**
     * Gets an instance of a default execution configuration.
     * @return an instance of a default execution configuration.
     */
    public static ExecutionConfig defaultConfig() {
      return new ExecutionConfig();
    }

    /**
     * Hidden constructor.
     */
    private ExecutionConfig() {
    }

    /**
     * Gets the maximum size of the pool of thread.<br>
     * @return Zero or negative value indicates an undefined pool size.
     */
    int getMaxThreadPoolSize() {
      return maxThreadPoolSize;
    }

    /**
     * Indicates if a timeout has been explicitly set.
     * @return true if a timeout has been explicitly set, false otherwise.
     */
    boolean isTimeout() {
      return isTimeout;
    }

    /**
     * Gets the timeout.
     * @return the timeout.
     */
    long getTimeout() {
      return timeout;
    }

    /**
     * Gets the timeout time unit.
     * @return the time unit of the timeout.
     */
    TimeUnit getTimeUnit() {
      return timeUnit;
    }

    /**
     * Sets a maximum size of the pool of threads.<br>
     * This is only useful when invoking {@link Callable} instances.
     * @param maxThreadPoolSize the maximum size of the pool of threads.
     * @return the instance of {@link ExecutionConfig}.
     */
    public ExecutionConfig withMaxThreadPoolSizeOf(final int maxThreadPoolSize) {
      this.maxThreadPoolSize = maxThreadPoolSize;
      return this;
    }


    /**
     * Sets a timeout in milliseconds after that the invocation process will give back hand.
     * @param timeout the timeout after which the hand must be get back to the caller.
     * @return the instance of {@link ExecutionConfig}.
     */
    public ExecutionConfig withTimeoutOf(final long timeout) {
      return withTimeoutOf(timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * Sets a timeout after that the invocation process will give back hand.
     * @param timeout the timeout after which the hand must be get back to the caller.
     * @param timeUnit the time unit of the given timeout (default is milliseconds).
     * @return the instance of {@link ExecutionConfig}.
     */
    public ExecutionConfig withTimeoutOf(final long timeout, TimeUnit timeUnit) {
      this.isTimeout = true;
      this.timeout = timeout;
      this.timeUnit = timeUnit;
      return this;
    }

    /**
     * Indicates if the running thread must be killed after an effective timeout.
     * @return false if threads must be killed, true otherwise.
     */
    boolean isRunningInBackgroundAfterTimeout() {
      return runInBackgroundAfterTimeout;
    }

    /**
     * By default the caller get again the control after the
     * timeout while {@link java.lang.Runnable} threads continue to run.
     * Calling this method if it is required that current running thread must be killed after an
     * effective timeout.
     * @return the instance of {@link ExecutionConfig}.
     */
    public ExecutionConfig killThreadsAfterTimeout() {
      this.runInBackgroundAfterTimeout = false;
      return this;
    }
  }
}