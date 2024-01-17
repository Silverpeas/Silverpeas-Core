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
package org.silverpeas.core.scheduler.quartz;

import org.quartz.JobDetail;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.TransactionRuntimeException;
import org.silverpeas.core.scheduler.Job;
import org.silverpeas.core.scheduler.PersistentScheduling;
import org.silverpeas.core.scheduler.SchedulerEventListener;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.inject.Singleton;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * A persistent scheduler implementation using Quartz as scheduling backend. It wraps a Quartz
 * scheduler and delegates to it all of the calls after transforming the parameters into their
 * Quartz counterparts. The Quartz scheduler is configured to use a JDBC Job store instead of a
 * RAM Job store so that all the jobs being scheduled are also persisted and then can be retrieved
 * after a restart of the scheduling backend. Because of the persistence of the jobs and of the
 * triggers, the scheduler mechanism is more weighty and takes more time to fire jobs. This is why
 * such scheduler should be use with care and only for very short-time jobs.
 * @author mmoquillon
 */
@Service
@Singleton
@PersistentScheduling
public class PersistentQuartzScheduler extends QuartzScheduler {

  private static final String QUARTZ_PROPERTIES =
      "org.silverpeas.scheduler.settings.persistent-scheduler";

  /**
   * Constructs a new persistent scheduler.
   */
  protected PersistentQuartzScheduler() {
  }

  @Override
  public void init() {
    setUpQuartzScheduler(QUARTZ_PROPERTIES);
  }

  @Override
  public void release() throws Exception {
    shutdown();
  }

  @Override
  protected String encodeJob(final Job job) {
    return job.getClass().getName();
  }

  @Override
  protected String encodeEventListener(final SchedulerEventListener listener) {
    return listener.getClass().getName();
  }

  @Override
  @SuppressWarnings("unchecked")
  protected Class<PersistentJobExecutor> getJobExecutor() {
    return PersistentJobExecutor.class;
  }

  @Override
  protected <T> void execute(final SchedulingTask<T> schedulingTask) throws SchedulerException {
    try {
      Transaction.performInOne(() -> {
        schedulingTask.execute();
        return null;
      });
    } catch (TransactionRuntimeException e) {
      throw new SchedulerException(e.getCause());
    }
  }

  public static class PersistentJobExecutor extends JobExecutor {

    protected Job getJob(final JobDetail jobDetail) throws JobExecutionException {
      String jobClass = jobDetail.getJobDataMap().getString(ACTUAL_JOB);
      String jobName = jobDetail.getKey().getName();
      return getByReflection(jobClass, jobName);
    }

    protected SchedulerEventListener getSchedulerEventListener(final JobDetail jobDetail)
        throws JobExecutionException {
      String listenerClass = jobDetail.getJobDataMap().getString(JOB_LISTENER);
      return getByReflection(listenerClass, null);
    }

    @SuppressWarnings("unchecked")
    private <T> T getByReflection(final String className, final String param)
        throws JobExecutionException {
      Class<T> propertyClass;
      try {
        propertyClass = (Class<T>) Class.forName(className);
      } catch (ClassNotFoundException e) {
        throw new JobExecutionException(e);
      }

      try {
        return ServiceProvider.getService(propertyClass);
      } catch (IllegalStateException e) {
        SilverLogger.getLogger(getClass())
            .debug("The " + className + " isn't in the IoC container");
      }

      try {
        if (StringUtil.isDefined(param)) {
          Constructor<T> constructor = propertyClass.getDeclaredConstructor(String.class);
          return constructor.newInstance(param);
        } else {
          Constructor<T> constructor = propertyClass.getDeclaredConstructor();
          return constructor.newInstance();
        }
      } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
          InvocationTargetException e) {
        throw new JobExecutionException(e);
      }
    }
  }
}
  