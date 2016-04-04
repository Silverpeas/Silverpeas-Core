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

package org.silverpeas.web.selection.servlets;

import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.silverpeas.web.selection.control.SelectionPeasWrapperSessionController;
import org.silverpeas.core.web.http.HttpRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple wrapper for the userpanel.
 */
public class SelectionPeasWrapper extends
    ComponentRequestRouter<SelectionPeasWrapperSessionController> {

  private static final long serialVersionUID = 1L;

  /**
   * Returns a new session controller
   */
  public SelectionPeasWrapperSessionController createComponentSessionController(
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
  public String getDestination(String function, SelectionPeasWrapperSessionController session,
      HttpRequest request) {
    try {
      if (function.equals("open")) {
        session.setFormName(request.getParameter("formName"));
        session.setElementId(request.getParameter("elementId"));
        session.setElementName(request.getParameter("elementName"));
        boolean selectionMultiple =
            StringUtil.getBooleanValue(request.getParameter("selectionMultiple"));
        String instanceId = request.getParameter("instanceId");
        List<String> roles = getRoles(request.getParameter("roles"));

        session.setSelectable(request.getParameter("selectable"));
        session.setDomainIdFilter(request.getParameter("domainIdFilter"));

        if (session.isGroupSelectable()) {
          if (selectionMultiple) {
            session.setSelectedGroupIds(request.getParameter("selectedGroups"));
          } else {
            session.setSelectedGroupId(request.getParameter("selectedGroup"));
          }
        } else {
          if (selectionMultiple) {
            session.setSelectedUserIds(request.getParameter("selectedUsers"));
          } else {
            session.setSelectedUserId(request.getParameter("selectedUser"));
          }
        }
        return session.initSelectionPeas(selectionMultiple, instanceId, roles);
      } else if (function.equals("close")) {
        session.getSelectionPeasSelection();
        request.setAttribute("formName", session.getFormName());
        request.setAttribute("elementId", session.getElementId());
        request.setAttribute("elementName", session.getElementName());

        if (session.isGroupSelectable()) {
          if (session.getSelection().isMultiSelect()) {
            request.setAttribute("groups", session.getSelectedGroups());
            return "/selectionPeas/jsp/closeWrapperMultiple.jsp";
          } else {
            request.setAttribute("group", session.getSelectedGroup());
            return "/selectionPeas/jsp/closeWrapper.jsp";
          }
        } else {
          if (session.getSelection().isMultiSelect()) {
            request.setAttribute("users", session.getSelectedUsers());
            return "/selectionPeas/jsp/closeWrapperMultiple.jsp";
          } else {
            request.setAttribute("user", session.getSelectedUser());
            return "/selectionPeas/jsp/closeWrapper.jsp";
          }
        }

      } else {
        return "/admin/jsp/errorpageMain.jsp";
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      return "/admin/jsp/errorpageMain.jsp";
    }
  }

  private List<String> getRoles(String param) {
    if (StringUtil.isDefined(param)) {
      return Arrays.asList(StringUtil.split(param, ","));
    }
    return new ArrayList<String>();
  }

}