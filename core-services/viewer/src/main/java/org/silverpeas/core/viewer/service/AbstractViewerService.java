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
package org.silverpeas.core.viewer.service;

import org.apache.commons.lang3.tuple.Pair;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.io.temp.TemporaryDataManagementSetting;
import org.silverpeas.core.io.temp.TemporaryWorkspaceTranslation;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.viewer.model.ViewerSettings;

import java.io.File;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;

import static java.text.MessageFormat.format;
import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.io.FilenameUtils.getFullPath;
import static org.silverpeas.core.util.logging.SilverLogger.getLogger;
import static org.silverpeas.core.viewer.model.ViewerSettings.nbMaxConversionsAtSameInstant;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractViewerService {

  // Extension of pdf document file
  public static final String PDF_DOCUMENT_EXTENSION = "pdf";

  private static final Object OBJECT_FOR_SYNC = new Object();
  private static final ConcurrentMap<String, Object> cache = new ConcurrentHashMap<>();

  private static final Object EXECUTION_SEM_MUTEX = new Object();
  private static int executionSemCount = nbMaxConversionsAtSameInstant();
  private static final Semaphore EXECUTION_SEM = new Semaphore(executionSemCount);

  /**
   * Generate a tmp file
   * @param fileExtension
   * @return
   */
  protected File generateTmpFile(final ViewerContext viewerContext, final String fileExtension) {
    TemporaryWorkspaceTranslation workspace = viewerContext.getWorkspace();
    if (!workspace.exists()) {
      // It is a not cached treatment, there is no need to use workspace translation.
      workspace.getRootPath().mkdirs();
    }
    return new File(workspace.getRootPath(), "file." + fileExtension);
  }

  /**
   * Changes the extension of a file
   * @param fileExtension
   * @return
   */
  protected File changeFileExtension(final File file, final String fileExtension) {
    return new File(
        getFullPath(file.getPath()) + getBaseName(file.getPath()) + "." + fileExtension);
  }

  /**
   * This method permits to start the setting of a {@link ViewerTreatment}.<br>
   * It manages also a cache mechanism in order to avoid taking too much resources at a same time
   * and also in order to preserve memory space of filesystem.
   * @param processName the name of the process (preview for example).
   * @param viewerTreatment the treatment to perform.
   * @param <R>
   * @return
   */
  protected <R extends Serializable> ViewerProcess<R> process(
      String processName, ViewerTreatment<R> viewerTreatment) {
    return new ViewerProcess<>(processName, viewerTreatment);
  }

  protected interface ViewerTreatment<R> {

    R execute();

    /**
     * This method is called after a successful execution of {@link #execute()} method.
     * @param result the result obtained with {@link #execute()}.
     * @return the result.
     */
    default R performAfterSuccess(R result) {
      return result;
    }
  }

  /**
   * This class handles the execution of a {@link ViewerTreatment}.
   * It provides the centralization of caching synchronization.
   * @param <R> the type of the value returned by the viewer treatment.
   * @return the value computed by the specified viewer treatment.
   */
  protected final class ViewerProcess<R extends Serializable> {
    private static final String CACHE_WORKSPACE_KEY_PREFIX = "workspace_viewer_services_";
    private static final String CACHE_RESULT_KEY = "cache_result";
    private final String processName;
    private final ViewerTreatment<R> viewerTreatment;

    /**
     * Default constructor.
     * @param processName
     * @param viewerTreatment
     */
    protected ViewerProcess(final String processName,
        final ViewerTreatment<R> viewerTreatment) {
      this.processName = processName;
      this.viewerTreatment = viewerTreatment;
    }

    /**
     * Initializes the treatment and returns a semaphore in order to handle concurrency processes
     * working on same resource.
     * @param viewerContext the context of current view processing.
     * @return a pair of {@link TemporaryWorkspaceTranslation} and {@link Semaphore}.
     */
    private Pair<TemporaryWorkspaceTranslation, Semaphore> initialize(ViewerContext viewerContext) {
      synchronized (OBJECT_FOR_SYNC) {
        final String workspaceCacheKey = CACHE_WORKSPACE_KEY_PREFIX + viewerContext.getViewId();
        getLogger(this).debug(
            () -> format("initializing workspace of view context {0}", viewerContext.getViewId()));
        final TemporaryWorkspaceTranslation workspace = (TemporaryWorkspaceTranslation) cache
            .computeIfAbsent(workspaceCacheKey, k -> viewerContext.getWorkspace());

        // If the workspace already exists, then retrieving the semaphore of a working process if
        // any, creating a new one otherwise (in the second case and for a same resource, a request
        // will never wait for the end of an other).
        if (workspace.exists()) {
          getLogger(this).debug(() -> format("workspace of view context {0} exists already",
              viewerContext.getViewId()));

          if (!workspace.isWorkInProgress() && workspace.empty()) {
            // If workspace is empty, something is wrong and the workspace must be removed to be
            // again created.
            getLogger(this).debug(
                () -> format("workspace is empty for view context {0}, create it again",
                    viewerContext.getViewId()));
            workspace.remove();
          } else if (workspace.lastModified() >=
              viewerContext.getOriginalSourceFile().lastModified()) {
            // If the original resource has not changed since the last conversion, then getting the
            // converted data that exist already.
            Semaphore currentProcessing = (Semaphore) cache.get(workspace.getRootPath().getPath());
            if (currentProcessing != null) {
              getLogger(this).debug(() -> format("semaphore exists already for view context {0}",
                  viewerContext.getViewId()));
              return Pair.of(workspace, currentProcessing);
            }

            // At this level, if no data is in cache, so the server has been killed or an
            // exception has been thrown in a previous conversion treatment.
            // So, trying again...
            if (workspace.get(CACHE_RESULT_KEY) == null) {
              getLogger(this).debug(() -> format(
                  "no conversion result in cache, removing workspace of view context {0} for new " +
                      "creation", viewerContext.getViewId()));
              workspace.remove();
            } else {
              return renewSemaphore(workspaceCacheKey, workspace);
            }
          } else {
            // Source file has been modified, the conversion processes must be performed again.
            getLogger(this).debug(() -> format(
                "remove workspace of view context {0} because file {1} has been modified",
                viewerContext.getViewId(), viewerContext.getOriginalSourceFile().getName()));
            workspace.remove();
          }
        }

        // The workspace does not exist, it must be created and put into application cache.
        getLogger(this).debug(
            () -> format("creating workspace of view context {0} with its semaphore",
                viewerContext.getViewId()));
        viewerContext.processingCache();
        workspace.markWorkInProgress();
        workspace.create();
        Semaphore newSemaphore = new Semaphore(1);
        cache.put(workspace.getRootPath().getPath(), newSemaphore);
        return Pair.of(workspace, newSemaphore);
      }
    }

    private Pair<TemporaryWorkspaceTranslation, Semaphore> renewSemaphore(
        final String workspaceCacheKey, final TemporaryWorkspaceTranslation workspace) {
      // Handle time to live in cache if necessary
      if (ViewerSettings.isTimeToLiveEnabled()) {
        long fileAgeThreshold = DateUtil.getNow().getTime() -
            ((long) (TemporaryDataManagementSetting.getTimeAfterThatFilesMustBeDeleted() * 0.25));
        if (workspace.lastModified() < fileAgeThreshold) {
          workspace.updateLastModifiedDate();
        }
      }
      // Data exists in cache, returning a new Semaphore
      getLogger(this)
          .debug(() -> format("creating semaphore for workspace {0}", workspaceCacheKey));
      return Pair.of(workspace, new Semaphore(1));
    }

    /**
     * Finalizes the treatment according to the context.
     * @param viewerContext the context of current view processing.
     * @param init the one obtained with {@link #initialize(ViewerContext)}.
     */
    private void performAtExecutionEnd(ViewerContext viewerContext,
        Pair<TemporaryWorkspaceTranslation, Semaphore> init) {
      synchronized (OBJECT_FOR_SYNC) {
        String workspaceCacheKey = CACHE_WORKSPACE_KEY_PREFIX + viewerContext.getViewId();
        cache.remove(workspaceCacheKey);
        cache.remove(viewerContext.getWorkspace().getRootPath().getPath());
      }
      getLogger(this).debug(
          () -> format("releasing semaphore dedicated to workspace of view context {0}",
              viewerContext.getViewId()));
      init.getValue().release();
    }

    /**
     * This method calls the execute method of a {@link ViewerTreatment} instance.
     * One of the aim of this mechanism is to centralize tha management of caching.
     * @return the value computed by the specified viewer treatment.
     */
    public R execute(ViewerContext viewerContext) {
      viewerContext.fromInitializerProcessName(processName);
      if (!viewerContext.isCacheRequired()) {
        getLogger(this).debug(() -> "no cache required, performing document conversion");
        final R returnValue = doConversion();
        return viewerTreatment.performAfterSuccess(returnValue);
      }

      // Acquiring a semaphore and acquiring it
      final Pair<TemporaryWorkspaceTranslation, Semaphore> init = initialize(viewerContext);
      final TemporaryWorkspaceTranslation workspace = init.getKey();
      final Semaphore semaphore = init.getValue();
      try {
        getLogger(this).debug(
            () -> format("acquiring semaphore dedicated to workspace of view context {0}",
                viewerContext.getViewId()));
        semaphore.acquire();
      } catch (Exception e) {
        throw new SilverpeasRuntimeException(e);
      }

      // Dealing with the cache mechanism
      final R returnValue;
      try {
        if (viewerContext.isProcessingCache()) {
          // The current process is the one in charge of processing the data
          getLogger(this).debug(
              () -> format("performing document conversion into workspace of view context {0}",
                  viewerContext.getViewId()));
          returnValue = doConversion();
          workspace.put(CACHE_RESULT_KEY, returnValue);
        } else {
          // The data have been processed by an other process
          getLogger(this).debug(
              () -> format("getting document conversion from workspace cache of view context {0}",
                  viewerContext.getViewId()));
          returnValue = workspace.get(CACHE_RESULT_KEY);
        }
      } catch (RuntimeException re) {
        getLogger(this).error(re);
        workspace.put(CACHE_RESULT_KEY, null);
        throw re;
      } finally {
        performAtExecutionEnd(viewerContext, init);
      }

      // After successful dealing with cache, then performing a other treatment if any implemented.
      return viewerTreatment.performAfterSuccess(returnValue);
    }

    private R doConversion() {
      // The current process is the one in charge of processing the data
      try {
        EXECUTION_SEM.acquire();
        synchronized (EXECUTION_SEM_MUTEX) {
          executionSemCount = executionSemCount - 1;
          getLogger(this)
              .debug(() -> format("acquiring access (new count of {0})", executionSemCount));
        }
        return viewerTreatment.execute();
      } catch (Exception e) {
        throw new SilverpeasRuntimeException(e);
      } finally {
        EXECUTION_SEM.release();
        synchronized (EXECUTION_SEM_MUTEX) {
          executionSemCount = executionSemCount + 1;
          getLogger(this)
              .debug(() -> format("releasing access (new count of {0})", executionSemCount));
        }
      }
    }
  }
}
