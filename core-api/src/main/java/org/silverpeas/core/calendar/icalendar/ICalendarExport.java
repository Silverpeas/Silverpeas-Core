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
 * FLOSS exception. You should have received a copy of the text describing
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

import org.silverpeas.core.calendar.event.CalendarEvent;

import java.io.OutputStream;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * In charge of exporting {@link CalendarEvent} instances to {@link OutputStream} instance.<br/>
 * The {@link CalendarEvent} instances are encoded into ICALENDAR format (and so, as string).
 * @author Yohann Chastagnier
 */
public class ICalendarExport {

  private final Supplier<Stream<CalendarEvent>> calendarEventSupplier;
  private Supplier<OutputStream> outputSupplier;

  /**
   * Hidden constructor.
   * @param calendarEventSupplier the supplier of {@link CalendarEvent} instances.
   */
  private ICalendarExport(Supplier<Stream<CalendarEvent>> calendarEventSupplier) {
    this.calendarEventSupplier = calendarEventSupplier;
  }

  /**
   * Initialize a new instance or the exporter.
   * @param calendarEventSupplier the supplier of {@link CalendarEvent} instances.
   * @return a new initialized instance.
   */
  public static ICalendarExport from(Supplier<Stream<CalendarEvent>> calendarEventSupplier) {
    return new ICalendarExport(calendarEventSupplier);
  }

  /**
   * Executes the treatment of exportation.
   */
  public void to(Supplier<OutputStream> outputSupplier) throws ICalendarException {
    this.outputSupplier = outputSupplier;
    ICalendarExchange exchange = ICalendarExchange.get();
    exchange.export(this);
  }

  /**
   * Gets {@link CalendarEvent} instances to export.
   * @return a list.
   */
  Stream<CalendarEvent> streamCalendarEvents() {
    return calendarEventSupplier.get();
  }

  /**
   * Gets {@link OutputStream} instance into which the export will be written.
   * @return an output stream.
   */
  OutputStream getOutput() {
    return outputSupplier.get();
  }
}
