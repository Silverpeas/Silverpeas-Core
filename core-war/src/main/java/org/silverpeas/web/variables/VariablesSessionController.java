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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.web.variables;

import org.silverpeas.core.variables.Variable;
import org.silverpeas.core.web.mvc.controller.AbstractAdminComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.webapi.variables.VariablesWebManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VariablesSessionController extends AbstractAdminComponentSessionController {
  private static final long serialVersionUID = -6077929073402709139L;

  private final Set<String> selectedValueIds = new HashSet<>();
  private Variable currentVariable;

  public VariablesSessionController(MainSessionController mainSessionCtrl,
      ComponentContext context) {
    super(mainSessionCtrl, context, "org.silverpeas.variables.multilang.variables");
  }

  Variable getCurrentVariable() {
    return currentVariable;
  }

  List<Variable> getAllVariables() {
    return getManager().getAllVariables();
  }

  Variable getVariable(String id) {
    currentVariable = getManager().getVariable(id);
    return currentVariable;
  }

  void deleteSelectedVariables() {
    getManager().deleteVariables(new ArrayList<>(getSelectedVariableIds()));
    getSelectedVariableIds().clear();
  }

  private VariablesWebManager getManager() {
    return VariablesWebManager.get();
  }

  Set<String> getSelectedVariableIds() {
    return selectedValueIds;
  }
}