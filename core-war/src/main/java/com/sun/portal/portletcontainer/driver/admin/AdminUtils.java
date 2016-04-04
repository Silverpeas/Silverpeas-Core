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
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package com.sun.portal.portletcontainer.driver.admin;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.sun.portal.portletcontainer.admin.registry.PortletRegistryConstants;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryException;

/**
 * AdminUtils is a utility class for admin UI related tasks
 */
public class AdminUtils {

  private static Logger logger = Logger.getLogger(AdminUtils.class.getPackage()
      .getName(), "org.silverpeas.portlets.PCDLogMessages");

  protected static HttpSession getClearedSession(HttpServletRequest request) {
    HttpSession session = request.getSession(true);
    session.removeAttribute(AdminConstants.PORTLETS_ATTRIBUTE);
    session.removeAttribute(AdminConstants.PORTLET_APPLICATIONS_ATTRIBUTE);
    session.removeAttribute(AdminConstants.PORTLET_WINDOWS_ATTRIBUTE);

    session.removeAttribute(AdminConstants.SHOW_WINDOW_ATTRIBUTE);
    session.removeAttribute(AdminConstants.HIDE_WINDOW_ATTRIBUTE);
    session.removeAttribute(AdminConstants.THICK_WINDOW_ATTRIBUTE);
    session.removeAttribute(AdminConstants.THIN_WINDOW_ATTRIBUTE);

    session.removeAttribute(AdminConstants.DEPLOYMENT_SUCCEEDED_ATTRIBUTE);
    session.removeAttribute(AdminConstants.DEPLOYMENT_FAILED_ATTRIBUTE);
    session.removeAttribute(AdminConstants.UNDEPLOYMENT_SUCCEEDED_ATTRIBUTE);
    session.removeAttribute(AdminConstants.UNDEPLOYMENT_FAILED_ATTRIBUTE);
    session.removeAttribute(AdminConstants.CREATION_SUCCEEDED_ATTRIBUTE);
    session.removeAttribute(AdminConstants.CREATION_FAILED_ATTRIBUTE);
    session.removeAttribute(AdminConstants.MODIFY_SUCCEEDED_ATTRIBUTE);
    session.removeAttribute(AdminConstants.MODIFY_FAILED_ATTRIBUTE);
    session.removeAttribute(AdminConstants.NO_WINDOW_DATA_ATTRIBUTE);
    session.removeAttribute(AdminConstants.SELECTED_PORTLET_WINDOW_ATTRIBUTE);

    session.removeAttribute(AdminConstants.CURRENT_SILVERPEAS_ELEMENT_ID);
    return session;
  }

  protected static void refreshList(HttpServletRequest request, String locale) {
    refreshList(request, "useless", "useless", "useless", locale);
  }

  protected static void refreshList(HttpServletRequest request, String context,
      String userId, String spaceId, String locale) {
    try {
      PortletAdminData portletAdminData = PortletAdminDataFactory
          .getPortletAdminData(context);
      HttpSession session = request.getSession(true);
      setAttributes(session, portletAdminData, context, userId, spaceId, locale);
    } catch (PortletRegistryException pre) {
      logger.log(Level.SEVERE, "PSPCD_CSPPD0038", pre);
    }
  }

  protected static void setAttributes(HttpSession session,
      PortletAdminData portletAdminData, String elementId, String userId,
      String spaceId, String locale) {
    session.removeAttribute(AdminConstants.PORTLETS_ATTRIBUTE);
    session.removeAttribute(AdminConstants.PORTLET_APPLICATIONS_ATTRIBUTE);
    session.removeAttribute(AdminConstants.PORTLET_WINDOWS_ATTRIBUTE);
    session.removeAttribute(AdminConstants.CURRENT_SILVERPEAS_ELEMENT_ID);
    session.removeAttribute(AdminConstants.CURRENT_SILVERPEAS_USER_ID);
    session.removeAttribute(AdminConstants.CURRENT_SILVERPEAS_SPACE_ID);

    session.setAttribute(AdminConstants.PORTLETS_ATTRIBUTE, portletAdminData
        .getPortlets(locale));
    session.setAttribute(AdminConstants.PORTLET_APPLICATIONS_ATTRIBUTE,
        portletAdminData.getPortletApplicationNames());
    session.setAttribute(AdminConstants.PORTLET_WINDOWS_ATTRIBUTE,
        portletAdminData.getPortletWindowNames());
    session.setAttribute(AdminConstants.CURRENT_SILVERPEAS_ELEMENT_ID,
        elementId);
    session.setAttribute(AdminConstants.CURRENT_SILVERPEAS_USER_ID, userId);
    session.setAttribute(AdminConstants.CURRENT_SILVERPEAS_SPACE_ID, spaceId);
  }

  protected static void setPortletWindowAttributes(HttpSession session,
      PortletAdminData portletAdminData, String portletWindowName)
      throws Exception {
    // If portlet window name is null, get the name from the portlet window list
    if (portletWindowName == null) {
      List<String> list = portletAdminData.getPortletWindowNames();
      if (list != null) {
        portletWindowName = list.get(0);
      }
    }
    if (portletWindowName != null) {
      session.removeAttribute(AdminConstants.SHOW_WINDOW_ATTRIBUTE);
      session.removeAttribute(AdminConstants.HIDE_WINDOW_ATTRIBUTE);
      session.removeAttribute(AdminConstants.THICK_WINDOW_ATTRIBUTE);
      session.removeAttribute(AdminConstants.THIN_WINDOW_ATTRIBUTE);

      boolean visible = portletAdminData.isVisible(portletWindowName);
      String width = portletAdminData.getWidth(portletWindowName);
      if (visible) {
        session.setAttribute(AdminConstants.SHOW_WINDOW_ATTRIBUTE, "checked");
        session.setAttribute(AdminConstants.HIDE_WINDOW_ATTRIBUTE, "");
      } else {
        session.setAttribute(AdminConstants.HIDE_WINDOW_ATTRIBUTE, "checked");
        session.setAttribute(AdminConstants.SHOW_WINDOW_ATTRIBUTE, "");
      }
      if (PortletRegistryConstants.WIDTH_THICK.equals(width)) {
        session.setAttribute(AdminConstants.THICK_WINDOW_ATTRIBUTE, "selected");
        session.setAttribute(AdminConstants.THIN_WINDOW_ATTRIBUTE, "");
      } else {
        session.setAttribute(AdminConstants.THIN_WINDOW_ATTRIBUTE, "selected");
        session.setAttribute(AdminConstants.THICK_WINDOW_ATTRIBUTE, "");
      }
    }
  }
}
