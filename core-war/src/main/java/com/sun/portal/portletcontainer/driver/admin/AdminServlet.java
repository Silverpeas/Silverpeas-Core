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

import org.silverpeas.web.portlets.portal.DesktopMessages;
import org.silverpeas.core.web.portlets.portal.PortletWindowData;
import org.silverpeas.core.web.portlets.portal.PortletWindowDataImpl;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.admin.user.model.UserDetail;
import com.sun.portal.portletcontainer.admin.registry.PortletRegistryConstants;
import com.sun.portal.portletcontainer.context.registry.PortletRegistryException;
import com.sun.portal.portletcontainer.invoker.WindowInvokerConstants;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * AdminServlet is a router for admin related requests like deploying/undeploying of portlets and
 * creating of portlet windows.
 */
public class AdminServlet extends HttpServlet {

  private static final long serialVersionUID = -7492755183604919041L;
  private ServletContext context;

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
    context = config.getServletContext();
  }

  @Override
  protected void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    String elementId = getUserIdOrSpaceId(request, false);
    String spaceId = getSpaceId(request);
    UserDetail user = getCurrentUser();
    String userId = user.getId();
    String language = user.getUserPreferences().getLanguage();

    DesktopMessages.init(language);
    response.setContentType("text/html;charset=UTF-8");
    HttpSession session = AdminUtils.getClearedSession(request);
    PortletAdminData portletAdminData;
    String portletsRenderer = "/portlet/jsp/jsr/admin.jsp";
    try {
      portletAdminData = PortletAdminDataFactory.getPortletAdminData(elementId);
    } catch (PortletRegistryException pre) {
      throw new IOException(pre.getMessage());
    }
    AdminUtils.setAttributes(session, portletAdminData, elementId, userId, spaceId, language);

    if (isParameterPresent(request, AdminConstants.CREATE_PORTLET_WINDOW_SUBMIT)) {
      createPortletWindow(request, portletAdminData, session);
      if (isDefined(spaceId)) {
        portletsRenderer = "/dt?"+WindowInvokerConstants.DRIVER_SPACEID+"="+spaceId+"&"+WindowInvokerConstants.DRIVER_ROLE+"=Admin";
      } else {
        portletsRenderer = "/dt";
      }
    } else if (isParameterPresent(request, AdminConstants.MODIFY_PORTLET_WINDOW_SUBMIT)) {
      updatePortletWindow(request, portletAdminData, session);
    } else if (isParameterPresent(request, AdminConstants.MOVE_PORTLET_WINDOW)) {
      movePortletWindow(request, portletAdminData, session);
      portletsRenderer = getPresentationURI(request);
    } else if (isParameterPresent(request, AdminConstants.PORTLET_WINDOW_LIST)) {
      selectPortletWindow(request, portletAdminData, session);
    } else {
      try {
        AdminUtils.setPortletWindowAttributes(session, portletAdminData, null);
      } catch (Exception ex) {
        StringBuilder messageBuilder =
            new StringBuilder(DesktopMessages.getLocalizedString(AdminConstants.NO_WINDOW_DATA));
        messageBuilder.append(".");
        messageBuilder.append(ex.getMessage());
        session.setAttribute(AdminConstants.NO_WINDOW_DATA_ATTRIBUTE, messageBuilder.toString());
      }
    }

    RequestDispatcher reqd = context.getRequestDispatcher(portletsRenderer);
    reqd.forward(request, response);
  }

  private boolean isParameterPresent(HttpServletRequest request,
      String parameter) {
    String name = request.getParameter(parameter);
    return (name != null);
  }

  private void setSelectedPortletWindow(HttpSession session,
      String portletWindowName) {
    session.removeAttribute(AdminConstants.SELECTED_PORTLET_WINDOW_ATTRIBUTE);
    session.setAttribute(AdminConstants.SELECTED_PORTLET_WINDOW_ATTRIBUTE,
        portletWindowName);
  }

  protected String getPresentationURI(HttpServletRequest request) {
    String spaceId = getSpaceId(request);

    if (!isDefined(spaceId) || isSpaceBackOffice(request)) {
      return "/portlet/jsp/jsr/desktop.jsp";
    } else {
      request.setAttribute("DisableMove", Boolean.TRUE);
      return "/portlet/jsp/jsr/spaceDesktop.jsp";
    }
  }

  private String getUserIdOrSpaceId(HttpServletRequest request, boolean getSpaceIdOnly) {
    String spaceId = getSpaceId(request);

    if (getSpaceIdOnly) {
      return spaceId;
    }

    if (!isDefined(spaceId)) {
      return getCurrentUser().getId();
    }
    return spaceId;
  }

  private UserDetail getCurrentUser() {
    return UserDetail.getCurrentRequester();

  }

  private String getSpaceId(HttpServletRequest request) {
    String spaceId = request.getParameter("SpaceId");
    if (!isDefined(spaceId)) {
      spaceId = request.getParameter(WindowInvokerConstants.DRIVER_SPACEID);
    }

    if (isDefined(spaceId)) {
      // Display the space homepage
      if (!spaceId.startsWith("space")) {
        spaceId = "space" + spaceId;
      }

      // If the home page of the space is the standard one, then spaceId is unset
      SpaceInst spaceStruct =
          OrganizationControllerProvider.getOrganisationController()
              .getSpaceInstById(spaceId.replace("space", ""));
      if (spaceStruct == null || spaceStruct.getFirstPageType() == SpaceInst.FP_TYPE_STANDARD) {
        spaceId = null;
      }
    }
    return spaceId;
  }

  private boolean isSpaceBackOffice(HttpServletRequest request) {
    return (isDefined(getSpaceId(request)) && "admin".equalsIgnoreCase(request
        .getParameter(WindowInvokerConstants.DRIVER_ROLE)));
  }

  private void createPortletWindow(HttpServletRequest request, PortletAdminData portletAdminData,
      HttpSession session) {
    String portletWindowName = String.valueOf(new Date().getTime());

    String portletName = request.getParameter(AdminConstants.PORTLET_LIST);
    String title = request.getParameter(AdminConstants.PORTLET_WINDOW_TITLE);
    if (portletName == null) {
      String message = DesktopMessages.getLocalizedString(AdminConstants.NO_BASE_PORTLET);
      session.setAttribute(AdminConstants.CREATION_FAILED_ATTRIBUTE, message);
    } else {
      boolean isValid = isValid(portletWindowName);
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
          isValid = isValid(title);
        }
        StringBuilder messageBuilder =
            new StringBuilder(DesktopMessages.getLocalizedString(AdminConstants.CREATION_FAILED));
        if (isValid) {
          boolean success = false;
          try {
            success = portletAdminData.createPortletWindow(portletName, portletWindowName, title);
          } catch (Exception ex) {
            messageBuilder.append(".");
            messageBuilder.append(ex.getMessage());
          }
          if (success) {
            UserDetail user = getCurrentUser();
            String elementId = getUserIdOrSpaceId(request, false);
            String spaceId = getSpaceId(request);
            String userId = user.getId();
            String language = user.getUserPreferences().getLanguage();
            String message =
                DesktopMessages.getLocalizedString(AdminConstants.CREATION_SUCCEEDED);
            session.setAttribute(AdminConstants.CREATION_SUCCEEDED_ATTRIBUTE, message);
            AdminUtils.refreshList(request, elementId, userId, spaceId, language);
          } else {
            session.setAttribute(AdminConstants.CREATION_FAILED_ATTRIBUTE, messageBuilder
                .toString());
          }
        } else {
          String message = DesktopMessages.getLocalizedString(AdminConstants.INVALID_CHARACTERS);
          session.setAttribute(AdminConstants.CREATION_FAILED_ATTRIBUTE, message);
        }
      }
    }
  }

  private void updatePortletWindow(HttpServletRequest request, PortletAdminData portletAdminData,
      HttpSession session) {
    String portletWindowName = request.getParameter(AdminConstants.PORTLET_WINDOW_LIST);
    setSelectedPortletWindow(session, portletWindowName);
    String width = request.getParameter(AdminConstants.WIDTH_LIST);
    String visibleValue = request.getParameter(AdminConstants.VISIBLE_LIST);
    boolean visible = PortletRegistryConstants.VISIBLE_TRUE.equals(visibleValue);
    if (portletWindowName == null) {
      String message = DesktopMessages.getLocalizedString(AdminConstants.NO_BASE_PORTLET_WINDOW);
      session.setAttribute(AdminConstants.MODIFY_FAILED_ATTRIBUTE, message);
    } else {
      StringBuilder messageBuilder =
          new StringBuilder(DesktopMessages.getLocalizedString(AdminConstants.MODIFY_FAILED));
      boolean success = false;
      try {
        success = portletAdminData.modifyPortletWindow(portletWindowName, width, visible, null);
        AdminUtils.setPortletWindowAttributes(session, portletAdminData, portletWindowName);
      } catch (Exception ex) {
        messageBuilder.append(".");
        messageBuilder.append(ex.getMessage());
      }
      if (success) {
        String message = DesktopMessages.getLocalizedString(AdminConstants.MODIFY_SUCCEEDED);
        session.setAttribute(AdminConstants.MODIFY_SUCCEEDED_ATTRIBUTE, message);
      } else {
        session.setAttribute(AdminConstants.MODIFY_FAILED_ATTRIBUTE, messageBuilder.toString());
      }
    }
  }

  private void movePortletWindow(HttpServletRequest request, PortletAdminData portletAdminData,
      HttpSession session) {
    // setSelectedPortletWindow(session, portletWindowName);
    String column1 = request.getParameter("column1");
    String column2 = request.getParameter("column2");

    List<PortletWindowData> windows = new ArrayList<>();
    List<String> portletWindowNames = portletAdminData.getPortletWindowNames();

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
      for (String portletWindowName : portletWindowNames) {
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

      for (String portletWindowName : portletWindowNames) {
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
      SilverTrace.error("portlet", "AdminServlet.movePortletWindow()", "root.EX_NO_MESSAGE", e);
    }
  }

  private void selectPortletWindow(HttpServletRequest request, PortletAdminData portletAdminData,
      HttpSession session) {
    String portletWindowName = request.getParameter(AdminConstants.PORTLET_WINDOW_LIST);
    setSelectedPortletWindow(session, portletWindowName);
    if (portletWindowName == null) {
      String message = DesktopMessages.getLocalizedString(AdminConstants.NO_BASE_PORTLET_WINDOW);
      session.setAttribute(AdminConstants.NO_WINDOW_DATA_ATTRIBUTE, message);
    } else {
      StringBuilder messageBuilder =
          new StringBuilder(DesktopMessages.getLocalizedString(AdminConstants.NO_WINDOW_DATA));
      // Set the attribues for show/hide and thick/thin
      boolean success = false;
      try {
        AdminUtils.setPortletWindowAttributes(session, portletAdminData, portletWindowName);
        success = true;
      } catch (Exception ex) {
        messageBuilder.append(".");
        messageBuilder.append(ex.getMessage());
      }
      if (!success) {
        session.setAttribute(AdminConstants.NO_WINDOW_DATA_ATTRIBUTE, messageBuilder.toString());
      }
    }
  }

  private boolean isValid(String term) {
    return term != null && !term.trim().isEmpty();
  }
}
