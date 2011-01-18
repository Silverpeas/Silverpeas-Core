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
package com.sun.portal.portletcontainer.driver.admin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.silverpeas.portlets.portal.DesktopMessages;
import com.silverpeas.portlets.portal.PortletWindowDataImpl;
import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.sun.portal.portletcontainer.admin.registry.PortletRegistryConstants;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryException;
import com.sun.portal.portletcontainer.invoker.WindowInvokerConstants;

/**
 * AdminServlet is a router for admin related requests like deploying/undeploying of portlets and
 * creating of portlet windows.
 */
public class AdminServlet extends HttpServlet {

  private ServletContext context;

  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    context = config.getServletContext();
  }

  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doGetPost(request, response);
  }

  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    doGetPost(request, response);
  }

  public void doGetPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    String elementId = getUserIdOrSpaceId(request, false);
    String spaceId = getSpaceId(request);
    String userId = getUserId(request);
    String language = getLanguage(request);

    DesktopMessages.init(request);
    response.setContentType("text/html;charset=UTF-8");
    HttpSession session = AdminUtils.getClearedSession(request);
    PortletAdminData portletAdminData = null;
    try {
      portletAdminData = PortletAdminDataFactory.getPortletAdminData(elementId);
    } catch (PortletRegistryException pre) {
      throw new IOException(pre.getMessage());
    }
    AdminUtils.setAttributes(session, portletAdminData, elementId, userId, spaceId, language);

    if (isParameterPresent(request, AdminConstants.CREATE_PORTLET_WINDOW_SUBMIT)) {
      // String portletWindowName = request.getParameter(AdminConstants.PORTLET_WINDOW_NAME);
      Date timestamp = new Date();
      String portletWindowName = String.valueOf(timestamp.getTime());
      timestamp = null;

      String portletName = request.getParameter(AdminConstants.PORTLET_LIST);
      String title = request.getParameter(AdminConstants.PORTLET_WINDOW_TITLE);
      if (portletName == null) {
        String message = DesktopMessages.getLocalizedString(AdminConstants.NO_BASE_PORTLET);
        session.setAttribute(AdminConstants.CREATION_FAILED_ATTRIBUTE, message);
      } else {
        boolean isValid = validateString(portletWindowName, false);
        boolean isDuplicate = false;
        if (isValid) {
          // Check if a portlet window already exists with the same name.
          List<String> portletWindowNames = portletAdminData.getPortletWindowNames();
          if (portletWindowNames != null) {
            for (String tempPortletWindowName : portletWindowNames) {
              if (portletWindowName.equals(tempPortletWindowName)) {
                String message =
                    DesktopMessages.getLocalizedString(
                    AdminConstants.PORTLET_WINDOW_NAME_ALREADY_EXISTS,
                    new String[] { portletWindowName });
                session.setAttribute(AdminConstants.CREATION_FAILED_ATTRIBUTE, message);
                isDuplicate = true;
                break;
              }
            }
          }
        }

        if (!isDuplicate) {
          if (isValid) {
            isValid = validateString(title, true);
          }
          StringBuffer messageBuffer =
              new StringBuffer(DesktopMessages.getLocalizedString(AdminConstants.CREATION_FAILED));
          if (isValid) {
            boolean success = false;
            try {
              success = portletAdminData.createPortletWindow(portletName, portletWindowName, title);
            } catch (Exception ex) {
              messageBuffer.append(".");
              messageBuffer.append(ex.getMessage());
            }
            if (success) {
              String message =
                  DesktopMessages.getLocalizedString(AdminConstants.CREATION_SUCCEEDED);
              session.setAttribute(AdminConstants.CREATION_SUCCEEDED_ATTRIBUTE, message);
              AdminUtils.refreshList(request, elementId, userId, spaceId, language);
            } else {
              session.setAttribute(AdminConstants.CREATION_FAILED_ATTRIBUTE, messageBuffer
                  .toString());
            }
          } else {
            String message = DesktopMessages.getLocalizedString(AdminConstants.INVALID_CHARACTERS);
            session.setAttribute(AdminConstants.CREATION_FAILED_ATTRIBUTE, message);
          }
        }
      }
    } else if (isParameterPresent(request, AdminConstants.MODIFY_PORTLET_WINDOW_SUBMIT)) {
      String portletWindowName = request.getParameter(AdminConstants.PORTLET_WINDOW_LIST);
      setSelectedPortletWindow(session, portletWindowName);
      String width = request.getParameter(AdminConstants.WIDTH_LIST);
      String visibleValue = request.getParameter(AdminConstants.VISIBLE_LIST);
      boolean visible;
      if (PortletRegistryConstants.VISIBLE_TRUE.equals(visibleValue)) {
        visible = true;
      } else {
        visible = false;
      }
      if (portletWindowName == null) {
        String message = DesktopMessages.getLocalizedString(AdminConstants.NO_BASE_PORTLET_WINDOW);
        session.setAttribute(AdminConstants.MODIFY_FAILED_ATTRIBUTE, message);
      } else {
        StringBuffer messageBuffer =
            new StringBuffer(DesktopMessages.getLocalizedString(AdminConstants.MODIFY_FAILED));
        boolean success = false;
        try {
          success = portletAdminData.modifyPortletWindow(portletWindowName, width, visible, null);
          AdminUtils.setPortletWindowAttributes(session, portletAdminData, portletWindowName);
        } catch (Exception ex) {
          messageBuffer.append(".");
          messageBuffer.append(ex.getMessage());
        }
        if (success) {
          String message = DesktopMessages.getLocalizedString(AdminConstants.MODIFY_SUCCEEDED);
          session.setAttribute(AdminConstants.MODIFY_SUCCEEDED_ATTRIBUTE, message);
        } else {
          session.setAttribute(AdminConstants.MODIFY_FAILED_ATTRIBUTE, messageBuffer.toString());
        }
      }
    } else if (isParameterPresent(request, AdminConstants.MOVE_PORTLET_WINDOW)) {
      // setSelectedPortletWindow(session, portletWindowName);
      String column1 = request.getParameter("column1");
      String column2 = request.getParameter("column2");

      List windows = new ArrayList<PortletWindowDataImpl>();

      List list = portletAdminData.getPortletWindowNames();

      StringTokenizer tokenizer = new StringTokenizer(column1, ",");
      PortletWindowDataImpl window = new PortletWindowDataImpl();
      String token = null;
      int i = 0;
      while (tokenizer.hasMoreTokens()) {
        token = tokenizer.nextToken();
        window = new PortletWindowDataImpl();
        window.setPortletWindowName(token);
        window.setWidth("thick");
        window.setRowNumber(i);
        windows.add(window);
        // movePortletWindow(token, "thick", Integer.toString(i), true, session, portletAdminData);
        i++;
      }

      try {
        String portletWindowName = null;
        for (int p = 0; list != null && p < list.size(); p++) {
          portletWindowName = (String) list.get(p);
          if (!portletAdminData.isVisible(portletWindowName)) {
            if ("thick".equals(portletAdminData.getWidth(portletWindowName))) {
              // movePortletWindow(portletWindowName, "thick", Integer.toString(i), false, session,
              // portletAdminData);
              window = new PortletWindowDataImpl();
              window.setPortletWindowName(token);
              window.setWidth("thick");
              window.setRowNumber(i);
              windows.add(window);
              i++;
            }
          }
        }

        tokenizer = new StringTokenizer(column2, ",");
        i = 0;
        while (tokenizer.hasMoreTokens()) {
          token = tokenizer.nextToken();
          // movePortletWindow(token, "thin", Integer.toString(i), true, session, portletAdminData);
          window = new PortletWindowDataImpl();
          window.setPortletWindowName(token);
          window.setWidth("thin");
          window.setRowNumber(i);
          windows.add(window);
          i++;
        }

        for (int p = 0; list != null && p < list.size(); p++) {
          portletWindowName = (String) list.get(p);
          if (!portletAdminData.isVisible(portletWindowName)) {
            if ("thin".equals(portletAdminData.getWidth(portletWindowName))) {
              // movePortletWindow(portletWindowName, "thin", Integer.toString(i), false, session,
              // portletAdminData);
              window = new PortletWindowDataImpl();
              window.setPortletWindowName(token);
              window.setWidth("thin");
              window.setRowNumber(i);
              windows.add(window);
              i++;
            }
          }
        }

        portletAdminData.movePortletWindows(windows);
      } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }

      RequestDispatcher reqd = context.getRequestDispatcher(getPresentationURI(request));
      reqd.forward(request, response);

    } else if (isParameterPresent(request, AdminConstants.PORTLET_WINDOW_LIST)) {
      String portletWindowName = request.getParameter(AdminConstants.PORTLET_WINDOW_LIST);
      setSelectedPortletWindow(session, portletWindowName);
      if (portletWindowName == null) {
        String message = DesktopMessages.getLocalizedString(AdminConstants.NO_BASE_PORTLET_WINDOW);
        session.setAttribute(AdminConstants.NO_WINDOW_DATA_ATTRIBUTE, message);
      } else {
        StringBuffer messageBuffer =
            new StringBuffer(DesktopMessages.getLocalizedString(AdminConstants.NO_WINDOW_DATA));
        // Set the attribues for show/hide and thick/thin
        boolean success = false;
        try {
          AdminUtils.setPortletWindowAttributes(session, portletAdminData, portletWindowName);
          success = true;
        } catch (Exception ex) {
          messageBuffer.append(".");
          messageBuffer.append(ex.getMessage());
        }
        if (!success) {
          session.setAttribute(AdminConstants.NO_WINDOW_DATA_ATTRIBUTE, messageBuffer.toString());
        }
      }
    } else {
      try {
        AdminUtils.setPortletWindowAttributes(session, portletAdminData, null);
      } catch (Exception ex) {
        StringBuffer messageBuffer =
            new StringBuffer(DesktopMessages.getLocalizedString(AdminConstants.NO_WINDOW_DATA));
        messageBuffer.append(".");
        messageBuffer.append(ex.getMessage());
        session.setAttribute(AdminConstants.NO_WINDOW_DATA_ATTRIBUTE, messageBuffer.toString());
      }
    }

    RequestDispatcher reqd = context.getRequestDispatcher("/portlet/jsp/jsr/admin.jsp");
    reqd.forward(request, response);
  }
  

  private boolean validateString(String name, boolean allowSpaces) {
    if (name == null || name.trim().length() == 0) {
      return false;
    }
    return true;
  }

  private boolean isParameterPresent(HttpServletRequest request,
      String parameter) {
    String name = request.getParameter(parameter);
    return (name == null ? false : true);
  }

  private void setSelectedPortletWindow(HttpSession session,
      String portletWindowName) {
    session.removeAttribute(AdminConstants.SELECTED_PORTLET_WINDOW_ATTRIBUTE);
    session.setAttribute(AdminConstants.SELECTED_PORTLET_WINDOW_ATTRIBUTE,
        portletWindowName);
  }

  protected String getPresentationURI(HttpServletRequest request) {
    String spaceId = getSpaceId(request);

    if (!StringUtil.isDefined(spaceId) || isSpaceBackOffice(request)) {
      return "/portlet/jsp/jsr/desktop.jsp";
    } else {
      request.setAttribute("DisableMove", Boolean.TRUE);
      return "/portlet/jsp/jsr/spaceDesktop.jsp";
    }
  }

  private String getUserIdOrSpaceId(HttpServletRequest request,
      boolean getSpaceIdOnly) {
    String userId = null;
    String spaceId = getSpaceId(request);

    if (getSpaceIdOnly)
      return spaceId;

    if (!StringUtil.isDefined(spaceId)) {
      return getUserId(request);
    }
    return spaceId;
  }

  private String getUserId(HttpServletRequest request) {
    // Display the private user homepage
    // retrieve userId from session
    HttpSession session = request.getSession();
    MainSessionController m_MainSessionCtrl = (MainSessionController) session
        .getAttribute("SilverSessionController");

    String userId = m_MainSessionCtrl.getUserId();

    return userId;

  }

  private String getLanguage(HttpServletRequest request) {
    // Display the private user homepage
    // retrieve userId from session
    HttpSession session = request.getSession();
    MainSessionController m_MainSessionCtrl = (MainSessionController) session
        .getAttribute("SilverSessionController");

    return m_MainSessionCtrl.getFavoriteLanguage();
  }

  private String getSpaceId(HttpServletRequest request) {
    String spaceId = request.getParameter("SpaceId");
    if (!StringUtil.isDefined(spaceId))
      spaceId = request.getParameter(WindowInvokerConstants.DRIVER_SPACEID);

    if (StringUtil.isDefined(spaceId)) {
      // Display the space homepage
      if (!spaceId.startsWith("space"))
        spaceId = "space" + spaceId;
    }
    return spaceId;
  }

  private boolean isSpaceBackOffice(HttpServletRequest request) {
    return (StringUtil.isDefined(getSpaceId(request)) && "admin"
        .equalsIgnoreCase(request
        .getParameter(WindowInvokerConstants.DRIVER_ROLE)));
  }

  private boolean isSpaceFrontOffice(HttpServletRequest request) {
    return (StringUtil.isDefined(getSpaceId(request)) && !StringUtil
        .isDefined(request.getParameter(WindowInvokerConstants.DRIVER_ROLE)));
  }
}
