/*
 * CDDL HEADER START
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.sun.com/cddl/cddl.html and legal/CDDLv1.0.txt
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 * CDDL HEADER END
 */
package com.sun.portal.portletcontainer.admin;

import com.sun.portal.portletcontainer.admin.registry.PortletAppRegistryContext;
import com.sun.portal.portletcontainer.admin.registry.PortletWindowPreferenceRegistryContext;
import com.sun.portal.portletcontainer.admin.registry.PortletWindowRegistryContext;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * PortletRegistryCache maintains the cache of Portlet registry objects.
 */
public class PortletRegistryCache {

  private static Logger logger = Logger.getLogger("com.sun.portal.portletcontainer.admin",
      "org.silverpeas.portlets.PALogMessages");

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
    return value == -1;
  }

  /**
   * Returns the monitored config file name. The monitored file name is same as the file name with
   * "." prepended.
   */
  public static String getMonitoredFileName(String fileName) {
    String registryLocation = null;
    StringBuilder buffer = new StringBuilder();
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
