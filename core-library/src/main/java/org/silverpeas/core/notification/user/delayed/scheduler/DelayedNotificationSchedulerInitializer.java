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
package org.silverpeas.core.notification.user.delayed.scheduler;

import org.silverpeas.core.scheduler.Scheduler;
import org.silverpeas.core.scheduler.trigger.JobTrigger;
import org.silverpeas.core.notification.user.client.AbstractNotification;
import org.apache.commons.lang3.StringUtils;
import org.silverpeas.core.initialization.Initialization;

import javax.inject.Inject;

import static org.silverpeas.core.notification.user.client.NotificationManagerSettings
    .getCronOfDelayedNotificationSending;

/**
 * @author Yohann Chastagnier
 */
public class DelayedNotificationSchedulerInitializer extends AbstractNotification
    implements Initialization {

  public static final String JOB_NAME = "DelayedNotificationJob";

  @Inject
  private Scheduler scheduler;
  @Inject
  private DelayedNotificationListener listener;

  @Override
  public void init() throws Exception {
    final String cron = getCronOfDelayedNotificationSending();
    scheduler.unscheduleJob(JOB_NAME);
    if (StringUtils.isNotBlank(cron)) {
      scheduler.scheduleJob(JOB_NAME, JobTrigger.triggerAt(cron), listener);
    }
  }
}
