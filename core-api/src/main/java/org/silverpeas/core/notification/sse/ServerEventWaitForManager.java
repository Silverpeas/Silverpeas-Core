/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
import org.silverpeas.core.notification.sse.behavior.SendEveryAmountOfTime;
import org.silverpeas.core.scheduler.Job;
import org.silverpeas.core.scheduler.JobExecutionContext;
import org.silverpeas.core.scheduler.Scheduler;
import org.silverpeas.core.scheduler.SchedulerException;
import org.silverpeas.core.scheduler.SchedulerProvider;
import org.silverpeas.core.scheduler.trigger.JobTrigger;
import org.silverpeas.core.scheduler.trigger.TimeUnit;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.logging.SilverLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.valueOf;
import static java.text.MessageFormat.format;
import static java.util.Collections.synchronizedMap;
import static java.util.Optional.ofNullable;
import static org.silverpeas.core.notification.user.client.NotificationManagerSettings.sendEveryAmountOfSecondsFor;

/**
 * This manager handles all {@link ServerEvent} implementation implementing also
 * {@link SendEveryAmountOfTime} interface.
 * <p>
 * When an event could be send several times in a short time (could be the case when event is
 * about all connected users), it can be useful to limit a send after a minimum delay.
 * </p>
 * @author silveryocha
 */
@Service
public class ServerEventWaitForManager {

  private static final Map<String, CacheContext> contextsByEventType = synchronizedMap(new HashMap<>());

  ServerEventWaitForManager() {
  }

  public static ServerEventWaitForManager get() {
    return ServiceProvider.getSingleton(ServerEventWaitForManager.class);
  }

  /**
   * Puts a {@link ServerEvent} to send into context of waiting mechanism.
   * <p>
   * If the {@link ServerEvent} implementation does not implement
   * {@link SendEveryAmountOfTime} interface, no wait is performed.
   * </p>
   * @param event a {@link ServerEvent} instance.
   * @return true if send has to wait.
   */
  public boolean mustSendImmediately(final ServerEvent event) {
    final int delayInSeconds = event instanceof SendEveryAmountOfTime ?
        sendEveryAmountOfSecondsFor(event.getName()) :
        -1;
    boolean mustSendImmediately = delayInSeconds <= 0;
    if (!mustSendImmediately) {
      final SendEveryAmountOfTime serverEvent = (SendEveryAmountOfTime) event;
      mustSendImmediately = serverEvent.hasWaitingFor();
      if (!mustSendImmediately) {
        final String eventType = serverEvent.getStoreDiscriminator();
        synchronized (contextsByEventType) {
          final CacheContext context = contextsByEventType.computeIfAbsent(eventType, k -> new CacheContext());
          if (!context.isJobInitialized()) {
            mustSendImmediately = true;
            context.setJob(new ServerEventJob(serverEvent));
          } else {
            mustSendImmediately = false;
            final SendEveryAmountOfTime previousEvent = context.setEvent(serverEvent);
            if (previousEvent == null) {
              SseLogger.get()
                  .debug(() -> format("WAIT FOR - {0} - first event waiting for job execution",
                      eventType));
            } else {
              SseLogger.get()
                  .debug(() -> format(
                      "WAIT FOR - {0} - new event replacing a previous to wait for job execution ({1} replacements)",
                      eventType, valueOf(context.count)));
            }
          }
        }
      }
    }
    return mustSendImmediately;
  }

  /**
   * JOB of {@link ServerEvent} implementing {@link SendEveryAmountOfTime} send.
   */
  static class ServerEventJob extends Job {
    private final ServerEvent.ServerEventName eventName;
    private final String eventType;
    private Integer delayInSeconds = -1;

    private ServerEventJob(final SendEveryAmountOfTime event) {
      super("SEE_JOB_FOR_EVENT_" + event.getStoreDiscriminator());
      this.eventName = event.getName();
      this.eventType = event.getStoreDiscriminator();
      setOrVerifyJobDelay();
    }

    private void setOrVerifyJobDelay() {
      final Scheduler scheduler = SchedulerProvider.getVolatileScheduler();
      final int newDelayInSeconds = sendEveryAmountOfSecondsFor(eventName);
      if (newDelayInSeconds > 0) {
        try {
          if (delayInSeconds != newDelayInSeconds) {
            if (delayInSeconds != -1) {
              SseLogger.get()
                  .debug(() -> format("WAIT FOR - {0} - JOB - stopping because of changed delay",
                      eventType));
              scheduler.unscheduleJob(getName());
            }
            scheduler.scheduleJob(this, JobTrigger.triggerEvery(newDelayInSeconds, TimeUnit.SECOND));
            SseLogger.get()
                .debug(() -> format(
                    "WAIT FOR - {0} - JOB - starting to be invoked every {1} seconds",
                    eventType, valueOf(newDelayInSeconds)));
          }
        } catch (SchedulerException e) {
          SilverLogger.getLogger(this).error(e);
        }
        delayInSeconds = newDelayInSeconds;
      } else {
        try {
          SseLogger.get()
              .debug(() -> format("WAIT FOR - {0} - JOB - stopping because of deactivation",
                  eventType));
          scheduler.unscheduleJob(getName());
          clearCacheContext();
        } catch (SchedulerException e) {
          SseLogger.get().error(e);
        }
        delayInSeconds = -1;
      }
    }

    @Override
    public void execute(final JobExecutionContext context) {
      SseLogger.get().debug(() -> format("WAIT FOR - {0} - JOB - invoked", eventType));
      synchronized (contextsByEventType) {
        final CacheContext cacheContext = contextsByEventType.get(eventType);
        final Optional<SendEveryAmountOfTime> event = cacheContext.consumeEvent();
        if (event.isPresent()) {
          SseLogger.get()
              .debug(() -> format("WAIT FOR - {0} - JOB - dispatch last event waiting for",
                  eventType));
          ServerEventDispatcherTask.dispatch(event.get());
        } else {
          SseLogger.get()
              .debug(() -> format("WAIT FOR - {0} - JOB - no event to dispatch", eventType));
        }
        setOrVerifyJobDelay();
      }
    }

    private void clearCacheContext() {
      contextsByEventType.remove(eventType);
      SseLogger.get().debug(() -> format("WAIT FOR - {0} - JOB - clearing context cache", eventType));
    }
  }

  private static class CacheContext {
    private SendEveryAmountOfTime event = null;
    private int count = 0;
    private ServerEventJob job = null;

    private SendEveryAmountOfTime setEvent(final SendEveryAmountOfTime event) {
      final SendEveryAmountOfTime previous = this.event;
      this.event = event;
      event.markAsWaitingFor();
      count++;
      return previous;
    }

    private void setJob(final ServerEventJob job) {
      this.job = job;
    }

    private Optional<SendEveryAmountOfTime> consumeEvent() {
      final SendEveryAmountOfTime eventToConsume = this.event;
      this.event = null;
      count = 0;
      return ofNullable(eventToConsume);
    }

    public boolean isJobInitialized() {
      return job != null;
    }
  }
}
