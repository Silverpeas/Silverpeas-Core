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
 * FLOSS exception. You should have received a copy of the text describing
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

package org.silverpeas.core.admin.component.model;

import org.silverpeas.core.ui.DisplayI18NHelper;

import java.util.HashMap;
import java.util.List;

/**
 * @author Yohann Chastagnier
 */
public interface SilverpeasComponent {

  /**
   * Gets the value of the name property.
   * @return possible object is {@link String }
   */
  String getName();

  /**
   * Gets the value of the label property.
   * @return possible object is {@link Multilang }
   */
  HashMap<String, String> getLabel();

  /**
   * Gets the value of the label property according to a given language.
   * @param lang the language into which the label must be translated.
   * @return possible object is {@link Multilang }
   */
  default String getLabel(String lang) {
    if (getLabel().containsKey(lang)) {
      return getLabel().get(lang);
    }
    return getLabel().get(DisplayI18NHelper.getDefaultLanguage());
  }

  /**
   * Gets the value of the description property.
   * @return possible object is {@link Multilang }
   */
  HashMap<String, String> getDescription();

  /**
   * Gets the value of the description property according to a given language.
   * @param lang the language into which the description must be translated.
   * @return possible object is {@link Multilang }
   */
  default String getDescription(String lang) {
    if (getDescription().containsKey(lang)) {
      return getDescription().get(lang);
    }
    return getDescription().get(DisplayI18NHelper.getDefaultLanguage());
  }

  /**
   * Gets the value of the visible property.
   */
  boolean isVisible();

  /**
   * Gets the value of the parameters property.
   * @return list of {@link Parameter}
   */
  List<Parameter> getParameters();

  /**
   * Indicates if a parameter is defined which name is equal to the given method parameter.
   * @param parameterName the parameter name to perform.
   * @return true if a parameter is defined behind the specified method parameter, false otherwise.
   */
  boolean hasParameterDefined(String parameterName);

  /**
   * Gets same parameters as {@link #getParameters()}, sorted by order and name.
   * @return sorted parameters.
   */
  List<Parameter> getSortedParameters();

  /**
   * Gets same parameters as {@link #getParameters()} but filled into structured object {@link
   * ParameterList}.
   * @return parameters contained into {@link ParameterList} instance.
   */
  default ParameterList getAllParameters() {
    ParameterList result = new ParameterList();
    result.addAll(getParameters());
    for (GroupOfParameters group : getGroupsOfParameters()) {
      result.addAll(group.getParameters());
    }
    return result;
  }

  /**
   * Gets groups of parameters.
   * @return the list of groups of parameters.
   */
  List<GroupOfParameters> getGroupsOfParameters();
}
