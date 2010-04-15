/**
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
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.portlets.portal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.stratelia.webactiv.util.ResourceLocator;

/**
 * PropertiesContext loads the content of driverconfig.properties
 */
public class PropertiesContext {

  private static Properties configProperties;
  // private static String CONFIG_FILE = "DriverConfig.properties";
  private static String CONFIG_FILE = "DriverConfig";
  // Constants for properties defined in the config file
  private static final String PORTLET_RENDER_MODE_PARALLEL = "portletRenderModeParallel";
  private static final String ENABLE_AUTODEPLOY = "enableAutodeploy";
  private static final String AUTODEPLOY_DIR_WATCH_INTERVAL = "autodeployDirWatchInterval";

  public static void init() {
    InputStream defaultConfigBundle = null;
    Properties defaultProperties = new Properties();
    try {
      ResourceLocator properties = new ResourceLocator(
          "com.silverpeas.portlets." + CONFIG_FILE, "");
      defaultProperties = properties.getProperties();
    } finally {
      if (defaultConfigBundle != null) {
        try {
          defaultConfigBundle.close();
        } catch (IOException e) {
          // drop through
        }
      }
    }
    configProperties = new Properties(defaultProperties);
  }

  public static boolean isPortletRenderModeParallel() {
    String value = configProperties.getProperty(PORTLET_RENDER_MODE_PARALLEL);
    if ("true".equals(value))
      return true;
    return false;
  }

  public static boolean enableAutodeploy() {
    String value = configProperties.getProperty(ENABLE_AUTODEPLOY);
    if ("true".equals(value))
      return true;
    return false;
  }

  public static long getAutodeployDirWatchInterval() {
    String value = configProperties.getProperty(AUTODEPLOY_DIR_WATCH_INTERVAL);
    long watchInterval;
    try {
      watchInterval = Long.parseLong(value);
    } catch (NumberFormatException nfe) {
      watchInterval = -1;
    }
    if (watchInterval <= 0) {
      watchInterval = 5;
    }
    return (watchInterval * 1000);
  }

}
