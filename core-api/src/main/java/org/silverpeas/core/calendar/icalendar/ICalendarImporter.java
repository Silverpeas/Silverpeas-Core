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
package org.silverpeas.core.calendar.icalendar;

import org.apache.commons.lang3.tuple.Pair;
import org.silverpeas.core.calendar.CalendarEvent;
import org.silverpeas.core.calendar.CalendarEventOccurrence;
import org.silverpeas.core.importexport.ImportDescriptor;
import org.silverpeas.core.importexport.ImportException;
import org.silverpeas.core.importexport.Importer;
import org.silverpeas.core.util.ServiceProvider;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * An importer of an input stream of iCalendar text into a stream of calendar events.
 * @author mmoquillon
 */
public interface ICalendarImporter
    extends Importer<Stream<Pair<CalendarEvent, List<CalendarEventOccurrence>>>> {

  /**
   * Gets an instance of the implementation of the {@link ICalendarImporter} interface.
   * @return an {@link ICalendarImporter} instance.
   */
  static ICalendarImporter get() {
    return ServiceProvider.getService(ICalendarImporter.class);
  }

  /**
   * Imports the events serialized in the iCal format from the input stream provided by the
   * descriptor and passes the import stream to the specified consumer.
   * @param descriptor the import descriptor that describes how the import has to be done.
   * @param consumer the consumer that takes the resource that was decoded. It ends the import
   * process by, for example, saving it into Silverpeas.
   * @throws ImportException
   */
  @Override
  void imports(final ImportDescriptor descriptor,
      Consumer<Stream<Pair<CalendarEvent, List<CalendarEventOccurrence>>>> consumer)
      throws ImportException;
}
