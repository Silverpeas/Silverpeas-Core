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

/*
 * JobStartPagePeasSettings.java
 */

package com.silverpeas.jobStartPagePeas;

import com.stratelia.webactiv.util.ResourceLocator;

/**
 * This class manage the informations needed for job start page
 * @c.bonin
 */
public class JobStartPagePeasSettings {
  public static boolean m_IsProfileEditable;
  public static boolean isBackupEnable;
  public static boolean isBasketEnable;
  public static boolean useBasketWhenAdmin;
  public static boolean isInheritanceEnable;
  public static boolean isPublicParameterEnable;
  public static boolean useJSR168Portlets;
  public static boolean useComponentsCopy;
  public static String SPACEDISPLAYPOSITION_AFTER;
  public static String SPACEDISPLAYPOSITION_BEFORE;
  public static String SPACEDISPLAYPOSITION_TODEFINE;
  public static String SPACEDISPLAYPOSITION_CONFIG;
  public static boolean recoverRightsEnable;

  static {
    ResourceLocator rs = new ResourceLocator(
        "com.silverpeas.jobStartPagePeas.settings.jobStartPagePeasSettings", "");
    m_IsProfileEditable = rs.getBoolean("IsProfileEditable", false);
    isBackupEnable = rs.getBoolean("IsBackupEnable", false);
    isBasketEnable = rs.getBoolean("UseBasket", false);
    useBasketWhenAdmin = rs.getBoolean("UseBasketWhenAdmin", false);
    isInheritanceEnable = rs.getBoolean("UseProfileInheritance", false);
    isPublicParameterEnable = rs.getBoolean("UsePublicParameter", true);
    useJSR168Portlets = rs.getBoolean("UseJSR168Portlets", false);
    useComponentsCopy = rs.getBoolean("UseComponentsCopy", false);
    SPACEDISPLAYPOSITION_CONFIG = rs.getString("DisplaySpacePositionConfiguration", "BEFORE");
    SPACEDISPLAYPOSITION_AFTER = rs.getString("DisplaySpacesAfterComponents", "AFTER");
    SPACEDISPLAYPOSITION_BEFORE = rs.getString("DisplaySpacesBeforeComponents", "BEFORE");
    SPACEDISPLAYPOSITION_TODEFINE = rs.getString("DisplaySpacesToDefine", "TODEFINE");
    recoverRightsEnable = rs.getBoolean("EnableRecoverRightsOperation", false);
  }
}
