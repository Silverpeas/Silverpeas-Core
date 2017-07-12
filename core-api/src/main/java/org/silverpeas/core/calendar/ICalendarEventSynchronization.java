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
package org.silverpeas.core.calendar;

import org.silverpeas.core.importexport.ImportDescriptor;
import org.silverpeas.core.importexport.ImportException;
import org.silverpeas.core.persistence.Transaction;
import org.silverpeas.core.util.ServiceProvider;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;

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
@Singleton
public class ICalendarEventSynchronization {

  @Inject
  private ICalendarEventImportProcessor importer;

  private ICalendarEventSynchronization() {

  }

  /**
   * Gets an instance of a synchronization processor.
   * @return a calendar synchronization processor.
   */
  public static ICalendarEventSynchronization get() {
    return ServiceProvider.getService(ICalendarEventSynchronization.class);
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
      return Transaction.performInOne(() -> {
        final OffsetDateTime synchronizationDateTime = OffsetDateTime.now();
        calendar.setLastSynchronizationDate(synchronizationDateTime);
        Calendar syncCalendar = Calendar.getById(calendar.getId());
        syncCalendar.setLastSynchronizationDate(synchronizationDateTime);
        syncCalendar.save();

        ICalendarImportResult result =
            importer.importInto(syncCalendar, ImportDescriptor.withInputStream(source));

        removeDeletedEvents(syncCalendar, result);
        return result;
      });
    } catch (IOException e) {
      throw new ImportException(e.getMessage(), e);
    }
  }

  private void removeDeletedEvents(final Calendar calendar, final ICalendarImportResult result) {
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
  