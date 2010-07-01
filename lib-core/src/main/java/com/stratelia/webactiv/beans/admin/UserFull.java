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
package com.stratelia.webactiv.beans.admin;


import com.stratelia.silverpeas.silvertrace.SilverTrace;
import java.util.HashMap;
import java.util.Map.Entry;

public class UserFull extends UserDetail {

  private static final long serialVersionUID = 1L;
  protected HashMap<String, String> m_hInfos = null;
  protected AbstractDomainDriver m_pDomainDriver = null;
  protected String m_password = "";
  protected boolean m_isPasswordValid = false;
  protected boolean m_isPasswordAvailable = false;

  /** Creates new UserFull */
  public UserFull() {
    super();
    m_hInfos = new HashMap<String, String>();
  }

  public UserFull(AbstractDomainDriver domainDriver) {
    super();
    m_hInfos = new HashMap<String, String>();
    m_pDomainDriver = domainDriver;
  }

  public UserFull(AbstractDomainDriver domainDriver, UserDetail toClone) {
    super(toClone);
    m_hInfos = new HashMap<String, String>();
    m_pDomainDriver = domainDriver;
  }

  // Password specific entries
  public boolean isPasswordAvailable() {
    return m_isPasswordAvailable;
  }

  public void setPasswordAvailable(boolean pa) {
    m_isPasswordAvailable = pa;
  }

  public boolean isPasswordValid() {
    return m_isPasswordValid;
  }

  public void setPasswordValid(boolean pv) {
    m_isPasswordValid = pv;
  }

  public String getPassword() {
    return (m_password == null) ? "" : m_password;
  }

  public void setPassword(String p) {
    m_password = p;
  }

  // Values' getters
  public String[] getPropertiesNames() {
    if (m_pDomainDriver != null) {
      return m_pDomainDriver.getPropertiesNames();
    }
    return new String[0];

  }

  public HashMap<String, String> getSpecificDetails() {
    return m_hInfos;
  }

  public String getValue(String propertyName, String defaultValue) {
    String valret;

    valret = m_hInfos.get(propertyName);
    if (valret == null) {
      valret = defaultValue;
    }
    return valret;
  }

  public String getValue(String propertyName) {
    return getValue(propertyName, "");
  }

  public boolean getValue(String propertyName, boolean defaultValue) {
    boolean valret = defaultValue;

    String sValret = m_hInfos.get(propertyName);
    if (sValret != null) {
      valret = Boolean.parseBoolean(sValret);
    }
    return valret;
  }

  // Labels' getters
  public HashMap<String, String> getSpecificLabels(String language) {
    if (m_pDomainDriver != null) {
      return m_pDomainDriver.getPropertiesLabels(language);
    }
    return null;
  }

  public String getSpecificLabel(String language, String propertyName) {
    String valret = null;

    if (m_pDomainDriver != null) {
      HashMap<String, String> theLabels = m_pDomainDriver.getPropertiesLabels(language);
      valret = theLabels.get(propertyName);
    }
    if (valret == null) {
      valret = "";
    }
    return valret;
  }

  public String getPropertyType(String propertyName) {
    String valret = null;

    if (m_pDomainDriver != null) {
      DomainProperty domainProperty = m_pDomainDriver.getProperty(propertyName);
      if (domainProperty != null) {
        valret = domainProperty.getType();
      }
    }
    if (valret == null) {
      valret = "";
    }
    return valret;
  }

  public boolean isPropertyUpdatableByUser(String property) {
    if (m_pDomainDriver != null) {
      DomainProperty domainProperty = m_pDomainDriver.getProperty(property);
      if (domainProperty != null) {
        return domainProperty.isUpdateAllowedToUser();
      }
    }
    return false;
  }

  // Values' setters
  public void setValue(String propertyName, String value) {
    m_hInfos.put(propertyName, value);
  }

  public void setValue(String propertyName, boolean bValue) {
    m_hInfos.put(propertyName, String.valueOf(bValue));
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof UserFull) {
      return this.equals((UserFull) other);
    }
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 53 * hash + (this.m_hInfos != null ? this.m_hInfos.hashCode() : 0);
    hash = 53 * super.hashCode();
    return hash;
  }

  public boolean equals(UserFull cmpUser) {
    if (super.equals(cmpUser)) {
      String[] keys = (String[]) m_hInfos.keySet().toArray(new String[0]);
      boolean isTheSame = true;
      for (int i = 0; (i < keys.length) && isTheSame; i++) {
        isTheSame = getValue(keys[i]).equals(cmpUser.getValue(keys[i]));
      }
      return isTheSame;
    }
    return false;
  }

  @Override
  public void traceUser() {
    super.traceUser();
    for (Entry<String, String> entry : m_hInfos.entrySet()) {
      SilverTrace.info("admin", "UserFull.traceUser", "admin.MSG_DUMP_USER", entry.getKey()
          + " : " + entry.getValue());
    }
  }
}
