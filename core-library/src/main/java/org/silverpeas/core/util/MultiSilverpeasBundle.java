/*
 * Copyright (C) 2000 - 2016 Silverpeas
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * <p>
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.core.util;

import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.SilverpeasBundle;
import org.silverpeas.core.util.StringUtil;

import java.text.ParseException;
import java.util.Date;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * A bundle that is a wrapper of several kinds of resource bundles.
 */
public class MultiSilverpeasBundle {

  private LocalizationBundle specificMultilang = null;
  private SettingBundle specificIcons = null;
  private SettingBundle specificSettings = null;
  private String language = null;

  /**
   * Constructs a new multiple bundle.
   * @param specificMultilang the localized messages
   * @param specificIcons the icons
   * @param language the language of the localized resources.
   */
  public MultiSilverpeasBundle(LocalizationBundle specificMultilang,
      SettingBundle specificIcons, String language) {
    this.specificMultilang = specificMultilang;
    this.specificIcons = specificIcons;
    this.language = language;
  }

  /**
   * Constructs a new multiple bundle.
   * @param specificMultilang the localized messages
   * @param specificIcons the icons
   * @param specificSettings the settings
   * @param language the language of the localized resources.
   */
  public MultiSilverpeasBundle(LocalizationBundle specificMultilang,
      SettingBundle specificIcons, SettingBundle specificSettings, String language) {
    this.specificMultilang = specificMultilang;
    this.specificIcons = specificIcons;
    this.language = language;
    this.specificSettings = specificSettings;
  }

  /**
   * Constructs a new multiple bundle.
   * @param specificMultilang the localized messages
   * @param language the language of the localized resources.
   */
  public MultiSilverpeasBundle(LocalizationBundle specificMultilang, String language) {
    this.specificMultilang = specificMultilang;
    this.language = language;
  }

  /**
   * Return the ResourceBundle for usage in JSTL.
   * @return the ResourceBundle.
   */
  public ResourceBundle getMultilangBundle() {
    return specificMultilang;
  }

  /**
   * Return the icons settings.
   * @return the SettingBundle.
   */
  public SettingBundle getIconsBundle() {
    return specificIcons;
  }

  /**
   * @param key - key in the multilang file
   * @return the value of the key according to the key. If key starts with "GML.", we look at in the
   * general multilang. Else, we look at in the component multilang
   */
  public String getString(String key) {
    String valret = null;
    if (key != null) {
      valret = key;
      if (specificMultilang != null) {
        valret = specificMultilang.getString(key);
      }
    }
    if (valret == null) {
      valret = "";
    }
    return valret;
  }

  public String getStringWithParams(String key, String param) {
    String[] params = {param};
    return getStringWithParams(key, params);
  }

  public String getStringWithParams(String key, String[] params) {
    String valret = null;

    if (key != null) {
      valret = key;
      if (specificMultilang != null) {
        valret = specificMultilang.getStringWithParams(key, params);
      }
    }
    if (valret == null) {
      valret = "";
    }
    return valret;
  }

  public String getIcon(String key) {
    return URLUtil.getApplicationURL() + getValue(key, specificIcons);
  }

  public String getLanguage() {
    return language;
  }

  /**
   * We look at the key in the specific settings file.
   * @param key - key in the settings file
   * @return the value of the key if the key exists and if a value is specified. null otherwise.
   */
  public String getSetting(String key) {
    return getSetting(key, null);
  }

  public String getSetting(String key, String defaultValue) {
    return specificSettings.getString(key, defaultValue);
  }

  public boolean getSetting(String key, boolean defaultValue) {
    return specificSettings.getBoolean(key, defaultValue);
  }

  public int getSetting(String key, int defaultValue) {
    return specificSettings.getInteger(key, defaultValue);
  }

  public String getOutputDate(Date date) {
    return DateUtil.getOutputDate(date, language);
  }

  public String getOutputDate(String dateDB) throws ParseException {
    return DateUtil.getOutputDate(dateDB, language);
  }

  public String getOutputDateAndHour(Date date) {
    return DateUtil.getOutputDateAndHour(date, language);
  }

  /**
   * Display first not null date
   * @param date date to display
   * @param defaultDate extra date to display if date1 is empty (or null)
   * @return the formatted date
   */
  public String getOutputDateAndHour(Date date, Date defaultDate) {
    String formatedDate = DateUtil.getOutputDateAndHour(date, language);
    if (!StringUtil.isDefined(formatedDate)) {
      formatedDate = DateUtil.getOutputDateAndHour(defaultDate, language);
    }
    return formatedDate;
  }

  public String getInputDate(Date date) {
    return DateUtil.getInputDate(date, language);
  }

  public String getInputDate(String dateDB) throws ParseException {
    return DateUtil.getInputDate(dateDB, language);
  }

  public String getDBDate(String date) throws ParseException {
    return DateUtil.date2SQLDate(date, language);
  }

  public String getDBDate(Date date) {
    return DateUtil.date2SQLDate(date);
  }

  public Date getDate(String date) throws ParseException {
    return DateUtil.stringToDate(date, language);
  }

  private String getValue(String key, SilverpeasBundle bundle) {
    String valret = null;
    if (key != null) {
      valret = key;
      if (bundle != null && bundle.exists()) {
        try {
          valret = bundle.getString(key);
        } catch (MissingResourceException ex) {
          valret = null;
        }
      }
    }
    if (valret == null) {
      valret = "";
    }
    return valret;
  }
}