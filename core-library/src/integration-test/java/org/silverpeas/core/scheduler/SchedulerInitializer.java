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
package org.silverpeas.core.scheduler;

import org.silverpeas.core.scheduler.quartz.PersistentQuartzScheduler;
import org.silverpeas.core.scheduler.quartz.VolatileQuartScheduler;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.logging.SilverLogger;

import javax.annotation.PreDestroy;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.inject.Singleton;

/***
 * Initializer of all of the scheduler implementations for the integration tests.
 * The release of the schedulers is performed automatically at shutdown; it is then no necessary to
 * explicitly release them.
 * @author mmoquillon
 */
@Singleton
public class SchedulerInitializer {

  @Inject
  @Default
  private VolatileQuartScheduler defaultScheduler;

  @Inject
  @PersistentScheduling
  private PersistentQuartzScheduler persistentScheduler;

  private boolean initialized = false;

  public enum SchedulerType {
    ALL,
    VOLATILE,
    PERSISTENT
  }

  public static SchedulerInitializer get() {
    return ServiceProvider.getService(SchedulerInitializer.class);
  }

  public void init() {
    init(SchedulerType.ALL);
  }

  public void init(final SchedulerType type) {
    if (!initialized) {
      if (type == SchedulerType.ALL || type == SchedulerType.VOLATILE) {
       defaultScheduler.init();
      }

      if (type == SchedulerType.ALL || type == SchedulerType.PERSISTENT) {
        persistentScheduler.init();
      }

      this.initialized = true;
    }
  }

  @PreDestroy
  public void release() {
    if (initialized) {
      try {
        defaultScheduler.release();
        persistentScheduler.release();
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e);
      } finally {
        this.initialized = false;
      }
    }
  }
}
  