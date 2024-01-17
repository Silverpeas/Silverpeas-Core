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

package org.silverpeas.core.persistence.jcr;

import org.apache.jackrabbit.api.management.DataStoreGarbageCollector;
import org.apache.jackrabbit.api.management.MarkEventListener;
import org.apache.jackrabbit.api.management.RepositoryManager;
import org.apache.jackrabbit.core.RepositoryImpl;
import org.apache.jackrabbit.core.SilverpeasRepositoryManager;
import org.apache.jackrabbit.core.config.RepositoryConfig;
import org.apache.jackrabbit.core.data.FileDataStore;
import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.backgroundprocess.AbstractBackgroundProcessRequest;
import org.silverpeas.core.backgroundprocess.BackgroundProcessTask;
import org.silverpeas.core.backgroundprocess.BackgroundProcessTask.LOCK_DURATION;
import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.core.scheduler.Job;
import org.silverpeas.core.scheduler.JobExecutionContext;
import org.silverpeas.core.scheduler.Scheduler;
import org.silverpeas.core.scheduler.SchedulerException;
import org.silverpeas.core.scheduler.SchedulerProvider;
import org.silverpeas.core.scheduler.trigger.JobTrigger;
import org.silverpeas.core.thread.ManagedThreadPool;
import org.silverpeas.core.util.MemoizedSyncSupplier;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Singleton;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.io.File;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Collections.synchronizedList;
import static java.util.Optional.ofNullable;
import static org.apache.commons.io.FileUtils.sizeOfDirectory;
import static org.silverpeas.core.persistence.jcr.JcrDatastoreTaskMonitor.StatusType.*;
import static org.silverpeas.core.persistence.jcr.JcrRepositoryConnector.openSystemSession;
import static org.silverpeas.core.util.ResourceLocator.getGeneralSettingBundle;
import static org.silverpeas.core.util.ResourceLocator.getSettingBundle;
import static org.silverpeas.core.util.ServiceProvider.getSingleton;

/**
 * This manager handles the datastore of the JCR.
 * @author silveryocha
 */
@Service
@Singleton
public class JcrDatastoreManager {

  private final MemoizedSyncSupplier<DatastorePathView> datastorePathView;
  private JcrDatastoreTaskMonitor previousTask;
  private JcrDatastoreTaskMonitor currentTask;

  JcrDatastoreManager() {
    datastorePathView = new MemoizedSyncSupplier<>(DatastorePathView::new);
  }

  public static JcrDatastoreManager get() {
    return getSingleton(JcrDatastoreManager.class);
  }

  /**
   * Gets the previous ran task if any.
   * @return the optional running {@link JcrDatastoreTaskMonitor} instance.
   */
  public synchronized Optional<JcrDatastoreTaskMonitor> getPreviousTask() {
    return ofNullable(previousTask);
  }

  /**
   * Gets the current running task if any.
   * @return the optional running {@link JcrDatastoreTaskMonitor} instance.
   */
  public synchronized Optional<JcrDatastoreTaskMonitor> getCurrentTask() {
    return ofNullable(currentTask);
  }

  /**
   * Call this method to indicate to this manager that data has been saved into repository.
   * <p>
   * This will perform after a delay of non use of the JCR a treatment of data cleaning.
   * </p>
   */
  public void notifyDataSave() {
    new GarbageCollectorJob().plan();
  }

  /**
   * Forces the JCR datastore garbage collector to run.
   */
  public synchronized void forceGC() {
    if (!isRunningTask()) {
      BackgroundProcessTask.push(new GarbageCollectorBackgroundProcess());
    } else {
      SilverLogger.getLogger(this).warn("Cleaning process of JCR datastore is already running...");
    }
  }

  /**
   * Gets a view upon the JCR Datastore path.
   * @return a {@link DatastorePathView} instance.
   */
  public DatastorePathView getDatastorePathView() {
    return datastorePathView.get();
  }

