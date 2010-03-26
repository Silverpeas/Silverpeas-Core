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
package com.sun.portal.portletcontainer.driver.admin;

import java.io.FileInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import com.silverpeas.portlets.portal.PortletAppData;
import com.silverpeas.portlets.portal.PortletAppDataImpl;
import com.silverpeas.util.StringUtil;
import com.sun.portal.container.EntityID;
import com.sun.portal.container.PortletType;
import com.sun.portal.portletcontainer.admin.mbeans.PortletAdmin;
import com.sun.portal.portletcontainer.admin.mbeans.PortletAdminMBean;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryContext;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryException;

/**
 * PortletAdminDataImpl provides concrete implementation of PortletAdminData interface
 */
public class PortletAdminDataImpl implements PortletAdminData, Serializable {

  private static Logger logger =
      Logger.getLogger(PortletAdminDataImpl.class.getPackage().getName(),
      "com.silverpeas.portlets.PCDLogMessages");
  static final long serialVersionUID = 3L;
  private PortletRegistryContext portletRegistryContext;

  public void init(PortletRegistryContext portletRegistryContext) throws PortletRegistryException {
    this.portletRegistryContext = portletRegistryContext;
  }

  public boolean deploy(String warName, boolean deployToContainer) throws Exception {
    return deploy(warName, null, null, deployToContainer);
  }

  public boolean deploy(String warName, String rolesFilename, String userInfoFilename,
      boolean deployToContainer) throws Exception {
    boolean success;
    try {
      PortletAdminMBean portletadmin = new PortletAdmin();
      Properties roleProperties = new Properties();
      if (rolesFilename != null) {
        roleProperties.load(new FileInputStream(rolesFilename));
      }
      Properties userInfoProperties = new Properties();
      if (userInfoFilename != null) {
        userInfoProperties.load(new FileInputStream(userInfoFilename));
      }
      portletadmin.deploy(warName, roleProperties, userInfoProperties, deployToContainer);
      success = true;
    } catch (Exception e) {
      if (logger.isLoggable(Level.SEVERE)) {
        LogRecord logRecord = new LogRecord(Level.SEVERE, "PSPCD_CSPPD0023");
        logRecord.setLoggerName(logger.getName());
        logRecord.setParameters(new String[] { warName });
        logRecord.setThrown(e);
        logger.log(logRecord);
      }
      success = false;
      throw e;
    }
    return success;
  }

  public boolean undeploy(String warName, boolean undeployFromContainer) throws Exception {
    Boolean success;
    try {
      PortletAdminMBean portletadmin = new PortletAdmin();
      success = portletadmin.undeploy(warName, undeployFromContainer);
    } catch (Exception e) {
      if (logger.isLoggable(Level.SEVERE)) {
        LogRecord logRecord = new LogRecord(Level.SEVERE, "PSPCD_CSPPD0031");
        logRecord.setLoggerName(logger.getName());
        logRecord.setParameters(new String[] { warName });
        logRecord.setThrown(e);
        logger.log(logRecord);
      }
      success = Boolean.FALSE;
      throw e;
    }
    return success.booleanValue();
  }

  public List<PortletAppData> getPortlets(String locale) {
    List<PortletAppData> portlets = new ArrayList<PortletAppData>();

    List<String> portletNames = getPortletNames();
    if (portletNames != null) {
      String displayName;
      String description;
      PortletAppDataImpl portlet;
      for (String portletName : portletNames) {
        if (portletName != null) {
          portlet = new PortletAppDataImpl(portletName);
          try {
            displayName = portletRegistryContext.getDisplayName(portletName, locale);
            if (!StringUtil.isDefined(displayName))
              displayName = portletRegistryContext.getTitle(portletName, locale);
            portlet.setLabel(displayName);

            description = portletRegistryContext.getDescription(portletName, locale);
            if (StringUtil.isDefined(description))
              portlet.setDescription(description);
          } catch (PortletRegistryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }

          portlets.add(portlet);
        }
      }
    }

    return portlets;
  }

