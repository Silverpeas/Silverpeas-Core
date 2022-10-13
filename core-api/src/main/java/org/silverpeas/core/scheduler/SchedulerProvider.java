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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.scheduler;

import org.silverpeas.core.util.ServiceProvider;

import javax.enterprise.util.AnnotationLiteral;

/**
 * The factory of Scheduler objects. The <code>SchedulerFactory</code> class wraps the actual
 * scheduling backend. It delivers SchedulerFactory instances that are built upon this backend, so
 * that they have the capability to provide instances of the actual scheduler implementation. It is
 * the single entry point to the actual scheduling system for Silverpeas components.
 */
public class SchedulerProvider {

  private SchedulerProvider() {

  }

  /**
   * Gets a volatile scheduler from the underlying scheduling backend. A volatile schedule is a
   * scheduler that stores all the scheduled jobs in memory meaning they will be lost at each
   * runtime restart. Such jobs requires then to be scheduled again. Such scheduler is useful for
   * occasional jobs.
   * @return an instance of the actual scheduler implementation.
   */
  public static Scheduler getVolatileScheduler() {
    return ServiceProvider.getSingleton(Scheduler.class);
  }

  /**
   * Gets a persistent scheduler from the underlying scheduling backend. A persistent scheduler is
   * a scheduler that stores all the scheduled jobs and triggers into a persistence context so
   * that they can be retrieved later between different runtime bootstrapping. Such a scheduler is
   * more weighty than the volatile one and requires to be used with case and mainly for very
   * short-time jobs. Warning, in order to
   * be persisted a {@link Job} or a {@link SchedulerEventListener} requires to be stateless and
   * not anonymous; indeed, only their class name is serialized so that they can be constructed
   * at each job
   * triggering meaning that any change in their execution logic will be then taken into account.
   * @return an instance of the actual implementation of a persistent scheduler.
   */
  public static Scheduler getPersistentScheduler() {
    return ServiceProvider.getService(Scheduler.class,
        new AnnotationLiteral<PersistentScheduling>() {});
  }
}
