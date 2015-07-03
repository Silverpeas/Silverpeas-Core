/**
 * Copyright (C) 2000 - 2013 Silverpeas
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
package com.silverpeas.jobStartPagePeas;

import com.silverpeas.admin.components.LocalizedGroupOfParameters;
import com.silverpeas.admin.components.LocalizedParameter;
import com.silverpeas.admin.components.LocalizedParameterList;

import java.util.List;

/**
 * @author Nicolas Eysseric
 */
public class AllComponentParameters {

  private LocalizedParameterList parameters;
  private List<LocalizedGroupOfParameters> groupsOfParameters;

  public AllComponentParameters(LocalizedParameterList parameters, List<LocalizedGroupOfParameters> groups) {
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
    LocalizedParameterList allParameters = new LocalizedParameterList();
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