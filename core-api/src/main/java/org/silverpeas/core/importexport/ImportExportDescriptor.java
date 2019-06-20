/*
 * Copyright (C) 2000 - 2019 Silverpeas
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
 * An import-export descriptor is an object that provides useful information to exporters and
 * importers for performing their tasks. Information is carried through import and export process
 * parameters.
 */
public abstract class ImportExportDescriptor {

  /**
   * A specific value for the export-import format MIME type indicating that the MIME type isn't
   * specified. In general, this specific value means the exporter or importer works only with a
   * serialization format in a single well-known MIME type and thus it is useless to specify it.
   */
  public static final String NO_MIMETYPE = "";

  private String mimeType = NO_MIMETYPE;
  private Map<String, Serializable> parameters = new HashMap<String, Serializable>();

  /**
   * Gets the MIME type of the serialized resource. If no MIME type is defined, then
   * {@link ImportExportDescriptor#NO_MIMETYPE} is returned.
   * @return the MIME type of the serialized resource.
   */
  public String getMimeType() {
    return mimeType;
  }

  /**
   * Sets a MIME type for the serialized resource to export or to import.
   * @param mimeType the export/import mimeType to set.
   * @return itself.
   */
  public <O extends ImportExportDescriptor> O inMimeType(String mimeType) {
    if (!isDefined(mimeType)) {
      this.mimeType = NO_MIMETYPE;
    }
    this.mimeType = mimeType;
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
