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

import org.silverpeas.core.importexport.ExportDescriptor;
import org.silverpeas.core.importexport.ExportException;
import org.silverpeas.core.importexport.Exporter;
import org.silverpeas.core.importexport.NoDataToExportException;
import org.silverpeas.core.silvertrace.SilverTrace;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.Writer;

/**
 * An exporter of calendar events into a file in the iCal format.
 */
@Singleton
public class ICalExporter implements Exporter<ExportableCalendar> {

  @Inject
  private ICalCodec iCalCodec;

  /**
   * Exports the specified events with a writer in the iCal format. If no events are specified, then
   * a NoDataToExportException is thrown as no export can be done. The writer with which the events
   * have to be exported is provided by the specified export descriptor.
   * @param calendar the calendar to export.
   * @param descriptor the export descriptor in which is passed the writer with which the events
   * should be exported.
   * @throws ExportException if the export fails (an IO issue occurs with the writer, no events to
   * export, ...).
   */
  @Override
  public void export(ExportDescriptor descriptor, ExportableCalendar calendar)
      throws ExportException {
    if (calendar.isEmpty()) {
      throw new NoDataToExportException("To export to iCal, the calendar should have at least one"
          + " event");
    }
    Writer writer = descriptor.getWriter();
    try {
      String iCalCalendar = getICalCodec().encode(calendar.getEvents());
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
