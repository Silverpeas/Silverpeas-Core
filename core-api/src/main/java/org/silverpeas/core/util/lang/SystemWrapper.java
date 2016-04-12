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

import org.silverpeas.core.util.ServiceProvider;

import java.util.Map;
import java.util.Properties;

/**
 * This wrapper interface permits to bootstrap different System mechanism according to the context
 * of execution.
 * This interface ensures nothing of wrong is done with the System properties and as such it can
 * use a security manager.
 * @author Yohann Chastagnier
 */
public interface SystemWrapper {

  /**
   * Gets the wrapped {@link System} instance.
   * @return the instance of the System Wrapper.
   */
  static SystemWrapper get() {
    return ServiceProvider.getService(SystemWrapper.class);
  }

  /**
   * Gets the value of a environment variable.
   * @param name the name of the variable.
   * @return the value of the requested environment variable.
   */
  String getenv(String name);

  /**
   * Gets all the environment variables.
   * @return the map of environment variables.
   */
  Map<String, String> getenv();

  /**
   * Gets the system properties.
   * @return the system properties.
   * @see System#getProperties()
   */
  Properties getProperties();

  /**
   * Sets the specified properties in the system properties. Opposite to the method
   * {@link java.lang.System#setProperties(java.util.Properties)} that replace the system properties
   * with the specified ones, this method adds the specified ones among the existing system
   * properties.
   * @param props the system properties to add.
   * @see java.util.Properties#putAll(java.util.Map)
   */
  void setProperties(Properties props);

  /**
   * Sets a new system property. If the property isn't valued, id est has a null or an empty value,
   * then it is not set. Only non-null and not empty property can be set.
   * @param key the key of the property.
   * @param value a non-null and non-empty value of the property.
   * @return the previous value of the system property,
   * or <code>null</code> if it did not have one or if the property to set isn't valid.
   * @see System#setProperty(String, String)
   */
  String setProperty(String key, String value);

  /**
   * Gets a system property.
   * @param key the key of the system property.
   * @return the string value of the system property,
   * or <code>null</code> if there is no property with that key.
   */
  String getProperty(String key);

  /**
   * Gets a system property.
   * @param key the key of the system property.
   * @param def the default value if there is no property value with the key.
   * @return the string value of the system property,
   * or the default value if there is no property with that key.
   */
  String getProperty(String key, String def);
}
