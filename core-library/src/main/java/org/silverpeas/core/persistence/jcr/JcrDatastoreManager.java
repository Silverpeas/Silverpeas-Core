/*
 * Copyright (C) 2000 - 2020 Silverpeas
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

package org.silverpeas.core.persistence.jcr;

import org.apache.jackrabbit.api.management.DataStoreGarbageCollector;
import org.apache.jackrabbit.api.management.RepositoryManager;
import org.apache.jackrabbit.core.SilverpeasRepositoryManager;
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
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Singleton;
import javax.jcr.RepositoryException;
import java.time.OffsetDateTime;

import static org.silverpeas.core.persistence.jcr.JcrRepositoryConnector.openSystemSession;
import static org.silverpeas.core.util.ResourceLocator.getSettingBundle;
import static org.silverpeas.core.util.ServiceProvider.getSingleton;

/**
 * This manager handles the datastore of the JCR.
 * @author silveryocha
 */
@Service
@Singleton
public class JcrDatastoreManager {

  JcrDatastoreManager() {
  }

  public static JcrDatastoreManager get() {
    return getSingleton(JcrDatastoreManager.class);
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
      BackgroundProcessTask.push(new GarbageCollectorBackgroundProcess());
    }
  }

  /**
   * This background process ensures that to not overload the server with JCR cleaning treatments.
   */
  static class GarbageCollectorBackgroundProcess extends AbstractBackgroundProcessRequest {

    private GarbageCollectorBackgroundProcess() {
      super("JcrGarbageCollectorBackgroundProcess", LOCK_DURATION.NO_TIME);
    }

    @Override
    protected void process() {
      SilverLogger.getLogger(this).debug("Cleaning the datastore of the JCR");
      try (final JcrSession session = openSystemSession()) {
        final RepositoryManager repositoryManager = new SilverpeasRepositoryManager(session.getRepository());
        final DataStoreGarbageCollector gc = repositoryManager.createDataStoreGarbageCollector();
        try {
          gc.mark();
          gc.sweep();
        } finally {
          gc.close();
        }
      } catch (RepositoryException e) {
        SilverLogger.getLogger(this).error(e);
        throw new SilverpeasRuntimeException(e);
      }
    }
  }
}
