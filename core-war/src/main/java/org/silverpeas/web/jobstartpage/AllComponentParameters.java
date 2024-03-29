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
package org.silverpeas.web.jobstartpage;

import org.silverpeas.core.admin.component.model.LocalizedGroupOfParameters;
import org.silverpeas.core.admin.component.model.LocalizedParameter;
import org.silverpeas.core.admin.component.model.LocalizedParameterList;
import org.silverpeas.core.admin.component.model.ParameterList;
import org.silverpeas.core.admin.component.model.SilverpeasComponent;

import java.util.List;

/**
 * @author Nicolas Eysseric
 */
public class AllComponentParameters {

  private final SilverpeasComponent component;
  private LocalizedParameterList parameters;
  private List<LocalizedGroupOfParameters> groupsOfParameters;

  public AllComponentParameters(SilverpeasComponent component, LocalizedParameterList parameters,
      List<LocalizedGroupOfParameters> groups) {
    this.component = component;
    this.parameters = parameters;
    this.groupsOfParameters = groups;
  }

  public LocalizedParameterList getUngroupedParameters() {
    return parameters;
  }

  public List<LocalizedGroupOfParameters> getGroupsOfParameters() {
    return groupsOfParameters;
  }

  public LocalizedParameterList getParameters() {
    final LocalizedParameterList allParameters = new LocalizedParameterList(component,
        new ParameterList(), parameters.getLanguage());
    allParameters.addAll(parameters);
    for (LocalizedGroupOfParameters group : groupsOfParameters) {
      allParameters.addAll(group.getParameters());
    }
    return allParameters;
  }

  public boolean isVisible() {
    return getParameters().isVisible();
  }

  public List<LocalizedParameter> getHiddenParameters() {
    return getParameters().getHiddenParameters();
  }
}