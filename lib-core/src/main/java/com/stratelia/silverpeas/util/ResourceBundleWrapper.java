/**
 * Copyright (C) 2000 - 2009 Silverpeas
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
 * "http://repository.silverpeas.com/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.stratelia.silverpeas.util;

import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import com.stratelia.webactiv.util.GeneralPropertiesManager;

public class ResourceBundleWrapper extends ResourceBundle {
  private ResourceBundle bundle;
  private ResourceBundle parentBundle;

  public ResourceBundleWrapper(String file, Locale locale, boolean hasParent) {
    this.bundle = java.util.ResourceBundle.getBundle(file, locale);
    if (hasParent) {
      this.parentBundle = GeneralPropertiesManager.getGeneralMultilang(
          locale.getLanguage()).getResourceBundle();
    }
  }

  public ResourceBundleWrapper(String file, Locale locale) {
    this(file, locale, !GeneralPropertiesManager.GENERAL_PROPERTIES_FILE
        .equalsIgnoreCase(file));
  }

  public Enumeration<String> getKeys() {
    return this.bundle.getKeys();
  }

  protected Object handleGetObject(String key) {
    Object result = null;
    try{
      result = this.bundle.getObject(key);
    }catch (MissingResourceException mrex){

    }
    if (result == null && this.parentBundle != null) {
      try{
      result = this.parentBundle.getObject(key);
      }catch (MissingResourceException mrex){
      }

    }
    return result;
  }
}
