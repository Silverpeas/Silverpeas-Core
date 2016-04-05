/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection withWriter Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/legal/licensing"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.core.admin.domain.driver.ldapdriver;

/**
 *
 * @author ehugonnet
 */
public class LdapConfiguration {

  private String ldapHost = "localhost";
  private int ldapPort = 389;
  private String username;
  private byte[] password;
  private int timeout = 0;

  /**
   * Get the value of timeout
   *
   * @return the value of timeout
   */
  public int getTimeout() {
    return timeout;
  }

  /**
   * Set the value of timeout
   *
   * @param timeout new value of timeout
   */
  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }
  private boolean secure = false;

  /**
   * Get the value of secure
   *
   * @return the value of secure
   */
  public boolean isSecure() {
    return secure;
  }

  /**
   * Set the value of secure
   *
   * @param secure new value of secure
   */
  public void setSecure(boolean secure) {
    this.secure = secure;
  }

  /**
   * Get the value of password
   *
   * @return the value of password
   */
  public byte[] getPassword() {
    return password;
  }

  /**
   * Set the value of password
   *
   * @param password new value of password
   */
  public void setPassword(byte[] password) {
    this.password = (password != null ? password.clone() : null);
  }

  /**
   * Get the value of ldapHost
   *
   * @return the value of ldapHost
   */
  public String getLdapHost() {
    return ldapHost;
  }

  /**
   * Set the value of ldapHost
   *
   * @param ldapHost new value of ldapHost
   */
  public void setLdapHost(String ldapHost) {
    this.ldapHost = ldapHost;
  }

  /**
   * Get the value of ldapPort
   *
   * @return the value of ldapPort
   */
  public int getLdapPort() {
    return ldapPort;
  }

  /**
   * Set the value of ldapPort
   *
   * @param ldapPort new value of ldapPort
   */
  public void setLdapPort(int ldapPort) {
    this.ldapPort = ldapPort;
  }

  /**
   * Get the value of username
   *
   * @return the value of username
   */
  public String getUsername() {
    return username;
  }

  /**
   * Set the value of username
   *
   * @param username new value of username
   */
  public void setUsername(String username) {
    this.username = username;
  }

  @Override
  public String toString() {
    return "LdapConfiguration{" + "ldapHost=" + ldapHost + ", ldapPort=" + ldapPort + ", username="
        + username + ", password=" + password + ", timeout=" + timeout + ", secure=" + secure + '}';
  }
}
