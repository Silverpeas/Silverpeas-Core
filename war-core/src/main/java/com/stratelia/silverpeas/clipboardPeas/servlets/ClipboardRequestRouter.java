/**
 * Copyright (C) 2000 - 2012 Silverpeas
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
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.stratelia.silverpeas.clipboardPeas.servlets;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.clipboardPeas.control.ClipboardSessionController;
import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.SessionManager;
import com.stratelia.silverpeas.peasCore.URLManager;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.silvertrace.SilverTrace;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Class declaration
 * @author
 */
public class ClipboardRequestRouter extends ComponentRequestRouter<ClipboardSessionController> {
  private static final long serialVersionUID = 1L;

  /**
   * Method declaration
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
   * @return
   */
  @Override
  public String getSessionControlBeanName() {
    return "clipboardScc";
  }

  /**
   * This method has to be implemented by the component request rooter it has to compute a
   * destination page
   * @param function The entering request function (ex : "Main.jsp")
   * @param clipboardSC The component Session Control, build and initialised.
   * @return The complete destination URL for a forward (ex :
   * "/almanach/jsp/almanach.jsp?flag=user")
   */
  @Override
  public String getDestination(String function, ClipboardSessionController clipboardSC,
      HttpServletRequest request) {
    SilverTrace.info("clipboardPeas",
        "ClipboardRequestRooter.getDestination()", "root.MSG_GEN_ENTER_METHOD",
        " componentName = " + clipboardSC.getClass().getName() + "; function = " + function);
    String destination = "";
    if (function.startsWith("copyForm")) {
      destination = "/clipboard/jsp/copyForm.jsp";
    } else if (function.startsWith("paste")) {
      clipboardSC.setComponentRooterName(request.getParameter("compR"));
      clipboardSC.setSpaceId(request.getParameter("SpaceFrom"));
      clipboardSC.setComponentId(request.getParameter("ComponentFrom"));
      clipboardSC.setJSPPage(request.getParameter("JSPPage"));
      clipboardSC.setTargetFrame(request.getParameter("TargetFrame"));
      if (StringUtil.isDefined(clipboardSC.getComponentRooterName())) {
        SilverTrace.info("clipboardPeas", "ClipboardRequestRooter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", "compR = " + clipboardSC.getComponentRooterName());
        if (StringUtil.isDefined(clipboardSC.getComponentId())) {
          destination = URLManager.getURL(null, request.getParameter("SpaceFrom"),
              clipboardSC.getComponentId()) + "paste.jsp";
        } else {
          destination = URLManager.getURL(URLManager.CMP_JOBSTARTPAGEPEAS) + "paste.jsp";
        }
      } else {
        SilverTrace.info("clipboardPeas", "ClipboardRequestRooter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", "compR is null");
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
            SilverTrace.info("clipboardPeas", "ClipboardRequestRooter.getDestination()",
                "root.MSG_GEN_PARAM_VALUE", "clipboardId" + i + " = " + removedValue);
            clipboardSC.removeClipboardElement(i - removed);
            removed++;
          }
        }
      } catch (Exception e) {
        SilverTrace.error("clipboardPeas", "ClipboardRequestRooter.getDestination()",
            "clipboardPeas.EX_CANT_WRITE", "delete.jsp", e);
      }
      destination = "/clipboard/jsp/clipboard.jsp";
    } else if (function.startsWith("selectObject")) {
      try {
        String objectIndex = request.getParameter("Id");
        String objectStatus = request.getParameter("Status");
        if ((objectIndex != null) && (objectStatus != null)) {
          SilverTrace.info("clipboardPeas", "ClipboardRequestRooter.getDestination()",
              "root.MSG_GEN_PARAM_VALUE", "selectObject " + objectIndex + " -> " + objectStatus);
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
      } catch (Exception e) {
        SilverTrace.error("clipboardPeas", "ClipboardRequestRooter.getDestination()",
            "clipboardPeas.EX_CANT_WRITE", "selectionpaste.jsp", e);
      }
      String componentName = clipboardSC.getComponentRooterName();
      if (componentName != null) {
        SilverTrace.info("clipboardPeas", "ClipboardRequestRooter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", "compR = " + componentName);
        destination = URLManager.getURL(null, request.getParameter("SpaceFrom"),
            request.getParameter("ComponentFrom")) + "paste.jsp";
      } else {
        SilverTrace.info("clipboardPeas", "ClipboardRequestRooter.getDestination()",
            "root.MSG_GEN_PARAM_VALUE", "compR is null");
        destination = "/clipboard/jsp/clipboard.jsp";
      }
    } else {
      destination = "/clipboard/jsp/" + function;
    }
    SilverTrace.info("clipboardPeas", "ClipboardRequestRooter.getDestination()",
        "root.MSG_GEN_EXIT_METHOD", "Destination=" + destination);
    return destination;
  }

  @Override
  public void updateSessionManagement(HttpSession session, String destination) {
    SilverTrace.info("clipboardPeas", "ClipboardRequestRouter.updateSessionManagement",
        "root.MSG_GEN_PARAM_VALUE", "dest=" + destination);
    if (destination.startsWith("/clipboard/jsp/Idle")) {
      // Set the isalived time
      SessionManager.getInstance().setIsAlived(session);
    } else {
      // Set the last accessed time
      SessionManager.getInstance().setLastAccess(session);
    }
  }
}
