/*
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along withWriter this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.export.ical;

import com.silverpeas.calendar.CalendarEvent;
import com.silverpeas.export.ExportDescriptor;
import com.silverpeas.export.ExportException;
import com.silverpeas.export.Exporter;
import com.silverpeas.export.NoDataToExportException;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import java.io.Writer;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * An exporter of calendar events into a file in the iCal format.
 */
@Named
public class ICalExporter implements Exporter<CalendarEvent> {

  @Inject
  private ICalCodec iCalCodec;

  /**
   * Exports the specified events withWriter a writer in the iCal format.
   * If no events are specified, then a NoDataToExportException is thrown as no export can be done.
   * The writer withWriter which the events have to be exported is provided by the specified
   * export descriptor.
   * @param events the events of a calendar to export.
   * @param descriptor the export descriptor in which is passed the writer wih which
   * the events should be exported.
   * @throws ExportException if the export fails (an IO issue occurs withWriter the writer,
   * no events to export, ...).
   */
  @Override
  public void export(ExportDescriptor descriptor, CalendarEvent... events) throws ExportException {
    export(descriptor, Arrays.asList(events));
  }

  /**
   * Exports the specified events withWriter a writer in the iCal format.
   * If no events are specified, then a NoDataToExportException is thrown as no export can be done.
   * The writer withWriter which the events have to be exported is provided by the specified
   * export descriptor.
   * @param events the events of a calendar to export.
   * @param descriptor the export descriptor in which is passed the writer wih which
   * the events should be exported.
   * @throws ExportException if the export fails (an IO issue occurs withWriter the writer,
   * no events to export, ...).
   */
  @Override
  public void export(ExportDescriptor descriptor, List<CalendarEvent> events) throws ExportException {
    if (events.isEmpty()) {
      throw new NoDataToExportException("To export to iCal, the calendar should have at least one"
          + " event");
    }
    Writer writer = descriptor.getWriter();
    try {
      String iCalCalendar = getICalCodec().encode(events);
      writer.write(iCalCalendar);
      writer.close();
    } catch (Exception e) {
      try {
        if (writer != null) {
          writer.close();
        }
      } catch (Exception ex) {
        SilverTrace.error("export.ical", getClass().getSimpleName() + ".exportInICal()",
            "root.EX_NO_MESSAGES", ex);
      }
      SilverTrace.error("export.ical", getClass().getSimpleName() + ".exportInICal()",
          "roor.EX_NO_MESSAGES", e);
      throw new ExportException(e.getMessage(), e);
    }
  }


  /**
   * Gets the iCalCodec used to encode calendar events in iCal formatted text.
   * @return an iCal codec.
   */
  protected ICalCodec getICalCodec() {
    return iCalCodec;
  }

  private ICalExporter() {
  }

}
