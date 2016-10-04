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
package com.stratelia.webactiv.applicationIndexer.control;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import org.apache.commons.lang3.tuple.Pair;
import org.silverpeas.cache.service.CacheServiceFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Handles the execution of huge indexation process.
 * @author Yohann Chastagnier
 */
public class IndexationProcessExecutor {
  private final static String SILVER_TRACE_MODULE = "applicationIndexer";
  private final static String INDEXATION_PROCESS_EXECUTOR_KEY = "INDEXATION_PROCESS_EXECUTOR_KEY";
  private final static ExecutorService executorService = Executors.newSingleThreadExecutor();

  /**
   * Indicates if an indexation operation is currently running.
   * @return true if there is a current execution, false otherwise.
   */
  @SuppressWarnings("unchecked")
  public static boolean isCurrentExecution() {
    Pair<IndexationProcess, Future<Void>> process = CacheServiceFactory.getApplicationCacheService()
        .get(INDEXATION_PROCESS_EXECUTOR_KEY, Pair.class);
    return process != null && !process.getRight().isDone();
  }

  /**
   * Stops a current indexation operation if it exists a running one.<br/>
   * Otherwise, nothing is done.
   */
  @SuppressWarnings("unchecked")
  public static void stopCurrentExecutionIfAny() {
    Pair<IndexationProcess, Future<Void>> process = CacheServiceFactory.getApplicationCacheService()
        .get(INDEXATION_PROCESS_EXECUTOR_KEY, Pair.class);
    final Future<Void> task = (process != null) ? process.getRight() : null;
    if (process != null && !task.isDone()) {
      try {
        task.cancel(true);
      } catch (Exception e) {
        SilverTrace
            .error(SILVER_TRACE_MODULE, "IndexationProcessExecutor.stopCurrentExecutionIfAny()",
                "IndexationProcessExecutor.EX_INDEXATION_PROCESS_STOPPING_FAILED", "", e);
      }
    }
  }

  /**
   * Executes an indexation process.<br/>
   * There is no error of an other one is already running.<br/>
   * The monitoring of executions has to be performed by caller by using {@link
   * #isCurrentExecution()} and {@link #stopCurrentExecutionIfAny()} methods.
   * @param indexationProcess the indexation process to execute.
   */
  public static void execute(IndexationProcess indexationProcess) {
    SilverTrace.setTraceLevel(SILVER_TRACE_MODULE, SilverTrace.TRACE_LEVEL_INFO);
    try {
      if (isCurrentExecution()) {
        SilverTrace.warn(SILVER_TRACE_MODULE, "IndexationProcessExecutor.execute()",
            "IndexationProcessExecutor.EX_INDEXATION_PROCESS_STARTING",
            "an indexation processing is already running, how is it possible ? That is a huge " +
                "treatment which one and only one should be running at a same time!!!");
      }
      Future<Void> future = executorService.submit(indexationProcess);
      CacheServiceFactory.getApplicationCacheService()
          .put(INDEXATION_PROCESS_EXECUTOR_KEY, Pair.of(indexationProcess, future), 0, 0);
    } catch (Exception e) {
      SilverTrace.error(SILVER_TRACE_MODULE, "IndexationProcessExecutor.execute()",
          "IndexationProcessExecutor.EX_INDEXATION_PROCESS_STARTING_FAILED", "", e);
    }
  }

  public static abstract class IndexationProcess implements Callable<Void> {
    public abstract void perform() throws Exception;

    boolean isRunning = false;

    @Override
    public Void call() throws Exception {
      isRunning = true;
      try {
        SilverTrace.info(SILVER_TRACE_MODULE, "IndexationProcess.run()",
            "IndexationProcess.EX_INDEXATION_PROCESS_STARTING",
            "Thread id = " + Thread.currentThread().getId());
        perform();
        SilverTrace.info(SILVER_TRACE_MODULE, "IndexationProcess.run()",
            "IndexationProcess.EX_INDEXATION_PROCESS_ENDING",
            "Thread id = " + Thread.currentThread().getId());
      } catch (Exception e) {
        SilverTrace.error(SILVER_TRACE_MODULE, "IndexationProcess.run()",
            "IndexationProcess.EX_INDEXATION_PROCESS_ERROR",
            "Thread id = " + Thread.currentThread().getId(), e);
      } finally {
        isRunning = false;
      }
      return null;
    }
  }
}
