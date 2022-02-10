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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.notification.sse;

import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.core.scheduler.Job;
import org.silverpeas.core.scheduler.JobExecutionContext;
import org.silverpeas.core.scheduler.Scheduler;
import org.silverpeas.core.scheduler.trigger.JobTrigger;
import org.silverpeas.core.scheduler.trigger.TimeUnit;

import javax.inject.Inject;
import java.util.List;

import static org.silverpeas.core.notification.sse.ServerEventDispatcherTask.getAsyncContextSnapshot;
import static org.silverpeas.core.notification.sse.ServerEventDispatcherTask.unregisterAsyncContext;
import static org.silverpeas.core.notification.user.client.NotificationManagerSettings.getSseAsyncJobTrigger;

/**
 * This JOB is in charge of cleanup the SSE contexts if necessary.
 */
@Service
class ServerEventJobInitializer implements Initialization {

  private static final String JOB_NAME = "ServerEventJob";

  @Inject
  private Scheduler scheduler;

  @Override
  public void init() throws Exception {

    // Setting JOB
    scheduler.unscheduleJob(JOB_NAME);
    final int sseAsyncJobTrigger = getSseAsyncJobTrigger();
    if (sseAsyncJobTrigger > 0) {

      // Job instance
      final ServerEventCleanerJob job = new ServerEventCleanerJob();
      scheduler.scheduleJob(job, JobTrigger.triggerEvery(sseAsyncJobTrigger, TimeUnit.SECOND));
    }
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
      List<SilverpeasAsyncContext> asyncContexts = getAsyncContextSnapshot();
      SseLogger.get()
          .debug("doing quietly some stuffs over {0} {0,choice, 1#async context| 1<async contexts}",
              asyncContexts.size());
      asyncContexts.forEach(c -> {
        if (!c.isSendPossible()) {
          // Sending is no more possible, unregistering the context
          unregisterAsyncContext(c);
        } else if (c.isHeartbeat()) {
          // Heartbeat is requested
          c.safeWrite(() -> {
            try {
              SseLogger.get().debug("send heartbeat to {0}", c);
              HeartbeatServerEvent.createFor(c.getSessionId())
                  .send(c.getRequest(), c.getResponse(), c.getSessionId(), c.getUser());
            } catch (Exception e) {
              SseLogger.get().error(e);
              unregisterAsyncContext(c);
            }
          });
        }
      });
    }
  }
}
