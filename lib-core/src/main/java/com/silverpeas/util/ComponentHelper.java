/**
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
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.silverpeas.util;

import com.silverpeas.admin.components.Instanciateur;
import com.silverpeas.admin.components.WAComponent;
import java.util.Set;

/**
 * @author ehugonnet
 */
public class ComponentHelper {

  private static final ComponentHelper instance = new ComponentHelper();

  private ComponentHelper() {
  }

  public synchronized static ComponentHelper getInstance() {
    return instance;
  }

  public Set<String> listComponentTypes() {
    return Instanciateur.getWAComponents().keySet();
  }

  public String extractComponentName(String componentId) {
    if (!StringUtil.isDefined(componentId)) {
      return componentId;
    }
    StringBuilder componentName = new StringBuilder(componentId.length());
    for (int i = 0; i < componentId.length(); i++) {
      char c = componentId.charAt(i);
      if (Character.isDigit(c)) {
        return componentName.toString();
      }
      componentName.append(c);
    }
    return componentName.toString().toLowerCase();
  }

  public WAComponent extractComponent(String componentId) {
    if (!StringUtil.isDefined(componentId)) {
      return null;
    }
    String componentName = extractComponentName(componentId);
    return Instanciateur.getWAComponent(componentName);
  }

  public boolean isThemeTracker(WAComponent component) {
    return isKmelia(component) || isKmax(component) || isToolbox(component);
  }

  public boolean isThemeTracker(String componentId) {
    return isThemeTracker(extractComponent(componentId));
  }

  public boolean isKmelia(WAComponent component) {
    if (component == null) {
      throw new IllegalArgumentException("The component must be not null");
    }
    return "kmelia".equalsIgnoreCase(component.getName());
  }

  public boolean isKmelia(String componentId) {
    return isKmelia(extractComponent(componentId));
  }

  public boolean isKmax(WAComponent component) {
    if (component == null) {
      throw new IllegalArgumentException("The component must be not null");
    }
    return "kmax".equalsIgnoreCase(component.getName());
  }

  public boolean isKmax(String componentId) {
    return isKmax(extractComponent(componentId));
  }

  public boolean isToolbox(WAComponent component) {
    if (component == null) {
      throw new IllegalArgumentException("The component must be not null");
    }
    return "toolbox".equalsIgnoreCase(component.getName());
  }

  public boolean isToolbox(String componentId) {
    return isToolbox(extractComponent(componentId));
  }
}
