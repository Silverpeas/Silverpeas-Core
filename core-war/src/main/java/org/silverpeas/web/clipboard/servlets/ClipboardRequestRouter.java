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
package org.silverpeas.web.clipboard.servlets;

import org.silverpeas.core.security.session.SessionInfo;
import org.silverpeas.core.security.session.SessionManagement;
import org.silverpeas.core.security.session.SessionManagementProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.clipboard.ClipboardException;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.web.clipboard.control.ClipboardSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.silverpeas.core.silvertrace.SilverTrace;
import javax.servlet.http.HttpSession;
import org.silverpeas.core.web.http.HttpRequest;

/**
 * Class declaration
 *
 * @author
 */
public class ClipboardRequestRouter extends ComponentRequestRouter<ClipboardSessionController> {

  private static final long serialVersionUID = 1L;

  /**
   * Method declaration
   *
   * @param mainSessionCtrl
   * @param componentContext
   * @return
   * @see
   */
  @Override
  public ClipboardSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new ClipboardSessionController(mainSessionCtrl, componentContext);
  }

  /**
   * This method has to be implemented in the component request rooter class. returns the session
   * control bean name to be put in the request object ex : for almanach, returns "almanach"
   *
   * @return
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
   * @param request
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
      clipboardSC.setComponentRooterName(request.getParameter("compR"));
      clipboardSC.setSpaceId(request.getParameter("SpaceFrom"));
      clipboardSC.setComponentId(request.getParameter("ComponentFrom"));
      clipboardSC.setJSPPage(request.getParameter("JSPPage"));
      clipboardSC.setTargetFrame(request.getParameter("TargetFrame"));
      if (StringUtil.isDefined(clipboardSC.getComponentRooterName())) {

        if (StringUtil.isDefined(clipboardSC.getComponentId())) {
          destination = URLUtil.getURL(null, request.getParameter("SpaceFrom"),
              clipboardSC.getComponentId()) + "paste.jsp";
        } else {
          destination = URLUtil.getURL(URLUtil.CMP_JOBSTARTPAGEPEAS) + "paste.jsp";
        }
      } else {

        destination = "/clipboard/jsp/clipboard.jsp";
      }
    } else if (function.startsWith("clipboardRefresh")) {
      destination = "/clipboard/jsp/clipboard.jsp";
    } else if (function.startsWith("clipboard")) {
      clipboardSC.setComponentRooterName(request.getParameter("compR"));
      clipboardSC.setSpaceId(request.getParameter("SpaceFrom"));
      clipboardSC.setComponentId(request.getParameter("ComponentFrom"));
      clipboardSC.setJSPPage(request.getParameter("JSPPage"));
      clipboardSC.setTargetFrame(request.getParameter("TargetFrame"));
      destination = "/clipboard/jsp/clipboard.jsp";
    } else if (function.startsWith("delete")) {
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
        SilverTrace.error("clipboardPeas", "ClipboardRequestRooter.getDestination()",
            "clipboardPeas.EX_CANT_WRITE", "delete.jsp", e);
      }
      destination = "/clipboard/jsp/clipboard.jsp";
    } else if (function.startsWith("selectObject")) {
      try {
        String objectIndex = request.getParameter("Id");
        String objectStatus = request.getParameter("Status");
        if ((objectIndex != null) && (objectStatus != null)) {

          clipboardSC.setClipboardSelectedElement(Integer.parseInt(objectIndex),
              Boolean.parseBoolean(objectStatus));
        }
      } catch (Exception e) {
        SilverTrace.error("clipboardPeas", "ClipboardRequestRooter.getDestination()",
            "clipboardPeas.EX_CANT_WRITE", "delete.jsp", e);
      }
      destination = "/clipboard/jsp/Idle.jsp";
    } else if (function.startsWith("selectionpaste")) {
      try {
        int max = clipboardSC.getClipboardSize();
        for (int i = 0; i < max; i++) {
          String removedValue = request.getParameter("clipboardId" + i);
          clipboardSC.setClipboardSelectedElement(i, removedValue != null);
        }
      } catch (ClipboardException e) {
        SilverTrace.error("clipboardPeas", "ClipboardRequestRooter.getDestination()",
            "clipboardPeas.EX_CANT_WRITE", "selectionpaste.jsp", e);
      }
      String componentName = clipboardSC.getComponentRooterName();
      if (componentName != null) {

        destination = URLUtil.getURL(null, request.getParameter("SpaceFrom"),
            request.getParameter("ComponentFrom")) + "paste.jsp";
      } else {

        destination = "/clipboard/jsp/clipboard.jsp";
      }
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
}
