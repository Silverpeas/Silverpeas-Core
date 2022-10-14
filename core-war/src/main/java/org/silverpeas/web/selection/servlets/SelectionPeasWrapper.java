/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.selection.servlets;

import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.silverpeas.web.selection.control.SelectionPeasWrapperSessionController;

import java.util.List;

import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;

/**
 * A simple wrapper for the userpanel.
 */
public class SelectionPeasWrapper extends
    ComponentRequestRouter<SelectionPeasWrapperSessionController> {

  private static final long serialVersionUID = 1L;
  private static final String ERROR_PAGE_PATH = "/admin/jsp/errorpageMain.jsp";

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
  public String getDestination(String function, SelectionPeasWrapperSessionController controller,
      HttpRequest request) {
    try {
      String destination = null;
      if ("open".equals(function)) {
        destination = openUserGroupPanel(request, controller);
      } else if ("close".equals(function)) {
        destination = closeUserGroupPanel(request, controller);
      }
      return defaultStringIfNotDefined(destination, ERROR_PAGE_PATH);
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      return ERROR_PAGE_PATH;
    }
  }

  public String openUserGroupPanel(final HttpRequest request,
      final SelectionPeasWrapperSessionController controller) {
    controller.setFormName(request.getParameter("formName"));
    controller.setElementId(request.getParameter("elementId"));
    controller.setElementName(request.getParameter("elementName"));
    boolean selectionMultiple = request.getParameterAsBoolean("selectionMultiple");
    boolean selectedUserLimit = request.getParameterAsBoolean("selectedUserLimit");
    String instanceId = request.getParameter("instanceId");
    List<String> roles = request.getParameterAsList("roles");
    boolean includeRemovedUsers = request.getParameterAsBoolean("includeRemovedUsers");
    boolean showDeactivated = request.getParameterAsBoolean("showDeactivated");

    controller.setSelectable(request.getParameter("selectable"));
    controller.setDomainIdFilter(request.getParameter("domainIdFilter"));
    controller.setResourceIdFilter(request.getParameter("resourceIdFilter"));

    if (controller.isGroupSelectable()) {
      if (selectionMultiple) {
        controller.setSelectedGroupIds(request.getParameter("selectedGroups"));
      } else {
        controller.setSelectedGroupId(request.getParameter("selectedGroup"));
      }
    }
    if (controller.isUserSelectable()) {
      if (selectionMultiple) {
        controller.setSelectedUserIds(request.getParameter("selectedUsers"));
      } else {
        controller.setSelectedUserId(request.getParameter("selectedUser"));
      }
    }
    return controller.initSelectionPeas(selectionMultiple, instanceId, roles, includeRemovedUsers,
        showDeactivated, selectedUserLimit);
  }

  public String closeUserGroupPanel(final HttpRequest request,
      final SelectionPeasWrapperSessionController controller) {
    controller.getSelectionPeasSelection();
    request.setAttribute("formName", controller.getFormName());
    request.setAttribute("elementId", controller.getElementId());
    request.setAttribute("elementName", controller.getElementName());

    if (controller.isGroupSelectable()) {
      if (controller.getSelection().isMultiSelect()) {
        request.setAttribute("groups", controller.getSelectedGroups());
        return "/selectionPeas/jsp/closeWrapperMultiple.jsp";
      } else {
        request.setAttribute("group", controller.getSelectedGroup());
        return "/selectionPeas/jsp/closeWrapper.jsp";
      }
    }
    if (controller.isUserSelectable()) {
      if (controller.getSelection().isMultiSelect()) {
        request.setAttribute("users", controller.getSelectedUsers());
        return "/selectionPeas/jsp/closeWrapperMultiple.jsp";
      } else {
        request.setAttribute("user", controller.getSelectedUser());
        return "/selectionPeas/jsp/closeWrapper.jsp";
      }
    }
    return null;
  }
}