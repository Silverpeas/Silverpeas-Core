/*
 * Copyright (C) 2000 - 2009 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.export;

import java.io.Writer;

/**
 * It represents a descriptor about the export of resources into a writer. As such it defines the
 * writer and the format into which the resources have to be exported. With the descriptor parameters,
 * additional information about the export process can be passed to the exporter.
 */
public class ExportDescriptor extends ImportExportDescriptor {

  private Writer writer = null;

  /**
   * Constructs a new export descriptor with the specified writer and export format.
   * @param writer the writer into wich the export will be serialized.
   * @param exportFormat the format of the export.
   */
  public ExportDescriptor(final Writer writer, String exportFormat) {
    if (writer == null) {
      throw new IllegalArgumentException("The writer cannot be null!");
    }
    this.writer = writer;
    setFormat(exportFormat);
  }

  /**
   * Constructs a new export descriptor with the specified writer. No specific format information
   * will be passed to the exporter.
   * @param writer the writer into wich the resources will be serialized.
   */
  public ExportDescriptor(final Writer writer) {
    this(writer, NO_FORMAT);
  }

  /**
   * Gets the writer with which the resources have to be exported.
   * @return the writer.
   */
  public Writer getWriter() {
    return this.writer;
  }

  /**
   * Sets a new writer with this descriptor.
   * @param writer the writer with which some resources will be exported.
   */
  public void setWriter(final Writer writer) {
    if (writer == null) {
      throw new IllegalArgumentException("The writer cannot be null!");
    }
    this.writer = writer;
  }

}
