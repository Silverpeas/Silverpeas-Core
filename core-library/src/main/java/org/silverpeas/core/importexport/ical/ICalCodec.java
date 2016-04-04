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

package org.silverpeas.core.importexport.ical;

import org.silverpeas.core.calendar.CalendarEvent;
import org.silverpeas.core.importexport.EncodingException;

import java.util.List;

/**
 * An iCal calendar encoder/decoder. It encodes the events of a calendar into the iCal format and
 * decodes a calendar in iCal format into Silverpeas event calendar.
 */
public interface ICalCodec {

  /**
   * Encodes the specified events in a calendar into the iCal format. If the encoding process failed
   * for an unexpected reason, then an EncodingException is thrown.
   * @param events the calendar events to encode in the iCal format. If the list of events is null
   * or empty, an IllegalArgumentException is thrown.
   * @return the textual representation in iCal format of the events..
   * @throws EncodingException a runtime exception that is thrown when an unexpected failure occurs
   * while encoding the specified calendar events.
   */
  String encode(final List<CalendarEvent> events) throws EncodingException;

}
