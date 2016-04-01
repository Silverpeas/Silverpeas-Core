/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.core.viewer.service;

import org.apache.commons.lang3.tuple.Pair;
import org.silverpeas.core.viewer.model.ViewerSettings;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.io.temp.TemporaryDataManagementSetting;
import org.silverpeas.core.io.temp.TemporaryWorkspaceTranslation;

import java.io.File;
import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;

import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.io.FilenameUtils.getFullPath;

/**
 * @author Yohann Chastagnier
 */
public abstract class AbstractViewerService {

  private static final Object OBJECT_FOR_SYNC = new Object();
  private static final ConcurrentMap<String, Object> cache = new ConcurrentHashMap<>();

  // Extension of pdf document file
  public static final String PDF_DOCUMENT_EXTENSION = "pdf";

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
   * This method permits to start the setting of a {@link ViewerTreatment}.<br/>
   * It manages also a cache mechanism in order to avoid taking too much resources at a same time
   * and also in order to preserve memory space of filesystem.
   * @param processName the name of the process (preview for example).
   * @param viewerTreatment the treatment to perform.
   * @param <RETURN_VALUE>
   * @return
   */
  protected <RETURN_VALUE extends Serializable> ViewerProcess<RETURN_VALUE> process(
      String processName, ViewerTreatment<RETURN_VALUE> viewerTreatment) {
    return new ViewerProcess<>(processName, viewerTreatment);
  }

  /**
   * This class handles the execution of a {@link ViewerTreatment}.
   * It provides the centralization of caching synchronization.
   * @param <RETURN_VALUE> the type of the value returned by the viewer treatment.
   * @return the value computed by the specified viewer treatment.
   */
  protected final class ViewerProcess<RETURN_VALUE extends Serializable> {
    private final static String CACHE_WORKSPACE_KEY_PREFIX = "workspace_viewer_services_";
    private final static String CACHE_RESULT_KEY = "cache_result";
    private final String processName;
    private final ViewerTreatment<RETURN_VALUE> viewerTreatment;

    /**
     * Default constructor.
     * @param processName
     * @param viewerTreatment
     */
    protected ViewerProcess(final String processName,
        final ViewerTreatment<RETURN_VALUE> viewerTreatment) {
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
        String workspaceCacheKey = CACHE_WORKSPACE_KEY_PREFIX + viewerContext.getViewId();
        TemporaryWorkspaceTranslation workspace =
            (TemporaryWorkspaceTranslation) cache.get(workspaceCacheKey);
        if (workspace == null) {
          workspace = viewerContext.getWorkspace();
          cache.put(workspaceCacheKey, workspace);
        }

        // If the workspace already exists, then retrieving the semaphore of a working process if
        // any, creating a new one otherwise (in the second case and for a same resource, a request
        // will never wait for the end of an other).
        if (workspace.exists()) {

          // If the original resource has not changed since the last conversion, then getting the
          // converted data that exist already.
          if (workspace.lastModified() >= viewerContext.getOriginalSourceFile().lastModified()) {
            Semaphore currentProcessing = (Semaphore) cache.get(workspace.getRootPath().getPath());
            if (currentProcessing != null) {
              return Pair.of(workspace, currentProcessing);
            }

            // At this level, if no data is in cache, so the server has been killed or an
            // exception has been thrown in a previous conversion treatment.
            // So, trying again...
            if (workspace.get(CACHE_RESULT_KEY) == null) {
              workspace.remove();
            } else {
              // Handle time to live in cache if necessary
              if (ViewerSettings.isTimeToLiveEnabled()) {
                long fileAgeThreshold = DateUtil.getNow().getTime() -
                    ((long) (TemporaryDataManagementSetting.getTimeAfterThatFilesMustBeDeleted() *
                        0.25));
                if (workspace.lastModified() < fileAgeThreshold) {
                  workspace.updateLastModifiedDate();
                }
              }
              // Data exists in cache, returning a new Semaphore
              return Pair.of(workspace, new Semaphore(1));
            }
          } else {
            // Source file has been modified, the conversion processes must be performed again.
            workspace.remove();
          }
        }

        // The workspace does not exist, it must be created and put into application cache.
        viewerContext.processingCache();
        workspace.create();
        Semaphore newSemaphore = new Semaphore(1);
        cache.put(workspace.getRootPath().getPath(), newSemaphore);
        return Pair.of(workspace, newSemaphore);
      }
    }

    /**
     * Finalizes the treatment according to the context.
     * @param viewerContext the context of current view processing.
     * @param init the one obtained with {@link #initialize(ViewerContext)}.
     */
    private void finalize(ViewerContext viewerContext,
        Pair<TemporaryWorkspaceTranslation, Semaphore> init) {
      synchronized (OBJECT_FOR_SYNC) {
        String workspaceCacheKey = CACHE_WORKSPACE_KEY_PREFIX + viewerContext.getViewId();
        cache.remove(workspaceCacheKey);
        cache.remove(viewerContext.getWorkspace().getRootPath().getPath());
      }
      init.getValue().release();
    }

    /**
     * This method calls the execute method of a {@link ViewerTreatment} instance.
     * One of the aim of this mechanism is to centralize tha management of caching.
     * @return the value computed by the specified viewer treatment.
     */
    @SuppressWarnings("unchecked")
    public RETURN_VALUE execute(ViewerContext viewerContext) {
      viewerContext.fromInitializerProcessName(processName);
      if (!viewerContext.isCacheRequired()) {
        RETURN_VALUE returnValue = viewerTreatment.execute();
        return viewerTreatment.performAfterSuccess(returnValue);
      }

      // Acquiring a semaphore and acquiring it
      Pair<TemporaryWorkspaceTranslation, Semaphore> init = initialize(viewerContext);
      TemporaryWorkspaceTranslation workspace = init.getKey();
      Semaphore semaphore = init.getValue();
      try {
        semaphore.acquire();
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }

      // Dealing with the cache mechanism
      final RETURN_VALUE returnValue;
      try {
        if (viewerContext.isProcessingCache()) {
          // The current process is the one in charge of processing the data
          returnValue = viewerTreatment.execute();
          workspace.put(CACHE_RESULT_KEY, returnValue);
        } else {
          // The data have been processed by an other process
          returnValue = workspace.get(CACHE_RESULT_KEY);
        }
      } catch (RuntimeException re) {
        workspace.put(CACHE_RESULT_KEY, null);
        throw re;
      } finally {
        finalize(viewerContext, init);
      }

      // After successful dealing with cache, then performing a other treatment if any implemented.
      return viewerTreatment.performAfterSuccess(returnValue);
    }
  }

  /**
   * Inner class handled by
   * @param <RETURN_VALUE>
   */
  protected abstract class ViewerTreatment<RETURN_VALUE> {

    public abstract RETURN_VALUE execute();

    /**
     * This method is called after a successful execution of {@link #execute()} method.
     * @param result the result obtained with {@link #execute()}.
     * @return the result.
     */
    public RETURN_VALUE performAfterSuccess(RETURN_VALUE result) {
      return result;
    }
  }
}
