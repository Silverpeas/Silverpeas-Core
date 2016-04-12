/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.web.portlets.portal;

import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;

import java.io.IOException;
import java.io.InputStream;
import java.util.MissingResourceException;

/**
 * PropertiesContext loads the portal driver properties. A portal driver is a lightweight portal
 * execution environment.
 */
public class PropertiesContext {

  private static PropertiesContext context = new PropertiesContext();
  private static final String CONFIG_FILE = "DriverConfig";
  // Constants for properties defined in the config file
  private static final String PORTLET_RENDER_MODE_PARALLEL = "portletRenderModeParallel";
  private static final String ENABLE_AUTODEPLOY = "enableAutodeploy";
  private static final String AUTODEPLOY_DIR_WATCH_INTERVAL = "autodeployDirWatchInterval";
  private SettingBundle configProperties;

  /**
   * Gets the properties context of the Silverpeas portal.
   *
   * @return a PropertiesContext instance.
   */
  public static PropertiesContext get() {
    return context;
  }

  /**
   * Constructs a PropertiesContext. It loads all of the properties about the portal context.
   */
  protected PropertiesContext() {
    InputStream defaultConfigBundle = null;
    SettingBundle properties;
    try {
      properties =
          ResourceLocator.getSettingBundle("org.silverpeas.portlets." + CONFIG_FILE);
      if (!properties.exists()) {
        throw new MissingResourceException("Missing org.silverpeas.portlets." + CONFIG_FILE,
            PropertiesContext.class.getName(), "");
      }
    } finally {
      if (defaultConfigBundle != null) {
        try {
          defaultConfigBundle.close();
        } catch (IOException e) {
          // drop through
        }
      }
    }
    configProperties = properties;
  }

  public boolean isPortletRenderModeParallel() {
    String value = configProperties.getString(PORTLET_RENDER_MODE_PARALLEL);
    if ("true".equals(value)) {
      return true;
    }
    return false;
  }

  public boolean enableAutodeploy() {
    String value = configProperties.getString(ENABLE_AUTODEPLOY);
    if ("true".equals(value)) {
      return true;
    }
    return false;
  }

  public long getAutodeployDirWatchInterval() {
    String value = configProperties.getString(AUTODEPLOY_DIR_WATCH_INTERVAL);
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
