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

import java.io.Reader;

/**
 * It represents a descriptor about the import of resources from a reader. As such it defines the
 * reader and the format from which the resources have to be imported. With the descriptor parameters,
 * additional information about the impport process can be passed to the importer.
 */
public class ImportDescriptor extends ImportExportDescriptor {

  private Reader reader = null;

  /**
   * Constructs a new import descriptor with the specified reader and import format.
   * @param reader the reader from wich the import will be deserialized.
   * @param importFormat the format in which are resources to import.
   */
  public ImportDescriptor(final Reader reader, String importFormat) {
    if (reader == null) {
      throw new IllegalArgumentException("The reader cannot be null!");
    }
    this.reader = reader;
    setFormat(importFormat);
  }

  /**
   * Constructs a new import descriptor with the specified reader. No specific format information
   * will be passed to the importer.
   * @param reader the reader from wich the resources will be deserialized.
   */
  public ImportDescriptor(final Reader reader) {
    this(reader, NO_FORMAT);
  }

  /**
   * Gets the reader with which the resources have to be imported.
   * @return the reader.
   */
  public Reader getReader() {
    return this.reader;
  }

  /**
   * Sets a new reader with this descriptor.
   * @param reader the reader with which some resources will be imported.
   */
  public void setReader(final Reader reader) {
    if (reader == null) {
      throw new IllegalArgumentException("The reader cannot be null!");
    }
    this.reader = reader;
  }

}
