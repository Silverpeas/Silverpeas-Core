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
package com.stratelia.silverpeas.domains;

import com.silverpeas.util.StringUtil;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.util.ResourceLocator;

/**
 * Extracting common code from LDAPSettings and SQLSettings
 * @author ehugonnet
 */
public abstract class DriverSettings {

  /**
   * Use this function to be sure to obtain a string without error, even if the property is not
   * found. (in that case, returns empty string)
   * @param rs the properties file
   * @param key the key value to retreive
   * @return  
   */
  protected String getSureString(ResourceLocator rs, String key) {
    String valret = null;

    try {
      valret = rs.getString(key, null);
      if (valret == null) {
        valret = "";
      }
    } catch (Exception e) {
      valret = "";
    }
    return valret;
  }

  protected String getStringValue(ResourceLocator rs, String key, String defaultValue) {
    String valret = defaultValue;
    try {
      valret = rs.getString(key, null);
      if (valret == null) {
        valret = defaultValue;
      }
    } catch (Exception e) {
      valret = defaultValue;
    }
    return valret;
  }

  protected boolean getBooleanValue(ResourceLocator rs, String key,
      boolean defaultValue) {
    String res = rs.getString(key, null);
    boolean valret = defaultValue;
    if (res != null) {
      valret = StringUtil.getBooleanValue(res);
    }
    return valret;
  }

  protected int getIntValue(ResourceLocator rs, String key, int defaultValue) {
    String res = rs.getString(key, null);
    int valret = defaultValue;
    if (res != null) {
      try {
        valret = Integer.parseInt(res);
      } catch (Exception e) {
        SilverTrace.error("admin", "LDAPSettings.getUserIds()",
            "admin.MSG_ERR_LDAP_GENERAL", "Int parse error : " + key + " = " + res, e);
      }
    }
    return valret;
  }
}