  public List<String> getPortletNames() {
    try {
      return portletRegistryContext.getAvailablePortlets();
    } catch (PortletRegistryException pre) {
      logger.log(Level.SEVERE, "PSPCD_CSPPD0024", pre);
    }
    return Collections.EMPTY_LIST;
  }

  public List<String> getPortletApplicationNames() {
    List portletApps = new ArrayList();
    try {
      List<EntityID> entityIds = portletRegistryContext.getEntityIds();
      if (entityIds != null) {
        String portletAppName;
        for (EntityID entityId : entityIds) {
          if (entityId != null) {
            portletAppName = entityId.getPortletApplicationName();
            if (portletAppName != null && !portletAppName.equals("null")
                && !portletApps.contains(portletAppName)) {
              portletApps.add(portletAppName);
            }
          }
        }
      }
    } catch (PortletRegistryException pre) {
      logger.log(Level.SEVERE, "PSPCD_CSPPD0032", pre);
    }
    return portletApps;
  }

  public List<String> getPortletDisplayNames(String locale) {
    List portletApps = new ArrayList();
    try {
      List<String> portletNames = getPortletNames();
      String displayName = null;
      for (String portletName : portletNames) {
        if (portletName != null) {
          displayName = portletRegistryContext.getDisplayName(portletName, locale);
          if (StringUtil.isDefined(displayName))
            portletApps.add(displayName);
        }
      }
    } catch (PortletRegistryException e) {
      logger.log(Level.SEVERE, "PSPCD_CSPPD0032", e);
    }
    return portletApps;
  }

  public List getPortletWindowNames() {
    try {
      return portletRegistryContext.getAllPortletWindows(PortletType.LOCAL);
    } catch (PortletRegistryException pre) {
      logger.log(Level.SEVERE, "PSPCD_CSPPD0024", pre);
    }
    return Collections.EMPTY_LIST;
  }

  public boolean createPortletWindow(String portletName, String portletWindowName, String title)
      throws Exception {
    boolean success;
    try {
      portletRegistryContext.createPortletWindow(portletName, portletWindowName, title, Locale
          .getDefault().toString());
      success = true;
    } catch (PortletRegistryException pre) {
      if (logger.isLoggable(Level.SEVERE)) {
        LogRecord logRecord = new LogRecord(Level.SEVERE, "PSPCD_CSPPD0025");
        logRecord.setLoggerName(logger.getName());
        logRecord.setParameters(new String[] { portletName });
        logRecord.setThrown(pre);
        logger.log(logRecord);
      }
      success = false;
      throw pre;
    }
    return success;
  }

  public boolean modifyPortletWindow(String portletWindowName, String width, boolean visible,
      String row) throws Exception {
    boolean success;
    try {
      String exisitingWidth = getWidth(portletWindowName);
      // if(!exisitingWidth.equals(width)) {
      portletRegistryContext.setWidth(portletWindowName, width, row);
      // }
      boolean exisitingVisibleValue = isVisible(portletWindowName);
      if (exisitingVisibleValue != visible) {
        portletRegistryContext.showPortletWindow(portletWindowName, visible);
      }
      success = true;
    } catch (PortletRegistryException pre) {
      if (logger.isLoggable(Level.SEVERE)) {
        LogRecord logRecord = new LogRecord(Level.SEVERE, "PSPCD_CSPPD0025");
        logRecord.setLoggerName(logger.getName());
        logRecord.setParameters(new String[] { portletWindowName });
        logRecord.setThrown(pre);
        logger.log(logRecord);
      }
      success = false;
      throw pre;
    }
    return success;
  }

  public boolean movePortletWindows(List portletWindows) throws Exception {
    boolean success;
    try {
      portletRegistryContext.movePortletWindows(portletWindows);
      success = true;
    } catch (Exception e) {
      success = false;
      throw e;
    }
    return success;
  }

  public boolean isVisible(String portletWindowName) throws Exception {
    return portletRegistryContext.isVisible(portletWindowName);
  }

  public String getWidth(String portletWindowName) throws Exception {
    return portletRegistryContext.getWidth(portletWindowName);
  }
}
