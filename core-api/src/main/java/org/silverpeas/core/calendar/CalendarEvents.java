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

package org.silverpeas.core.calendar;

import org.silverpeas.core.calendar.repository.CalendarEventRepository;

import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * This class permits to get events belonging calendars. Some constraints can be done to filter
 * the events.
 * @author Yohann Chastagnier
 */
public class CalendarEvents {

  private CalendarEventFilter filter = new CalendarEventFilter();

  CalendarEvents() {
  }

  /**
   * Filters the calendar events according to some predefined conditions. The different criterion
   * can be combined together to build a more complete criterion.
   * @param filterConsumer a function accepting a {@link CalendarEventFilter} instance to set
   * the different filtering criteria.
   * @return itself.
   */
  public CalendarEvents filter(Consumer<CalendarEventFilter> filterConsumer) {
    filterConsumer.accept(this.filter);
    return this;
  }

  /**
   * Gets as a stream all the events verifying the filters if any.<br>
   * Please be careful to always close the streams in order to avoid memory leaks!!!
   * @return a stream of events verifying the filters.
   */
  public Stream<CalendarEvent> stream() {
    return CalendarEventRepository.get().streamAll(filter);
  }
}
