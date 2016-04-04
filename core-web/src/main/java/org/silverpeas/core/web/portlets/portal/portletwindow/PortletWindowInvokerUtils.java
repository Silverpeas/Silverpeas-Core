/*
 * Copyright (C) 2000 - 2016 Silverpeas
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

package org.silverpeas.core.web.portlets.portal.portletwindow;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.portal.container.EntityID;
import com.sun.portal.container.PortletWindowContext;
import com.sun.portal.container.PortletWindowContextException;
import com.sun.portal.portletcontainer.invoker.InvokerException;

/**
 * PortletWindowInvokerUtils is a utility class used by the PortletWindowInvoker
 */
public class PortletWindowInvokerUtils {
  private static final Logger logger = Logger.getLogger(
      "org.silverpeas.core.web.portlets.portal.portletwindow",
      "org.silverpeas.portlets.PCDLogMessages");

  /**
   * Returns the entityID. It is stored in the portlet registry during deployment time.
   */
  public static EntityID getEntityID(PortletWindowContext pwc,
      String portletWindowName) throws InvokerException {
    try {
      EntityID entityId = pwc.getEntityID(portletWindowName);
      return entityId;
    } catch (PortletWindowContextException pwce) {
      throw new InvokerException("PortletWindowInvokerUtils.getEntityID():"
          + " couldn't get entityIDPrefix for portlet window "
          + portletWindowName, pwce);
    }
  }

  /**
   * Gets the userInfoMap property for a portlet window. It is stored in the registry during
   * deployment time.
   */
  public static Map getUserInfoMap(PortletWindowContext pwc,
      String portletWindowName) throws InvokerException {
    Map userInfoMap = null;
    try {
      String portletName = pwc.getPortletName(portletWindowName);
      userInfoMap = pwc.getUserInfoMap(portletName);
    } catch (PortletWindowContextException pwce) {
      throw new InvokerException("PortletWindowInvokerUtils.getUserInfoMap():"
          + " couldn't get roleMap for portlet window " + portletWindowName,
          pwce);
    }
    return userInfoMap;
  }

  /**
   * Gets the roleMap property for a portlet window. It is stored in the registry during deployment
   * time.
   */
  public static Map getRoleMap(PortletWindowContext pwc,
      String portletWindowName) throws InvokerException {
    Map roleMap = null;
    try {
      String portletName = pwc.getPortletName(portletWindowName);
      roleMap = pwc.getRoleMap(portletName);
    } catch (PortletWindowContextException pwce) {
      throw new InvokerException("PortletWindowInvokerUtils.getRoleMap():"
          + " couldn't get roleMap for portlet window " + portletWindowName,
          pwce);
    }
    return roleMap;
  }

  /**
   * Returns the title of a portlet window. It is stored in the registry during deployment time.
   */
  public static String getPortletWindowTitle(PortletWindowContext pwc,
      String portletWindowName) throws InvokerException {
    String title = null;
    try {
      title = pwc.getPortletWindowTitle(portletWindowName, pwc
          .getLocaleString());
    } catch (PortletWindowContextException pwce) {
      throw new InvokerException(
          "PortletWindowInvokerUtils.getPortletWindowTitle():"
          + " couldn't get title for portlet window " + portletWindowName,
          pwce);
    }
    return title;
  }

  /**
   * Returns the title of a portlet. It is stored in the registry during deployment time.
   */
  public static String getPortletTitle(String portletWindowName,
      PortletWindowContext pwc) throws InvokerException {
    String title = null;
    try {
      String portletName = pwc.getPortletName(portletWindowName);
      String localeString = pwc.getLocaleString();
      title = pwc.getTitle(portletName, localeString);
      if (title == null) {
        logger.log(Level.SEVERE, "PSPCD_CSPPD0021", new String[] { portletName,
            localeString });
      }
    } catch (PortletWindowContextException pwce) {
      throw new InvokerException("PortletWindowInvokerUtils.getPortletTitle():"
          + " couldn't get title for portlet " + portletWindowName, pwce);
    }
    return title;
  }
}
