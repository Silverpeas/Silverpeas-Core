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
 * FLOSS exception.  You should have received a copy of the text describing
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

package org.silverpeas.web.jobstartpage;

import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;

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
  public static boolean useComponentsCopy;
  public static String SPACEDISPLAYPOSITION_AFTER;
  public static String SPACEDISPLAYPOSITION_BEFORE;
  public static String SPACEDISPLAYPOSITION_TODEFINE;
  public static String SPACEDISPLAYPOSITION_CONFIG;
  public static boolean recoverRightsEnable;
  public static String TEMPLATE_PATH;
  public static String CUSTOMERS_TEMPLATE_PATH;
  public static boolean componentsInSpaceQuotaActivated = false;
  public static boolean dataStorageInSpaceQuotaActivated;
  public static long dataStorageInSpaceQuotaDefaultMaxCount;
  public static long dataStorageInPersonalSpaceQuotaDefaultMaxCount;
  public static String defaultAuthorizedFiles;
  public static String defaultForbiddenFiles;

  static {
    SettingBundle rs = ResourceLocator.getSettingBundle(
        "org.silverpeas.jobStartPagePeas.settings.jobStartPagePeasSettings");
    m_IsProfileEditable = rs.getBoolean("IsProfileEditable", false);
    isBackupEnable = rs.getBoolean("IsBackupEnable", false);
    isBasketEnable = rs.getBoolean("UseBasket", false);
    useBasketWhenAdmin = rs.getBoolean("UseBasketWhenAdmin", false);
    isInheritanceEnable = rs.getBoolean("UseProfileInheritance", false);
    isPublicParameterEnable = rs.getBoolean("UsePublicParameter", true);
    useComponentsCopy = rs.getBoolean("UseComponentsCopy", false);
    SPACEDISPLAYPOSITION_CONFIG = rs.getString("DisplaySpacePositionConfiguration", "BEFORE");
    SPACEDISPLAYPOSITION_AFTER = rs.getString("DisplaySpacesAfterComponents", "AFTER");
    SPACEDISPLAYPOSITION_BEFORE = rs.getString("DisplaySpacesBeforeComponents", "BEFORE");
    SPACEDISPLAYPOSITION_TODEFINE = rs.getString("DisplaySpacesToDefine", "TODEFINE");
    recoverRightsEnable = rs.getBoolean("EnableRecoverRightsOperation", false);
    TEMPLATE_PATH = rs.getString("templatePath");
    CUSTOMERS_TEMPLATE_PATH = rs.getString("customersTemplatePath");
    componentsInSpaceQuotaActivated = rs.getBoolean("quota.space.components.activated", false);
    dataStorageInSpaceQuotaActivated = rs.getBoolean("quota.space.datastorage.activated", false);
    dataStorageInSpaceQuotaDefaultMaxCount =
        rs.getLong("quota.space.datastorage.default.maxCount", 0);
    if (dataStorageInSpaceQuotaDefaultMaxCount < 0) {
      dataStorageInSpaceQuotaDefaultMaxCount = 0;
    }
    dataStorageInPersonalSpaceQuotaDefaultMaxCount =
        rs.getLong("quota.personalspace.datastorage.default.maxCount", 0);
    if (dataStorageInPersonalSpaceQuotaDefaultMaxCount < 0) {
      dataStorageInPersonalSpaceQuotaDefaultMaxCount = 0;
    }
    defaultAuthorizedFiles = rs.getString("file.authorized.default", "");
    defaultForbiddenFiles = rs.getString("file.forbidden.default", "");
  }
}
