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

package org.silverpeas.core.importexport;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import org.apache.commons.io.input.ReaderInputStream;

/**
 * It represents a descriptor about the import of resources from a reader or an input stream. As
 * such it defines the reader, the input stream and the format from which the resources have to be
 * imported. With the descriptor parameters, additional information about the import process can be
 * passed to the importer.
 */
public class ImportDescriptor extends ImportExportDescriptor {

  private Reader reader = null;
  private InputStream inputStream = null;

  /**
   * Creates and initializes a new descriptor on an import process with the specified reader and
   * import format. The input stream is initialized with the specified reader.
   * @param reader the reader to use for importing the serializable resources.
   * @return an import descriptor.
   */
  public static ImportDescriptor withReader(final Reader reader) {
    if (reader == null) {
      throw new IllegalArgumentException("The reader cannot be null!");
    }
    ImportDescriptor descriptor = new ImportDescriptor();
    descriptor.setReader(reader);
    descriptor.setInputStream(new ReaderInputStream(reader));
    return descriptor;
  }

  /**
   * Creates and initializes a new descriptor on an import process with the specified input stream
   * and import format. The reader is initialized with the specified input stream.
   * @param inputStream the input stream to use for importing the serializable resources.
   * @return an import descriptor.
   */
  public static ImportDescriptor withInputStream(final InputStream inputStream) {
    if (inputStream == null) {
      throw new IllegalArgumentException("The input stream cannot be null!");
    }
    ImportDescriptor descriptor = new ImportDescriptor();
    descriptor.setInputStream(inputStream);
    descriptor.setReader(new InputStreamReader(inputStream));
    return descriptor;
  }

  /**
   * Gets the reader with which the resources have to be imported.
   * @return the reader.
   */
  public Reader getReader() {
    return this.reader;
  }

  /**
   * Gets the input stream with which the resources have to be imported.
   * @return the input stream.
   */
  public InputStream getInputStream() {
    return this.inputStream;
  }

  private void setReader(final Reader reader) {
    this.reader = reader;
  }

  private void setInputStream(final InputStream inputStream) {
    this.inputStream = inputStream;
  }

}