  /**
   * Indicates that the current task has terminated.
   */
  synchronized void taskIsTerminated() {
    getDatastorePathView().notifyChanges();
    this.previousTask = currentTask;
    this.currentTask = null;
  }

  /**
   * Indicates if a task is currently running.
   * @return true is running, false otherwise.
   */
  public synchronized boolean isRunningTask() {
    return this.currentTask != null;
  }

  /**
   * Indicates that the given task starts running.
   * @param currentTask the running task.
   */
  synchronized void setRunningTask(final GarbageCollectorBackgroundProcess currentTask) {
    this.currentTask = currentTask;
  }

  /**
   * This JOB permits to handle a delay before the JCR cleaning.
   */
  static class GarbageCollectorJob extends Job implements Initialization {
    private static final Object MUTEX = new Object();
    private static final int DELAY_OF_ONE_DAY = 60 * 24;
    private static final int INACTIVE_BY_DEFAULT = 0;

    private GarbageCollectorJob() {
      super("GarbageCollectorJob_JOB_NAME");
    }

    @Override
    public void init() throws Exception {
      setup(true);
    }

    public void plan() {
      setup(false);
    }

    private void setup(final boolean jobInitialization) {
      final int delayInMinutes = getDelayInMinutes();
      final Scheduler scheduler = SchedulerProvider.getVolatileScheduler();
      synchronized (MUTEX) {
        if (isJobEnabled(delayInMinutes)) {
          final boolean setSchedulerRequired;
          if (!mustDelayBeResetAfterEachDataSave(delayInMinutes)) {
            setSchedulerRequired = !scheduler.isJobScheduled(getName());
          } else {
            setSchedulerRequired = !jobInitialization;
          }
          if (setSchedulerRequired) {
            final OffsetDateTime executionTime = OffsetDateTime.now().plusMinutes(delayInMinutes);
            try {
              scheduler.unscheduleJob(getName());
              scheduler.scheduleJob(this, JobTrigger.triggerAt(executionTime));
            } catch (SchedulerException e) {
              SilverLogger.getLogger(this).error(e);
            }
          }
        } else {
          if (scheduler.isJobScheduled(getName())) {
            try {
              scheduler.unscheduleJob(getName());
            } catch (SchedulerException e) {
              SilverLogger.getLogger(this).error(e);
            }
          }
        }
      }
    }

    private int getDelayInMinutes() {
      final SettingBundle settings = getSettingBundle("org.silverpeas.util.attachment.Attachment");
      return settings.getInteger("jcr.datastore.garbage.collector.delay", INACTIVE_BY_DEFAULT);
    }

    private boolean mustDelayBeResetAfterEachDataSave(final int delay) {
      return delay / DELAY_OF_ONE_DAY < 1;
    }

    private boolean isJobEnabled(final int delayInMinutes) {
      return delayInMinutes > 0;
    }

    @Override
    public void execute(final JobExecutionContext context) {
      JcrDatastoreManager.get().forceGC();
    }
  }

