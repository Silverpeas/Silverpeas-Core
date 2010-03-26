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
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.silverpeas.selectionPeas.servlets;

import javax.servlet.http.HttpServletRequest;

import com.stratelia.silverpeas.peasCore.ComponentContext;
import com.stratelia.silverpeas.peasCore.ComponentSessionController;
import com.stratelia.silverpeas.peasCore.MainSessionController;
import com.stratelia.silverpeas.peasCore.servlets.ComponentRequestRouter;
import com.stratelia.silverpeas.selectionPeas.control.SelectionPeasWrapperSessionController;

/**
 * A simple wrapper for the userpanel.
 */
public class SelectionPeasWrapper extends ComponentRequestRouter {

  private static final long serialVersionUID = 1L;

  /**
   * Returns a new session controller
   */
  public ComponentSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new SelectionPeasWrapperSessionController(mainSessionCtrl,
        componentContext);
  }

  /**
   * Returns the base name for the session controller of this router.
   */
  public String getSessionControlBeanName() {
    return "selectionPeasWrapper";
  }

  /**
   * Do the requested function and return the destination url.
   */
  public String getDestination(String function,
      ComponentSessionController componentSC, HttpServletRequest request) {
    SelectionPeasWrapperSessionController session =
        (SelectionPeasWrapperSessionController) componentSC;

    try {
      if (function.equals("open")) {
        session.setFormName(request.getParameter("formName"));
        session.setElementId(request.getParameter("elementId"));
        session.setElementName(request.getParameter("elementName"));
        String selectionMultiple = request.getParameter("selectionMultiple");
        String instanceId = request.getParameter("instanceId");
        if ("true".equals(selectionMultiple)) {
          session.setSelectedUserIds(request.getParameter("selectedUsers"));
          return session.initSelectionPeas(true, instanceId);
        } else {
          session.setSelectedUserId(request.getParameter("selectedUser"));
          return session.initSelectionPeas(false, null);
        }
      } else if (function.equals("close")) {
        session.getSelectionPeasSelection();
        request.setAttribute("formName", session.getFormName());
        request.setAttribute("elementId", session.getElementId());
        request.setAttribute("elementName", session.getElementName());

        if (session.getSelection().isMultiSelect()) {
          request.setAttribute("users", session.getSelectedUsers());
          return "/selectionPeas/jsp/closeWrapperMultiple.jsp";
        } else {
          request.setAttribute("user", session.getSelectedUser());
          return "/selectionPeas/jsp/closeWrapper.jsp";
        }

      } else {
        return "/admin/jsp/errorpageMain.jsp";
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      return "/admin/jsp/errorpageMain.jsp";
    }
  }

}