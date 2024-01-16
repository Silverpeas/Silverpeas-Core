/*
 * Copyright (C) 2000 - 2024 Silverpeas
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
package org.silverpeas.web.variables;

import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.util.SilverpeasList;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.web.http.HttpRequest;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;

public class VariablesRequestRouter extends ComponentRequestRouter<VariablesSessionController> {

  private static final String SESSION_BEAN_NAME = "Variables";

  @Override
  public String getSessionControlBeanName() {
    return SESSION_BEAN_NAME;
  }

  @Override
  public String getDestination(final String action, final VariablesSessionController sc,
      final HttpRequest request) {

    if (!User.getCurrentRequester().isAccessAdmin()) {
      throwHttpForbiddenError();
    }

    String destination = "/variables/jsp/variables.jsp";

    if ("Main".equals(action)) {
      request.mergeSelectedItemsInto(sc.getSelectedVariableIds());

      request.setAttribute("AllVariables", SilverpeasList.wrap(sc.getAllVariables()));
      request.setAttribute("SelectedIds", sc.getSelectedVariableIds());
    } else if ("EditVariable".equals(action)) {
      String id = request.getParameter("Id");
      if (StringUtil.isNotDefined(id)) {
        id = sc.getCurrentVariable().getId();
      }
      request.setAttribute("Variable", sc.getVariable(id));

      // clear previous selected items
      sc.getSelectedVariableIds().clear();

      destination = "/variables/jsp/variable.jsp";
    } else if ("DeleteSelectedVariables".equals(action)) {
      request.mergeSelectedItemsInto(sc.getSelectedVariableIds());
      if (!sc.getSelectedVariableIds().isEmpty()) {
        sc.deleteSelectedVariables();
      }
      destination = getDestination("Main", sc, request);
    }

    return destination;
  }

  @Override
  public VariablesSessionController createComponentSessionController(
      final MainSessionController mainSessionCtrl, final ComponentContext componentContext) {
    return new VariablesSessionController(mainSessionCtrl, componentContext);
  }
}