  /**
   * This background process ensures that to not overload the server with JCR cleaning treatments.
   */
  static class GarbageCollectorBackgroundProcess extends AbstractBackgroundProcessRequest
      implements MarkEventListener, JcrDatastoreTaskMonitor {

    private final SilverLogger logger;
    private final AtomicLong nodeCounter = new AtomicLong(0L);
    private final List<Status> statuses = synchronizedList(new ArrayList<>(StatusType.values().length));
    private Exception error;

    private GarbageCollectorBackgroundProcess() {
      super("JcrGarbageCollectorBackgroundProcess", LOCK_DURATION.NO_TIME);
      logger = SilverLogger.getLogger(this);
      statuses.add(new Status(WAITING));
      JcrDatastoreManager.get().setRunningTask(this);
    }

    @Override
    public String getType() {
      return "DATASTORE_PURGE";
    }

    @Override
    public List<Status> getStatuses() {
      // This code allows to avoid getting concurrent access
      return Stream.of(statuses.toArray(new Status[0])).collect(Collectors.toList());
    }

    @Override
    public Optional<Exception> getError() {
      return ofNullable(error);
    }

    @Override
    public long getNbNodeProcessed() {
      return nodeCounter.get();
    }

    @Override
    public void beforeScanning(final Node node) throws RepositoryException {
      final long nbNodes = nodeCounter.incrementAndGet() - 1;
      if (nbNodes >= 5000 && nbNodes % 5000 == 0) {
        logger.debug(() -> format("%s - Scanned %s nodes...", getType(), nbNodes));
      }
    }

    @Override
    protected void process() {
      statuses.add(new Status(STARTED));
      final JcrDatastoreManager jcrDatastoreManager = JcrDatastoreManager.get();
      logger.debug(() -> format("%s - Starting process", getType()));
      try (final JcrSession session = openSystemSession()) {
        final RepositoryManager repositoryManager = new SilverpeasRepositoryManager(
            session.getRepository());
        final DataStoreGarbageCollector gc = repositoryManager.createDataStoreGarbageCollector();
        gc.setMarkEventListener(this);
        try {
          logger.debug(() -> format("%s - Marking data to delete", getType()));
          statuses.add(new Status(MARKING));
          gc.mark();
          logger.debug(() -> format("%s - Deleting marked data", getType()));
          statuses.add(new Status(DELETING));
          gc.sweep();
        } finally {
          gc.close();
        }
      } catch (RepositoryException e) {
        logger.error(e);
        error = e;
        logger.debug(() -> format("%s - Ending process with errors", getType()));
        throw new SilverpeasRuntimeException(e);
      } finally {
        jcrDatastoreManager.taskIsTerminated();
        statuses.add(new Status(TERMINATED));
        logger.debug(() -> format("%s - Ending process successfully", getType()));
      }
    }
  }

  /**
   * Some data about the Path of the JCR DataStore.
   */
  public static class DatastorePathView {
    private static final Object MUTEX = new Object();
    private final String pathWithVariable;
    private final File path;
    private Future<Long> size;

    protected DatastorePathView() {
      final String dataHome = new File(getGeneralSettingBundle().getString("dataHomePath")).getPath();
      try (final JcrSession session = openSystemSession()) {
        final RepositoryConfig config = ((RepositoryImpl) session.getRepository()).getConfig();
        final FileDataStore dataStore = (FileDataStore) config.getDataStore();
        final String datastorePath = new File(dataStore.getPath()).getPath().replace(dataHome, "");
        path = Paths.get(dataHome, datastorePath).toFile();
        pathWithVariable = Paths.get("$SILVERPEAS_DATA_HOME", datastorePath).toFile().getPath();
      } catch (RepositoryException e) {
        SilverLogger.getLogger(this).error(e);
        throw new SilverpeasRuntimeException(e);
      }
    }

    /**
     * Gets the path with $SILVERPEAS_DATA_HOME environment variable.
     * <p>
     *   This is for displaying purpose.
     * </p>
     * @return a string representing a path.
     */
    public String getPathWithVariable() {
      return pathWithVariable;
    }

    /**
     * Indicates that the path is valid.
     * @return true if valid, false otherwise.
     */
    public boolean isValid() {
      return path.isDirectory();
    }

    /**
     * Gets the size, in bytes, of elements contained into Datastore path.
     * <p>
     *   If the optional is empty, that is because it has not be yet computed, but
     * </p>
     * @return an optional size of the JCR Datastore path content.
     */
    public Optional<Long> getContentSize() {
      synchronized (MUTEX) {
        if (isValid() && size == null) {
          try {
            size = ManagedThreadPool.getPool().invoke(() -> sizeOfDirectory(path));
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          }
        }
        return ofNullable(size).filter(Future::isDone).map(s -> {
          try {
            return s.get();
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
          } catch (ExecutionException ignore) {
            // nothing to do
          }
          size = null;
          return null;
        });
      }
    }

    public void notifyChanges() {
      synchronized (MUTEX) {
        size = null;
      }
    }
  }
}
