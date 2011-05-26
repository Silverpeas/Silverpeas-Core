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

/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent)
 ---*/

package com.stratelia.silverpeas.util;

import java.text.ParseException;
import java.util.Date;
import java.util.ResourceBundle;

import com.silverpeas.util.StringUtil;
import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.ResourceLocator;


/**
 * Class declaration
 * @author
 */
public class ResourcesWrapper {

  private ResourceLocator specificMultilang = null;
  private ResourceLocator specificIcons = null;
  private ResourceLocator specificSettings = null;
  private ResourceLocator genericMultilang = null;
  private String language = null;

  /**
   * @param specificMultilang - Multilang of component
   * @param specificIcons - Icons of component
   * @param language - user's language
   */
  public ResourcesWrapper(ResourceLocator specificMultilang,
      ResourceLocator specificIcons, String language) {
    this.specificMultilang = specificMultilang;
    this.specificIcons = specificIcons;
    this.genericMultilang = GeneralPropertiesManager.getGeneralMultilang(language);
    this.language = language;
  }

  /**
   * @param specificMultilang - Multilang of component
   * @param specificIcons - Icons of component
   * @param specificSettings - Settings of component
   * @param language - user's language
   */
  public ResourcesWrapper(ResourceLocator specificMultilang,
      ResourceLocator specificIcons, ResourceLocator specificSettings,
      String language) {
    this.specificMultilang = specificMultilang;
    this.specificIcons = specificIcons;
    this.genericMultilang = GeneralPropertiesManager.getGeneralMultilang(language);
    this.language = language;
    this.specificSettings = specificSettings;
  }

  /**
   * @param specificMultilang - Multilang of component
   * @param language - user's language
   */
  public ResourcesWrapper(ResourceLocator specificMultilang, String language) {
    this.specificMultilang = specificMultilang;
    this.genericMultilang = GeneralPropertiesManager.getGeneralMultilang(language);
    this.language = language;
  }

  /**
   * Return the ResourceBundle for usage in JSTL.
   * @return the ResourceBundle under the ResourceLocator.
   */
  public ResourceBundle getMultilangBundle() {
    return specificMultilang.getResourceBundle();
  }

  /**
   * Return the icons ResourceBundle for usage in JSTL.
   * @return the icons ResourceBundle under the ResourceLocator.
   */
  public ResourceBundle getIconsBundle() {
    return specificIcons.getResourceBundle();
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
      if (key.startsWith("GML.")) {
        if (genericMultilang != null) {
          valret = genericMultilang.getString(key);
        }
      } else {
        if (specificMultilang != null) {
          valret = specificMultilang.getString(key);
        }
      }
    }
    if (valret == null) {
      valret = "";
    }
    return valret;
  }

  public String getStringWithParam(String key, String param) {
    String[] params = {param};
    return getStringWithParams(key, params);
  }
  
  public String getStringWithParams(String key, String[] params) {
    String valret = null;

    if (key != null) {
      valret = key;
      if (key.startsWith("GML.")) {
        if (genericMultilang != null) {
          valret = genericMultilang.getStringWithParams(key, params);
        }
      } else {
        if (specificMultilang != null) {
          valret = specificMultilang.getStringWithParams(key, params);
        }
      }
    }
    if (valret == null) {
      valret = "";
    }
    return valret;
  }

  public String getIcon(String key) {
    return GeneralPropertiesManager.getGeneralResourceLocator().getString(
        "ApplicationURL")
        + getValue(key, specificIcons);
  }

  public String getLanguage() {
    return language;
  }

  /*
   * public String getLanguage(String code) { return I18NHelper.getLanguage(language, code); }
   */

  /**
   * We look at the key in the specific settings file.
   * @param key - key in the settings file
   * @return the value of the key if the key exists and if a value is specified. null otherwise.
   */
  public String getSetting(String key) {
    return getSetting(key, null);
  }

  public String getSetting(String key, String defaultValue) {
    return SilverpeasSettings.readString(specificSettings, key, defaultValue);
  }

  public boolean getSetting(String key, boolean defaultValue) {
    return SilverpeasSettings.readBoolean(specificSettings, key, defaultValue);
  }

  public int getSetting(String key, int defaultValue) {
    return SilverpeasSettings.readInt(specificSettings, key, defaultValue);
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
   * @param date1 date to display
   * @param date2 extra date to display if date1 is empty (or null)
   * @return the formatted date
   */
  public String getOutputDateAndHour(Date date1, Date date2) {
    String sDate = DateUtil.getOutputDateAndHour(date1, language);
    if (!StringUtil.isDefined(sDate)) {
      sDate = DateUtil.getOutputDateAndHour(date2, language);
    }
    return sDate;
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

  private String getValue(String key, ResourceLocator resourceLocator) {
    String valret = null;

    if (key != null) {
      valret = key;
      if (resourceLocator != null) {
        valret = resourceLocator.getString(key);
      }
    }
    if (valret == null) {
      valret = "";
    }
    return valret;
  }
}