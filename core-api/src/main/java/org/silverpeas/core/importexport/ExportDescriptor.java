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
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.importexport;

import org.apache.commons.io.output.WriterOutputStream;
import org.silverpeas.core.util.Charsets;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * It represents a descriptor for the export of a resource into a writer or an output stream.
 *
 * <p>
 * It provides the writer or the output stream to use in the export, the MIME type of the format
 * into which the resources have to be exported, and additional properties that can be required by
 * an exporter.
 * </p>
 */
public class ExportDescriptor extends ImportExportDescriptor {

  private Writer writer = null;
  private OutputStream outputStream = null;

  /**
   * Creates and initializes a new descriptor on an export process with the specified writer. The
   * output stream is initialized with the specified writer.
   * @param writer the writer to use for exporting the serializable resources.
   * @return an export descriptor.
   */
  public static ExportDescriptor withWriter(final Writer writer) {
    ExportDescriptor descriptor = new ExportDescriptor();
    if (writer == null) {
      throw new IllegalArgumentException("The writer cannot be null!");
    }
    descriptor.setWriter(writer);
    descriptor.setOutputStream(new WriterOutputStream(writer, Charsets.UTF_8));
    return descriptor;
  }

  /**
   * Creates and initializes a new descriptor on an export process with the specified output stream.
   * The writer is initialized with the specified output stream.
   * @param outputStream the output stream to use for exporting the serializable resources.
   * @return an export descriptor.
   */
  public static ExportDescriptor withOutputStream(final OutputStream outputStream) {
    ExportDescriptor descriptor = new ExportDescriptor();
    if (outputStream == null) {
      throw new IllegalArgumentException("The output stream cannot be null!");
    }
    descriptor.setOutputStream(outputStream);
    descriptor.setWriter(new OutputStreamWriter(outputStream));
    return descriptor;
  }

  /**
   * Gets the writer with which the resources have to be exported.
   * @return the writer.
   */
  public Writer getWriter() {
    return this.writer;
  }

  /**
   * Gets the output stream with which the resources have to be exported.
   * @return the output stream.
   */
  public OutputStream getOutputStream() {
    return this.outputStream;
  }

  private void setWriter(final Writer writer) {
    this.writer = writer;
  }

  private void setOutputStream(final OutputStream outputStream) {
    this.outputStream = outputStream;
  }

}
