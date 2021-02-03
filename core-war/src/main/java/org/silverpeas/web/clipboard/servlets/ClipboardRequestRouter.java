/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.clipboard.servlets;

import org.silverpeas.core.clipboard.ClipboardException;
import org.silverpeas.core.security.session.SessionInfo;
import org.silverpeas.core.security.session.SessionManagement;
import org.silverpeas.core.security.session.SessionManagementProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.silverpeas.web.clipboard.control.ClipboardSessionController;

import javax.servlet.http.HttpSession;

/**
 * Clipboard request router.
 */
public class ClipboardRequestRouter extends ComponentRequestRouter<ClipboardSessionController> {
  private static final long serialVersionUID = 1L;

  private static final String SPACE_FROM_PARAM = "SpaceFrom";
  private static final String COMPONENT_FROM_PARAM = "ComponentFrom";
  private static final String CLIPBOARD_JSP = "/clipboard/jsp/clipboard.jsp";
  private static final String SUFFIX_OF_PASTE_JSP = "paste.jsp";

  /**
   * Called in order to get an instance of clipboard session controller.
   */
  @Override
  public ClipboardSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new ClipboardSessionController(mainSessionCtrl, componentContext);
  }

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   * @return the name of the controller.
   */
  @Override
  public String getSessionControlBeanName() {
    return "clipboardScc";
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   *
   * @param function The entering request function (ex : "Main.jsp")
   * @param clipboardSC The component Session Control, build and initialised.
   * @param request the current request.
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  @Override
  public String getDestination(String function, ClipboardSessionController clipboardSC,
      HttpRequest request) {
    String destination;
    if (function.startsWith("copyForm")) {
      destination = "/clipboard/jsp/copyForm.jsp";
    } else if (function.startsWith("paste")) {
      destination = performPaste(request, clipboardSC);
    } else if (function.startsWith("clipboardRefresh")) {
      destination = CLIPBOARD_JSP;
    } else if (function.startsWith("clipboard")) {
      destination = performClipboard(request, clipboardSC);
    } else if (function.startsWith("delete")) {
      destination = performDelete(request, clipboardSC);
    } else if (function.startsWith("selectObject")) {
      destination = performSelectObject(request, clipboardSC);
    } else if (function.startsWith("selectionpaste")) {
      destination = performSelectionPaste(request, clipboardSC);
    } else {
      destination = "/clipboard/jsp/" + function;
    }

    return destination;
  }

  @Override
  public void updateSessionManagement(HttpSession session, String destination) {
    SessionManagement sessionManagement = SessionManagementProvider.getSessionManagement();
    SessionInfo sessionInfo = sessionManagement.getSessionInfo(session.getId());
    if (sessionInfo.isDefined()) {
      if (destination.startsWith("/clipboard/jsp/Idle")) {
        // Set the idle time
        sessionInfo.setAsIdle();
      } else {
        // Set the last accessed time
        sessionInfo.updateLastAccess();
      }
    }
  }

  private String performClipboard(final HttpRequest request,
      final ClipboardSessionController clipboardSC) {
    final String destination;
    clipboardSC.setComponentRooterName(request.getParameter("compR"));
    clipboardSC.setSpaceId(request.getParameter(SPACE_FROM_PARAM));
    clipboardSC.setComponentId(request.getParameter(COMPONENT_FROM_PARAM));
    clipboardSC.setJSPPage(request.getParameter("JSPPage"));
    clipboardSC.setTargetFrame(request.getParameter("TargetFrame"));
    destination = CLIPBOARD_JSP;
    return destination;
  }

  private String performDelete(final HttpRequest request,
      final ClipboardSessionController clipboardSC) {
    final String destination;
    try {
      int max = clipboardSC.getClipboardSize();
      int removed = 0;
      for (int i = 0; i < max; i++) {
        String removedValue = request.getParameter("clipboardId" + i);
        if (removedValue != null) {

          clipboardSC.removeClipboardElement(i - removed);
          removed++;
        }
      }
    } catch (ClipboardException e) {
      SilverLogger.getLogger(this).error(e);
    }
    destination = CLIPBOARD_JSP;
    return destination;
  }

  private String performSelectObject(final HttpRequest request,
      final ClipboardSessionController clipboardSC) {
    final String destination;
    try {
      String objectIndex = request.getParameter("Id");
      String objectStatus = request.getParameter("Status");
      if ((objectIndex != null) && (objectStatus != null)) {

        clipboardSC.setClipboardSelectedElement(Integer.parseInt(objectIndex),
            Boolean.parseBoolean(objectStatus));
      }
    } catch (Exception e) {
      SilverLogger.getLogger(this).error(e);
    }
    destination = "/clipboard/jsp/Idle.jsp";
    return destination;
  }

  private String performSelectionPaste(final HttpRequest request,
      final ClipboardSessionController clipboardSC) {
    final String destination;
    try {
      int max = clipboardSC.getClipboardSize();
      for (int i = 0; i < max; i++) {
        String removedValue = request.getParameter("clipboardId" + i);
        clipboardSC.setClipboardSelectedElement(i, removedValue != null);
      }
    } catch (ClipboardException e) {
      SilverLogger.getLogger(this).error(e);
    }
    String componentName = clipboardSC.getComponentRooterName();
    if (componentName != null) {
      destination = URLUtil.getURL(null, request.getParameter(SPACE_FROM_PARAM),
          request.getParameter(COMPONENT_FROM_PARAM)) + SUFFIX_OF_PASTE_JSP;
    } else {
      destination = CLIPBOARD_JSP;
    }
    return destination;
  }

  private String performPaste(final HttpRequest request,
      final ClipboardSessionController clipboardSC) {
    final String destination;
    clipboardSC.setComponentRooterName(request.getParameter("compR"));
    clipboardSC.setSpaceId(request.getParameter(SPACE_FROM_PARAM));
    clipboardSC.setComponentId(request.getParameter(COMPONENT_FROM_PARAM));
    clipboardSC.setJSPPage(request.getParameter("JSPPage"));
    clipboardSC.setTargetFrame(request.getParameter("TargetFrame"));
    if (StringUtil.isDefined(clipboardSC.getComponentRooterName())) {
      if (StringUtil.isDefined(clipboardSC.getComponentId())) {
        destination = URLUtil.getURL(null, request.getParameter(SPACE_FROM_PARAM),
            clipboardSC.getComponentId()) + SUFFIX_OF_PASTE_JSP;
      } else {
        destination =
            URLUtil.getURL(URLUtil.CMP_JOBSTARTPAGEPEAS, null, null) + SUFFIX_OF_PASTE_JSP;
      }
    } else {
      destination = CLIPBOARD_JSP;
    }
    return destination;
  }
}
