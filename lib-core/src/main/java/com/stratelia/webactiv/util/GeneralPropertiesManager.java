/**
 * Copyright (C) 2000 - 2011 Silverpeas
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
package com.stratelia.webactiv.util;

/**
 * @author Norbert CHAIX
 * @version
 */
public class GeneralPropertiesManager {

  public static final int DVIS_ALL = 0;
  public static final int DVIS_ONE = 1;
  public static final int DVIS_EACH = 2;
  public static final String GENERAL_PROPERTIES_FILE = "com.stratelia.webactiv.multilang.generalMultilang";
  static final ResourceLocator s_GeneralProperties = new ResourceLocator(
      "com.stratelia.webactiv.general", "");
  static int dvis = Integer.parseInt(s_GeneralProperties.getString("domainVisibility", "0"));

  static public ResourceLocator getGeneralResourceLocator() {
    return s_GeneralProperties;
  }
  
  
  static public int getInteger(String property, int defaultValue) {
    return s_GeneralProperties.getInteger(property, defaultValue);
  }
  
  
  static public String getString(String property, String defaultValue) {
    return s_GeneralProperties.getString(property, defaultValue);
  }
  
  static public String getString(String property) {
    return s_GeneralProperties.getString(property);
  }
  
  static public boolean getBoolean(String property, boolean defaultValue) {
    return s_GeneralProperties.getBoolean(property, defaultValue);
  }

  static public int getDomainVisibility() {
    return dvis;
  }

  static public ResourceLocator getGeneralMultilang(String language) {
    return new ResourceLocator(GENERAL_PROPERTIES_FILE, language);
  }

  private GeneralPropertiesManager() {
  }
}
