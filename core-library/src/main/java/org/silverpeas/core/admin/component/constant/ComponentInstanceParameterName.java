/*
 * Copyright (C) 2000 - 2012 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.core.admin.component.constant;

import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;

/**
 * Centralization of common parameter names of component instances.
 * User: Yohann Chastagnier
 * Date: 13/12/12
 */
public enum ComponentInstanceParameterName {
  authorizedFileExtension("file.authorized.default"),
  forbiddenFileExtension("file.forbidden.default");

  private final static SettingBundle settings = ResourceLocator.getSettingBundle(
      "org.silverpeas.jobStartPagePeas.settings.jobStartPagePeasSettings");

  private final String defaultValueKey;

  private ComponentInstanceParameterName(String defaultValueKey) {
    this.defaultValueKey = defaultValueKey;
  }

  /**
   * Gets the default value.
   * @return
   */
  public String getDefaultValue() {
    if (StringUtil.isDefined(defaultValueKey)) {
      return settings.getString(defaultValueKey, "");
    }
    return "";
  }

  /**
   * Gets the emnum instance (case insensitive)
   * @param componentParameterName
   * @return
   */
  public static ComponentInstanceParameterName from(final String componentParameterName) {
    for (ComponentInstanceParameterName current : values()) {
      if (current.name().equalsIgnoreCase(componentParameterName)) {
        return current;
      }
    }
    return null;
  }
}