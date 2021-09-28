/*
 * Copyright (C) 2000 - 2020 Silverpeas
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
package org.silverpeas.core.admin.user.model;

import org.silverpeas.core.admin.domain.DomainDriver;
import org.silverpeas.core.admin.domain.model.DomainProperty;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.util.ArrayUtil;
import org.silverpeas.core.util.StringUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.silverpeas.core.util.WebEncodeHelper.javaStringToHtmlString;

public class UserFull extends UserDetail {

  private static final long serialVersionUID = 1L;
  private HashMap<String, String> infos = null;
  private transient DomainDriver domainDriver = null;
  private String password = "";
  private boolean isPasswordValid = false;
  private boolean isPasswordAvailable = false;

  /**
   * Gets the full profile of the user with the specified identifier.
   * @param userId the unique identifier of the user in Silverpeas.
   * @return the full profile of the user.
   */
  public static UserFull getById(String userId) {
    return getOrganisationController().getUserFull(userId);
  }

  /**
   * Gets full profiles of user corresponding to the specified identifiers.
   * @param userIds the unique identifiers of users in Silverpeas.
   * @return list of full profile of user.
   */
  public static List<UserFull> getByIds(Collection<String> userIds) {
    return getOrganisationController().getUserFulls(userIds);
  }

  /**
   * Creates new UserFull
   */
  public UserFull() {
    super();
    infos = new HashMap<>();
  }

  public UserFull(DomainDriver domainDriver) {
    super();
    infos = new HashMap<>();
    this.domainDriver = domainDriver;
  }

  public UserFull(DomainDriver domainDriver, UserDetail toClone) {
    super(toClone);
    infos = new HashMap<>();
    this.domainDriver = domainDriver;
  }

  // Password specific entries
  public boolean isPasswordAvailable() {
    return isPasswordAvailable;
  }

  public void setPasswordAvailable(boolean pa) {
    isPasswordAvailable = pa;
  }

  public boolean isPasswordValid() {
    return isPasswordValid;
  }

  public void setPasswordValid(boolean pv) {
    isPasswordValid = pv;
  }

  public String getPassword() {
    return (password == null) ? "" : password;
  }

  public void setPassword(String p) {
    password = p;
  }

  // Values' getters
  public String[] getPropertiesNames() {
    if (domainDriver != null) {
      return domainDriver.getPropertiesNames();
    }
    return ArrayUtil.emptyStringArray();

  }

  public Map<String, String> getSpecificDetails() {
    return infos;
  }

  public String getValue(String propertyName, String defaultValue) {
    String valret;

    valret = infos.get(propertyName);
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

    String sValret = infos.get(propertyName);
    if (sValret != null) {
      valret = Boolean.parseBoolean(sValret);
    }
    return valret;
  }

  // Labels' getters
  public Map<String, String> getSpecificLabels(String language) {
    if (domainDriver != null) {
      return domainDriver.getPropertiesLabels(language);
    }
    return null;
  }

  public String getSpecificLabel(String language, String propertyName) {
    String valret = null;

    if (domainDriver != null) {
      Map<String, String> theLabels = domainDriver.getPropertiesLabels(language);
      valret = theLabels.get(propertyName);
    }
    if (valret == null) {
      valret = "";
    }
    return valret;
  }

  public String getPropertyType(String propertyName) {
    String valret = null;

    if (domainDriver != null) {
      DomainProperty domainProperty = domainDriver.getProperty(propertyName);
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
    if (domainDriver != null) {
      DomainProperty domainProperty = domainDriver.getProperty(property);
      if (domainProperty != null) {
        return domainProperty.isUpdateAllowedToUser();
      }
    }
    return false;
  }

  public boolean isPropertyUpdatableByAdmin(String property) {
    if (domainDriver != null) {
      DomainProperty domainProperty = domainDriver.getProperty(property);
      if (domainProperty != null) {
        return domainProperty.isUpdateAllowedToAdmin();
      }
    }
    return false;
  }

  public boolean isAtLeastOnePropertyUpdatableByAdmin() {
    if (domainDriver != null) {
      String[] properties = domainDriver.getPropertiesNames();
      for (String property : properties) {
        if (isPropertyUpdatableByAdmin(property)) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean isAtLeastOnePropertyUpdatableByUser() {
    if (domainDriver != null) {
      String[] properties = domainDriver.getPropertiesNames();
      for (String property : properties) {
        if (isPropertyUpdatableByUser(property)) {
          return true;
        }
      }
    }
    return false;
  }

  public DomainProperty getProperty(String property) {
    if (domainDriver != null) {
      return domainDriver.getProperty(property);
    }
    return null;
  }

  // Values' setters
  public void setValue(String propertyName, String value) {
    infos.put(propertyName, value);
  }

  public void setValue(String propertyName, boolean bValue) {
    infos.put(propertyName, String.valueOf(bValue));
  }

  public Map<String, String> getDefinedDomainValues() {
    Map<String, String> values = new HashMap<>();
    Set<String> keys = getSpecificDetails().keySet();
    for (String key : keys) {
      String value = getValue(key);
      if (StringUtil.isDefined(value)) {
        values.put(key, javaStringToHtmlString(value));
      }
    }
    return values;
  }

  public Map<String, String> getDefinedExtraFormValues(String language) {
    return PublicationTemplateManager.getInstance().getDirectoryFormValues(this.getId(), language);
  }

  public Map<String, String> getAllDefinedValues(String language) {
    Map<String, String> allValues = getDefinedDomainValues();
    allValues.putAll(getDefinedExtraFormValues(language));
    return allValues;
  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof UserFull) {
      UserFull cmpUser = (UserFull) other;
      if (super.equals(cmpUser)) {
        String[] keys = infos.keySet().toArray(new String[0]);
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
    return 53 * super.hashCode();
  }

}
