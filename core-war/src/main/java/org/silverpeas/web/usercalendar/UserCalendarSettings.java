/*
 * Copyright (C) 2000 - 2019 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
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
package org.silverpeas.web.usercalendar;


import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;



/**
 * It gathers all the settings and i18n relative to the User Calendar component.
 * @author Yohann Chastagnier
 */
public final class UserCalendarSettings {

  /**
   * The name of the User Calendar component in Silverpeas.
   */
  public static final String COMPONENT_NAME = "userCalendar";
  /**
   * The relative path of the properties file containing the settings of the User Calendar
   * component.
   */
  public static final String SETTINGS_PATH =
      "org.silverpeas.usercalendar.settings.UserCalendarSettings";
  /**
   * The relative path of the i18n bundle of the User Calendar component.
   */
  public static final String MESSAGES_PATH =
      "org.silverpeas.usercalendar.multilang.UserCalendarBundle";
  /**
   * The relative path of the properties file containing the references of the icons dedicated to
   * the User Calendar component.
   */
  public static final String ICONS_PATH = "org.silverpeas.usercalendar.settings.UserCalendarIcons";

  /**
   * Hidden constructor.
   */
  private UserCalendarSettings() {
  }

  /**
   * Gets all the messages for the User Calendar component and translated in the specified
   * language.
   * @param language the language in which are written the messages.
   * @return the resource with the translated messages.
   */
  public static LocalizationBundle getMessagesIn(String language) {
    return ResourceLocator.getLocalizationBundle(MESSAGES_PATH, language);
  }

  /**
   * Gets all the settings of the User Calendar component.
   * @return the resource with the different component settings.
   */
  public static SettingBundle getSettings() {
    return ResourceLocator.getSettingBundle(SETTINGS_PATH);
  }

  /**
   * Gets all the icons definitions particular to the User Calendar component.
   * @return the resource with icons definition.
   */
  public static LocalizationBundle getIcons() {
    return ResourceLocator.getLocalizationBundle(ICONS_PATH);
  }
}
