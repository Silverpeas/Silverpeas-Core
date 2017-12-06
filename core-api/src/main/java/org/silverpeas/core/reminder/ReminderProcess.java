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
package org.silverpeas.core.reminder;

import org.silverpeas.core.scheduler.SchedulerEvent;
import org.silverpeas.core.scheduler.SchedulerEventListener;
import org.silverpeas.core.util.ServiceProvider;

import javax.inject.Singleton;

/**
 * The process to send a notification to the user aimed by a reminder.
 * @author mmoquillon
 */
@Singleton
public class ReminderProcess implements SchedulerEventListener {

  public static ReminderProcess get() {
    return ServiceProvider.getService(ReminderProcess.class);
  }

  @Override
  public void triggerFired(final SchedulerEvent anEvent) throws Exception {

  }

  @Override
  public void jobSucceeded(final SchedulerEvent anEvent) {

  }

  @Override
  public void jobFailed(final SchedulerEvent anEvent) {

  }
}
  