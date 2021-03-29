/*
 * Copyright (C) 2000 - 2021 Silverpeas
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
package org.silverpeas.core.admin.component.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ParameterList extends ArrayList<Parameter> {

  public static ParameterList copy(List<Parameter> parameters) {
    return new ParameterList(parameters.stream().map(Parameter::new).collect(Collectors.toList()));
  }

  public ParameterList() {
    super();
  }

  public ParameterList(List<Parameter> parameters) {
    super(parameters);
  }

  public void setValues(List<Parameter> parameters) {
    for (Parameter parameterToMerge : parameters) {
      Parameter parameter = getParameterByName(parameterToMerge.getName());
      if (parameter == null) {
        // Le parametre existe en base mais plus dans le xmlComponent

      } else {
        parameter.setValue(parameterToMerge.getValue());
      }
    }
  }

  public List<Parameter> getVisibleParameters() {
    List<Parameter> parameters = new ArrayList<>();
    for (Parameter parameter : this) {
      if (parameter.isVisible()) {
        parameters.add(parameter);
      }
    }
    return parameters;
  }

  public List<Parameter> getHiddenParameters() {
    List<Parameter> parameters = new ArrayList<>();
    for (Parameter parameter : this) {
      if (parameter.isHidden()) {
        parameters.add(parameter);
      }
    }
    return parameters;
  }

  public void sort() {
    this.sort(new ParameterSorter());
  }

  private Parameter getParameterByName(String name) {
    for (final Parameter param : this) {
      if (param.getName().equals(name)) {
        return param;
      }
    }
    return null;
  }

  public boolean isVisible() {
    for (final Parameter param : this) {
      if (param.isVisible()) {
        return true;
      }
    }
    return false;
  }

  public LocalizedParameterList localize(String lang) {
    LocalizedParameterList localized = new LocalizedParameterList();
    for (Parameter param : this) {
      localized.add(new LocalizedParameter(param, lang));
    }
    return localized;
  }

  public ParameterList copy() {
    ParameterList copy = new ParameterList();
    this.stream().map(Parameter::new).forEach(copy::add);
    return copy;
  }
}
