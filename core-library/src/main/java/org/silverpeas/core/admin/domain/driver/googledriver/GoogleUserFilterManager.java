/*
 * Copyright (C) 2000 - 2022 Silverpeas
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.admin.domain.driver.googledriver;

import org.silverpeas.core.admin.domain.DomainDriver;
import org.silverpeas.core.admin.service.AdminException;
import org.silverpeas.core.admin.user.model.User;
import org.silverpeas.core.util.SettingBundle;

import java.util.Properties;

import static org.silverpeas.core.util.ResourceLocator.saveSettingBundle;
import static org.silverpeas.core.util.StringUtil.defaultStringIfNotDefined;

/**
 * @author silveryocha
 */
public class GoogleUserFilterManager implements DomainDriver.UserFilterManager {

  private static final String USER_FILTER_RULE = "google.user.filter.rule";
  private final DomainDriver driver;
  private final SettingBundle settings;

  GoogleUserFilterManager(final DomainDriver driver, final SettingBundle settings) {
    this.driver = driver;
    this.settings = settings;
  }

  @Override
  public String getRuleKey() {
    return USER_FILTER_RULE;
  }

  @Override
  public String getRule() {
    return settings.getString(USER_FILTER_RULE, "");
  }

  @Override
  public User[] validateRule(final String rule) throws AdminException {
    final String previous = getRule();
    final Properties properties = settings.asProperties();
    try {
      properties.setProperty(USER_FILTER_RULE, defaultStringIfNotDefined(rule));
      saveSettingBundle(settings, properties);
      return driver.getAllUsers();
    } catch (Exception e) {
      throw new AdminException(e);
    } finally {
      properties.setProperty(USER_FILTER_RULE, previous);
      saveSettingBundle(settings, properties);
    }
  }

  @Override
  public User[] saveRule(final String rule) throws AdminException {
    final String previous = getRule();
    final Properties properties = settings.asProperties();
    try {
      properties.setProperty(USER_FILTER_RULE, defaultStringIfNotDefined(rule));
      saveSettingBundle(settings, properties);
      return driver.getAllUsers();
    } catch (Exception e) {
      properties.setProperty(USER_FILTER_RULE, previous);
      saveSettingBundle(settings, properties);
      throw new AdminException(e);
    }
  }
}
