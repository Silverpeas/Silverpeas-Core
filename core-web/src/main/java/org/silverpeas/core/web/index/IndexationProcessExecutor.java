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
package org.silverpeas.core.web.index;

import org.apache.commons.lang3.tuple.Pair;
import org.silverpeas.core.cache.model.Cache;
import org.silverpeas.core.cache.service.CacheAccessorProvider;
import org.silverpeas.core.thread.ManagedThreadPool;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Singleton;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import static org.silverpeas.core.thread.ManagedThreadPool.ExecutionConfig.defaultConfig;

/**
 * Handles the execution of huge indexation process.
 * @author Yohann Chastagnier
 */
@Singleton
public class IndexationProcessExecutor {
  private static final String INDEXATION_PROCESS_EXECUTOR_KEY = "INDEXATION_PROCESS_EXECUTOR_KEY";

  /**
   * Hidden constructor.
   */
  private IndexationProcessExecutor() {
  }

  public static IndexationProcessExecutor get() {
    return ServiceProvider.getService(IndexationProcessExecutor.class);
  }

  /**
   * Indicates if an indexation operation is currently running.
   * @return true if there is a current execution, false otherwise.
   */
  @SuppressWarnings("unchecked")
  public boolean isCurrentExecution() {
    final Cache cache = CacheAccessorProvider.getApplicationCacheAccessor().getCache();
    Pair<IndexationProcess, Future<Void>> process =
        cache.get(INDEXATION_PROCESS_EXECUTOR_KEY, Pair.class);
    return process != null && !process.getRight().isDone();
  }

  /**
   * Stops a current indexation operation if it exists a running one.<br>
   * Otherwise, nothing is done.
   */
  @SuppressWarnings({"unchecked", "WeakerAccess", "unused"})
  public void stopCurrentExecutionIfAny() {
    final Cache cache = CacheAccessorProvider.getApplicationCacheAccessor().getCache();
    Pair<IndexationProcess, Future<Void>> process =
        cache.get(INDEXATION_PROCESS_EXECUTOR_KEY, Pair.class);
    final Future<Void> task = (process != null) ? process.getRight() : null;
    if (process != null && !task.isDone()) {
      try {
        task.cancel(true);
      } catch (Exception e) {
        SilverLogger.getLogger(this).error("stopping indexation process failed", e);
      }
    }
  }

  /**
   * Executes an indexation process.<br>
   * There is no error of an other one is already running.<br>
   * The monitoring of executions has to be performed by caller by using {@link
   * #isCurrentExecution()} and {@link #stopCurrentExecutionIfAny()} methods.
   * @param indexationProcess the indexation process to execute.
   */
  public void execute(IndexationProcess indexationProcess) {
    final Cache cache = CacheAccessorProvider.getApplicationCacheAccessor().getCache();
    try {
      if (isCurrentExecution()) {
        SilverLogger.getLogger(this).warn(
            "an indexation processing is already running, how is it possible ? That is a huge " +
                "treatment which one and only one should be running at a same time!!!");
        SilverLogger.getLogger(this).warn("so the new indexation request has been ignored");
        return;
      }
      Future<Void> future =
          ManagedThreadPool.getPool().invoke(indexationProcess, defaultConfig().withMaxThreadPoolSizeOf(1));
      cache.put(INDEXATION_PROCESS_EXECUTOR_KEY, Pair.of(indexationProcess, future), 0, 0);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error("starting indexation process failed", e);
      if (e instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }
    }
  }

  public abstract static class IndexationProcess implements Callable<Void> {

    public abstract void perform();

    @Override
    public Void call() {
      final String threadId = String.valueOf(Thread.currentThread().getId());
      try {
        SilverLogger.getLogger(IndexationProcessExecutor.class)
            .info("starting indexation process on thread with id ''{0}'')", threadId);
        perform();
        SilverLogger.getLogger(IndexationProcessExecutor.class)
            .info("ending indexation process on thread with id ''{0}'')", threadId);
      } catch (Exception e) {
        SilverLogger.getLogger(IndexationProcessExecutor.class)
            .error("indexation process failure on thread with id ''{0}'')", new Object[]{threadId},
                e);
      }
      return null;
    }
  }
}
