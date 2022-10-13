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
package org.silverpeas.core.calendar.icalendar;

import org.silverpeas.core.calendar.CalendarEvent;
import org.silverpeas.core.importexport.ExportDescriptor;
import org.silverpeas.core.importexport.ExportException;
import org.silverpeas.core.importexport.Exporter;
import org.silverpeas.core.util.ServiceProvider;

import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * An exporter of a stream of calendar events into an output stream of an iCalendar text.
 *
 * @author mmoquillon
 */
public interface ICalendarExporter extends Exporter<Stream<CalendarEvent>> {

  /**
   * The name of the additional parameter in the descriptor that has the calendar concerned by
   * the export.
   */
  String CALENDAR = "calendar";
  String HIDE_PRIVATE_DATA = "hidePrivateData";

  /**
   * Gets an instance of the implementation of the {@link ICalendarExporter} interface.
   * @return an {@link ICalendarExporter} instance.
   */
  static ICalendarExporter get() {
    return ServiceProvider.getService(ICalendarExporter.class);
  }

  /**
   * Exports a the supplied stream of events of a given calendar into the output stream provided by
   * the specified descriptor. The calendar instance must be provided by the descriptor.
   * @param descriptor the export descriptor that describes how the export has to be done.
   * @param supplier the supplier that provides what resource to export. It starts the export
   * process by, for example, getting the resource from the Silverpeas data source.
   * @throws ExportException
   */
  @Override
  void exports(final ExportDescriptor descriptor, final Supplier<Stream<CalendarEvent>> supplier)
      throws ExportException;
}
