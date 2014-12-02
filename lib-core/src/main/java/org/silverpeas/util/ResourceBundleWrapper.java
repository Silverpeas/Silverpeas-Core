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
package org.silverpeas.util;

import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResourceBundleWrapper extends ResourceBundle {

  private ResourceBundle bundle;
  private ResourceBundle parentBundle = null;

  public ResourceBundleWrapper(ResourceBundle bundle) {
    this.bundle = bundle;
  }

  public ResourceBundleWrapper(ResourceBundle bundle, ResourceBundle parentBundle) {
    this.bundle = bundle;
    this.parentBundle = parentBundle;
  }

  @Override
  public Enumeration<String> getKeys() {
    return this.bundle.getKeys();
  }

  @Override
  protected Object handleGetObject(String key) {
    Object result = null;
    try {
      result = this.bundle.getObject(key);
    } catch (MissingResourceException mrex) {
    }
    if (result == null && this.parentBundle != null) {
      try {
        result = this.parentBundle.getObject(key);
      } catch (MissingResourceException mrex) {
      }
    }
    return VariableResolver.resolve(result);
  }
}
