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
package org.silverpeas.core.admin.component.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Nicolas Eysseric
 */
public class LocalizedParameterList extends ArrayList<LocalizedParameter> {

  private String lang;

  public LocalizedParameterList() {
    super();
  }

  public LocalizedParameterList(ParameterList parameters, String lang) {
    super(parameters.localize(lang));
    this.lang = lang;
  }

  public List<LocalizedParameter> getVisibleParameters() {
    List<LocalizedParameter> parameters = new ArrayList<>();
    for (LocalizedParameter parameter : this) {
      if (parameter.isVisible()) {
        parameters.add(parameter);
      }
    }
    return parameters;
  }

  public List<LocalizedParameter> getHiddenParameters() {
    List<LocalizedParameter> parameters = new ArrayList<>();
    for (LocalizedParameter parameter : this) {
      if (parameter.isHidden()) {
        parameters.add(parameter);
      }
    }
    return parameters;
  }

  public boolean isVisible() {
    return !getVisibleParameters().isEmpty();
  }

  public String getLanguage() {
    return lang;
  }
}
