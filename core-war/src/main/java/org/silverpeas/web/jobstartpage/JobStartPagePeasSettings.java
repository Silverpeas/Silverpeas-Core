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
package org.silverpeas.web.jobstartpage;

import org.silverpeas.kernel.bundle.ResourceLocator;
import org.silverpeas.kernel.bundle.SettingBundle;

/**
 * This class manage the informations needed for job start page
 * @author c.bonin
 */
public class JobStartPagePeasSettings {
  public static final boolean IS_PROFILE_EDITABLE;
  public static final boolean IS_BACKUP_ENABLED;
  public static final boolean IS_BASKET_ENABLED;
  public static final boolean USE_BASKET_WHEN_ADMIN;
  public static final boolean IS_INHERITANCE_ENABLED;
  public static final boolean IS_PUBLIC_PARAMETER_ENABLED;
  public static final boolean USE_COMPONENTS_COPY;
  public static final String SPACE_DISPLAY_POSITION_AFTER;
  public static final String SPACE_DISPLAY_POSITION_BEFORE;
  public static final String SPACE_DISPLAY_POSITION_TODEFINE;
  public static final String SPACE_DISPLAY_POSITION_CONFIG;
  public static final boolean RECOVER_RIGHTS_ENABLED;
  public static final String TEMPLATE_PATH;
  public static final String CUSTOMERS_TEMPLATE_PATH;
  public static final boolean COMPONENTS_IN_SPACE_QUOTA_ENABLED;
  public static final boolean DATA_STORAGE_IN_SPACE_QUOTA_ENABLED;
  public static final long DATA_STORAGE_IN_SPACE_QUOTA_DEFAULT_MAX_COUNT;
  public static final long DATA_STORAGE_IN_PERSONAL_SPACE_QUOTA_DEFAULT_MAX_COUNT;
  public static final String DEFAULT_AUTHORIZED_FILES;
  public static final String DEFAULT_FORBIDDEN_FILES;

  static {
    SettingBundle rs = ResourceLocator.getSettingBundle(
        "org.silverpeas.jobStartPagePeas.settings.jobStartPagePeasSettings");
    IS_PROFILE_EDITABLE = rs.getBoolean("IsProfileEditable", false);
    IS_BACKUP_ENABLED = rs.getBoolean("IsBackupEnable", false);
    IS_BASKET_ENABLED = rs.getBoolean("UseBasket", false);
    USE_BASKET_WHEN_ADMIN = rs.getBoolean("UseBasketWhenAdmin", false);
    IS_INHERITANCE_ENABLED = rs.getBoolean("UseProfileInheritance", false);
    IS_PUBLIC_PARAMETER_ENABLED = rs.getBoolean("UsePublicParameter", true);
    USE_COMPONENTS_COPY = rs.getBoolean("UseComponentsCopy", false);
    SPACE_DISPLAY_POSITION_CONFIG = rs.getString("DisplaySpacePositionConfiguration", "BEFORE");
    SPACE_DISPLAY_POSITION_AFTER = rs.getString("DisplaySpacesAfterComponents", "AFTER");
    SPACE_DISPLAY_POSITION_BEFORE = rs.getString("DisplaySpacesBeforeComponents", "BEFORE");
    SPACE_DISPLAY_POSITION_TODEFINE = rs.getString("DisplaySpacesToDefine", "TODEFINE");
    RECOVER_RIGHTS_ENABLED = rs.getBoolean("EnableRecoverRightsOperation", false);
    TEMPLATE_PATH = rs.getString("templatePath");
    CUSTOMERS_TEMPLATE_PATH = rs.getString("customersTemplatePath");
    COMPONENTS_IN_SPACE_QUOTA_ENABLED = rs.getBoolean("quota.space.components.activated", false);
    DATA_STORAGE_IN_SPACE_QUOTA_ENABLED = rs.getBoolean("quota.space.datastorage.activated", false);
    var quota = rs.getLong("quota.space.datastorage.default.maxCount", 0);
    DATA_STORAGE_IN_SPACE_QUOTA_DEFAULT_MAX_COUNT =  quota < 0 ? 0 : quota;
    quota = rs.getLong("quota.personalspace.datastorage.default.maxCount", 0);
    DATA_STORAGE_IN_PERSONAL_SPACE_QUOTA_DEFAULT_MAX_COUNT = quota < 0 ? 0 : quota;
    DEFAULT_AUTHORIZED_FILES = rs.getString("file.authorized.default", "");
    DEFAULT_FORBIDDEN_FILES = rs.getString("file.forbidden.default", "");
  }
}
