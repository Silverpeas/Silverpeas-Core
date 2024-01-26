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
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.calendar;

import org.silverpeas.kernel.SilverpeasException;
import org.silverpeas.core.SilverpeasExceptionMessages.LightExceptionMessage;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.importexport.ImportDescriptor;
import org.silverpeas.core.importexport.ImportException;
import org.silverpeas.core.initialization.Initialization;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.persistence.datasource.OperationContext;
import org.silverpeas.core.scheduler.*;
import org.silverpeas.core.scheduler.trigger.JobTrigger;
import org.silverpeas.core.thread.ManagedThreadPool;
import org.silverpeas.core.thread.ManagedThreadPool.ExecutionConfig;
import org.silverpeas.core.thread.ManagedThreadPoolException;
import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.kernel.bundle.SettingBundle;
import org.silverpeas.kernel.logging.SilverLogger;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.stream.Stream;

import static java.text.MessageFormat.format;

/**
 * A processor of synchronization of calendar events from a remote calendar into the Silverpeas
 * Calendar Engine. The synchronization works only with remote calendars providing an iCalendar
 * source and only with synchronized-flagged Silverpeas calendars.
 * <p>
 * The synchronization is an import process of calendar events from a remote iCalendar source
 * accessible through the Web in which events can be added, updated but also removed in a Silverpeas
 * calendar. A Silverpeas calendar can be synchronized only if it is marked as a synchronized one ;
 * a synchronized calendar in Silverpeas is the exact copy or image of a remote calendar at one
 * given instant. So, the synchronization follows the rules below:
 * </p>
 * <ul>
 * <li>any events in the iCalendar source that aren't in the Silverpeas calendar are added;</li>
 * <li>any events in the iCalendar source that are yet in the Silverpeas calendar replace their
 * counterpart in the Silverpeas calendar;</li>
 * <li>any events in the Silverpeas calendar that aren't in the iCalendar source are deleted.</li>
 * </ul>
 * <p>
 * As consequently, any changes done by hand in a synchronized calendar will be lost at the end
 * of its synchronization.
 * </p>
 * @author mmoquillon
 */
@Service
@Singleton
public class ICalendarEventSynchronization implements Initialization {

  /**
   * The namespace of the logger used to report the synchronization of a calendar.
   */
  public static final String REPORT_NAMESPACE = "silverpeas.core.calendar.synchronization";
  private static final String CALENDAR_SETTINGS = "org.silverpeas.calendar.settings.calendar";
  private static final String SYNCHRONIZATION_ERROR_MSG = "Synchronize error on calendar {0} of instance {1} -> {2}";

  @Inject
  private ICalendarEventImportProcessor importer;

  @Inject
  private Event<CalendarBatchSynchronizationErrorEvent> notifier;

  private ICalendarEventSynchronization() {

  }

  /**
   * Gets an instance of a synchronization processor.
   * @return a calendar synchronization processor.
   */
  public static ICalendarEventSynchronization get() {
    return ServiceProvider.getService(ICalendarEventSynchronization.class);
  }

  @Override
  public void init() throws Exception {
    try {
      final SettingBundle settings = ResourceLocator.getSettingBundle(CALENDAR_SETTINGS);
      final String cron = settings.getString("calendar.synchronization.cron");

      Scheduler scheduler = SchedulerProvider.getVolatileScheduler();
      scheduler.unscheduleJob(getClass().getSimpleName());
      scheduler.scheduleJob(new Job(getClass().getSimpleName()) {
        @Override
        public void execute(final JobExecutionContext context) throws SilverpeasException {
          synchronizeAll();
        }
      }, JobTrigger.triggerAt(cron));
    } catch (MissingResourceException | SchedulerException | ParseException e) {
      SilverLogger.getLogger(this).error("The synchronization scheduling failed to start", e);
    }
  }

  @Override
  public void release() throws Exception {
    Scheduler scheduler = SchedulerProvider.getVolatileScheduler();
    scheduler.unscheduleJob(getClass().getSimpleName());
  }

  /**
   * Synchronizes the specified calendar in Silverpeas with the calendar events coming from its
   * external counterpart (its origin).
   * <p>
   * If the specified calendar isn't a synchronized one, then an {@link IllegalArgumentException}
   * is thrown.
   * </p>
   * <p>
   * The calendar must exist in Silverpeas otherwise an {@link IllegalArgumentException} is thrown.
   * </p>
   * @param calendar a synchronized calendar in Silverpeas.
   * @return the result of the synchronization with the number of events that was added, updated
   * and deleted in the calendar.
   * @throws ImportException exception thrown if the synchronization fails.
   */
  public ICalendarImportResult synchronize(final Calendar calendar) throws ImportException {
    if (!calendar.isSynchronized() || !calendar.isPersisted()) {
      throw new IllegalArgumentException(
          "The calendar " + calendar.getId() + " isn't a synchronized!");
    }
    try {
      InputStream source = (InputStream) calendar.getExternalCalendarUrl().getContent();
      return synchronizeCalendarFrom(calendar, source);
    } catch (IOException e) {
      final String message = format(SYNCHRONIZATION_ERROR_MSG, calendar.getId(),
          calendar.getComponentInstanceId(), "no data found from the synchronization link");
      throw new ImportException(new LightExceptionMessage(this, e).singleLineWith(message));
    } catch (Exception e) {
      final String message = format(SYNCHRONIZATION_ERROR_MSG, calendar.getId(),
          calendar.getComponentInstanceId(), e.getMessage());
      throw new ImportException(message, e);
    }
  }

