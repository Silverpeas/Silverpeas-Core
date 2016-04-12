/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin.user.model;

import org.silverpeas.core.admin.domain.DomainDriver;
import org.silverpeas.core.admin.domain.model.DomainProperty;
import org.silverpeas.core.util.ArrayUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.silverpeas.core.admin.user.UserReference;
import org.silverpeas.core.security.token.exception.TokenException;
import org.silverpeas.core.security.token.exception.TokenRuntimeException;
import org.silverpeas.core.security.token.persistent.PersistentResourceToken;

public class UserFull extends UserDetail {

  private static final long serialVersionUID = 1L;
  protected HashMap<String, String> m_hInfos = null;
  protected DomainDriver m_pDomainDriver = null;
  protected String m_password = "";
  protected boolean m_isPasswordValid = false;
  protected boolean m_isPasswordAvailable = false;

  /**
   * Gets the full profile of the user with the specified identifier.
   *
   * @param userId the unique identifier of the user in Silverpeas.
   * @return the full profile of the user.
   */
  public static UserFull getById(String userId) {
    return getOrganisationController().getUserFull(userId);
  }

  /**
   * Creates new UserFull
   */
  public UserFull() {
    super();
    m_hInfos = new HashMap<String, String>();
  }

  public UserFull(DomainDriver domainDriver) {
    super();
    m_hInfos = new HashMap<String, String>();
    m_pDomainDriver = domainDriver;
  }

  public UserFull(DomainDriver domainDriver, UserDetail toClone) {
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

  public String getToken() {
    try {
      UserReference ref = UserReference.fromUser(this);
      return PersistentResourceToken.getOrCreateToken(ref).getValue();
    } catch (TokenException e) {
      throw new TokenRuntimeException(e.getMessage(), e);
    }
  }

  public void setPassword(String p) {
    m_password = p;
  }

  // Values' getters
  public String[] getPropertiesNames() {
    if (m_pDomainDriver != null) {
      return m_pDomainDriver.getPropertiesNames();
    }
    return ArrayUtil.EMPTY_STRING_ARRAY;

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
  public Map<String, String> getSpecificLabels(String language) {
    if (m_pDomainDriver != null) {
      return m_pDomainDriver.getPropertiesLabels(language);
    }
    return null;
  }

  public String getSpecificLabel(String language, String propertyName) {
    String valret = null;

    if (m_pDomainDriver != null) {
      Map<String, String> theLabels = m_pDomainDriver.getPropertiesLabels(language);
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

  public boolean isPropertyUpdatableByAdmin(String property) {
    if (m_pDomainDriver != null) {
      DomainProperty domainProperty = m_pDomainDriver.getProperty(property);
      if (domainProperty != null) {
        return domainProperty.isUpdateAllowedToAdmin();
      }
    }
    return false;
  }

  public boolean isAtLeastOnePropertyUpdatableByAdmin() {
    if (m_pDomainDriver != null) {
      String[] properties = m_pDomainDriver.getPropertiesNames();
      for (String property : properties) {
        if (isPropertyUpdatableByAdmin(property)) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean isAtLeastOnePropertyUpdatableByUser() {
    if (m_pDomainDriver != null) {
      String[] properties = m_pDomainDriver.getPropertiesNames();
      for (String property : properties) {
        if (isPropertyUpdatableByUser(property)) {
          return true;
        }
      }
    }
    return false;
  }

  public DomainProperty getProperty(String property) {
    if (m_pDomainDriver != null) {
      return m_pDomainDriver.getProperty(property);
    }
    return null;
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
      UserFull cmpUser = (UserFull) other;
      if (super.equals(cmpUser)) {
        String[] keys = m_hInfos.keySet().toArray(new String[m_hInfos.size()]);
        boolean isTheSame = true;
        for (int i = 0; (i < keys.length) && isTheSame; i++) {
          isTheSame = getValue(keys[i]).equals(cmpUser.getValue(keys[i]));
        }
        return isTheSame;
      }
      return false;
    }
    return false;
  }

  @Override
  public int hashCode() {
    //int hash = 3;
    //hash = 53 * hash + (this.m_hInfos != null ? this.m_hInfos.hashCode() : 0);
    int hash = 53 * super.hashCode();
    return hash;
  }

  @Override
  public void traceUser() {
    super.traceUser();
    for (Entry<String, String> entry : m_hInfos.entrySet()) {

    }
  }
}
