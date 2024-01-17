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

import org.silverpeas.core.SilverpeasRuntimeException;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.notification.sse.behavior.SendEveryAmountOfTime;
import org.silverpeas.core.scheduler.Job;
import org.silverpeas.core.scheduler.JobExecutionContext;
import org.silverpeas.core.scheduler.Scheduler;
import org.silverpeas.core.scheduler.SchedulerException;
import org.silverpeas.core.scheduler.trigger.JobTrigger;
import org.silverpeas.core.scheduler.trigger.TimeUnit;
import org.silverpeas.core.util.Process;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.logging.SilverLogger;

import java.io.Serializable;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;
import java.util.function.Predicate;

import static java.lang.String.valueOf;
import static java.text.MessageFormat.format;
import static java.util.Optional.ofNullable;
import static org.silverpeas.core.notification.user.client.NotificationManagerSettings.sendEveryAmountOfSecondsFor;
import static org.silverpeas.core.scheduler.SchedulerProvider.getVolatileScheduler;

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

  private static final ConcurrentMap<String, CacheContext> contextsByEventType = new ConcurrentHashMap<>();

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
        mustSendImmediately = contextsByEventType
            .computeIfAbsent(eventType, t -> {
              SseLogger.get()
                  .debug(() -> format("WAIT FOR - {0} - added into cache of contexts", t));
              return new CacheContext(t);
            })
            .setEvent(serverEvent);
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
    }

    private boolean setOrVerifyJobDelay() {
      boolean scheduled = false;
      final int newDelayInSeconds = sendEveryAmountOfSecondsFor(eventName);
      if (newDelayInSeconds > 0) {
        try {
          if (delayInSeconds != newDelayInSeconds) {
            final Scheduler scheduler = getVolatileScheduler();
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
        scheduled = true;
      } else {
        try {
          SseLogger.get()
              .debug(() -> format("WAIT FOR - {0} - JOB - stopping because of deactivation",
                  eventType));
          getVolatileScheduler().unscheduleJob(getName());
        } catch (SchedulerException e) {
          SseLogger.get().error(e);
        }
        delayInSeconds = -1;
      }
      return scheduled;
    }

    @Override
    public void execute(final JobExecutionContext context) {
      contextsByEventType.get(eventType)
          .consumeEvent("JOB - starts invoke", "JOB - ends invoke", event -> {
        if (event.isPresent()) {
          SseLogger.get()
              .debug(() -> format("WAIT FOR - {0} - JOB - dispatch last event waiting for",
                  eventType));
          ServerEventDispatcherTask.dispatch(event.get());
        } else {
          SseLogger.get()
              .debug(() -> format("WAIT FOR - {0} - JOB - no event to dispatch", eventType));
        }
        return setOrVerifyJobDelay();
      });
    }
  }

  private static class CacheContext implements Serializable {
    private static final long serialVersionUID = 679271503199577304L;

    private final Semaphore semaphore = new Semaphore(1, true);
    private final String eventType;
    private transient SendEveryAmountOfTime event;
    private int count = -1;

    public CacheContext(final String eventType) {
      this.eventType = eventType;
    }

    /**
     * Sets event into context.
     * <p>
     * The first set event induced the context initialization. In a such case, this event MUST be
     * sent immediately.
     * </p>
     * @param event a {@link SendEveryAmountOfTime} event instance.
     * @return true if given event MUST be sent immediately, false otherwise.
     */
    private boolean setEvent(final SendEveryAmountOfTime event) {
      event.markAsWaitingFor();
      return safeExecute("CONTEXT - try set event into context", "CONTEXT - event set into context",
          () -> {
        boolean mustSendImmediately = false;
        if (count == -1) {
          SseLogger.get()
              .debug(() -> format("WAIT FOR - {0} - CONTEXT - first event after context initialization is sent immediately",
                  eventType));
          mustSendImmediately = true;
          count = 0;
          new ServerEventJob(event).setOrVerifyJobDelay();
        } else {
          if (count == 0) {
            SseLogger.get()
                .debug(() -> format("WAIT FOR - {0} - CONTEXT - first event waiting for job execution",
                    eventType));
          } else {
            SseLogger.get()
                .debug(() -> format(
                    "WAIT FOR - {0} - CONTEXT - new event replacing a previous to wait for job execution ({1} replacements)",
                    eventType, valueOf(count)));
          }
        }
        this.event = event;
        count++;
        return mustSendImmediately;
      });
    }

    /**
     * Handles the event consume.
     * <p>
     * If JOB ends, the cache of context is cleared about the current handled event name.
     * </p>
     * @param acquireMsg for debug messages.
     * @param releaseMsg for debug messages.
     * @param function a lambda taking as parameter an event and returning true to indicate that
     * the JOB keep alive to process next events or false to indicate the end of JOB.
     */
    private void consumeEvent(final String acquireMsg, final String releaseMsg,
        final Predicate<Optional<SendEveryAmountOfTime>> function) {
      safeExecute(acquireMsg, releaseMsg, () -> {
        if (!Boolean.TRUE.equals(function.test(ofNullable(event)))) {
          contextsByEventType.remove(eventType);
          SseLogger.get()
              .debug(() -> format("WAIT FOR - {0} - CONTEXT - removed from cache of contexts",
                  eventType));
        }
        event = null;
        count = 0;
        return null;
      });
    }

    private <T> T safeExecute(final String acquireMsg, final String releaseMsg, final Process<T> process) {
      SseLogger.get()
          .debug(() -> format("WAIT FOR - {0} - {1} ({2} are waiting)",
              eventType, acquireMsg, valueOf(semaphore.getQueueLength())));
      try {
        semaphore.acquire();
        return process.execute();
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new SilverpeasRuntimeException(e);
      } catch (Exception e) {
        throw new SilverpeasRuntimeException(e);
      } finally {
        semaphore.release();
        SseLogger.get()
            .debug(() -> format("WAIT FOR - {0} - {1} ({2} are waiting)",
                eventType, releaseMsg, valueOf(semaphore.getQueueLength())));
      }
    }
  }
}
