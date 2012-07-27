/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.export;

import com.silverpeas.export.ical.ExportableCalendar;
import com.silverpeas.export.ical.ICalExporter;
import javax.inject.Inject;

/**
 * A factory of exporters in Silverpeas. The factory hides the concrete implementation used in the
 * exporting process and it wraps the life-cycle of the exporter objects.
 */
public class ExporterFactory {

  private static final ExporterFactory instance = new ExporterFactory();

  @Inject
  private ICalExporter exporter;

  /**
   * Gets an instance of factory of ICalExporter objects.
   * @return an instance of ICalExporterFactory.
   */
  public static ExporterFactory getFactory() {
    return instance;
  }

  /**
   * Gets an exporter of a calendar in iCal format.
   * @return an exporter of a calendar.
   */
  public Exporter<ExportableCalendar> getICalExporter() {
    return exporter;
  }

  private ExporterFactory() {

  }
}
