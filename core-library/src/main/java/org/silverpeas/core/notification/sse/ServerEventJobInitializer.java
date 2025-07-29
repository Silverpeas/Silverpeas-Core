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
package org.silverpeas.core.notification.sse;

import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.scheduler.Job;
import org.silverpeas.core.scheduler.JobExecutionContext;
import org.silverpeas.core.scheduler.Scheduler;
import org.silverpeas.core.scheduler.SchedulingInitializer;
import org.silverpeas.core.scheduler.trigger.JobTrigger;
import org.silverpeas.core.scheduler.trigger.TimeUnit;
import org.silverpeas.kernel.annotation.NonNull;

import javax.inject.Inject;
import java.util.List;

import static org.silverpeas.core.notification.sse.ServerEventDispatcherTask.getContextSnapshot;
import static org.silverpeas.core.notification.user.client.NotificationManagerSettings.getSseAsyncJobTrigger;

/**
 * This JOB is in charge of cleanup the SSE contexts if necessary.
 */
@Service
class ServerEventJobInitializer extends SchedulingInitializer {

  private static final String JOB_NAME = "ServerEventJob";

  @Inject
  private Scheduler scheduler;
  private final Job job = new ServerEventCleanerJob();
  private final int sseAsyncJobTrigger = getSseAsyncJobTrigger();

  @Override
  protected JobTrigger getTrigger() {
    return JobTrigger.triggerEvery(sseAsyncJobTrigger, TimeUnit.SECOND);
  }

  @NonNull
  @Override
  protected String getCron() {
    return "";
  }

  @NonNull
  @Override
  protected Job getJob() {
    return job;
  }

  @Override
  protected boolean isSchedulingEnabled() {
    return sseAsyncJobTrigger > 0;
  }

  private static class ServerEventCleanerJob extends Job {

    /**
     * Creates a new job.
     */
    private ServerEventCleanerJob() {
      super(JOB_NAME);
    }

    @Override
    public void execute(final JobExecutionContext context) {
      List<SilverpeasServerEventContext> asyncContexts = getContextSnapshot();
      SseLogger.get()
          .debug("doing quietly some stuffs over {0} {0,choice, 1#async context| 1<async contexts}",
              asyncContexts.size());
      asyncContexts.forEach(SilverpeasServerEventContext::sendHeartbeatIfEnabled);
    }
  }
}
