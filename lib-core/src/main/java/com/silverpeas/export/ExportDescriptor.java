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

import static com.silverpeas.util.StringUtil.*;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * It represents a descriptor about the export of resources into a writer. As such it defines the
 * writer and the format into which the resources have to be exported. With the descriptor parameters,
 * additional information about the export process can be passed to the exporter.
 */
public class ExportDescriptor {

  /**
   * A specific value for the export format indicating that no explicit format is defined for the
   * export. In general, this specific value means the exporter is defined for one single export
   * format and thus it is useless to specify the export format.
   */
  public static final String NO_FORMAT = "";

  private Writer writer = null;
  private String exportFormat = NO_FORMAT;
  private Map<String, Object> parameters = new HashMap<String, Object> ();

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
    if (! isDefined(exportFormat)) {
      this.exportFormat = NO_FORMAT;
    } else {
      this.exportFormat = exportFormat;
    }
  }

  /**
   * Constructs a new export descriptor with the specified writer. No specific format information
   * will be passed to the exporter.
   * @param writer the writer into wich the export will be serialized.
   */
  public ExportDescriptor(final Writer writer) {
    this(writer, NO_FORMAT);
  }

  /**
   * Gets the export format of the resources.
   * If no export format is defined, then NO_FORMAT is returned.
   * @return the export format.
   */
  public String getExportFormat() {
    return exportFormat;
  }

  /**
   * Sets a format into which the resource have to be exported.
   * @param exportFormat the export format to set.
   */
  public void setExportFormat(String exportFormat) {
    if (! isDefined(exportFormat)) {
      this.exportFormat = NO_FORMAT;
    }
    this.exportFormat = exportFormat;
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

  /**
   * Adds a new export parameter. If a parameter already exists with the specifed name, the value
   * is replaced.
   * @param <T> the type of the parameter value.
   * @param name the parameter name.
   * @param value the parameter value.
   */
  public <T> void addParameter(String name, final T value) {
    this.parameters.put(name, value);
  }

  /**
   * Removes the export parameter identified by the specified name. If no parameter with the
   * specified name exists, nothing is done.
   * @param name the parameter name.
   */
  public void removeParameter(String name) {
    this.parameters.remove(name);
  }

  /**
   * Gets the parameter identified by the specified name. If no parameter with the specified name
   * exists, null is returned.
   * @param <T> the type of the parameter value.
   * @param name the parameter name.
   * @return the value of the parameter or null if no such parameter exists.
   */
  @SuppressWarnings("unchecked")
  public <T> T getParameter(String name) {
    return (T) this.parameters.get(name);
  }

  /**
   * Gets a list of the parameter names from this descriptor.
   * @return a list of parameter names.
   */
  public List<String> getParameters() {
    return new ArrayList<String>(this.parameters.keySet());
  }

  /**
   * Is the parameter identified by the specified name set within this descriptor?
   * @param name the parameter name
   * @return true if the parameter is set, false otherwise.
   */
  public boolean isParameterSet(String name) {
    return this.parameters.containsKey(name);
  }
}
