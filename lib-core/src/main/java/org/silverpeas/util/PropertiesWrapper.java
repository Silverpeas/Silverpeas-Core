/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Enumeration;
import java.util.InvalidPropertiesFormatException;
import java.util.Properties;
import java.util.Set;

/**
 * A decorator of the Properties class. It enhances it by supporting the environment variables
 * resolution.
 * @author mmoquillon
 */
public class PropertiesWrapper extends Properties {

  private Properties properties;

  public PropertiesWrapper(final Properties properties) {
    super();
    this.properties = properties;
  }

  @Override
  public Object setProperty(final String key, final String value) {
    return properties.setProperty(key, value);
  }

  @Override
  public void load(final Reader reader) throws IOException {
    properties.load(reader);
  }

  @Override
  public void load(final InputStream inStream) throws IOException {
    properties.load(inStream);
  }

  @Override
  public void store(final Writer writer, final String comments) throws IOException {
    properties.store(writer, comments);
  }

  @Override
  public void store(final OutputStream out, final String comments) throws IOException {
    properties.store(out, comments);
  }

  @Override
  public void loadFromXML(final InputStream in)
      throws IOException, InvalidPropertiesFormatException {
    properties.loadFromXML(in);
  }

  @Override
  public void storeToXML(final OutputStream os, final String comment) throws IOException {
    properties.storeToXML(os, comment);
  }

  @Override
  public void storeToXML(final OutputStream os, final String comment, final String encoding)
      throws IOException {
    properties.storeToXML(os, comment, encoding);
  }

  @Override
  public String getProperty(final String key) {
    return VariableResolver.resolve(properties.getProperty(key));
  }

  @Override
  public String getProperty(final String key, final String defaultValue) {
    return VariableResolver.resolve(properties.getProperty(key, defaultValue));
  }

  @Override
  public Enumeration<?> propertyNames() {
    return properties.propertyNames();
  }

  @Override
  public Set<String> stringPropertyNames() {
    return properties.stringPropertyNames();
  }

  @Override
  public void list(final PrintStream out) {
    properties.list(out);
  }

  @Override
  public void list(final PrintWriter out) {
    properties.list(out);
  }
}
