/*
 * Copyright (C) 2000 - 2023 Silverpeas
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
 * "https://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package org.silverpeas.core.jcr.impl.oak.configuration;

import org.silverpeas.core.util.StringUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Configuration of the node storage used as backend by Oak for the JCR. This class provides all the
 * methods common for all configuration of a node store, whatever the type of the storage.
 * <p>
 * The node storage is a data source used by the JCR to store data within a tree structure of nodes.
 * A node in Oak is an unordered collection of named properties and child nodes. The configuration
 * of a node storage allows sysadmins to leverage the capability and the performance of this storage
 * by Oak.
 * @author mmoquillon
 */
public abstract class NodeStoreConfiguration {

  private final Properties properties;

  /**
   * Loads a configuration for a node storage from a properties file. Each configuration parameter
   * must be identified by a full qualified name. The full qualified name is formatted as a
   * dot-separated path of names, each of them defining a subset of properties. By convention, the
   * root name in the paths denotes the type of the node storage for which the configuration
   * parameters are defined.
   * @param props the {@link Properties} containing all the parameters of a node stare.
   */
  NodeStoreConfiguration(final Properties props) {
    this.properties = props;
  }

  protected boolean getBoolean(String name, boolean defaultValue) {
    String value = properties.getProperty(name);
    if (StringUtil.isNotDefined(value)) {
      return defaultValue;
    }
    return StringUtil.getBooleanValue(value);
  }

  protected int getInteger(String name, int defaultValue) {
    String value = properties.getProperty(name);
    if (StringUtil.isNotDefined(value)) {
      return defaultValue;
    }
    return Integer.parseInt(value);
  }

  protected long getLong(String name, long defaultValue) {
    String value = properties.getProperty(name);
    if (StringUtil.isNotDefined(value)) {
      return defaultValue;
    }
    return Long.parseLong(value);
  }

  protected String getString(String name, String defaultValue) {
    return properties.getProperty(name, defaultValue);
  }

  protected List<String> getList(@SuppressWarnings("SameParameterValue") String name,
      @SuppressWarnings("SameParameterValue") List<String> defaultValue) {
    String value = properties.getProperty(name);
    if (StringUtil.isNotDefined(value)) {
      return defaultValue;
    }
    return Arrays.stream(value.split(","))
        .map(String::trim)
        .collect(Collectors.toList());
  }
}
