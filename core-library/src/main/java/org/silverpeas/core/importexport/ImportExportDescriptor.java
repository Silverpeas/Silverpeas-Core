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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * An import export descriptor is an object that provides useful information to exporters and
 * importers for performing their tasks. Information is carried through import and export process
 * parameters.
 */
public abstract class ImportExportDescriptor {

  /**
   * A specific value for the export-import resource format indicating that no explicit format is
   * defined. In general, this specific value means the exporter or importer processes for a single
   * predefined defined format and thus it is useless to specify it.
   */
  public static final String NO_FORMAT = "";

  private String format = NO_FORMAT;
  private Map<String, Serializable> parameters = new HashMap<String, Serializable>();

  /**
   * Gets the format in (or from) which the resource has to be exported (or imported). If no format
   * is defined, then NO_FORMAT is returned.
   * @return the format for export and import.
   */
  public String getFormat() {
    return format;
  }

  /**
   * Sets a format in which the resource to export will be or the resource to import is.
   * @param format the export/import format to set.
   * @return itself.
   */
  public <O extends ImportExportDescriptor> O inFormat(String format) {
    if (!isDefined(format)) {
      this.format = NO_FORMAT;
    }
    this.format = format;
    return (O) this;
  }

  /**
   * Adds a new process parameter. If a parameter already exists with the specified name, the value
   * is replaced.
   * @param <T> the type of the parameter value.
   * @param name the parameter name.
   * @param value the parameter value.
   * @return itself.
   */
  public <T extends Serializable, O extends ImportExportDescriptor> O withParameter(String name,
      final T value) {
    this.parameters.put(name, value);
    return (O) this;
  }

  /**
   * Sets the specified process parameter with the specified value. If the parameter already
   * exists, the value is replaced with the specified one.
   * @param <T> the type of the parameter value to set.
   * @param name the name of the parameter.
   * @param value the parameter value to set.
   */
  public <T extends Serializable> void setParameter(String name, final T value) {
    this.parameters.put(name, value);
  }

  /**
   * Removes the process parameter identified by the specified name. If no parameter with the
   * specified name exists, nothing is done.
   * @param name the parameter name.
   * @return itself.
   */
  public <O extends ImportExportDescriptor> O withoutParameter(String name) {
    this.parameters.remove(name);
    return (O) this;
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
