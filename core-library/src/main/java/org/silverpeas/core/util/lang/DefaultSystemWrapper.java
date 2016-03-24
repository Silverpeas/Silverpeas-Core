/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
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

package org.silverpeas.core.util.lang;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

/**
 * Default implementation that is nothing more than a delegate of {@link System} class.
 * @author Yohann Chastagnier
 */
@Singleton
public class DefaultSystemWrapper implements SystemWrapper {

  @PostConstruct
  protected void preloadSystemSettings() {
    // we don't use the ResourceLocator API here in order to avoid cyclic dependency between
    // SystemWrapper and ResourceLocator
    Properties systemSettings = new Properties();
    try {
      systemSettings.load(new FileReader(
          Paths.get(System.getenv("SILVERPEAS_HOME"), "properties", "org", "silverpeas",
              "systemSettings.properties").toFile()));
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
    systemSettings.stringPropertyNames()
        .stream()
        .forEach(key -> setProperty(key, systemSettings.getProperty(key)));
  }

  @Override
  public String getenv(final String name) {
    return System.getenv(name);
  }

  @Override
  public Map<String, String> getenv() {
    return System.getenv();
  }

  @Override
  public Properties getProperties() {
    return System.getProperties();
  }

  @Override
  public void setProperties(final Properties props) {
    Enumeration<?> propertyNames = props.propertyNames();
    while (propertyNames.hasMoreElements()) {
      String key = (String) propertyNames.nextElement();
      setProperty(key, props.getProperty(key));
    }
  }

  @Override
  public String setProperty(final String key, final String value) {
    String previousValue = null;
    if (value != null && !value.trim().isEmpty()) {
      previousValue = System.setProperty(key, value);
    }
    return previousValue;
  }

  @Override
  public String getProperty(final String key) {
    return System.getProperty(key);
  }

  @Override
  public String getProperty(final String key, final String def) {
    return System.getProperty(key, def);
  }
}
