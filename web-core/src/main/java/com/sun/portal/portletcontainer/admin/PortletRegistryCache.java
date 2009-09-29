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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.sun.portal.portletcontainer.admin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.sun.portal.portletcontainer.admin.registry.PortletAppRegistryContext;
import com.sun.portal.portletcontainer.admin.registry.PortletWindowPreferenceRegistryContext;
import com.sun.portal.portletcontainer.admin.registry.PortletWindowRegistryContext;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryException;

/**
 * PortletRegistryCache maintains the cache of Portlet registry objects.
 * 
 */
public class PortletRegistryCache {

  private static Logger logger = Logger.getLogger(
      "com.sun.portal.portletcontainer.admin",
      "com.silverpeas.portlets.PALogMessages");

  private static PortletAppRegistryContext portletAppRegistryContext;
  private static PortletWindowRegistryContext portletWindowRegistryContext;
  private static PortletWindowPreferenceRegistryContext portletWindowPreferenceRegistryContext;
  private static Object syncObject = new Object();
  private static int NUMBER = 100;

  // During startup update the cache
  public static void init() {
    updatePortletRegistryCacheMonitor(PortletRegistryFile.PORTLET_APP_REGISTRY_FILE);
    updatePortletRegistryCacheMonitor(PortletRegistryFile.PORTLET_WINDOW_REGISTRY_FILE);
    updatePortletRegistryCacheMonitor(PortletRegistryFile.PORTLET_WINDOW_PREFERENCE_REGISTRY_FILE);
  }

  public static void refreshPortletAppRegistryCache(boolean refresh) {
    if (refresh) {
      updatePortletRegistryCacheMonitor(PortletRegistryFile.PORTLET_APP_REGISTRY_FILE);
    } else {
      clearPortletRegistryCacheMonitor(PortletRegistryFile.PORTLET_APP_REGISTRY_FILE);
    }
  }

  public static boolean readPortletAppRegistryCache() {
    return readPortletRegistryCacheMonitor(PortletRegistryFile.PORTLET_APP_REGISTRY_FILE);
  }

  public static void refreshPortletWindowRegistryCache(boolean refresh) {
    if (refresh) {
      updatePortletRegistryCacheMonitor(PortletRegistryFile.PORTLET_WINDOW_REGISTRY_FILE);
    } else {
      clearPortletRegistryCacheMonitor(PortletRegistryFile.PORTLET_WINDOW_REGISTRY_FILE);
    }
  }

  public static boolean readPortletWindowRegistryCache() {
    return readPortletRegistryCacheMonitor(PortletRegistryFile.PORTLET_WINDOW_REGISTRY_FILE);
  }

  public static void refreshPortletWindowPreferenceRegistryCache(boolean refresh) {
    if (refresh) {
      updatePortletRegistryCacheMonitor(PortletRegistryFile.PORTLET_WINDOW_PREFERENCE_REGISTRY_FILE);
    } else {
      clearPortletRegistryCacheMonitor(PortletRegistryFile.PORTLET_WINDOW_PREFERENCE_REGISTRY_FILE);
    }
  }

  public static boolean readPortletWindowPreferenceRegistryCache() {
    return readPortletRegistryCacheMonitor(PortletRegistryFile.PORTLET_WINDOW_PREFERENCE_REGISTRY_FILE);
  }

  public static void setPortletAppRegistryContext(
      PortletAppRegistryContext tmpPortletAppRegistryContext) {
    portletAppRegistryContext = tmpPortletAppRegistryContext;
  }

  public static void setPortletWindowRegistryContext(
      PortletWindowRegistryContext tmpPortletWindowRegistryContext) {
    portletWindowRegistryContext = tmpPortletWindowRegistryContext;
  }

  public static void setPortletWindowPreferenceRegistryContext(
      PortletWindowPreferenceRegistryContext tmpPortletWindowPreferenceRegistryContext) {
    portletWindowPreferenceRegistryContext = tmpPortletWindowPreferenceRegistryContext;
  }

  public static PortletAppRegistryContext getPortletAppRegistryContext() {
    return portletAppRegistryContext;
  }

  public static PortletWindowRegistryContext getPortletWindowRegistryContext() {
    return portletWindowRegistryContext;
  }

  public static PortletWindowPreferenceRegistryContext getPortletWindowPreferenceRegistryContext() {
    return portletWindowPreferenceRegistryContext;
  }

  private static void updatePortletRegistryCacheMonitor(String fileName) {
    FileOutputStream out = null;
    try {
      synchronized (syncObject) {
        out = new FileOutputStream(getMonitoredFileName(fileName));
        out.write(NUMBER);
      }
    } catch (IOException e) {
      if (logger.isLoggable(Level.INFO)) {
        LogRecord logRecord = new LogRecord(Level.INFO, "PSPL_CSPPAM0015");
        logRecord.setLoggerName(logger.getName());
        logRecord
            .setParameters(new String[] { getMonitoredFileName(fileName) });
        logRecord.setThrown(e);
        logger.log(logRecord);
      }
    } finally {
      try {
        if (out != null) {
          out.close();
        }
      } catch (Exception ignored) {
      }
    }
  }

  private static void clearPortletRegistryCacheMonitor(String fileName) {
    FileOutputStream out = null;
    try {
      synchronized (syncObject) {
        out = new FileOutputStream(getMonitoredFileName(fileName));
      }
    } catch (IOException e) {
      if (logger.isLoggable(Level.INFO)) {
        LogRecord logRecord = new LogRecord(Level.INFO, "PSPL_CSPPAM0015");
        logRecord.setLoggerName(logger.getName());
        logRecord
            .setParameters(new String[] { getMonitoredFileName(fileName) });
        logRecord.setThrown(e);
        logger.log(logRecord);
      }
    } finally {
      try {
        if (out != null) {
          out.close();
        }
      } catch (Exception ignored) {
      }
    }
  }

  private static boolean readPortletRegistryCacheMonitor(String fileName) {
    int value;
    FileInputStream in = null;
    try {
      synchronized (syncObject) {
        in = new FileInputStream(getMonitoredFileName(fileName));
        value = in.read();
      }
    } catch (IOException ignored) {
      // If exception do not read from the cache, hence set do not value -1
      value = NUMBER;
    } finally {
      try {
        if (in != null) {
          in.close();
        }
      } catch (Exception ignored) {
      }
    }
    // If the value is -1, it means, read from the cache
    if (value == -1) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * Returns the monitored config file name. The monitored file name is same as
   * the file name with "." prepended.
   */
  public static String getMonitoredFileName(String fileName) {
    String registryLocation = null;
    StringBuffer buffer = new StringBuffer();
    try {
      registryLocation = PortletRegistryHelper.getRegistryLocation();
    } catch (PortletRegistryException ignored) {
    }
    if (registryLocation != null) {
      buffer.append(registryLocation);
      buffer.append(File.separator);
    }
    buffer.append(".");
    buffer.append(fileName);
    logger.log(Level.FINER, "PSPL_CSPPAM0014", buffer);
    return buffer.toString();
  }
}
