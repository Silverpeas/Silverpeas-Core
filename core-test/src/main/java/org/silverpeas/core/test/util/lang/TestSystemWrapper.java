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

package org.silverpeas.core.test.util.lang;

import org.silverpeas.core.util.lang.SystemWrapper;

import javax.annotation.PostConstruct;
import javax.annotation.Priority;
import javax.enterprise.inject.Alternative;
import javax.inject.Singleton;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static javax.interceptor.Interceptor.Priority.APPLICATION;

/**
 * Default implementation that is nothing more than a delegate of {@link System} class.
 * @author Yohann Chastagnier
 */
@Singleton
@Alternative
@Priority(APPLICATION + 10)
public class TestSystemWrapper implements SystemWrapper {

  private Map<String, String> env = null;

  @PostConstruct
  public void setupDefaultParameters() {

    // Adding by default this environment parameter
    env = new HashMap<>();
    env.putAll(System.getenv());
    env.put("SILVERPEAS_HOME", "SilverpeasHome4Tests");

    // Adding by default this system parameter
    setProperty("SILVERPEAS_DATA_HOME", "SilverpeasDataHome4Tests");
  }

  @Override
  public String getenv(final String name) {
    return getenv().get(name);
  }

  @Override
  public Map<String, String> getenv() {
    return env;
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
      System.setProperty(key, props.getProperty(key));
    }
  }

  @Override
  public String setProperty(final String key, final String value) {
    if (value != null && !value.trim().isEmpty()) {
      return System.setProperty(key, value);
    }
    return null;
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
