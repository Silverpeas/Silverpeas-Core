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
/*
 * JobStartPagePeasSettings.java
 */

package com.silverpeas.jobStartPagePeas;

import com.stratelia.silverpeas.util.SilverpeasSettings;
import com.stratelia.webactiv.util.ResourceLocator;

/**
 * This class manage the informations needed for job start page
 * @c.bonin
 */
public class JobStartPagePeasSettings {
  public static boolean m_IsProfileEditable = false;
  public static boolean isBackupEnable = false;
  public static boolean isBasketEnable = false;
  public static boolean useBasketWhenAdmin = false;
  public static boolean isInheritanceEnable = false;
  public static boolean isPublicParameterEnable = false;
  public static boolean useJSR168Portlets = false;
  public static boolean useComponentsCopy = false;
  public static String SPACEDISPLAYPOSITION_AFTER = null;
  public static String SPACEDISPLAYPOSITION_BEFORE = null;
  public static String SPACEDISPLAYPOSITION_TODEFINE = null;
  public static String SPACEDISPLAYPOSITION_CONFIG = null;

  static {
    ResourceLocator rs = new ResourceLocator(
        "com.silverpeas.jobStartPagePeas.settings.jobStartPagePeasSettings", "");

    m_IsProfileEditable = SilverpeasSettings.readBoolean(rs,
        "IsProfileEditable", false);
    isBackupEnable = SilverpeasSettings
        .readBoolean(rs, "IsBackupEnable", false);
    isBasketEnable = SilverpeasSettings.readBoolean(rs, "UseBasket", false);
    useBasketWhenAdmin = SilverpeasSettings.readBoolean(rs,
        "UseBasketWhenAdmin", false);
    isInheritanceEnable = SilverpeasSettings.readBoolean(rs,
        "UseProfileInheritance", false);
    isPublicParameterEnable = SilverpeasSettings.readBoolean(rs,
        "UsePublicParameter", true);
    useJSR168Portlets = SilverpeasSettings.readBoolean(rs, "UseJSR168Portlets",
        false);
    useComponentsCopy = SilverpeasSettings.readBoolean(rs, "UseComponentsCopy",
        false);
    SPACEDISPLAYPOSITION_CONFIG =
        SilverpeasSettings.readString(rs, "DisplaySpacePositionConfiguration", "BEFORE");
    SPACEDISPLAYPOSITION_AFTER =
        SilverpeasSettings.readString(rs, "DisplaySpacesAfterComponents", "AFTER");
    SPACEDISPLAYPOSITION_BEFORE =
        SilverpeasSettings.readString(rs, "DisplaySpacesBeforeComponents", "BEFORE");
    SPACEDISPLAYPOSITION_TODEFINE =
        SilverpeasSettings.readString(rs, "DisplaySpacesToDefine", "TODEFINE");
  }
}
