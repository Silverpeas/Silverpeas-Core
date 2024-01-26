/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.portlets.portal;

import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;

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
   * Constructs a PropertiesContext. It loads all of the properties about the portal context.
   */
  protected PropertiesContext() {
    SettingBundle properties;
    properties = ResourceLocator.getSettingBundle("org.silverpeas.portlets." + CONFIG_FILE);
    if (!properties.exists()) {
      throw new MissingResourceException("Missing org.silverpeas.portlets." + CONFIG_FILE,
          PropertiesContext.class.getName(), "");
    }
    configProperties = properties;
  }

  /**
   * Gets the properties context of the Silverpeas portal.
   *
   * @return a PropertiesContext instance.
   */
  public static PropertiesContext get() {
    return context;
  }

  public boolean isPortletRenderModeParallel() {
    String value = configProperties.getString(PORTLET_RENDER_MODE_PARALLEL);
    return "true".equals(value);
  }

  public boolean enableAutodeploy() {
    String value = configProperties.getString(ENABLE_AUTODEPLOY);
    return "true".equals(value);
  }

  public long getAutodeployDirWatchInterval() {
    String value = configProperties.getString(AUTODEPLOY_DIR_WATCH_INTERVAL);
    final long defaultWatchInterval = 5;
    final long millisInSecond = 1000;
    long watchInterval;
    try {
      watchInterval = Long.parseLong(value);
    } catch (NumberFormatException nfe) {
      watchInterval = -1;
    }
    if (watchInterval <= 0) {
      watchInterval = defaultWatchInterval;
    }
    return (watchInterval * millisInSecond);
  }
}
