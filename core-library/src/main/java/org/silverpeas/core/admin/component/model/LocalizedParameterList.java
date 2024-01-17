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
package org.silverpeas.core.admin.component.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Nicolas Eysseric
 */
public class LocalizedParameterList extends ArrayList<LocalizedParameter> {
  private static final long serialVersionUID = 8621941317822315512L;

  private final String lang;

  protected LocalizedParameterList(LocalizedGroupOfParameters bundle, ParameterList parameters) {
    super(parameters.stream()
        .map(p -> new LocalizedParameter(bundle, p))
        .collect(Collectors.toList()));
    this.lang = bundle.getLanguage();
  }

  public LocalizedParameterList(SilverpeasComponent component, ParameterList parameters, String lang) {
    super(parameters.stream()
        .map(p -> new LocalizedParameter(component, p, lang))
        .collect(Collectors.toList()));
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

  @Override
  public boolean equals(final Object o) {
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
