/**
 * Copyright (C) 2000 - 2012 Silverpeas
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

package com.silverpeas.scheduler;

import com.stratelia.silverpeas.silvertrace.SilverTrace;
import javax.inject.Inject;

/**
 * The factory of Scheduler objects. The <code>SchedulerFactory</code> class wraps the actual
 * scheduling backend. It delivers SchedulerFactory instances that are built upon this backend, so
 * that they have the capability to provide instances of the actual scheduler implementation. It is
 * the single entry point to the actual scheduling system for Silverpeas components.
 */
public class SchedulerFactory {

  /**
   * The name of the scheduling system in Silverpeas.
   */
  public static final String MODULE_NAME = "scheduler";
  private static final SchedulerFactory instance = new SchedulerFactory();
  @Inject
  private Scheduler actualScheduler;

  /**
   * Gets a SchedulerFactory instance.
   * @return a SchedulerFactory instance.
   */
  public static SchedulerFactory getFactory() {
    return instance;
  }

  /**
   * Gets a scheduler from the underlying scheduling backend.
   * @return an instance of the actual scheduler implementation.
   */
  public Scheduler getScheduler() {
    if (actualScheduler == null) {
      SilverTrace.error(SchedulerFactory.MODULE_NAME, getClass().getSimpleName() +
          ".getScheduler()",
          "root.EX_NO_MESSAGE", "Unable to initialize the scheduling backend");
    }
    return actualScheduler;
  }

  /**
   * Boostraps a scheduling system.
   */
  private SchedulerFactory() {
  }
}
