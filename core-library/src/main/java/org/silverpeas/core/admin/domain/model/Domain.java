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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin.domain.model;

import org.silverpeas.core.admin.domain.DomainServiceProvider;
import org.silverpeas.core.admin.domain.quota.UserDomainQuotaKey;
import org.silverpeas.core.admin.quota.exception.QuotaException;
import org.silverpeas.core.admin.quota.exception.QuotaRuntimeException;
import org.silverpeas.core.admin.quota.model.Quota;
import org.silverpeas.core.security.authentication.AuthDomain;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Domain implements AuthDomain, Serializable {

  private static final long serialVersionUID = 7451639218436788229L;

  public static final String MIXED_DOMAIN_ID = "-1";
  private String id;
  private String name;
  private String description;
  private String driverClassName;
  private String propFileName;
  private String authenticationServer;
  private String silverpeasServerURL = "";

  /**
   * This data is not used in equals and hashcode process as it is an extra information.
   */
  private Quota userDomainQuota;

  @Override
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }


  @Override
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }


  public String getDescription() {
    return description;
  }


  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Gets the full class name of the domain driver that manages this domain.
   * @return the full path of the driver class.
   */
  public String getDriverClassName() {
    return driverClassName;
  }

  /**
   * Sets the full class name of the domain driver that manages this domain.
   * @param className the full path of the driver class.
   */
  public void setDriverClassName(String className) {
    this.driverClassName = className;
  }

  /**
   * Gets the full name of the properties file that defines this domain.
   * @return the full path of the properties file defining this domain.
   */
  public String getPropFileName() {
    return propFileName;
  }

  /**
   * Sets the full name of the properties file that defines this domain.
   * @param propFileName the full path of the properties file defining this domain.
   */
  public void setPropFileName(String propFileName) {
    this.propFileName = propFileName;
  }

  /**
   * Gets the name of the authentication server used by this authentication domain.
   * @return the unique name of an authentication server.
   */
  public String getAuthenticationServer() {
    return authenticationServer;
  }

  /**
   * Sets the name of the authentication server that will perform the authentication process for
   * this authentication domain.
   * @param authenticationServer the class to be used.
   */
  public void setAuthenticationServer(String authenticationServer) {
    this.authenticationServer = authenticationServer;
  }

  public String getSilverpeasServerURL() {
    return silverpeasServerURL;
  }

  public void setSilverpeasServerURL(String silverpeasServerURL) {
    this.silverpeasServerURL = silverpeasServerURL;
  }

  /**
   * Centralizes the user in domain quota loading
   */
  private void loadUserDomainQuota() {
    try {
      userDomainQuota =
          DomainServiceProvider.getUserDomainQuotaService().get(UserDomainQuotaKey.from(this));
    } catch (final QuotaException qe) {
      throw new QuotaRuntimeException("Cannot get quota for user domain", qe);
    }
  }

  /**
   * @return the userDomainQuota
   */
  public Quota getUserDomainQuota() {
    if (userDomainQuota == null) {
      loadUserDomainQuota();
    }
    return userDomainQuota;
  }

  /**
   * Sets the max count of users allowed for this domain.
   *
   * @param userDomainQuotaMaxCount the quota about the maximum users allowed in this domain.
   * @throws QuotaException if an error occurs while setting the quota.
   */
  public void setUserDomainQuotaMaxCount(final String userDomainQuotaMaxCount) throws QuotaException {
    loadUserDomainQuota();
    userDomainQuota.setMaxCount(userDomainQuotaMaxCount);
    userDomainQuota.validateBounds();
  }

  public boolean isQuotaReached() {
    loadUserDomainQuota();
    return userDomainQuota.isReached();
  }

  public boolean isMixedOne() {
    return MIXED_DOMAIN_ID.equals(getId());
  }

  public List<String> getLooks() {
    String param = getSettings().getString("domain.looks", "");
    return Arrays.asList(StringUtil.split(param, ','));
  }

  public SettingBundle getSettings() {
    return ResourceLocator.getSettingBundle(getPropFileName());
  }

  public boolean getProperty(String name, boolean defaultValue) {
    return getSettings().getBoolean(name, defaultValue);
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("Domain{id=");
    sb.append(id);
    sb.append(", name=");
    sb.append(name);
    sb.append(", description=");
    sb.append(description);
    sb.append(", driverClassName=");
    sb.append(driverClassName);
    sb.append(", propFileName=");
    sb.append(propFileName);
    sb.append(", authenticationServer=");
    sb.append(authenticationServer);
    sb.append(", silverpeasServerURL=");
    sb.append(silverpeasServerURL);
    if (userDomainQuota != null) {
      sb.append(", usersInDomainQuotaMaxCount=");
      sb.append(userDomainQuota.getMaxCount());
    }
    sb.append('}');
    return sb.toString();
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Domain)) {
      return false;
    }
    final Domain domain = (Domain) o;
    return Objects.equals(id, domain.id) && Objects.equals(name, domain.name) &&
        Objects.equals(description, domain.description) &&
        Objects.equals(driverClassName, domain.driverClassName) &&
        Objects.equals(propFileName, domain.propFileName) &&
        Objects.equals(authenticationServer, domain.authenticationServer) &&
        Objects.equals(silverpeasServerURL, domain.silverpeasServerURL) &&
        Objects.equals(userDomainQuota, domain.userDomainQuota);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, description, driverClassName, propFileName, authenticationServer,
        silverpeasServerURL, userDomainQuota);
  }
}