  private ICalendarImportResult synchronizeCalendarFrom(Calendar calendar, InputStream source) {
    return Transaction.performInOne(() -> {
      final Instant synchronizationDateTime = Instant.now();
      calendar.setLastSynchronizationDate(synchronizationDateTime);
      Calendar syncCalendar = Calendar.getById(calendar.getId());
      syncCalendar.setLastSynchronizationDate(synchronizationDateTime);
      syncCalendar.save();

      ICalendarImportResult result =
          importer.importInto(syncCalendar, ImportDescriptor.withInputStream(source));

      removeDeletedEvents(syncCalendar, result);
      return result;
    });
  }

  /**
   * Synchronizes all the synchronized calendars in Silverpeas with their remote external
   * counterpart. Each synchronization of a calendar will be done as they were requested by their
   * creator.
   * <p>
   * The synchronized calendars will be synchronized in a fixed pool of threads whose the size is
   * provided by the <code>calendar.synchronization.processors</code> property in the
   * <code>org.silverpeas.calendar.settings.calendar.properties</code> properties file. If no such
   * number is set in the settings, then the size of the pool is computed by this method according
   * to the number of available processors in the runtime. This will ensure that only a subset of
   * calendars are synchronized simultaneously to avoid of overloading Silverpeas.
   * </p>
   * @throws ImportException if the synchronization fails to start for at least one of the
   * calendar.
   */
  public void synchronizeAll() throws ImportException {
    final SettingBundle settings = ResourceLocator.getSettingBundle(CALENDAR_SETTINGS);
    int processors = settings.getInteger("calendar.synchronization.processors", 0);
    if (processors <= 0) {
      processors = Runtime.getRuntime().availableProcessors();
    }
    List<Calendar> calendars = Calendar.getSynchronizedCalendars();
    try {
      ManagedThreadPool.getPool()
          .invokeAndAwaitTermination(synchronizationProcessorsOf(calendars),
              ExecutionConfig.maxThreadPoolSizeOf(processors));
    } catch (ManagedThreadPoolException e) {
      throw new ImportException("Fail to synchronize the synchronized calendars!", e);
    }
  }

  private void removeDeletedEvents(final Calendar calendar, final ICalendarImportResult result) {
    if (calendar.getLastSynchronizationDate().isPresent()) {
      Calendar.getEvents()
          .filter(f -> f.onCalendar(calendar)
              .onSynchronizationDateLimit(calendar.getLastSynchronizationDate().get()))
          .stream()
          .forEach(e -> {
            e.delete();
            result.incDeleted();
          });
    }
  }

  private Stream<? extends Runnable> synchronizationProcessorsOf(final List<Calendar> calendars) {
    return calendars.stream().map(c -> () -> {
      try {
        // we set the creator of the calendar as the requester for the synchronization in this
        // thread
        User currentUser = User.getCurrentRequester();
        OperationContext.fromUser(currentUser != null ? currentUser : c.getCreator());
        ICalendarImportResult result = c.synchronize();
        String report = generateReport(c, result);
        SilverLogger.getLogger(REPORT_NAMESPACE).info(report);
      } catch (ImportException e) {
        if (e.getCause() != null) {
          SilverLogger.getLogger(REPORT_NAMESPACE).error(e);
        } else {
          SilverLogger.getLogger(REPORT_NAMESPACE).error(e.getMessage());
        }
        notifier.fire(new CalendarBatchSynchronizationErrorEvent(c));
      }
    });
  }

  private String generateReport(final Calendar calendar, final ICalendarImportResult result) {
    String duration;
    String synchroDate;
    Optional<Instant> lastSynchroDate = calendar.getLastSynchronizationDate();
    if (lastSynchroDate.isPresent()) {
      Instant dateTime = lastSynchroDate.get();
      synchroDate = OffsetDateTime.ofInstant(dateTime, ZoneId.systemDefault()).toString();
      duration = String.valueOf(Duration.between(dateTime, Instant.now()).getSeconds());
    } else {
      duration = "N/A";
      synchroDate = "N/A";
    }

    return "Report of the synchronization of calendar " + calendar.getId() +
        " ('" +
        calendar.getTitle() +
        "')\n" +
        "author: " +
        calendar.getCreator().getDisplayedName() +
        " (id '" +
        calendar.getCreator().getDisplayedName() +
        "')" +
        "\n" +
        "Synchronization date: " +
        synchroDate +
        "\n" +
        "Synchronization duration: " +
        duration +
        " seconds" +
        "\n" +
        "Number of events added: " +
        result.added() +
        "\n" +
        "Number of events updated: " +
        result.updated() +
        "\n" +
        "Number of events deleted: " +
        result.deleted() +
        "\n";
  }

  /**
   * Event notified on a synchronization error by batch.
   */
  public static class CalendarBatchSynchronizationErrorEvent {
    private final Calendar calendar;

    private CalendarBatchSynchronizationErrorEvent(final Calendar calendar) {
      this.calendar = calendar;
    }

    public Calendar getCalendar() {
      return calendar;
    }
  }
}
  