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

package com.stratelia.silverpeas.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.stratelia.webactiv.util.ResourceLocator;

/**
 * Standard SilverpeasSettings parent class
 * @author
 */
public class SilverpeasSettings {
  /*
   * Successors should declare a static initialisation like :
   * -------------------------------------------------------- public static int m_UsersByPage = 10;
   * public static int m_GroupsByPage = 10; static { ResourceLocator rs = new
   * ResourceLocator("com.stratelia.efb.mailSender.settings.jobDomainPeasSettings" , "");
   * m_UsersByPage = readInt(rs, "UsersByPage", 10); m_GroupsByPage = readInt(rs, "GroupsByPage",
   * 10); }
   */

  /**
   * Read an int from a Settings-file
   * @param rs
   * @param propName
   * @param defaultValue
   * @return
   * @see
   */
  static public int readInt(ResourceLocator rs, String propName,
      int defaultValue) {
    String s = rs.getString(propName, null);

    if ((s != null) && (s.length() > 0)) {
      return Integer.parseInt(s);
    } else {
      return defaultValue;
    }
  }

  /**
   * Read an float from a Settings-file
   * @param rs resourceLocator object
   * @param propName property name to get from properties file
   * @param defaultValue default value if no value has been found
   * @return a float value
   */
  public static float readFloat(ResourceLocator rs, String propName,
      float defaultValue) {
    String propValue = rs.getString(propName, null);

    if (StringUtils.isNotEmpty(propValue)) {
      return Float.parseFloat(propValue);
    } else {
      return defaultValue;
    }
  }

  static public long readLong(ResourceLocator rs, String propName,
      long defaultValue) {
    String s = rs.getString(propName, null);

    if ((s != null) && (s.length() > 0)) {
      return Long.parseLong(s);
    } else {
      return defaultValue;
    }
  }

  /**
   * Read a String from a Settings-file
   * @param rs
   * @param propName
   * @param defaultValue
   * @return
   * @see
   */
  static public String readString(ResourceLocator rs, String propName,
      String defaultValue) {
    return rs.getString(propName, defaultValue);
  }

  /**
   * Read a boolean from a Settings-file
   * @param rs
   * @param propName
   * @param defaultValue
   * @return
   * @see
   */
  static public boolean readBoolean(ResourceLocator rs, String propName,
      boolean defaultValue) {
    String s = rs.getString(propName, null);
    boolean valret = defaultValue;

    if (s != null) {
      if (defaultValue) {
        if (s.equalsIgnoreCase("false") || s.equalsIgnoreCase("0")
            || s.equalsIgnoreCase("no") || s.equalsIgnoreCase("non"))
          valret = false;
        else
          valret = true;
      } else {
        if (s.equalsIgnoreCase("true") || s.equalsIgnoreCase("1")
            || s.equalsIgnoreCase("yes") || s.equalsIgnoreCase("oui"))
          valret = true;
        else
          valret = false;
      }
    }
    return valret;
  }

  /**
   * Read a String-List from a Settings-file with indexes from 1 to n If max is -1, the functions
   * reads until the propertie's Id is not founded. If max >= 1, the functions returns an array of
   * 'max' elements (the elements not founded are set to "")
   * @param rs
   * @param propNamePrefix
   * @param propNameSufix
   * @param max the maximum index (-1 for no maximum value)
   * @return
   * @see
   */
  static public String[] readStringArray(ResourceLocator rs,
      String propNamePrefix, String propNameSufix, int max) {
    String s = null;
    int i = 1;
    List<String> valret = new ArrayList<String>();

    while ((i <= max) || (max == -1)) {
      s = rs.getString(propNamePrefix + Integer.toString(i) + propNameSufix,
          null);
      if (s != null) {
        valret.add(s);
      } else {
        if (max == -1) {
          max = i;
        } else {
          valret.add("");
        }
      }
      i++;
    }
    return (String[]) (valret.toArray(new String[0]));
  }
}
