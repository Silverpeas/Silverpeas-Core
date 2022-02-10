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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.security.authentication;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * A credential is a set of security-related capabilities for a given user, it contains information
 * used to authenticate him.
 *
 * This credential defines, among the attributes, the password, the login and the domain to which the
 * user belongs. The domain is a repository of users having its own security policy and then its own
 * authentication server. The login is the minimum information used in an authentication; indeed, for
 * some security policies, the login can be a unique security token identifying uniquely a user in
 * Silverpeas, whatever its domain, and that is known only by the user himself. For other security
 * policies, only the login and the domain are required to authenticate the user, like in NTLM
 * negotiation. By default, to open a WEB session with Silverpeas, the user has to authenticate
 * himself by using both his login, his password and the domain to which he belongs.
 *
 * The credential may also contain data that simply enable certain security-related capabilities like,
 * for example, the password change. These capabilities are generally set by the authentication
 * process from the response of the authentication server related to the user domain, once the user
 * is successfully authenticated.
 */
public class AuthenticationCredential {

  private String login;
  private String password;
  private String domainId;

  private Map<String, Serializable> capabilities = new HashMap<>();

  private AuthenticationCredential() {

  }

  /**
   * Creates a new authentication credential with the specified login.
   * @param login the login of the user to authenticate
   * @return an AuthenticationCredential instance.
   */
  public static AuthenticationCredential newWithAsLogin(String login) {
    AuthenticationCredential credential = new AuthenticationCredential();
    credential.setLogin(login);
    return credential;
  }

  /**
   * Returns this credential with its password attribute set with the specified value.
   * @param password a password.
   * @return itself.
   */
  public AuthenticationCredential withAsPassword(String password) {
    setPassword(password);
    return this;
  }

  /**
   * Returns this credential with its domain attribute set with the specified value.
   * @param domainId the unique identifier of the domain to which the user belongs.
   * @return itself.
   */
  public AuthenticationCredential withAsDomainId(String domainId) {
    setDomainId(domainId);
    return this;
  }

  /**
   * Gets the user login to use in the authentication.
   * @return the login.
   */
  public String getLogin() {
    return login;
  }

  /**
   * Gets the password that is mapped with the user login.
   * @return the password.
   */
  public String getPassword() {
    return password;
  }

  /**
   * Gets the unique identifier of the domain to which the user belongs.
   * @return the unique identifier of the user domain.
   */
  public String getDomainId() {
    return domainId;
  }

  /**
   * Gets the security-related capabilities associated with this credential.
   * These capabilities are generally set during the authentication from the response of an
   * authentication service.
   * @return a map of capabilities in which the key is the name of the capability and the value the
   * capability value.
   */
  public Map<String, Serializable> getCapabilities() {
    return capabilities;
  }

  private void setLogin(String login) {
    this.login = login;
  }

  /**
   * Sets the specified password to this credential.
   * @param password a password.
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * Sets the specified domain to this credential.
   * @param domainId the unique identifier of the domain.
   */
  public void setDomainId(String domainId) {
    this.domainId = domainId;
  }

  /**
   * Is the password set in this credential?
   * According to some security policy, the password isn't required to participate in an authentication;
   * for example in an NTLM negotiation.
   * @return true if the password attribute is set in this credential, false otherwise.
   */
  public boolean isPasswordSet() {
    return this.password != null;
  }
}
