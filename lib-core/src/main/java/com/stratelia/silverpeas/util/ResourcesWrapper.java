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
/*--- formatted by Jindent 2.1, (www.c-lab.de/~jindent) 
 ---*/

package com.stratelia.silverpeas.util;

import java.text.ParseException;
import java.util.Date;
import java.util.ResourceBundle;

import com.stratelia.webactiv.util.DateUtil;
import com.stratelia.webactiv.util.GeneralPropertiesManager;
import com.stratelia.webactiv.util.ResourceLocator;

/*
 * CVS Informations
 *
 * $Id: ResourcesWrapper.java,v 1.9 2008/04/16 14:45:00 neysseri Exp $
 *
 * $Log: ResourcesWrapper.java,v $
 * Revision 1.9  2008/04/16 14:45:00  neysseri
 * no message
 *
 * Revision 1.8.2.2  2008/04/14 06:36:12  ehugonnet
 * Les icons sont maintenant dans le properties icons
 * Revision 1.8.2.1 2008/04/09 06:05:18
 * ehugonnet Gestion des Resources comme des ResourceBundle plutot que des
 * properties afin de les rendre disponible pour JSTL Revision 1.8 2008/03/21
 * 12:03:14 neysseri Ajout de la méthode boolean getSetting(String key, boolean
 * defaultValue)
 *
 * Revision 1.7 2007/12/03 15:02:17 neysseri no message
 *
 * Revision 1.6 2007/12/03 13:48:58 neysseri no message
 *
 * Revision 1.5.10.2 2007/10/01 14:07:19 neysseri no message
 *
 * Revision 1.5.10.1 2007/09/14 10:32:45 neysseri no message
 *
 * Revision 1.5 2006/04/28 17:21:47 neysseri no message
 *
 * Revision 1.4 2005/09/30 14:22:36 neysseri Centralisation de la gestion des
 * dates
 *
 * Revision 1.3 2005/08/18 11:14:50 neysseri no message
 *
 * Revision 1.2.8.1 2005/08/10 17:26:23 neysseri Ajout méthode getSetting()
 *
 * Revision 1.2 2003/02/10 14:09:58 neysseri no message
 *
 * Revision 1.1.1.1 2002/08/06 14:48:19 nchaix no message
 *
 * Revision 1.3 2002/02/25 17:10:04 neysseri Maintenant, la méthode getIcon()
 * renvoie le contexte+le contenu du properties
 *
 * Revision 1.2 2002/02/07 10:55:15 tleroi no message
 *
 */

/**
 * Class declaration
 * 
 * 
 * @author
 */
public class ResourcesWrapper {

  private ResourceLocator specificMultilang = null;
  private ResourceLocator specificIcons = null;
  private ResourceLocator specificSettings = null;
  private ResourceLocator genericMultilang = null;
  private String language = null;

  /**
   * @param specificMultilang
   *          - Multilang of component
   * @param specificIcons
   *          - Icons of component
   * @param language
   *          - user's language
   */
  public ResourcesWrapper(ResourceLocator specificMultilang,
      ResourceLocator specificIcons, String language) {
    this.specificMultilang = specificMultilang;
    this.specificIcons = specificIcons;
    this.genericMultilang = GeneralPropertiesManager
        .getGeneralMultilang(language);
    this.language = language;
  }

  /**
   * @param specificMultilang
   *          - Multilang of component
   * @param specificIcons
   *          - Icons of component
   * @param specificSettings
   *          - Settings of component
   * @param language
   *          - user's language
   */
  public ResourcesWrapper(ResourceLocator specificMultilang,
      ResourceLocator specificIcons, ResourceLocator specificSettings,
      String language) {
    this.specificMultilang = specificMultilang;
    this.specificIcons = specificIcons;
    this.genericMultilang = GeneralPropertiesManager
        .getGeneralMultilang(language);
    this.language = language;
    this.specificSettings = specificSettings;
  }

  /**
   * @param specificMultilang
   *          - Multilang of component
   * @param language
   *          - user's language
   */
  public ResourcesWrapper(ResourceLocator specificMultilang, String language) {
    this.specificMultilang = specificMultilang;
    this.genericMultilang = GeneralPropertiesManager
        .getGeneralMultilang(language);
    this.language = language;
  }

  /**
   * Return the ResourceBundle for usage in JSTL.
   * 
   * @return the ResourceBundle under the ResourceLocator.
   */
  public ResourceBundle getMultilangBundle() {
    return specificMultilang.getResourceBundle();
  }

  /**
   * Return the icons ResourceBundle for usage in JSTL.
   * 
   * @return the icons ResourceBundle under the ResourceLocator.
   */
  public ResourceBundle getIconsBundle() {
    return specificIcons.getResourceBundle();
  }

  /**
   * @param key
   *          - key in the multilang file
   * @return the value of the key according to the key. If key starts with
   *         "GML.", we look at in the general multilang. Else, we look at in
   *         the component multilang
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
    String valret = null;

    if (key != null) {
      valret = key;
      if (key.startsWith("GML.")) {
        if (genericMultilang != null) {
          valret = genericMultilang.getStringWithParam(key, param);
        }
      } else {
        if (specificMultilang != null) {
          valret = specificMultilang.getStringWithParam(key, param);
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
   * public String getLanguage(String code) { return
   * I18NHelper.getLanguage(language, code); }
   */

  /**
   * We look at the key in the specific settings file.
   * 
   * @param key
   *          - key in the settings file
   * @return the value of the key if the key exists and if a value is specified.
   *         null otherwise.
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

  public String getOutputDate(Date date) {
    return DateUtil.getOutputDate(date, language);
  }

  public String getOutputDate(String dateDB) throws ParseException {
    return DateUtil.getOutputDate(dateDB, language);
  }

  public String getOutputDateAndHour(Date date) {
    return DateUtil.getOutputDateAndHour(date, language);
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