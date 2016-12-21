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

import org.silverpeas.core.calendar.Calendar;
import org.silverpeas.core.calendar.event.CalendarEvent;

import java.io.InputStream;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * In charge of importing ICALENDAR events from {@link InputStream} instance.<br/>
 * The {@link CalendarEvent} instances are encoded into Silverpeas calendar events.
 * @author Yohann Chastagnier
 */
public class ICalendarImport {

  private final Calendar calendar;
  private final Supplier<InputStream> inputSupplier;
  private Consumer<CalendarEvent> calendarEventConsumer;

  /**
   * Hidden constructor.
   * @param calendar the calendar which supplier belong to.
   * @param inputSupplier the supplier of {@link InputStream} instance.
   */
  private ICalendarImport(final Calendar calendar, Supplier<InputStream> inputSupplier) {
    this.calendar = calendar;
    this.inputSupplier = inputSupplier;
  }

  /**
   * Initialize a new instance or the importer.
   * @param calendar the calendar which supplier belong to.
   * @param inputSupplier the supplier of {@link InputStream} instance.
   * @return a new initialized instance.
   */
  public static ICalendarImport from(Calendar calendar, Supplier<InputStream> inputSupplier) {
    return new ICalendarImport(calendar, inputSupplier);
  }

  /**
   * Executes the treatment of importation.
   */
  public void forEach(Consumer<CalendarEvent> calendarEventConsumer) throws ICalendarException {
    this.calendarEventConsumer = calendarEventConsumer;
    ICalendarExchange exchange = ICalendarExchange.get();
    exchange.doImportOf(this);
  }

  Supplier<InputStream> getInputSupplier() {
    return inputSupplier;
  }

  Consumer<CalendarEvent> getCalendarEventConsumer() {
    return calendarEventConsumer;
  }

  /**
   * Gets the calendar which supplier belong to.
   * @return a {@link Calendar} instance.
   */
  Calendar getCalendar() {
    return calendar;
  }
}
